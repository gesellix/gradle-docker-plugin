package de.gesellix.gradle.docker.client

import com.jayway.restassured.RestAssured
import com.xebialabs.restito.semantics.Action
import com.xebialabs.restito.semantics.Condition
import com.xebialabs.restito.server.StubServer
import spock.lang.Shared
import spock.lang.Specification

import static com.xebialabs.restito.builder.stub.StubHttp.whenHttp
import static com.xebialabs.restito.builder.verify.VerifyHttp.verifyHttp
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
        match(Condition.post("/images/create"),
              Condition.parameter("fromImage", "scratch")).
        then(Action.status(OK_200),
             Action.resourceContent(getClass().getResource("pull_image_responses.chunked.json")));

    when:
    def imageId = dockerClient.pull("scratch")

    then:
    verifyHttp(server).once(
        Condition.method(POST),
        Condition.uri("/images/create"),
        Condition.parameter("fromImage", "scratch")
    )

    and:
    imageId == "511136ea3c5a"
  }
}
