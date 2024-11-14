package com.example

import cats.effect._
import doobie._
import doobie.hikari._
import doobie.implicits._
import com.comcast.ip4s._
import org.http4s.ember.server._
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.server.Router
import io.circe.generic.auto._
import org.http4s.circe._
import org.http4s.circe.CirceEntityEncoder.circeEntityEncoder

object Database {
  val host = "localhost"
  val port = 5432
  val dbName = "fruitlabs"
  val dbUser = "postgres"
  val dbPassword = "password"

  val dbUrl = s"jdbc:postgresql://$host:$port/$dbName"

  def transactor: Resource[IO, HikariTransactor[IO]] = {
    HikariTransactor.newHikariTransactor[IO](
      url = dbUrl,
      user = dbUser,
      pass = dbPassword,
      connectEC = ExecutionContexts.synchronous,
      driverClassName = "org.postgresql.Driver"
    )
  }
}

def getFruits: IO[List[Fruit]] =
  val query =
    sql"SELECT id, name, quantity, pricePerPound FROM inventory".query[Fruit]
  Database.transactor.use { xa =>
    query.to[List].transact(xa)
  }

case class Fruit(id: Int, name: String, quantity: Int, pricePerPound: Int)
implicit val fruitEncoder: EntityEncoder[IO, Fruit] = jsonEncoderOf[IO, Fruit]

val fruitService = HttpRoutes.of[IO] { case GET -> Root / "fruits" =>
  for {
    fruits <- getFruits
    response <- Ok(fruits)
  } yield response
}

val httpApp = Router("/api" -> fruitService).orNotFound

object Main extends IOApp {
  def run(args: List[String]): IO[ExitCode] =
    EmberServerBuilder
      .default[IO]
      .withHost(ipv4"0.0.0.0")
      .withPort(port"8080")
      .withHttpApp(httpApp)
      .build
      .use(_ => IO.never)
      .as(ExitCode.Success)
}
