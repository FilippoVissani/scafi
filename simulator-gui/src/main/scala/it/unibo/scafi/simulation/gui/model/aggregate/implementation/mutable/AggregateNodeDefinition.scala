package it.unibo.scafi.simulation.gui.model.aggregate.implementation.mutable

trait AggregateNodeDefinition extends AggregateConcept {
  self: AggregateNodeDefinition.Dependency =>

  /**
    * skeleton of a mutable node
    * @param id the node id
    * @param position the initial node position
    * @param shape the node shape
    */
  abstract class AbstractMutableNode(val id : ID, var position: P, val shape : Option[S]) extends AggregateMutableNode {
    /**
      * the internal representation of device collection
      */
    protected var devs : Map[NAME,MUTABLE_DEVICE] = Map.empty

    override def addDevice(device: MUTABLE_DEVICE): Boolean = {
      if(devs.contains(device.name)) {
        false
      } else {
        devs += device.name -> device
        true
      }
    }

    override def removeDevice(name: NAME): Boolean = {
      if(devs.contains(name)) {
        devs -= name
        true
      } else {
        false
      }
    }

    override def devices: Set[DEVICE] = devs.values map{_.view} toSet

    def getMutableDevice(name : NAME) : Option[MUTABLE_DEVICE] = devs.get(name)
  }

  /**
    * abstract node builder used to create a single instance of node
    * @param id the node id
    * @param shape the node shape
    * @param position the node position
    * @param deviceProducer a set of device producer
    */
  abstract class AbstractNodeBuilder(val id : ID,
                                     val shape : Option[S] = None,
                                     val position : P,
                                     val deviceProducer : Iterable[DEVICE_PRODUCER] = Set.empty
                                    ) extends RootNodeProducer

}

object AggregateNodeDefinition {
  type Dependency = AggregateConcept.Dependency
}