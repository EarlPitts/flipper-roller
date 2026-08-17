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

import flipper.web.*
import flipper.db.*
import flipper.db.Db.*
import flipper.scheduler.*

implicit val loggerFactory: LoggerFactory[IO] = Slf4jFactory.create[IO]

object Main extends ResourceApp.Forever:
  def run(args: List[String]) =
    val dbConfig = DbConfig("flipper-roller")
    val tz = ZoneId.of("Europe/Budapest")
    val dayTime = DayTime(List(DayOfWeek.MONDAY), LocalTime.parse("08:12"))
    val job = Job(dayTime, IO.println("Hello from job"))

    schedule(tz, Clock[IO].realTimeInstant, job).flatMap { _ =>
      Db.make(dbConfig).flatMap { db =>
        Web.make(db).void
      }
    }
