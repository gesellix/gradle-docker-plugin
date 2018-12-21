package de.gesellix.gradle.docker.engine

import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpServer

import java.util.concurrent.Executors

class HttpTestServer {

    HttpServer httpServer

    def start(String context, HttpHandler handler) {
        InetSocketAddress address = new InetSocketAddress(0)

        httpServer = HttpServer.create(address, address.port)

        httpServer.with {
            createContext(context, handler)
            setExecutor(Executors.newCachedThreadPool())
            start()
        }
        return httpServer.address
    }

    def stop() {
        if (httpServer) {
            httpServer.stop(0)
        }
    }
}
