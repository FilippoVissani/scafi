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

package it.unibo.scafi.renderer3d.util

import it.unibo.scafi.renderer3d.node.NetworkNode
import it.unibo.scafi.renderer3d.util.rendering.Rendering3DUtils.createMaterial
import it.unibo.scafi.renderer3d.util.math.MathUtils
import javafx.scene.PerspectiveCamera
import javafx.scene.input.MouseEvent
import it.unibo.scafi.renderer3d.util.ScalaFxExtras._
import scalafx.geometry.{Point2D, Point3D}
import scalafx.scene.Node
import scalafx.scene.paint.Color
import scalafx.scene.shape.Shape3D
import scalafx.scene.transform.Rotate

/** Object that contains implicit classes to enrich the language (using "Pimp my Library"), raising the level of
 * abstraction when working with ScalaFx and JavaFx. */
object RichScalaFx extends RichScalaFxHelper {

  implicit class RichPerspectiveCamera(camera: PerspectiveCamera) {
    /** Checks if the given node is visible from the camera. Set useSmallerFOVWindow to false for frustum culling.
     * @param node the node to check
     * @param useSmallerFOVWindow specifies if it's ok to use a smaller FOV for the calculation or not
     * @return whether the node is visible from the camera */
    def isNodeVisible(node: NetworkNode, useSmallerFOVWindow: Boolean = false): Boolean = {
      val window = camera.getScene.getWindow
      val windowMaximumSize = Math.max(window.getHeight, window.getWidth)
      val windowMinimumSize = Math.min(window.getHeight, window.getWidth)
      val cameraForward = MathUtils.rotateVector(Rotate.XAxis, Rotate.YAxis, (-camera.getYRotationAngle).toRadians)
      val angleBetweenDirections = cameraForward.angle(node.getNodePosition - camera.getPosition) - 170
      val (defaultFOVReduction, higherFOVReduction) = (10, 13)
      angleBetweenDirections.abs < camera.getFieldOfView*windowMaximumSize/windowMinimumSize*9/16 -
        (if(useSmallerFOVWindow) higherFOVReduction else defaultFOVReduction)
    }
  }

  implicit class RichNode(node: Node) {
    /** Gets the node's position in the 3D scene. ATTENTION: Don't use this on Group objects such as SimpleNetworkNode,
     *  since it would always return Point3D(0, 0, 0)
     * @return the node's position */
    final def getPosition: Point3D = node.delegate.getPosition

    /** Gets the node's 2D position, from the point of view of the camera. Using it on Group instances may not be ideal.
     * @return the node's 2D position */
    final def getScreenPosition: Point2D = node.delegate.getScreenPosition

    /** Moves the node to another 3D position. ATTENTION: Using this on Group objects such as SimpleNetworkNode adds the
     *  position to the current position of the object, instead of actually moving the node at the specified position.
     * @param position the new position of the node */
    final def moveTo(position: Point3D): Unit = node.delegate.moveTo(position)

    /** Rotates the node around itself, with the specified rotation axis
     * @param angle the rotation angle
     * @param axis the rotation axis */
    final def rotateOnSelf(angle: Double, axis: Point3D): Unit = node.delegate.rotateOnSelf(angle, axis)

    /** Rotates the node around itself so that it faces the point. ATTENTION: this resets the node's transformations.
     * @param point the point that the node will face
     * @param currentPosition the current position of the node */
    final def lookAt(point: Point3D, currentPosition: Point3D): Unit = node.delegate.lookAt(point, currentPosition)

    /** Rotates the node around itself but only on the Y axis, so that it faces the specified point.
     * @param point the point that the node will face */
    final def lookAtOnXZPlane(point: Point3D): Unit = node.delegate.lookAtOnXZPlane(point)

    /** Retrieves the angle between the X axis and the vector from the node's position and the specified 3d position.
     * @param point the specified 3d position used to calculate the angle
     * @return the rotation angle on the Y axis */
    final def getLookAtAngleOnXZPlane(point: Point3D): Double = node.delegate.getLookAtAngleOnXZPlane(point)

    /** Sets the scale of the node.
     * @param scale the new scale of the node */
    final def setScale(scale: Double): Unit = node.delegate.setScale(scale)

    /** Retrieves the euler angle on the Y axis of the current node.
     * @return the angle on the Y axis, in the range from -180 to 180. */
    final def getYRotationAngle: Double = node.delegate.getYRotationAngle

    /** Checks if the node is intersecting with the other node. ATTENTION: this is not accurate if the nodes are rotated.
     * @param otherNode the node to check for intersection with the current one
     * @return whether the two nodes are intersecting */
    final def isIntersectingWith(otherNode: Node): Boolean = node.delegate.isIntersectingWith(otherNode.delegate)
  }

  implicit class RichShape3D(shape: Shape3D) {
    /** See [[it.unibo.scafi.renderer3d.util.RichScalaFx.RichJavaShape3D#setColor(java.awt.Color)]] */
    final def setColor(color: java.awt.Color): Unit = shape.delegate.setColor(color)

    /** See [[it.unibo.scafi.renderer3d.util.RichScalaFx.RichJavaShape3D#setColor(java.awt.Color)]] */
    final def setColor(color: Color): Unit = shape.delegate.setColor(color)
  }

  implicit class RichJavaShape3D(shape: javafx.scene.shape.Shape3D) {
    /** Sets the color of the shape using java.awt.Color
     * @param color the new java.awt.Color of the shape */
    final def setColor(color: java.awt.Color): Unit =
    {val material = createMaterial(color.toScalaFx); onFX(shape.setMaterial(material))}

    /** Sets the color of the shape using scalafx.scene.paint.Color.
     * @param color the new scalafx.scene.paint.Color of the shape */
    final def setColor(color: Color): Unit = onFX(shape.setMaterial(createMaterial(color)))
  }

  implicit class RichColor(color: java.awt.Color) {
    /** Converts the java.awt.Color to scalafx.scene.paint.Color
     * @return the color of type scalafx.scene.paint.Color */
    final def toScalaFx: Color = Color.rgb(color.getRed, color.getGreen, color.getBlue, color.getAlpha/255d)
  }

  implicit class RichMouseEvent(event: MouseEvent) {
    /** Gets the current 2D screen position of the mouse.
     * @return the screen position of the mouse */
    final def getScreenPosition: Point2D = new Point2D(event.getScreenX, event.getScreenY)
  }

  implicit class RichProduct3Double(product: Product3[Double, Double, Double]) {
    /** Converts the Product3 of Double to scalafx.geometry.Point3D
     * @return the product as scalafx.geometry.Point3D */
    final def toPoint3D: Point3D = new Point3D(product._1, product._2, product._3)
  }
}
