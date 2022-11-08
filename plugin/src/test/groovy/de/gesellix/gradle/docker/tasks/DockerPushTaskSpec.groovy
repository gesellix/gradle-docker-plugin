package de.gesellix.gradle.docker.tasks

import de.gesellix.docker.authentication.AuthConfig
import de.gesellix.docker.client.DockerClient
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification
import spock.lang.Unroll

import java.time.Duration
import java.time.temporal.ChronoUnit

class DockerPushTaskSpec extends Specification {

  def project
  def task
  def dockerClient = Mock(DockerClient)

  def setup() {
    project = ProjectBuilder.builder().build()
    task = project.tasks.register('dockerPush', DockerPushTask).get()
    task.dockerClient = dockerClient
    task.pushTimeout = Duration.of(1, ChronoUnit.SECONDS)
  }

  @Unroll
  def "delegates to dockerClient with registry=#registry"() {
    given:
    def authDetails = new AuthConfig("username": "gesellix",
                                     "password": "-yet-another-password-",
                                     "email": "tobias@gesellix.de",
                                     "serveraddress": "https://index.docker.io/v1/")
    task.repositoryName = "repositoryName"
    task.registry = registry
    task.authConfig = authDetails
//    task.authConfigEncoded = "--auth.base64--"

    when:
    task.push()

    then:
    1 * dockerClient.encodeAuthConfig(authDetails) >> "--auth.base64--"

    then:
    1 * dockerClient.push(_, _, "repositoryName", "--auth.base64--", registry)

    where:
    registry << [null, "registry.docker.io"]
  }
}
