package com.wooga.trazzle.intellij
{
  import java.net.{SocketException, Socket}
  import actors.Actor
  import java.io._
  import flex.messaging.io.amf._
  import flex.messaging.io.{SerializationContext, MessageDeserializer}
  import java.nio.charset.Charset
  import collection.mutable.HashMap
  import java.util.ArrayList
  import java.nio.{ByteOrder, ByteBuffer}
  import scala.collection.JavaConversions._
  import replutils._
  import scala.Some

  class Client(socket: Socket, clientId: Int, client: ITrazzleLogClient) extends Actor {

    val syncronizer: ClientSyncronizer = new ClientSyncronizer(socket.getOutputStream)

    def act() {
      try {
        syncronizer.start()

        val inputBuffer = new BufferedInputStream(socket.getInputStream)

        val header: Array[Byte] = new Array[Byte](9)
        inputBuffer.read(header, 0, 9)
        if (header(8) == 0)
        {
          client.addClient(clientId)

          StdOutput !(clientId, "Client connected from " + socket.getInetAddress + ":" + socket.getPort)
          StdOutput !(clientId, "assigning id " + clientId)

          while (true) {

            try {
              val message = new ActionMessage();
              inputBuffer.read(header, 0, 4)

              val blazeDeserializer: MessageDeserializer = new AmfMessageDeserializer
              blazeDeserializer.initialize(new SerializationContext, inputBuffer, new AmfTrace)
              blazeDeserializer.readMessage(message, new ActionContext())

              for (body <- message.getBodies.toList) {
                syncronizer ! body.asInstanceOf[MessageBody]
              }
            } catch {
              case e =>
                socket.getOutputStream.write(PolicyFileActor.policy.getBytes(Charset.forName("UTF-8")))
                socket.getOutputStream.flush
                client.removeClient(clientId)
                socket.close()
                throw new EOFException
            }
          }
        }else{
          socket.getOutputStream.write(PolicyFileActor.policy.getBytes(Charset.forName("UTF-8")))
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
        client.removeClient(clientId)

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
              val bytes = new Array[Byte](4)
              ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN).putInt(outBuffer.size)
              out.write(bytes,0,4)
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
            case _ =>
              StdOutput !(clientId, "receive back")
          }
        }
      }

      def handleMessage(message: MessageBody) {

        message.getTargetURI match {
          case "CoreService.setConnectionParams" =>
            client.addSwfUrl( clientId, message.getData.asInstanceOf[Array[Object]](0).asInstanceOf[ASObject].get("swfURL").toString )
          case "LoggingService.log" =>
            FlashLogger !(clientId, message.getData.asInstanceOf[Array[Object]](0))
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
}
