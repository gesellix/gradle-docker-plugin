package de.gesellix.gradle.docker.tasks

import de.gesellix.docker.client.DockerClient
import de.gesellix.docker.client.EngineResponseContent
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class DockerPsTaskSpec extends Specification {

  def project
  def task
  def dockerClient = Mock(DockerClient)

  def setup() {
    project = ProjectBuilder.builder().build()
    task = project.tasks.register('dockerPs', DockerPsTask).get()
    task.dockerClient = dockerClient
  }

  def "delegates to dockerClient and saves result"() {
    given:
    def expectedResult = new EngineResponseContent([])

    when:
    task.ps()

    then:
    1 * dockerClient.ps() >> expectedResult
    and:
    task.containers == expectedResult
  }
}
