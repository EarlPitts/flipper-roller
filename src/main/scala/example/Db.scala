package flipper.db

import cats.effect.*
import cats.*
import cats.implicits.*

import flipper.web.Flipper.*
import flipper.web.Flipper

object Db:
  def make(): Resource[IO, Db] =
    Resource.make(IO.pure(List.empty))(_ => IO.unit)

type Db = List[Flipper]
