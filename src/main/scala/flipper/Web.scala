package flipper.web

import cats.effect.*
import cats.*
import cats.implicits.*
import org.http4s.dsl.io._
import org.http4s.ember.server.*
import org.http4s.server.Router
import org.http4s.HttpRoutes
import org.http4s.implicits.*
import com.comcast.ip4s.*
import org.http4s.server.Server
import org.http4s.UrlForm
import org.http4s.scalatags.*
import org.typelevel.log4cats.Logger

import flipper.core.*
import flipper.core.Flipper.*
import flipper.web.view.*
import flipper.db.*

object Web:
  def make(db: Db)(implicit logger: Logger[IO]): Resource[IO, Server] =
    EmberServerBuilder
      .default[IO]
      .withHost(ipv4"0.0.0.0")
      .withPort(port"8080")
      .withHttpApp(httpApp(db))
      .build

def httpApp(db: Db)(implicit logger: Logger[IO]) = Router {
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
              _ <- logger.info(s"Adding flipper $flipper")
              _ <- db.addFlipper(flipper)
              flippers <- db.getFlippers
              resp <- Ok(flippersView(flippers))
            } yield resp
      }

    case DELETE -> Root =>
      logger.info("Deleting flippers") >>
        db.deleteAll >>
        db.getFlippers
          .flatMap { flippers =>
            Ok(flippersView(flippers))
          }

    case DELETE -> Root / nameStr =>
      Name.mkName(nameStr) match
        case None       => BadRequest()
        case Some(name) =>
          logger.info(s"Deleting flipper $name") >>
            db.delete(name) >>
            db.getFlippers
              .flatMap { flippers =>
                Ok(flippersView(flippers))
              }
  }
}.orNotFound
