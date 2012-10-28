package com.wooga.trazzle.intellij
{
  import actors.Actor
  import scala.Predef._

  object StdOutput extends Actor {

      var client: ITrazzleLogClient = new DefaultLogger

      def configureClient(client: ITrazzleLogClient) {
        this.client = client;
      }

      def act {
        loop {
          react {
            case (clientId: Int, message: String) =>
              client.log(clientId, message, null, null, 0, 0)
            case (clientId: Int, message: String, level: String, stacktrace: String, timestamp: Number, stackIndex: Int) =>
              client.log(clientId, message, level, stacktrace, timestamp, stackIndex)
            case _ =>
          }
        }
      }
    }

  object FlashLogger extends Actor {
    def act {
      StdOutput !(-1, "FlashLogger started")
      loop {
        react {
          case (clientId: Int, log: FlashLogMessage) =>
            StdOutput !(clientId, log.getMessage, log.getLevelName, log.getStacktrace, log.getTimestamp, log.getStackIndex)
          case Stop =>
            StdOutput !(-1, "FlashLogger stopped")
            exit

          case _ =>
        }
      }
    }
  }
}

import reflect.BeanProperty

class FlashLogMessage {
  @BeanProperty var message: String = null
  @BeanProperty var encodeHTML: Boolean = true
  @BeanProperty var stacktrace: String = null
  @BeanProperty var levelName: String = null
  @BeanProperty var timestamp: Number = 0
  @BeanProperty var stackIndex: Int = 0
}