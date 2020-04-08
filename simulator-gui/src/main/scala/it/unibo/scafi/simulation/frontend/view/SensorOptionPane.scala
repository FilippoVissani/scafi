/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package it.unibo.scafi.simulation.frontend.view

import java.awt._
import java.awt.event.ActionEvent

import it.unibo.scafi.simulation.frontend.controller.GeneralController
import it.unibo.scafi.simulation.frontend.utility.Utils
import javax.swing._

class SensorOptionPane(title: String, controller: GeneralController) extends JDialog {
  final private val sensorsChoice: JComboBox[String] = new JComboBox[String]
  final private val operators: JComboBox[String] = new JComboBox[String]

  val sensorNameField: JTextField = new JTextField(20)
  val valueField: JTextField = new JTextField(10)
  val enter: JButton = new JButton("OK")
  val cancel: JButton = new JButton("Cancel")

  sensorNameField.setToolTipText("e.g.: sens1")
  valueField.setToolTipText("The space in the middle is required, e.g.: int 5")
  setTitle(title)
  setSize(600, 300)
  this.setLocationRelativeTo(null)

  enter.addActionListener((e:ActionEvent) => {
    /*
    if (operators.getItemCount() > 1) {
      controller.checkSensor(sensorsChoice.getSelectedItem().toString,
        operators.getSelectedItem().toString,
        valueField.getText()
      )
    } else {
      controller.setSensor(sensorsChoice.getSelectedItem().toString,
        valueField.getText()
      )
    }
    */
    val sensorName = sensorNameField.getText

    controller.setSensor(sensorName, Utils.parseSensorValue(valueField.getText))

    this.dispose()
  })
  cancel.addActionListener((e:ActionEvent) => this.dispose())
  val panel: JPanel = new JPanel(new GridBagLayout)
  val c: GridBagConstraints = new GridBagConstraints
  c.insets = new Insets(5, 5, 5, 15)
  c.fill = GridBagConstraints.HORIZONTAL
  c.gridx = 0
  c.gridy = 0
  //panel.add(sensorsChoice, c)
  panel.add(sensorNameField, c)
  c.anchor = GridBagConstraints.CENTER
  c.gridx = 1
  c.gridy = 0
  panel.add(operators, c)
  c.fill = GridBagConstraints.HORIZONTAL
  c.gridx = 2
  c.gridy = 0
  panel.add(valueField, c)
  c.insets = new Insets(10, 0, 0, 0)
  c.fill = GridBagConstraints.NONE
  c.anchor = GridBagConstraints.LINE_END
  c.gridx = 2
  c.gridy = 1
  panel.add(cancel, c)
  c.anchor = GridBagConstraints.LINE_START //bottom of space
  c.gridx = 3 //aligned with button 2
  c.gridy = 1 //third row
  panel.add(enter, c)
  setContentPane(panel)
  setVisible(true)

  def addSensor(sensorName: String) {
    sensorsChoice.addItem(sensorName)
  }

  def addOperator(operator: String) {
    operators.addItem(operator)
  }
}
