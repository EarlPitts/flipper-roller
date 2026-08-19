package flipper.core

import scala.jdk.CollectionConverters.*

case class Name private (unName: String)

object Name:
  def mkName(str: String): Option[Name] =
    Option.when(
      str.length < 100 &&
        str.forall(c => c.isLower || c == '_')
    )(Name(str))

enum Flipper:
  case Waiting(name: Name)
  case InProgress(name: Name)
  case Done(name: Name)

object Flipper:
  def fromDTO(dto: FlipperDTO): Option[Flipper] =
    Name
      .mkName(dto.name)
      .map(Waiting(_))

  def toDTO(flipper: Flipper): java.util.Map[String, Object] =
    flipper match
      case Waiting(name) =>
        Map("name" -> name.unName, "status" -> "waiting").asJava
      case InProgress(name) =>
        Map("name" -> name.unName, "status" -> "inprogress").asJava
      case Done(name) =>
        Map("name" -> name.unName, "status" -> "done").asJava

case class FlipperDTO(name: String, status: String)
