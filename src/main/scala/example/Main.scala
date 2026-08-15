package flipper

import cats.effect.*
import cats.*
import cats.implicits.*
import org.typelevel.log4cats.LoggerFactory
import org.typelevel.log4cats.slf4j.Slf4jFactory

import flipper.web.*
import flipper.db.*
import flipper.db.Db.*

implicit val loggerFactory: LoggerFactory[IO] = Slf4jFactory.create[IO]

object Main extends ResourceApp.Forever:
  def run(args: List[String]) =
    Db.make().flatMap { db =>
      Web.make(db).void
    }
