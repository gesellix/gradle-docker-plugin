package de.gesellix.gradle.docker

import de.gesellix.gradle.docker.client.DockerClient
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class DockerDeployTaskSpec extends Specification {

  def project
  def task
  def dockerClient = Mock(DockerClient)

  def setup() {
    project = ProjectBuilder.builder().build()
    task = project.task('dockerDeploy', type: DockerDeployTask)
    task.dockerClient = dockerClient
  }

  def "delegates to dockerClient"() {
    given:
    task.imageName = "scratch"

    when:
    task.deploy()

    then:
    1 * dockerClient.pull("scratch")
  }
}
