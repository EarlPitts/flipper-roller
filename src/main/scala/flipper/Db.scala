package flipper.db

import cats.effect.*
import cats.*
import cats.implicits.*
import com.google.cloud.firestore.FirestoreOptions
import scala.jdk.CollectionConverters.*

import flipper.core.Flipper.*
import flipper.core.*
import com.google.cloud.firestore.CollectionReference

trait Db:
  def addFlipper(flipper: Flipper): IO[Unit]
  def getFlippers: IO[List[Flipper]]
  def deleteAll: IO[Unit]
  def delete(name: Name): IO[Unit]

object Db:
  case class Config(projectId: String)

  def make(config: Config): Resource[IO, Db] =
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
        val collection = store.collection("flippers")
        new Db {
          def getFlippers = Db.getFlippers(collection)
          def addFlipper(flipper: Flipper) = Db.addFlipper(collection)(flipper)
          def deleteAll = Db.deleteAll(collection)
          def delete(name: Name) = Db.delete(collection)(name)
        }
      }

  def getFlippers(collection: CollectionReference) =
    IO.blocking(collection.get().get())
      .map {
        _.getDocuments.asScala.toList
          .map { doc =>
            val name = doc.getString("name") // TODO handle error
            val status = doc.getString("status") // TODO handle error
            val date = doc.getString("date") // TODO handle error
            val dto = FlipperDTO(name, status, date)
            Flipper.fromDTO(dto).get // TODO handle error
          }
      }

  def addFlipper(collection: CollectionReference)(flipper: Flipper) =
    IO.blocking(collection.add(Flipper.toDTO(flipper)).get).void

  def deleteAll(collection: CollectionReference) = IO.blocking {
    val documents = collection.get.get.getDocuments.asScala.toList
    documents.foreach(_.getReference.delete.get())
  }

  def delete(collection: CollectionReference)(name: Name) = IO.blocking {
    val documents =
      collection.get.get.getDocuments.asScala.toList
    documents
      .find(_.getString("name") == name.unName)
      .map(_.getReference.delete.get)
  }.void
