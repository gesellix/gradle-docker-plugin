package de.gesellix.gradle.docker.tasks

import de.gesellix.docker.client.DockerClient
import de.gesellix.docker.engine.EngineResponse
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class DockerVolumesTaskSpec extends Specification {

  def project
  def task
  def dockerClient = Mock(DockerClient)

  def setup() {
    project = ProjectBuilder.builder().build()
    task = project.task('dockerVolumes', type: DockerVolumesTask)
    task.dockerClient = dockerClient
  }

  def "delegates to dockerClient and saves result"() {
    given:
    def expectedResult = new EngineResponse(content: ["Name": "id"])

    when:
    task.volumes()

    then:
    1 * dockerClient.volumes([:]) >> expectedResult

    and:
    task.volumes == expectedResult
  }

  def "delegates with query to dockerClient and saves result"() {
    given:
    def expectedResult = new EngineResponse(content: ["Name": "id"])

    when:
    task.configure {
      query = [filters: [dangling: ["true"]]]
    }
    task.volumes()

    then:
    1 * dockerClient.volumes([filters: [dangling: ["true"]]]) >> expectedResult

    and:
    task.volumes == expectedResult
  }
}
