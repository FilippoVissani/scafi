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

package it.unibo.scafi.simulation.frontend.controller.controller3d.helper

import it.unibo.scafi.config.{Grid3DSettings, SimpleRandomSettings}
import it.unibo.scafi.simulation.frontend.controller.ControllerUtils
import it.unibo.scafi.simulation.frontend.model.Node
import it.unibo.scafi.simulation.frontend.model.implementation.NodeImpl
import it.unibo.scafi.space.SpaceHelper

/**
 * Utility object that has methods to create the scene nodes.
 * */
private[controller3d] object NodesGenerator {

  /**
   * Creates the scene nodes with the given topology.
   * @param topology it explains how the noes should be placed in the scene
   * @param nodeCount the number of nodes to create
   * @param seed it will be used as an input for the randomization of the positions
   * @return a map of (UID -> Node) entries, from the unique ID of the node to the node itself.
   * */
  def createNodes(topology: String, nodeCount: Int, seed: Long): Map[Int, Node] = {
    val locations = if(topology.toLowerCase.contains("grid")){
      val nodeCountInSide = Math.cbrt(nodeCount).toInt
      val step = 1d / nodeCountInSide
      val OFFSET = 1d/40
      val variance = ControllerUtils.getTolerance(topology)
      val gridSettings = Grid3DSettings.cube(nodeCountInSide, step = step, tolerance = variance, offset = OFFSET)
      SpaceHelper.grid3DLocations(gridSettings, seed)
    } else {
      SpaceHelper.random3DLocations(SimpleRandomSettings(), nodeCount, seed)
    }
    locations.zipWithIndex.toMap.map(_.swap).map({case (index, position) => (index, new NodeImpl(index, position))})
  }

}
