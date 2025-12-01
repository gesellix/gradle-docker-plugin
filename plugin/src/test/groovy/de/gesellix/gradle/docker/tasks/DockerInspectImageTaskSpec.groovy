package de.gesellix.gradle.docker.tasks

import de.gesellix.docker.client.DockerClient
import de.gesellix.docker.client.EngineResponseContent
import de.gesellix.docker.remote.api.ImageInspect
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class DockerInspectImageTaskSpec extends Specification {

  def project
  def task
  def dockerClient = Mock(DockerClient)

  def setup() {
    project = ProjectBuilder.builder().build()
    task = project.tasks.register('dockerInspect', DockerInspectImageTask).get()
    task.dockerClient = dockerClient
  }

  def "delegates to dockerClient and returns result"() {
    given:
    task.imageId = "my.image:dev"
    def inspect = new ImageInspect().tap {
      it.id = "sha256:1234"
      it.parent = "parent"
    }
    def expectedResponse = new EngineResponseContent(inspect)

    when:
    task.inspect()

    then:
    1 * dockerClient.inspectImage("my.image:dev") >> expectedResponse
    and:
    task.imageInfo == expectedResponse
  }
}
