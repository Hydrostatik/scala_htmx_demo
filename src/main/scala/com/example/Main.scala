package com.example
import cats.effect.IO

@main
def run() =
  val _ = HelloWorld.say().flatMap(IO.println)
  ()
