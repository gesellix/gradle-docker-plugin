package de.gesellix.gradle.docker.tasks

import de.gesellix.docker.client.DockerClient
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class DockerPushTaskSpec extends Specification {

  def project
  def task
  def dockerClient = Mock(DockerClient)

  def setup() {
    project = ProjectBuilder.builder().build()
    task = project.task('dockerPush', type: DockerPushTask)
    task.dockerClient = dockerClient
  }

  def "delegates to dockerClient"() {
    given:
    task.repositoryName = "repositoryName"
    task.authConfig = "--auth.base64--"

    when:
    task.push()

    then:
    1 * dockerClient.push("repositoryName", "--auth.base64--")
  }
}
