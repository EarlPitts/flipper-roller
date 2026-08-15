package flipper.web

import cats.effect.*
import cats.*
import cats.implicits.*
import org.http4s.dsl.io._
import org.http4s.ember.server.*
import org.http4s.server.Router
import org.http4s.HttpRoutes
import org.http4s.implicits.*
import scalatags.Text.all.*
import org.http4s.scalatags.*
import scalatags.Text.TypedTag
import com.comcast.ip4s.*
import org.http4s.server.Server

import Flipper.*
import flipper.db.*

object Web:
  def make(db: Db): Resource[IO, Server] = EmberServerBuilder
    .default[IO]
    .withHost(ipv4"0.0.0.0")
    .withPort(port"8080")
    .withHttpApp(httpApp)
    .build

val httpApp = Router {
  "/" -> HttpRoutes.of[IO] { case GET -> Root =>
    Ok(template("Flipper Roller", mainView(Name.example)))
  }

}.orNotFound

def template(t: String, content: TypedTag[String]) = html(
  head(
    // TODO figure out title
    // script(src := "..."),
    // script(
    //   "alert('Hello World')"
    // )
  ),
  body(content)
)

def mainView(flippers: List[Flipper]) = div(
  // TODO add new flipper
  flippersView(flippers)
)

def flippersView(flippers: List[Flipper]) = div(
  h1(id := "title", "Flipper Roller"),
  flippers.map(flipperView)
)

def flipperView(flipper: Flipper) = div(
  flipper match {
    case Waiting(name)    => span(name.unName, " ", "Waiting")
    case InProgress(name) => span(name.unName, " ", "In Progress")
    case Done(name)       => span(name.unName, " ", "Done")
  }
)

case class Name private (unName: String)

object Name:

  val example = List(Waiting(Name("sajt")), InProgress(Name("kacsa")))

  def mkName(str: String): Option[Name] =
    Option.when(
      str.length < 100 &&
        str.forall(c => c.isLower || c == '_')
    )(Name(str))

enum Flipper:
  case Waiting(name: Name)
  case InProgress(name: Name)
  case Done(name: Name)
