package it.unibo.scafi.simulation.s2.frontend.configuration.command.factory

import java.util.Locale

import it.unibo.scafi.simulation.s2.frontend.configuration.command.{Command, CommandFactory}
import it.unibo.scafi.simulation.s2.frontend.configuration.launguage.ResourceBundleManager
import it.unibo.scafi.simulation.s2.frontend.util.Result
import it.unibo.scafi.simulation.s2.frontend.util.Result.Fail

/**
  * a factory used to create command to change language
  */
class LanguageCommandFactory extends CommandFactory {
  import CommandFactory._
  import LanguageCommandFactory._
  import ResourceBundleManager._
  //language supported
  private val map = Map("en" -> Locale.ENGLISH, "it" -> Locale.ITALIAN)
  private val supported = LimitedValueType(map.keySet.toSeq:_*)
  override val name: String = "language"

  override def commandArgsDescription: Seq[CommandFactory.CommandArgDescription] =
    List(CommandArgDescription(Name,supported,description = international(name,Name) ,defaultValue = Some("en")))

  override protected def createPolicy(args: CommandArg): (Result, Option[Command]) = args.get(Name) match {
    case Some(nameValue : String) => if(map.contains(nameValue)) {
      easyResultCreation(() => ResourceBundleManager.locale = map(nameValue))
    } else {
      creationFailed(Fail(wrongTypeParameter(supported,Name)))
    }
    case Some(_) => creationFailed(Fail(wrongTypeParameter(supported,Name)))
    case _ => creationFailed(Fail(wrongParameterName(Name)))
  }
}

object LanguageCommandFactory {
  val Name = "name"
}