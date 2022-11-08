package de.gesellix.gradle.docker.tasks

import de.gesellix.docker.client.DockerClient
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class DockerRestartTaskSpec extends Specification {

  def project
  def task
  def dockerClient = Mock(DockerClient)

  def setup() {
    project = ProjectBuilder.builder().build()
    task = project.tasks.register('dockerRestart', DockerRestartTask).get()
    task.dockerClient = dockerClient
  }

  def "delegates to dockerClient"() {
    given:
    task.containerId = "4711"

    when:
    task.restart()

    then:
    1 * dockerClient.restart("4711")
  }
}
