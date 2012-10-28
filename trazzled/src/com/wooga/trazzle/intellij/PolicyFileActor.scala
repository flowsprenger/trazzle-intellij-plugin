import actors.Actor
import java.net.ServerSocket
import java.nio.charset.Charset

package com.wooga.trazzle.intellij
{
  object PolicyFileActor extends Actor {

    val policy = "<?xml version=\"1.0\"?> <!DOCTYPE cross-domain-policy SYSTEM \"/xml/dtds/cross-domain-policy.dtd\"> <cross-domain-policy><site-control permitted-cross-domain-policies=\"master-only\"/><allow-access-from domain=\"*\" to-ports=\"3457\" /> </cross-domain-policy> "

    def act() {
      val policyPort = 843;
      val policyListener = new ServerSocket(policyPort)

      StdOutput !(-1, "Policy Server on port " + policyPort)

      while (true) {
        val socket = policyListener.accept
        socket.getOutputStream.write(policy.getBytes(Charset.forName("UTF-8")))
        socket.getOutputStream.flush
        socket.close
      }
    }
  }
}

