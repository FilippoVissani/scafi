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

package it.unibo.scafi.renderer3d.manager.scene

import java.awt.{Color, Image}

import it.unibo.scafi.renderer3d.camera.{FpsCamera, SimulationCamera}
import it.unibo.scafi.renderer3d.manager.node.NodeManager
import it.unibo.scafi.renderer3d.manager.scene.SceneManagerHelper._
import it.unibo.scafi.renderer3d.manager.selection.SelectionManager
import it.unibo.scafi.renderer3d.util.RichScalaFx._
import javafx.beans.{InvalidationListener, Observable}
import javafx.scene.input._
import javafx.scene.paint.ImagePattern
import javafx.stage.Window
import it.unibo.scafi.renderer3d.util.ScalaFxExtras._
import it.unibo.scafi.renderer3d.util.rendering.Rendering3DUtils
import javafx.event.{Event, EventHandler}
import scalafx.application.Platform
import scalafx.embed.swing.SwingFXUtils
import scalafx.geometry.{Point2D, Point3D}
import scalafx.scene.transform.Rotate
import scalafx.scene.{Group, Scene, SceneAntialiasing}

/** Trait that contains some of the main API of the renderer-3d module regarding the scene. */
private[manager] trait SceneManager {
  this: NodeManager with SelectionManager => //NodeManager and SelectionManager have to also be mixed in

  private final val simulationCamera: SimulationCamera = FpsCamera()
  protected val mainScene: Scene
  protected final var sceneSize = 1d

  /** Sets the scene's size and also the camera, connections and nodes scale accordingly, so that the scene is visible.
   * Setting this value correctly makes it possible to update the labels' position only when needed. ATTENTION: big
   * values will cause performance problems, while small values move the labels too far away from the nodes. If
   * sceneSize is 1000 the 3d points should be positioned in a 1000*1000*1000 space. This method has to be called before
   * setting the scale of the nodes.
   * @param sceneSize it's the side's length of the imaginary cube that encloses the whole scene
   * @return Unit, since it has the side effect of changing the scene's size */
  final def setSceneSize(sceneSize: Double): Unit =
    onFX {this.sceneSize = sceneSize; mainScene.getCamera.setScale(sceneSize/10000)}

  protected final def sceneScaleMultiplier: Double = {val DEFAULT_SCENE_SIZE = 1000; sceneSize / DEFAULT_SCENE_SIZE}

  /** Sets the background image of the scene.
   * @param image the image to set as background
   * @return Unit, since it has the side effect of setting the scene's background image */
  //scalastyle:off null
  final def setBackgroundImage(image: Image): Unit =
    onFX {mainScene.setFill(new ImagePattern(SwingFXUtils.toFXImage(toBufferedImage(image), null)))}

  /** Sets the background color of the scene.
   * @param color the color to set
   * @return Unit, since it has the side effect of setting the scene's color */
  final def setBackgroundColor(color: Color): Unit = onFX {mainScene.setFill(color.toScalaFx)}

  /** Deletes all the nodes and connections, obtaining an empty scene with only a light and the camera facing the nodes.
   * @return Unit, since it has the side effect of resetting the scene */
  final def resetScene(): Unit = onFX {
    getAllNetworkNodes.foreach(node => removeNode(node.UID))
    mainScene.getCamera.moveTo(Point3D.Zero)
    mainScene.getCamera.lookAtOnXZPlane(Rotate.ZAxis*(-1))
    mainScene.getCamera.moveTo((Rotate.ZAxis*sceneSize*1.2 - Rotate.XAxis*sceneSize/2)*(-1) + Rotate.YAxis*sceneSize/2)
    endSelectionMovementIfNeeded(None) //forcing selection reset
  }

  /** Sets the camera scale to the new one, making it bigger or smaller. This is useful if the scene is not well visible
   * because the nodes are too big or small to be seen.
   * @param scale the scale to set */
  final def setCameraScale(scale: Double): Unit = mainScene.getCamera.setScale(scale)

  protected final def createScene(): Scene =
    new Scene(0, 0, true, SceneAntialiasing.Balanced) {
      camera = simulationCamera
      root = new Group(simulationCamera, Rendering3DUtils.createAmbientLight)
    }

  protected def setupCameraAndListeners(): Unit = {
    simulationCamera.initialize(mainScene, () => if (System.currentTimeMillis() % 2 == 0) rotateNodeLabelsIfNeeded())
    setMouseInteraction(mainScene, simulationCamera)
    setKeyboardInteraction(mainScene, simulationCamera)
  }

  protected final def stopMovingOnFocusLoss(window: Window): Unit  = {//call after initialization, so window is != null
    val listener = new InvalidationListener { //without this the camera would sometimes keep moving without user input
      override def invalidated(observable: Observable): Unit = simulationCamera.stopMovingAndRotating()
    }
    Seq(window.focusedProperty(), window.heightProperty(), window.widthProperty()).foreach(_.addListener(listener))
  }

  private[this] final def setMouseInteraction(scene: Scene, camera: SimulationCamera): Unit = {
    setMousePressedAndDragged(scene, camera)
    scene.addEventFilter(MouseEvent.DRAG_DETECTED, toHandler((_: MouseEvent) => scene.startFullDrag()))
    scene.addEventFilter(MouseDragEvent.MOUSE_DRAG_ENTERED,
      toHandler((event: MouseDragEvent) => if(isPrimaryButton(event) && !isSelectionComplete) startSelection(event)))
    scene.addEventFilter(MouseEvent.MOUSE_RELEASED,
      toHandler((event: MouseEvent) => if(isPrimaryButton(event)) endSelectionAndRotateSelectedLabels(event)))
  }

  private def toHandler[T<: Event](function: T => Unit): EventHandler[T] =
    new EventHandler[T]{ override def handle(event: T): Unit = function(event)}

  private[this] final def setKeyboardInteraction(scene: Scene, camera: SimulationCamera): Unit =
    scene.addEventFilter(KeyEvent.KEY_PRESSED, toHandler((event: KeyEvent) => {
      val movement = event.getCode match {
        case KeyCode.H => Option(new Point2D(-1, 0))
        case KeyCode.K => Option(new Point2D(1, 0))
        case KeyCode.U => Option(new Point2D(0, 1))
        case KeyCode.N => Option(new Point2D(0, -1))
        case _ => None
      }
      movement.fold()(movement => changeSelectionVolumeSizesIfNeeded(camera, movement))
    }))

  private[this] final def setMousePressedAndDragged(scene: Scene, camera: SimulationCamera): Unit = {
    scene.addEventFilter(MouseEvent.MOUSE_PRESSED, toHandler((event: MouseEvent) => if(isPrimaryButton(event)) {
      endSelectionMovementIfNeeded(Option(event))
      if(isSelectionComplete && !movementComplete) setMousePosition(event) else setSelectionVolumeCenter(event)
    } else if(isMiddleMouse(event)) {
      camera.startMouseRotation(event)
    }))
    scene.addEventFilter(MouseEvent.MOUSE_DRAGGED, toHandler((event: MouseEvent) =>
      if(isPrimaryButton(event)) {
        if(isSelectionComplete) moveSelectedNodesIfNeeded(camera, event) else scaleSelectionVolumeIfNeeded(camera, event)
    } else if(isMiddleMouse(event)) {
      camera.rotateByMouseEvent(event)
    } else if(event.getButton == MouseButton.SECONDARY) {
      changeSelectionVolumeSizesIfNeeded(camera, event)
    }))
  }
}
