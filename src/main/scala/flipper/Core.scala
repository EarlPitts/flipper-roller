package flipper.core

import scala.jdk.CollectionConverters.*
import java.time.LocalDate
import scala.util.Try

case class Name private (unName: String)

object Name:
  def mkName(str: String): Option[Name] =
    Option.when(
      str.length < 100 &&
        str.forall(c => c.isLower || c == '_')
    )(Name(str))

enum Flipper:
  case Waiting(name: Name, start: LocalDate)
  case InProgress(name: Name, start: LocalDate)
  case Done(name: Name, finished: LocalDate)

object Flipper:
  def fromDTO(dto: FlipperDTO): Option[Flipper] = for
    name <- Name.mkName(dto.name)
    date <- Try(LocalDate.parse(dto.date)).toOption
    flipper <- dto.status match
      case "waiting"    => Some(Waiting(name, date))
      case "inprogress" => Some(InProgress(name, date))
      case "done"       => Some(Done(name, date))
      case _            => None
  yield flipper

  def fromCreateDTO(
      currDate: LocalDate
  )(dto: CreateFlipperDTO): Option[Flipper] = for
    name <- Name.mkName(dto.name)
    date <- Try(LocalDate.parse(dto.date)).toOption
    flipper =
      if date.isAfter(currDate) then Waiting(name, date)
      else InProgress(name, date)
  yield flipper

  def toDTO(flipper: Flipper): java.util.Map[String, Object] =
    flipper match
      case Waiting(name, start) =>
        Map(
          "name" -> name.unName,
          "status" -> "waiting",
          "date" -> start.toString
        ).asJava
      case InProgress(name, start) =>
        Map(
          "name" -> name.unName,
          "status" -> "inprogress",
          "date" -> start.toString
        ).asJava
      case Done(name, finished) =>
        Map(
          "name" -> name.unName,
          "status" -> "done",
          "date" -> finished.toString
        ).asJava

case class FlipperDTO(name: String, status: String, date: String)

case class CreateFlipperDTO(name: String, date: String)
