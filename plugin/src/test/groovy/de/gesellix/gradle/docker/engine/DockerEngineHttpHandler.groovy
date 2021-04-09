package de.gesellix.gradle.docker.engine

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler

class DockerEngineHttpHandler implements HttpHandler {

  List<ExpectedRequestWithResponse> expectedRequests = []

  @Override
  void handle(HttpExchange httpExchange) throws IOException {
    println("engine> ${httpExchange.requestMethod} ${httpExchange.requestURI}")

    // TODO consume fully
//        httpExchange.requestBody

    if (expectedRequests.first().matches(httpExchange.requestMethod, httpExchange.requestURI)) {
      def response = expectedRequests.remove(0).response.bytes
      httpExchange.getResponseHeaders().set("Content-Type", "application/json")
      httpExchange.sendResponseHeaders(200, response.length)
      httpExchange.responseBody.write(response)
      httpExchange.responseBody.close()
    }
    else {
      httpExchange.sendResponseHeaders(500, 0)
    }
  }
}
