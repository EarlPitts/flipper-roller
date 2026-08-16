package flipper.db

import cats.effect.*
import cats.*
import cats.implicits.*
import com.google.cloud.firestore.{Firestore, FirestoreOptions}

import flipper.core.Flipper.*
import flipper.core.*

import scala.jdk.CollectionConverters.*

case class DbConfig(projectId: String)

trait Db:
  def addFlipper(flipper: Flipper): IO[Unit]
  def getFlippers: IO[List[Flipper]]
  def deleteAll: IO[Unit]

object Db:
  def make(config: DbConfig): Resource[IO, Db] =
    Resource
      .fromAutoCloseable {
        IO.blocking(
          FirestoreOptions.getDefaultInstance.toBuilder
            .setProjectId(config.projectId)
            .build()
            .getService
        )
      }
      .map { store =>
        new Db {
          def getFlippers =
            IO.blocking(store.collection("flippers").get().get())
              .map {
                _.getDocuments.asScala.toList
                  .map { doc =>
                    val name = doc.getString("name") // TODO handle error
                    val status = doc.getString("status") // TODO handle error
                    val dto = FlipperDTO(name, status)
                    Flipper.fromDTO(dto).get // TODO handle error
                  }
              }

          def addFlipper(flipper: Flipper) = flipper match
            case Done(name)    => ???
            case Waiting(name) =>
              IO.blocking(
                store
                  .collection("flippers")
                  .add(Map("name" -> name.unName, "status" -> "waiting").asJava)
                  .get
              )
            case InProgress(name) => ???

          def deleteAll = IO.blocking {
            val documents =
              store
                .collection("flippers")
                .get()
                .get()
                .getDocuments
                .asScala
                .toList
            documents.foreach(_.getReference.delete().get())
          }
        }

      }
