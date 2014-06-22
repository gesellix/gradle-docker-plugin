package de.gesellix.gradle.docker.tasks

import de.gesellix.docker.client.DockerClient
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class DockerPsTaskSpec extends Specification {

  def project
  def task
  def dockerClient = Mock(DockerClient)

  def setup() {
    project = ProjectBuilder.builder().build()
    task = project.task('dockerPs', type: DockerPsTask)
    task.dockerClient = dockerClient
  }

  def "delegates to dockerClient"() {
    when:
    task.ps()

    then:
    1 * dockerClient.ps()
  }
}
