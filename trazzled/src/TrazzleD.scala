import com.wooga.trazzle.intellij.{TrazzleD}
import java.io._
import java.net.{ServerSocket}
import scala.actors.Actor

object PortableTrazzleD {
  def main(args: Array[String]) {
    TrazzleD.start();
  }
}

package com.wooga.trazzle.intellij{

  object TrazzleD extends Actor {
    var client: ITrazzleLogClient = new DefaultLogger

    def configureClient(client: ITrazzleLogClient) {
      this.client = client
      StdOutput.configureClient(client)
    }

    def act() {
      val port = 3457
      StdOutput.configureClient(client)
      StdOutput.start
      FlashLogger.start
      PolicyFileActor.start

      try {
        val listener = new ServerSocket(port)
        listener.setReceiveBufferSize(10)
        var numClients = 1

        StdOutput !(-1, "Listening on port " + port)

        while (true) {
          new Client(listener.accept, numClients, client).start()
          numClients += 1
        }

        listener.close

        FlashLogger ! Stop
      }
      catch {
        case e: IOException =>
          StdOutput !(-1, "Could not listen on port: " + port + ".")
          System.exit(-1)
      }
    }
  }



  object Stop {

  }

}




