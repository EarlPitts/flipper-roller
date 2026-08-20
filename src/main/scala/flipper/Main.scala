package flipper

import cats.effect.*
import cats.effect.implicits.*
import cats.effect.Clock
import cats.*
import cats.effect.Clock
import cats.implicits.*
import org.typelevel.log4cats.LoggerFactory
import org.typelevel.log4cats.slf4j.Slf4jFactory
import java.time.{ZoneId, ZonedDateTime, DayOfWeek, LocalTime, LocalDateTime}
import ciris.*

import flipper.web.*
import flipper.db.*
import flipper.scheduler.*

implicit val loggerFactory: LoggerFactory[IO] = Slf4jFactory.create[IO]

object Main extends ResourceApp.Forever:
  def run(args: List[String]) =
    val dayTime = DayTime(List(DayOfWeek.MONDAY), LocalTime.parse("08:12"))
    val job = Job(dayTime, IO.println("Hello from job"))

    Resource.eval(LoggerFactory[IO].create).flatMap { implicit logger =>
      Resource.eval(getConfig).flatMap { config =>
        schedule(config.tz, Clock[IO].realTimeInstant, job).flatMap { _ =>
          Db.make(config.dbConfig).flatMap { db =>
            Web.make(db).void
          }
        }
      }
    }

  def getConfig: IO[Config] =
    (
      env("DATABASE_PROJECT")
        .as[String]
        .default("flipper-roller")
        .map(Db.Config.apply),
      env("TIMEZONE")
        .as[String]
        .default("Europe/Budapest")
        .map(ZoneId.of)
    ).parMapN(Config.apply)
      .load[IO]

  case class Config(dbConfig: Db.Config, tz: ZoneId)
