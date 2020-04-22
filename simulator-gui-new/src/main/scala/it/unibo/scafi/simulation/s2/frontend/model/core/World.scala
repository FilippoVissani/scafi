package it.unibo.scafi.simulation.s2.frontend.model.core

import it.unibo.scafi.space.SpatialAbstraction.Bound
import it.unibo.scafi.space.{Point3D, Shape}

/**
  * describe a place where an immutable set of node are located
  */
trait World {
  /**
    * define the type of node id
    */
  type ID
  /**
    * define the type of name of device
    */
  type NAME
  /**
    * define the type of device in this world
    */
  type DEVICE <: Device
  /**
    * define the type of position in this world
    */
  type P = Point3D
  /**
    * define the type of shape in this world
    */
  type S <: Shape

  /**
    * the type of node in this world
    */
  type NODE <: Node
  /**
    * A boundary of the world (a world may has no boundary)
    */
  def boundary : Option[Bound]
  /**
    * get all nodes on this world
    */
  def nodes :Set[NODE]

  /**
    * return a specific node on the world
    * @param id
    *   the id of the node
    * @return
    *   the node if it is in the world
    */
  def apply(id : ID) : Option[NODE]

  /**
    * return a set of node in the world
    * @param nodes the ids of the node
    * @return the set of the node
    */
  def apply(nodes : Set[ID]) : Set[NODE]
  /**
    * Node describe an immutable object in a world
    */
  //noinspection AbstractValueInTrait
  trait Node {

    /**
      * the id associated with this node
      */
    val id : ID

    /**
      * @return
      *   the current position
      */
    def position : P
    /**
      *
      * @return
      *   the shape of the node if the node has a shape, none otherwise
      */
    def shape : Option[S]
    /**
      * @return
      *   all devices attach on this node
      */
    //TEMPLATE METHOD
    def devices: Set[DEVICE]
    /**
      *
      * @param name of device
      * @return a device if it is attach on the node
      */
    def getDevice(name : NAME) : Option[DEVICE] = devices find {_.name == name}

    override def toString = s"Node id = $id, position = $position, shape = $shape, devices = $devices"
  }
  /**
    * a generic immutable device that could be attached to a node
    */
  //noinspection AbstractValueInTrait
  trait Device {
    /**
      * the name of the device, it must immutable
      */
    val name : NAME

    override def toString: String = "device name = " + name
  }
}
