package flipper.db

import cats.effect.*
import cats.*
import cats.implicits.*

import flipper.web.Flipper.*
import flipper.web.Flipper

trait Db:
  def addFlipper(flipper: Flipper): IO[Unit]
  def getFlippers: IO[List[Flipper]]

object Db:
  def make: Resource[IO, Db] =
    Resource.make(init)(_ => IO.unit)

  def init: IO[Db] =
    Ref[IO].of(List.empty[Flipper]).flatMap { flippersRef =>
      new Db {
        def getFlippers = flippersRef.get
        def addFlipper(flipper: Flipper) = flippersRef.update(flipper :: _)
      }.pure[IO]
    }
