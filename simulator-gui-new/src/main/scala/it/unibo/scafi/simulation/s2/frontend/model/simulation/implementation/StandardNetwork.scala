package it.unibo.scafi.simulation.s2.frontend.model.simulation.implementation

import it.unibo.scafi.simulation.s2.frontend.model.common.network.ConnectedWorld.NeighbourChanged
import it.unibo.scafi.simulation.s2.frontend.model.simulation.PlatformDefinition.SensorPlatform

import scala.collection.mutable.{Map => MMap}
/**
  * a standard network definition
  * it doesn't checks network
  * topology and network correctness
  */
trait StandardNetwork  {
  self: SensorPlatform =>

  override type NET = Network

  val network : NET = new NetworkImpl

  private class NetworkImpl extends Network {
    private var neigh : Map[ID,Set[ID]] = Map.empty

    override def neighbours(n: ID): Set[ID] = neigh.getOrElse(n,Set())

    override def neighbours(): Map[ID, Set[ID]] = neigh

    override def setNeighbours(node: ID, neighbour: Set[ID]): Unit = {
      neigh += node -> neighbour
      StandardNetwork.this.notify(StandardNetwork.this.NodeEvent(node,NeighbourChanged))
    }
  }

}