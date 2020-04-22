/*
 * Copyright (C) 2016-2017, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENCE.txt file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package it.unibo.scafi.simulation.frontend.controller.controller3d

import java.awt.Image

import it.unibo.scafi.simulation.frontend.{Settings, Simulation}
import it.unibo.scafi.simulation.frontend.controller.{ControllerUtils, PopupMenuUtils}
import it.unibo.scafi.simulation.frontend.controller.controller3d.helper._
import it.unibo.scafi.simulation.frontend.controller.controller3d.helper.sensor.DefaultSensorSetter
import it.unibo.scafi.simulation.frontend.controller.controller3d.helper.updater.{DefaultNodeUpdater, NodeUpdater}
import it.unibo.scafi.simulation.frontend.model.{NodeValue, SimulationManager}
import it.unibo.scafi.simulation.frontend.view.ConfigurationPanel
import it.unibo.scafi.simulation.frontend.view.ui3d.{DefaultSimulatorUI3D, SimulatorUI3D}
import javax.swing.{JFrame, SwingUtilities}

/** 3D version of the app's Controller, uses [[SimulatorUI3D]] as the view. It handles user interaction. */
class DefaultController3D(simulation: Simulation, simulationManager: SimulationManager) extends Controller3D {
  private var gui: SimulatorUI3D = _
  private var nodeValueTypeToShow: NodeValue = NodeValue.EXPORT
  private var observation: Option[Any => Boolean] = None
  private var nodeIds: Set[Int] = Set()
  private var nodeUpdater: NodeUpdater = _

  /** See [[it.unibo.scafi.simulation.frontend.controller.controller3d.Controller3D#startup()]] */
  def startup(): Unit = {
    simulation.setController(this)
    startGUI()
    nodeUpdater = DefaultNodeUpdater(this, gui.getSimulationPanel, simulation)
    PopupMenuUtils.addPopupObservations(gui.customPopupMenu,
      () => gui.getSimulationPanel.toggleConnections(), this)
    PopupMenuUtils.addPopupActions(this, gui.customPopupMenu)
    ControllerUtils.setupSensors(Settings.Sim_Sensors)
    if (!Settings.ShowConfigPanel) startSimulation()
  }

  private def startGUI(): Unit = SwingUtilities.invokeAndWait(new Runnable {
    override def run(): Unit = {
      gui = DefaultSimulatorUI3D(DefaultController3D.this)
      ControllerStarter.setupGUI(gui)
      if (Settings.ShowConfigPanel) new ConfigurationPanel(DefaultController3D.this)
    }
  })

  /** See [[it.unibo.scafi.simulation.frontend.controller.GeneralController#getUI()]] */
  override def getUI: JFrame = gui

  /** See [[it.unibo.scafi.simulation.frontend.controller.GeneralController#setShowValue(it.unibo.scafi.simulation.frontend.model.NodeValue)]] */
  override def setShowValue(valueType: NodeValue): Unit = {this.nodeValueTypeToShow = valueType}

  /** See [[it.unibo.scafi.simulation.frontend.controller.controller3d.Controller3D#getNodeValueTypeToShow()]] */
  override def getNodeValueTypeToShow: NodeValue = this.nodeValueTypeToShow

  /** See [[it.unibo.scafi.simulation.frontend.controller.GeneralController#startSimulation()]] */
  override def startSimulation(): Unit = {
    gui.reset() //this resets any accidental camera movement happened while ConfigPanel was open
    simulationManager.setUpdateNodeFunction(nodeUpdater.updateNode(_))
    nodeIds = ControllerStarter.setupSimulation(simulation, gui, simulationManager)
    simulationManager.start()
  }

  /** See [[it.unibo.scafi.simulation.frontend.controller.GeneralController#stopSimulation()]] */
  override def stopSimulation(): Unit = simulationManager.stop()

  /** See [[it.unibo.scafi.simulation.frontend.controller.GeneralController#pauseSimulation()]] */
  override def pauseSimulation(): Unit = simulationManager.pause()

  /** See [[it.unibo.scafi.simulation.frontend.controller.GeneralController#resumeSimulation()]] */
  override def resumeSimulation(): Unit = simulationManager.resume()

  /** See [[it.unibo.scafi.simulation.frontend.controller.GeneralController#stepSimulation(int)]] */
  override def stepSimulation(stepCount: Int): Unit = simulationManager.step(stepCount)

  /** See [[it.unibo.scafi.simulation.frontend.controller.GeneralController#clearSimulation()]] */
  override def clearSimulation(): Unit = {
    this.nodeValueTypeToShow = NodeValue.EXPORT
    this.observation = None
    ControllerResetter.resetSimulation(simulationManager, nodeUpdater, gui)
  }

  /** See [[it.unibo.scafi.simulation.frontend.controller.controller3d.Controller3D#handleNumberButtonPress(int)]] */
  override def handleNumberButtonPress(sensorIndex: Int): Unit =
    DefaultSensorSetter(gui.getSimulationPanel, simulation, nodeUpdater).handleNumberButtonPress(sensorIndex)

  /** See [[it.unibo.scafi.simulation.frontend.controller.controller3d.Controller3D#shutDown()]] */
  override def shutDown(): Unit = System.exit(0)

  /** See [[it.unibo.scafi.simulation.frontend.controller.controller3d.Controller3D#decreaseFontSize()]] */
  override def decreaseFontSize(): Unit = gui.getSimulationPanel.decreaseFontSize()

  /** See [[it.unibo.scafi.simulation.frontend.controller.controller3d.Controller3D#increaseFontSize()]] */
  override def increaseFontSize(): Unit = gui.getSimulationPanel.increaseFontSize()

  /** See [[it.unibo.scafi.simulation.frontend.controller.controller3d.Controller3D#slowDownSimulation()]] */
  override def slowDownSimulation(): Unit = simulationManager.simulation.setDeltaRound(getSimulationDeltaRound + 10)

  private def getSimulationDeltaRound: Double = simulationManager.simulation.getDeltaRound()

  /** See [[it.unibo.scafi.simulation.frontend.controller.controller3d.Controller3D#speedUpSimulation()]] */
  override def speedUpSimulation(): Unit = {
    val currentDeltaRound = getSimulationDeltaRound
    val newValue = if (currentDeltaRound - 10 < 0) 0 else currentDeltaRound - 10
    simulationManager.simulation.setDeltaRound(newValue)
  }

  /** See [[it.unibo.scafi.simulation.frontend.controller.GeneralController#selectionAttempted()]] */
  override def selectionAttempted: Boolean = gui.getSimulationPanel.isAttemptingSelection

  /** See [[it.unibo.scafi.simulation.frontend.controller.GeneralController#showImage(java.awt.Image, boolean)]] */
  override def showImage(image: Image, showed: Boolean): Unit =
    if(showed){
      gui.getSimulationPanel.setBackgroundImage(image)
    }  else {
      gui.getSimulationPanel.setBackgroundColor(Settings.Color_background)
    }

  /** See [[it.unibo.scafi.simulation.frontend.controller.GeneralController#setObservation(scala.Function1)]] */
  override def setObservation(observation: Any => Boolean): Unit = {this.observation = Option(observation)}

  /** See [[it.unibo.scafi.simulation.frontend.controller.GeneralController#getObservation()]] */
  override def getObservation: Any => Boolean = observation match {
    case Some(observation) => observation;
    case None => _ => false
  }

  /** See [[it.unibo.scafi.simulation.frontend.controller.GeneralController#setSensor(java.lang.String, java.lang.Object)]] */
  override def setSensor(sensorName: String, value: Any): Unit =
    DefaultSensorSetter(gui.getSimulationPanel, simulation, nodeUpdater).setSensor(sensorName, value, selectionAttempted)

  /** See [[it.unibo.scafi.simulation.frontend.controller.controller3d.Controller3D#isObservationSet()]] */
  override def isObservationSet: Boolean = observation.isDefined

  /** See [[it.unibo.scafi.simulation.frontend.controller.controller3d.Controller3D#isLedActivatorSet()]] */
  override def isLedActivatorSet: Boolean = Settings.Led_Activator(true)

  /** See [[it.unibo.scafi.simulation.frontend.controller.controller3d.Controller3D#getCreatedNodesID()]] */
  override def getCreatedNodesID: Set[Int] = nodeIds
}

object DefaultController3D {
  def apply(simulation: Simulation, simulationManager: SimulationManager): DefaultController3D =
    new DefaultController3D(simulation, simulationManager)
}
