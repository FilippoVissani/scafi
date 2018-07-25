package it.unibo.scafi.simulation.gui.demos

import it.unibo.scafi.incarnations.BasicSimulationIncarnation.{AggregateProgram, BlockG, _}
import it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.ScafiSimulation.RadiusSimulation
import it.unibo.scafi.simulation.gui.incarnation.scafi.world.ScafiWorldInitializer.Random
import it.unibo.scafi.simulation.gui.launcher.scafi.ScafiProgramBuilder
import it.unibo.scafi.simulation.gui.view.scalaFX.drawer.StandardFXOutputPolicy
object Test extends App {
  ScafiProgramBuilder (
    worldInitializer = Random(1000,1920,1080),
    simulation = RadiusSimulation(program = classOf[Simple], radius = 60),
    outputPolicy = StandardFXOutputPolicy,
    neighbourRender = true
  ).launch()
}
@Demo
class Simple extends AggregateProgram  with BlockG with StandardSensors {
  self: AggregateProgram =>


  def channel(source: Boolean, target: Boolean, width: Double): Boolean =
    distanceTo(source) + distanceTo(target) <= distanceBetween(source, target) + width

  import it.unibo.scafi.simulation.gui.configuration.SensorName._
  override def main() = branch(sense[Boolean](sensor3.name)) {false} {channel(sense[Boolean](sensor1.name), sense[Boolean](sensor2.name), 1)}
}