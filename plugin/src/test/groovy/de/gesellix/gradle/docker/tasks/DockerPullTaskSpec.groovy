package de.gesellix.gradle.docker.tasks

import de.gesellix.docker.authentication.AuthConfig
import de.gesellix.docker.client.DockerClient
import de.gesellix.docker.engine.EngineResponse
import de.gesellix.docker.engine.EngineResponseStatus
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

import java.time.Duration
import java.time.temporal.ChronoUnit

class DockerPullTaskSpec extends Specification {

  def project
  def task
  def dockerClient = Mock(DockerClient)

  def setup() {
    project = ProjectBuilder.builder().build()
    task = project.task('dockerPull', type: DockerPullTask)
    task.dockerClient = dockerClient
    task.pullTimeout = Duration.of(1, ChronoUnit.SECONDS)
  }

  def "delegates to dockerClient"() {
    given:
    task.authConfig = new AuthConfig(username: "user", password: "pass")
    task.imageName = "imageName"
    task.imageTag = "latest"
    task.registry = "registry.example.com:4711"
    def response = Mock(EngineResponse)
    response.status >> Mock(EngineResponseStatus)

    when:
    task.pull()

    then:
    1 * dockerClient.encodeAuthConfig(new AuthConfig(username: "user", password: "pass")) >> "-foo-"
    then:
    1 * dockerClient.pull(_, _, "registry.example.com:4711/imageName", "latest", "-foo-")
  }
}
