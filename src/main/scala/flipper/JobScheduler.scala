package flipper.scheduler

import java.time.{Instant, Duration}
import java.time.{ZoneId, ZonedDateTime, DayOfWeek, LocalTime, LocalDateTime}
import scala.jdk.DurationConverters.*
import java.time.temporal.ChronoField
import cats.effect.*
import cats.effect.implicits.*
import cats.*
import cats.implicits.*
import org.typelevel.log4cats.Logger

case class Job(time: DayTime, action: IO[Unit])

case class DayTime(dows: List[DayOfWeek], tod: LocalTime)

def schedule(
    tz: ZoneId,
    getTime: IO[Instant],
    job: Job
)(implicit logger: Logger[IO]): Resource[IO, IO[OutcomeIO[Unit]]] =
  val loop: IO[Unit] = (for
    now <- getTime
    delay = getDelay(tz, now, job.time).toScala
    _ <- IO.whenA(delay > Duration.ZERO.toScala) {
      logger.info(s"Scheduled in: ${delay.toMinutes}") >>
        IO.sleep(delay) >> job.action
    }
  yield ()).foreverM
  loop.background

def getDelay(tz: ZoneId, now: Instant, dayTime: DayTime): Duration =
  val nowDateTime = now.atZone(tz).toLocalDateTime
  val next = nextOccurrence(nowDateTime, dayTime)
  Duration.between(nowDateTime, next)

def nextOccurrence(
    current: LocalDateTime,
    dayTime: DayTime
): LocalDateTime =
  dayTime.dows.map { dow =>
    val candidate = current
      .`with`(ChronoField.DAY_OF_WEEK, dow.getValue)
      .toLocalDate
      .atTime(dayTime.tod)
    if candidate.isAfter(current)
    then candidate
    else candidate.plusWeeks(1)
  }.min
