package com.example

import cats.effect.{IO, Resource}
import doobie._
import doobie.hikari._
import doobie.implicits._
import cats.effect.unsafe.implicits.global

object Database {
  val host = "localhost"
  val port = 5432
  val dbName = "fruitlabs"
  val dbUser = "postgres"
  val dbPassword = "password"

  val dbUrl = s"jdbc:postgresql://$host:$port/$dbName"

  def transactor: Resource[IO, HikariTransactor[IO]] = {
    HikariTransactor.newHikariTransactor[IO](
      driverClassName = "org.postgresql.Driver",
      url = dbUrl,
      user = dbUser,
      pass = dbPassword,
      connectEC = ExecutionContexts.synchronous
    )
  }
}

case class Fruit(id: Int, name: String, quantity: Int, pricePerPound: Int)

@main
def run() =
  // Print the database URL to verify it
  println(s"Database URL: ${Database.dbUrl}")

  // Example query to check the connection
  val query = sql"SELECT id, name, quantity, pricePerPound FROM inventory"
    .query[Fruit]

  val program = Database.transactor.use { xa =>
    query.to[List].transact(xa).flatMap { inventoryList =>
      IO {
        inventoryList.foreach { inventory =>
          println(
            s"ID: ${inventory.id}, Name: ${inventory.name}, Quantity: ${inventory.quantity}, Price per Pound: ${inventory.pricePerPound}"
          )
        }
      }
    }
  }
  program.unsafeRunSync()
