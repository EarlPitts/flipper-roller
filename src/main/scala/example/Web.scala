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
import org.http4s.UrlForm

object Web:
  def make(db: Db): Resource[IO, Server] = EmberServerBuilder
    .default[IO]
    .withHost(ipv4"0.0.0.0")
    .withPort(port"8080")
    .withHttpApp(httpApp(db))
    .build

def httpApp(db: Db) = Router {
  "/" -> HttpRoutes.of[IO] {
    case GET -> Root =>
      db.getFlippers
        .flatMap { flippers =>
          Ok(template("Flipper Roller", mainView(flippers)))
        }

    case req @ POST -> Root =>
      req.as[UrlForm].flatMap { form =>
        val mName = form.getFirst("name").flatMap(Name.mkName)
        mName match
          case None       => BadRequest("Invalid name")
          case Some(name) =>
            val flipper = Waiting(name)
            for {
              _ <- db.addFlipper(flipper)
              flippers <- db.getFlippers
              resp <- Ok(template("Flipper Roller", flippersView(flippers)))
            } yield resp
      }
  }
}.orNotFound

val hxGet = attr("hx-get")
val hxPost = attr("hx-post")
val hxTarget = attr("hx-target")
val hxSwap = attr("hx-swap")
val hxTrigger = attr("hx-trigger")

def template(t: String, content: TypedTag[String]) = html(
  head(
    // TODO figure out title
    // script(src := "..."),
    script(
      src := "https://cdn.jsdelivr.net/npm/htmx.org@2.0.10/dist/htmx.min.js",
      integrity := "sha384-H5SrcfygHmAuTDZphMHqBJLc3FhssKjG7w/CeCpFReSfwBWDTKpkzPP8c+cLsK+V",
      crossorigin := "anonymous"
    )
    // script(
    //   "alert('Hello World')"
    // )
  ),
  body(content)
)

def mainView(flippers: List[Flipper]) = div(
  h1(id := "title", "Flipper Roller"),
  form(
    hxPost := "/",
    hxTarget := "#flippers",
    hxSwap := "outerHTML"
  )(
    input(`type` := "text", name := "name"),
    button(`type` := "submit")("Add")
  ),
  flippersView(flippers)
)

def flippersView(flippers: List[Flipper]) = div(id := "flippers")(
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
  def mkName(str: String): Option[Name] =
    Option.when(
      str.length < 100 &&
        str.forall(c => c.isLower || c == '_')
    )(Name(str))

enum Flipper:
  case Waiting(name: Name)
  case InProgress(name: Name)
  case Done(name: Name)
