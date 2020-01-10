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

package it.unibo.scafi.renderer3d.manager

import java.awt.{Color, Image}

import it.unibo.scafi.renderer3d.manager.connection.ConnectionManager
import it.unibo.scafi.renderer3d.manager.node.NodeManager
import it.unibo.scafi.renderer3d.manager.scene.SceneManager
import it.unibo.scafi.renderer3d.manager.selection.SelectionManager
import javafx.embed.swing.JFXPanel

/** Interface of the main entry point of this module. This renders all the nodes, the connections, etc.
 * Users of this module should use only this interface to interact with renderer-3d, as it offers all the main APIs
 * needed for adding, removing and moving nodes and connections, handling the selected nodes, etc. */
trait NetworkRenderer3D extends JFXPanel{

  //** connections API **
  /** See [[ConnectionManager.setConnectionsColor]] */
  def setConnectionsColor(color: Color): Unit

  /** See [[ConnectionManager.connect]] */
  def connect(node1UID: Int, node2UID: Int): Unit

  /** See [[ConnectionManager.disconnect]] */
  def disconnect(node1UID: Int, node2UID: Int): Unit

  /** See [[ConnectionManager.toggleConnections]] */
  def toggleConnections(): Unit

  //** nodes API **
  /** See [[NodeManager.enableNodeFilledSphere]] */
  def enableNodeFilledSphere(nodeUID: Int, enable: Boolean): Unit

  /** See [[NodeManager.setSpheresRadius]] */
  def setSpheresRadius(seeThroughSpheresRadius: Double, filledSpheresRadius: Double): Unit

  /** See [[NodeManager.addNode]] */
  def addNode(position: Product3[Double, Double, Double], UID: Int): Unit

  /** See [[NodeManager.removeNode]] */
  def removeNode(nodeUID: Int): Unit

  /** See [[NodeManager.moveNode]] */
  def moveNode(nodeUID: Int, position: Product3[Double, Double, Double], showDirection: Boolean): Unit

  /** See [[NodeManager.stopShowingNodeMovement]] */
  def stopShowingNodeMovement(nodeUID: Int): Unit

  /** See [[NodeManager.setNodeText]] */
  def setNodeText(nodeUID: Int, text: String): Unit

  /** See [[NodeManager.setNodeTextAsUIPosition]] */
  def setNodeTextAsUIPosition(nodeUID: Int, positionFormatter: Product2[Double, Double] => String): Unit

  /** See [[NodeManager.setNodeColor]] */
  def setNodeColor(nodeUID: Int, color: java.awt.Color): Unit

  /** See [[NodeManager.setNodesColors]] */
  def setNodesColor(defaultColor: java.awt.Color): Unit

  /** See [[NodeManager.setFilledSpheresColor]] */
  def setFilledSpheresColor(color: java.awt.Color): Unit

  /** See [[NodeManager.setNodesScale]] */
  def setNodesScale(scale: Double): Unit

  /** See [[NodeManager.increaseFontSize]] */
  def increaseFontSize(): Unit

  /** See [[NodeManager.decreaseFontSize]] */
  def decreaseFontSize(): Unit

  //** scene API **
  /** See [[SceneManager.setSceneSize]] */
  def setSceneSize(sceneSize: Double): Unit

  /** See [[SceneManager.setBackgroundImage]] */
  def setBackgroundImage(image: Image): Unit

  /** See [[SceneManager.setBackgroundColor]] */
  def setBackgroundColor(color: Color): Unit

  /** See [[SceneManager.resetScene]] */
  def resetScene(): Unit

  //** selection API **
  /** See [[SelectionManager.setActionOnMovedNodes]] */
  def setActionOnMovedNodes(action: (Set[Int], Product3[Double, Double, Double]) => Unit): Unit

  /** See [[SelectionManager.getSelectedNodesIDs]] */
  def getSelectedNodesIDs: Set[Int]

  /** See [[SelectionManager.getInitialSelectedNodeId]] */
  def getInitialSelectedNodeId: Option[Int]

  /** See [[SelectionManager.setCurrentSelectionColor]] */
  def setCurrentSelectionColor(color: java.awt.Color): Unit

  /** See [[SelectionManager.isAttemptingSelection]] */
  def isAttemptingSelection: Boolean

  /** See [[SelectionManager.setSelectionColor]] */
  def setSelectionColor(color: java.awt.Color): Unit

  //** rest of the API **
  /** See [[NetworkRendering3DPanel.blockUntilThreadIsFree]] */
  def blockUntilThreadIsFree(): JFXPanel
}
