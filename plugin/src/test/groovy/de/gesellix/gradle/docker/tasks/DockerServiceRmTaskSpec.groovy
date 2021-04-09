package de.gesellix.gradle.docker.tasks

import de.gesellix.docker.client.DockerClient
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class DockerServiceRmTaskSpec extends Specification {

  def project
  def task
  def dockerClient = Mock(DockerClient)

  def setup() {
    project = ProjectBuilder.builder().build()
    task = project.task('rmService', type: DockerServiceRmTask)
    task.dockerClient = dockerClient
  }

  def "delegates to dockerClient and saves result"() {
    given:
    task.serviceName = "a-service"

    when:
    task.rmService()

    then:
    1 * dockerClient.rmService("a-service") >> [content: "service-result"]

    and:
    task.response == [content: "service-result"]
  }
}
