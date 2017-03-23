package it.unibo.scafi.core

import scala.util.control.Exception._

/**
 * @author Mirko Viroli
 * @author Roberto Casadei
 *
 * This trait defines a component that extends Core and Language
 * It starts concretising the framework by implementing the key element of field-calculus semantics, namely:
 * - An export is a map from paths to values, and a value is a list of slots
 * - An Execution template implementing the whole operational semantics
 * - A basic Factory
 * - Additional ops to Context and Export, realised by family polymorphism
 *
 * This is still abstract in that we do not dictate how Context and Export are implemented and optimised internally
 */

trait Semantics extends Core with Language {

  override type CONTEXT <: Context with ContextOps
  override type EXPORT <: Export with ExportOps
  override type EXECUTION <: ExecutionTemplate

  trait Slot extends Serializable
  sealed case class Nbr[A](index: Int) extends Slot
  sealed case class Rep[A](index: Int) extends Slot
  sealed case class FunCall[A](index: Int, funId: Any) extends Slot
  sealed case class FoldHood[A](index: Int) extends Slot

  trait Path {
    def push(slot: Slot): Path
    def pull(): Path
    def matches(path: Path): Boolean
    def isRoot: Boolean

    def /(slot: Slot) = push(slot)
  }

  trait ExportOps { self: EXPORT =>
    def put[A](path: Path, value: A): A
    def get[A](path: Path): Option[A]
  }

  trait ContextOps { self: CONTEXT =>
    def readSlot[A](i: ID, p: Path): Option[A]
  }

  trait Factory {
    def emptyPath(): Path
    def emptyExport(): EXPORT
    def path(slots: Slot*): Path
    def export(exps: (Path,Any)*): EXPORT
  }

  implicit val factory: Factory

  trait AggregateProgramSpecification { constructs: Constructs =>
    type MainResult
    def main(): MainResult
  }

  /**
   * It implements the whole operational semantics.
   */
  trait ExecutionTemplate extends (CONTEXT => EXPORT) with Constructs with AggregateProgramSpecification {
    self:Constructs =>

    import ExecutionTemplate._


    @transient private var vm: RoundVM = _

    def apply(c: CONTEXT): EXPORT = {
      round(c,main())
    }

    def round(c: CONTEXT, e: =>Any = main()): EXPORT = {
        vm = new RoundVM(c)
        val result = e
        vm.registerRoot(result)
        vm.export
    }

    override def mid(): ID = vm.self

    override def rep[A](init: =>A)(fun: (A) => A): A = {
      vm.nest(Rep[A](vm.index))(true) {
        vm.locally {
          fun(vm.previousRoundVal.getOrElse(init))
        }
      }
    }

    override def foldhood[A](init: => A)(aggr: (A, A) => A)(expr: => A): A = {
      vm.nest(FoldHood[A](vm.index))(true) {
        vm.alignedNeighbours
          .map(id => vm.foldedEval(expr)(Some[ID](id)).getOrElse(vm.locally { init }))
          .fold(vm.locally { init })(aggr)
      }
    }

    override def nbr[A](expr: => A): A =
      vm.nest(Nbr[A](vm.index))(vm.neighbour.isEmpty || vm.neighbour.get==vm.self) {
        vm.neighbour match {
          case Some(nbr) if (nbr != vm.self) => vm.neighbourVal
          case _  => expr
        }
    }

    override def aggregate[T](f: => T): T =
      vm.nest(FunCall[T](vm.index, elicitAggregateFunctionTag()))(true) {
        f
      }

    def sense[A](name: LSNS): A = vm.localSense(name)

    def nbrvar[A](name: NSNS): A = vm.neighbourSense(name)

    private def elicitAggregateFunctionTag() = Thread.currentThread().getStackTrace()(4)
  }

  private[scafi] object ExecutionTemplate extends Serializable {

    class RoundVM(val context: CONTEXT){

      var export: EXPORT = factory.emptyExport
      var status: Status = Status()

      def registerRoot(v: Any): Unit = export.put(factory.emptyPath, v)
      def self: ID = context.selfId
      def neighbour: Option[ID] = status.neighbour
      def index: Int = status.index
      def path: Path = status.path
      def previousRoundVal[A]: Option[A] = context.readSlot[A](self, path)
      def neighbourVal[A]: A = context.readSlot[A](neighbour.get, path).getOrElse(throw new OutOfDomainException(context.selfId, neighbour.get, path))
      def inFolding: Boolean = !neighbour.isEmpty
      //def foldInto(id: Option[ID]): Unit = vm.status = vm.status.foldInto(id)
      //def exitFolding(): Unit = status = vm.status.foldOut()
      def incIndex(): Unit = status = status.incIndex()
      def foldedEval[A](expr: =>A)(id: Option[ID]): Option[A] =
        handling(classOf[OutOfDomainException]) by (_ => None) apply {
          try {
            status = status.push()
            status = status.foldInto(id)
            Some(expr)
          } finally {
            status = status.pop()
          }
        }
      def localSense[A](name: LSNS): A = context.sense[A](name).getOrElse(throw new SensorUnknownException(self, name))
      def neighbourSense[A](name: NSNS): A = context.nbrSense(name)(neighbour.get).getOrElse(throw new NbrSensorUnknownException(self, name, neighbour.get))

      def nest[A](slot: Slot)(write: Boolean)(expr: => A): A = {
        try {
          status = status.push().nest(slot)  // prepare nested call
          if (write) export.put(status.path, expr) else expr      // function return value is result of expr
        } finally {
          status = status.pop().incIndex();  // do not forget to restore the status
        }
      }
      def locally[A](a: =>A): A = {
        val currentNeighbour = neighbour
        try{
          status = status.foldOut()
          a
        } finally {
          status = status.foldInto(currentNeighbour)
        }
      }


      // self should be the last one to make nbrWork!
      // Why? Because in nbr nest performs 'exp.put(status.path, expr)'
      // So the export must be overridden by the current device (at last).
      def alignedNeighbours(): List[ID] = self ::
      context.exports
        .filter(_._1 != self)
        .filter(p => status.path.isRoot || p._2.get(status.path).isDefined)
        .map(_._1)
        .toList
    }


    trait Status extends Serializable {
      val path: Path
      val index: Int
      val neighbour: Option[ID]

      def isFolding: Boolean
      def foldInto(id: Option[ID]): Status
      def foldOut(): Status
      def nest(s: Slot): Status
      def incIndex(): Status
      def push(): Status
      def pop(): Status
    }

    private case class StatusImpl(
        path: Path = factory.emptyPath(),
        index: Int = 0,
        neighbour: Option[ID] = None,
        stack: List[(Path, Int, Option[ID])] = List()) extends Status {

      def isFolding: Boolean = neighbour.isDefined
      def foldInto(id: Option[ID]): Status = StatusImpl(path, index, id, stack)
      def foldOut(): Status = StatusImpl(path, index, None, stack)
      def push(): Status = StatusImpl(path, index, neighbour, (path, index, neighbour) :: stack)
      def pop(): Status = stack match {
        case (p, i, n) :: s => StatusImpl(p, i, n, s)
        case _           => throw new Exception()
      }
      def nest(s: Slot): Status = StatusImpl(path.push(s), 0, neighbour, stack)
      def incIndex(): Status = StatusImpl(path, index + 1, neighbour, stack)
    }

    object Status {
      def apply(): Status = StatusImpl()
    }

    def ensure(b: => Boolean, s: String): Unit = {
      b match {
        case false => throw new Exception(s)
        case _     =>
      }
    }

    case class OutOfDomainException(selfId: ID, nbr: ID, path: Path) extends Exception() {
      override def toString: String = s"OutOfDomainException: $selfId , $nbr, $path"
    }

    case class SensorUnknownException(selfId: ID, name: LSNS) extends Exception() {
      override def toString: String = s"SensorUnknownException: $selfId , $name"
    }

    case class NbrSensorUnknownException(selfId: ID, name: NSNS, nbr: ID) extends Exception() {
      override def toString: String = s"NbrSensorUnknownException: $selfId , $name, $nbr"
    }

  }

}
