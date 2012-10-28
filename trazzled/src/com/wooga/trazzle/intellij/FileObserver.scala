import actors.Actor
import java.io.File
package com.wooga.trazzle.intellij
{
  class FileObserver(clientId: Int, location: String, listener: Actor) extends Actor {
    def act() {
      StdOutput !(clientId, "Starting FileObserver " + location)

      val file = new File(location)
      var lastModified = file.lastModified

      val schedule = new Schedule(1000, this)
      schedule.start()
      loop {
        react {
          case 'timeout =>
            if (file.lastModified != lastModified)
            {
              listener ! location
              lastModified = file.lastModified
            }
          case Stop =>
            schedule ! Stop
            StdOutput !(clientId, "Stopping FileObserver " + location)
            exit
        }
      }
    }
  }

  class Schedule(interval: Int, owner: Actor) extends Actor {
    def act() {
      Thread.sleep(1000)
      loop {
        owner ! 'timeout
        receiveWithin(1000) {
          case Stop =>
            exit
          case _ =>
        }
        Thread.sleep(1000)
      }

    }
  }
}