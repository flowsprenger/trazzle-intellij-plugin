package com.wooga.trazzle.intellij

class DefaultLogger extends ITrazzleLogClient {
    def log(clientId: Integer, message: String, level: String, stacktrace: String, timestamp: Number, stackIndex: Integer) {
      println(message)
    }

    def addClient(clientId: Integer) {}

    def removeClient(clientId: Integer) {}

    def addSwfUrl(clientId:Integer, swfUrl:String) {}
  }
