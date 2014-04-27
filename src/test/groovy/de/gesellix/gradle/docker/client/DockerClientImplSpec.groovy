package de.gesellix.gradle.docker.client

import com.jayway.restassured.RestAssured
import com.xebialabs.restito.server.StubServer
import spock.lang.Shared
import spock.lang.Specification

import static com.xebialabs.restito.builder.stub.StubHttp.whenHttp
import static com.xebialabs.restito.builder.verify.VerifyHttp.verifyHttp
import static com.xebialabs.restito.semantics.Action.status
import static com.xebialabs.restito.semantics.Action.stringContent
import static com.xebialabs.restito.semantics.Condition.*
import static org.glassfish.grizzly.http.Method.POST
import static org.glassfish.grizzly.http.util.HttpStatus.OK_200

class DockerClientImplSpec extends Specification {

  @Shared
  StubServer server

  DockerClient dockerClient

  def setupSpec() {
    server = new StubServer().run()
    RestAssured.port = server.getPort()
  }

  def cleanupSpec() {
    server.stop()
  }

  def setup() {
    dockerClient = new DockerClientImpl("127.0.0.1", server.getPort())
  }

  def "build image"() {
    expect:
    dockerClient.build() == null
  }

  def "pull image"() {
    given:
    whenHttp(server).
        match(post("/images/create"),
              parameter("fromImage", "scratch")).
        then(status(OK_200),
             stringContent("{\"status\":\"Pulling repository scratch\"}\r\n{\"status\":\"Pulling image (latest) from scratch\",\"progressDetail\":{},\"id\":\"511136ea3c5a\"}{\"status\":\"Pulling image (latest) from scratch, endpoint: https://cdn-registry-1.docker.io/v1/\",\"\
      progressDetail\":{},\"id\":\"511136ea3c5a\"}{\"status\":\"Pulling dependent layers\",\"progressDetail\":{},\"id\":\"511136ea3c5a\"}{\"status\":\"Download complete\",\"progressDetail\":{},\"id\":\"511136ea3c5a\"}{\"status\":\"Download complete\",\"progressDetail\"\
      :{},\"id\":\"511136ea3c5a\"}"));

    when:
    def imageId = dockerClient.pull("scratch")

    then:
    verifyHttp(server).once(
        method(POST),
        uri("/images/create"),
        parameter("fromImage", "scratch")
    )

    and:
    imageId == "511136ea3c5a"
  }
}
