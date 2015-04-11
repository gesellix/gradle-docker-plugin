package de.gesellix.gradle.docker.tasks

import de.gesellix.docker.client.DockerClient
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class DockerPullTaskSpec extends Specification {

  def project
  def task
  def dockerClient = Mock(DockerClient)

  def setup() {
    project = ProjectBuilder.builder().build()
    task = project.task('dockerPull', type: DockerPullTask)
    task.dockerClient = dockerClient
  }

  def "delegates to dockerClient"() {
    given:
    task.authConfigPlain = [username: "user", password: "pass"]
    task.imageName = "imageName"
    task.tag = "latest"
    task.registry = "registry.example.com:4711"

    when:
    task.execute()

    then:
    1 * dockerClient.encodeAuthConfig(['username': 'user', 'password': 'pass']) >> "-foo-"
    then:
    1 * dockerClient.pull("imageName", "latest", "-foo-", "registry.example.com:4711")
  }
}
