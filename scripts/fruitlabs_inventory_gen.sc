#! /usr/bin/env -S scala-cli shebang -q

//> using scala "3.5.2"
//> using dep "org.scalacheck::scalacheck:1.16.0"

import org.scalacheck.Gen
import java.io.PrintWriter

case class Fruit(name: String, quantity: Int, pricePerPound: Int)

def run() =
  val fruits = List(
    Fruit("Apple", 10, 100),
    Fruit("Banana", 20, 50),
    Fruit("Cherry", 30, 200)
  ) ++ Gen
    .listOfN(
      1000,
      for {
        name <- Gen.alphaStr
          .flatMap(s => if s.length() < 10 then s else s.substring(0, 10))
          .suchThat(_.nonEmpty)
        quantity <- Gen.choose(1, 100)
        pricePerPound <- Gen.choose(100, 1000)
      } yield Fruit(name, quantity, pricePerPound)
    )
    .sample
    .get
  val writer = new PrintWriter("./scripts/fruits_inventory.csv")
  writer.write(fruitsToCSV(fruits))
  writer.close()
  println("CSV file 'fruits_inventory.csv' has been generated.")

def fruitsToCSV(fruits: List[Fruit]): String =
  val sb = new StringBuilder
  sb.append("name,quantity,pricePerPound\n")
  fruits.foreach { fruit =>
    sb.append(s"${fruit.name},${fruit.quantity},${fruit.pricePerPound}\n")
  }
  sb.toString

run()
