package it.unibo.scafi.simulation.s2.frontend.view.scalaFX.logger

import it.unibo.scafi.simulation.s2.frontend.controller.logger.LogManager.{IntLog, Log, LogObserver, TreeLog}
import it.unibo.scafi.simulation.s2.frontend.view.{GraphicsLogger, scalaFX}

import scalafx.scene.control.TabPane

/**
  * standard fx graphics logger used to show log accepted
  * by default FXLogger has standard representation for some log type:
  *   - IntLog are showed with a line chart
  *   - TreeLog are showed with a tree view
  *   - other are showed with textual form
  */
object FXLogger extends TabPane with LogObserver {
  scalaFX.initializeScalaFXPlatform()
  this.stylesheets.add("style/tab-pane.css")
  private var channelToStrategy : Map[String,FXLogStrategy] = Map.empty

  private def createStrategy(log : Log[_]) : FXLogStrategy = {
    GraphicsLogger.strategyType.get(log.channel) match {
      case Some(logType) => FXLogStrategy(logType)
      case _ => log match {
        case IntLog(_,_,_) => FXLogStrategy(GraphicsLogger.lineChart)
        case TreeLog(_,_,_) => FXLogStrategy(GraphicsLogger.treeView)
        case _ => FXLogStrategy(GraphicsLogger.textual)
      }
    }
  }
  override protected def processLog(log: Log[_]): Unit = {
    val strategy : FXLogStrategy = channelToStrategy.get(log.channel) match {
      case Some(s) => s
      case _ =>
        val res = createStrategy(log)
        res.init(log.channel)
        this.channelToStrategy += log.channel -> res
        res
    }
    strategy.out(log)
  }
}
