import collection.mutable.HashMap
import com.wooga.trazzle.intellij.{TrazzleD, ITrazzleLogClient}
import flex.messaging.io.{MessageDeserializer, SerializationContext}
import java.io._
import java.net.{ServerSocket, Socket, SocketException}
import java.nio.charset.Charset
import java.nio.file._
import java.util.ArrayList
import scala.actors.Actor
import flex.messaging.io.amf._
import scala.collection.JavaConversions._
import replutils._

object PortableTrazzleD {
  def main(args: Array[String]) {
    TrazzleD.start();
  }
}

package com.wooga.trazzle.intellij{

  import java.nio.{ByteOrder, ByteBuffer}

  class DefaultLogger extends ITrazzleLogClient {
    def log(clientId: Integer, message: String, level: String, stacktrace: String, timestamp: Number, stackIndex: Integer) {
      println(message)
    }

    def addClient(clientId: Integer) {

    }

    def removeClient(clientId: Integer) {}

    def addSwfUrl(clientId:Integer, swfUrl:String) {}
  }


  object TrazzleD extends Actor {
    var client: ITrazzleLogClient = new DefaultLogger

    def configureClient(client: ITrazzleLogClient) {
      this.client = client;
      StdOutput.configureClient(client);
    }

    def act() {
      val port = 3457
      StdOutput.configureClient(client);
      StdOutput.start()
      FlashLogger.start()
      PolicyFileServer.start()

      try {


        val listener = new ServerSocket(port)
        listener.setReceiveBufferSize(10)
        var numClients = 1

        StdOutput !(-1, "Listening on port " + port)

        while (true) {


          new ClientHandler(listener.accept(), numClients, client).start()
          numClients += 1
        }

        listener.close()

        FlashLogger ! Stop
      }
      catch {
        case e: IOException =>
          StdOutput !(-1, "Could not listen on port: " + port + ".")
          System.exit(-1)
      }
    }
  }

  class ClientHandler(socket: Socket, clientId: Int, client: ITrazzleLogClient) extends Actor {
    val syncronizer: ClientSyncronizer = new ClientSyncronizer(socket.getOutputStream())

    def act {
      try {
        syncronizer.start()

        var inputBuffer = new BufferedInputStream(socket.getInputStream()) //new BufferedInputStream(socket.getInputStream())

        val header: Array[Byte] = new Array[Byte](9)
        inputBuffer.read(header, 0, 9)
        if (header(8) == 0)
        {
          client.addClient(clientId)

          StdOutput !(clientId, "Client connected from " + socket.getInetAddress() + ":" + socket.getPort)
          StdOutput !(clientId, "assigning id " + clientId)

          while (true) {

            try {
              val message = new ActionMessage();
              inputBuffer.read(header, 0, 4)

              var blazeDeserializer: MessageDeserializer = new AmfMessageDeserializer
              blazeDeserializer.initialize(new SerializationContext, inputBuffer, new AmfTrace)
              blazeDeserializer.readMessage(message, new ActionContext())

              for (body <- message.getBodies.toList) {
                syncronizer ! body.asInstanceOf[MessageBody]
              }
            } catch {
              case e =>
                println(e.toString)
                val policy = "<?xml version=\"1.0\"?> <!DOCTYPE cross-domain-policy SYSTEM \"/xml/dtds/cross-domain-policy.dtd\"> <cross-domain-policy><site-control permitted-cross-domain-policies=\"master-only\"/><allow-access-from domain=\"*\" to-ports=\"3457\" /> </cross-domain-policy> ";
                socket.getOutputStream.write(policy.getBytes(Charset.forName("UTF-8")))
                socket.getOutputStream.flush
                client.removeClient(clientId);
                socket.close()
                throw new EOFException
            }
          }
        }else{
          val policy = "<?xml version=\"1.0\"?> <!DOCTYPE cross-domain-policy SYSTEM \"/xml/dtds/cross-domain-policy.dtd\"> <cross-domain-policy><site-control permitted-cross-domain-policies=\"master-only\"/><allow-access-from domain=\"*\" to-ports=\"3457\" /> </cross-domain-policy> ";
          socket.getOutputStream.write(policy.getBytes(Charset.forName("UTF-8")))
          socket.getOutputStream.flush
          socket.close()
          throw new EOFException
        }

      }
      catch {
        case e: SocketException =>
          StdOutput !(clientId, e)

        case e: IOException =>
          StdOutput !(clientId, e.printStackTrace())

        case e: EOFException =>
        case e =>
          StdOutput !(clientId, "Unknown error " + e)
      } finally {
        syncronizer ! Stop
        socket.close()
        client.removeClient(clientId);

      }
    }

    class ClientSyncronizer(out: OutputStream) extends Actor {
      val fileObservers = new HashMap[String, FileObserver]

      def act {
        val serializer = new LengthAwareAmfMessageSerializer()
        val outBuffer = new ByteArrayOutputStream()
        serializer.initialize(new SerializationContext, outBuffer, new AmfTrace)

        loop {
          react {
            case location: String =>
              val message = new ActionMessage
              val list = new Array[String](1)
              list(0) = location
              val body = new MessageBody("FileObservingService.fileDidChange", "/no", list)
              message.getBodies.asInstanceOf[ArrayList[Object]].add(body)
              serializer.writeMessage(message)
              outBuffer.flush
              val bytes = new Array[Byte](4);
              ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN).putInt(outBuffer.size);
              out.write(bytes,0,4)
              println("pushing length of "+outBuffer.size)
              out.write(outBuffer.toByteArray)
              outBuffer.reset

              StdOutput !(clientId, location + " changed informing listener")
            case message: MessageBody =>
              handleMessage(message)

            case Stop =>
              for ((key, observer) <- fileObservers) {
                observer ! Stop
              }
              exit
            case _ => StdOutput !(clientId, "receive back")
          }
        }
      }

      def handleMessage(message: MessageBody) {

        message.getTargetURI match {
          case "CoreService.setConnectionParams" =>
            client.addSwfUrl( clientId, message.getData.asInstanceOf[Array[Object]](0).asInstanceOf[ASObject].get("swfURL").toString );
          case "LoggingService.log" => FlashLogger !(clientId, message.getData.asInstanceOf[Array[Object]](0));
          case "FileObservingService.startObservingFile" =>
            val file = message.getData.asInstanceOf[Array[Object]](0).toString
            if (fileObservers.get(file) == None) {
              val observer = new FileObserver(clientId, file, this)
              fileObservers.put(file, observer)
              observer.start()
            }
          case "InspectionService.inspectObject_metadata" =>
            StdOutput !(clientId, "Inspect : " + printAttrValues(message.getData))
          case "FileObservingService.stopObservingFile" =>
            val file = message.getData.asInstanceOf[Array[Object]](0).toString
            if (fileObservers.get(file) != None) {
              val observer = fileObservers.get(file)
              observer match {
                case None =>
                case Some(x) =>
                  x ! Stop
                  fileObservers.remove(file)
              }

            }
          case _ =>
        }
      }
    }


  }

  class FileObserver(clientId: Int, location: String, listener: Actor) extends Actor {
    def act {
      StdOutput !(clientId, "Starting FileObserver " + location)

      val watcher = FileSystems.getDefault().newWatchService();
      val path = FileSystems.getDefault().getPath(location);

      val key = path.getParent.register(watcher, StandardWatchEventKinds.ENTRY_MODIFY)

      val sched = new Schedule(1000, this)
      sched.start()
      loop {
        react {
          case 'timeout =>
            try {
              val events = key.pollEvents();
              for (val event <- events.asInstanceOf[ArrayList[WatchEvent[Path]]]) {
                StdOutput !(clientId, event.context().getFileName + " changed" + event.kind().name() + " " + StandardWatchEventKinds.ENTRY_MODIFY)
                listener ! location
              }
              key.reset
            } catch {
              case e =>
                StdOutput !(clientId, "watching " + location + " fails due to exception " + e)
            }
          case Stop =>
            key.cancel
            watcher.close
            sched ! Stop
            StdOutput !(clientId, "Stopping FileObserver " + location)
            exit
        }
      }
    }
  }

  class Schedule(interval: Int, owner: Actor) extends Actor {
    def act {
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

  object FlashLogger extends Actor {
    def act {
      StdOutput !(-1, "FlashLogger started")
      loop {
        react {
          case (clientId: Int, log: FlashLogMessage) => StdOutput !(clientId, log.getMessage, log.getLevelName, log.getStacktrace, log.getTimestamp, log.getStackIndex)

          case Stop =>
            StdOutput !(-1, "FlashLogger stopped")
            exit

          case _ =>
        }
      }

    }

    def getLevelColor(level: String) {
      level match {

        case "" => "\u001B[0m"
        case "debug" => "\u001B[37m"
        case "info" => "\u001B[33m"
        case "notice" => "\u001B[32m"
        case "warning" => "\u001B[36m"
        case "error" => "\u001B[31m"
        case "critical" => "\u001B[31m"
        case "fatal" => "\u001B[31m"
        case _ => "\u001B[0m"
      }
    }
  }


  object PolicyFileServer extends Actor {

    def act {
      val policyPort = 843;
      val policyListener = new ServerSocket(policyPort)

      val policy = "<?xml version=\"1.0\"?> <!DOCTYPE cross-domain-policy SYSTEM \"/xml/dtds/cross-domain-policy.dtd\"> <cross-domain-policy><site-control permitted-cross-domain-policies=\"master-only\"/><allow-access-from domain=\"*\" to-ports=\"3457\" /> </cross-domain-policy> ";

      StdOutput !(-1, "Policy Server on port " + policyPort)

      while (true) {

        var socket = policyListener.accept()
        println("serving policy file request")
        socket.getOutputStream.write(policy.getBytes(Charset.forName("UTF-8")))
        socket.getOutputStream.flush
        socket.close()
      }
    }


  }

  object StdOutput extends Actor {

    var client: ITrazzleLogClient = new DefaultLogger

    def configureClient(client: ITrazzleLogClient) {
      this.client = client;
    }

    def act {
      loop {
        react {
          case (clientId: Int, message: String) => client.log(clientId, message, null, null, 0, 0)
          case (clientId: Int, message: String, level: String, stacktrace: String, timestamp: Number, stackIndex: Int) => client.log(clientId, message, level, stacktrace, timestamp, stackIndex)
          case _ =>
        }
      }
    }
  }

  object Stop {

  }

}

import reflect.BeanProperty

class FlashLogMessage {
  @BeanProperty var message: String = null;
  @BeanProperty var encodeHTML: Boolean = true;
  @BeanProperty var stacktrace: String = null;
  @BeanProperty var levelName: String = null;
  @BeanProperty var timestamp: Number = 0;
  @BeanProperty var stackIndex: Int = 0;
}




