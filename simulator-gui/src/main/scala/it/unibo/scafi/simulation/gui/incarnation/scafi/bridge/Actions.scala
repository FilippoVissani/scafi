package it.unibo.scafi.simulation.gui.incarnation.scafi.bridge

import it.unibo.scafi.simulation.gui.configuration.SensorName.output1
import it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.ScafiWorldIncarnation.EXPORT
import it.unibo.scafi.simulation.gui.incarnation.scafi.world.{ScafiLikeWorld, scafiWorld}

/**
  * describe action to actuate to the world, by export produced
  */
object Actions {
  type ACTION = PartialFunction[EXPORT,(ScafiLikeWorld,Int)=>Unit]

  val generalAction : ACTION = new ACTION {
    override def isDefinedAt(x: EXPORT): Boolean = true
    override def apply(export : EXPORT) : (ScafiLikeWorld, Int) => Unit = {
      (w : ScafiLikeWorld, id : Int) => {
        val devs = w(id).get.devices
        val dev = devs.find {y => y.name == output1}.get
        if(dev.value != export.root) scafiWorld.changeSensorValue(id,output1,export.root)
      }
    }
  }
}

class JavaActions(val action : PartialFunction[EXPORT,(ScafiLikeWorld,Int)=>Unit])