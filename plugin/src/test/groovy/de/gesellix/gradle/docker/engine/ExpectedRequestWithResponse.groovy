package de.gesellix.gradle.docker.engine

class ExpectedRequestWithResponse {

  String request
  String response

  boolean matches(String requestMethod, URI requestURI) {
    "$requestMethod $requestURI".startsWith(request)
  }
}
