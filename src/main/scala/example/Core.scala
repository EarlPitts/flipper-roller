package flipper.core

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

case class FlipperDTO(name: String, status: String)
