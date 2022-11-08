package de.gesellix.gradle.docker.tasks

import de.gesellix.docker.client.DockerClient
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class DockerTagTaskSpec extends Specification {

  def project
  def task
  def dockerClient = Mock(DockerClient)

  def setup() {
    project = ProjectBuilder.builder().build()
    task = project.tasks.register('dockerTag', DockerTagTask).get()
    task.dockerClient = dockerClient
  }

  def "delegates to dockerClient"() {
    given:
    task.imageId = "4711"
    task.imageTag = "aTag"

    when:
    task.tag()

    then:
    1 * dockerClient.tag("4711", "aTag")
  }
}
