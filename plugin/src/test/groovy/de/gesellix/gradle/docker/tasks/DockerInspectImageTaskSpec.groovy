package de.gesellix.gradle.docker.tasks

import de.gesellix.docker.client.DockerClient
import de.gesellix.docker.client.EngineResponseContent
import de.gesellix.docker.remote.api.GraphDriverData
import de.gesellix.docker.remote.api.ImageInspect
import de.gesellix.docker.remote.api.ImageInspectRootFS
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
    def expectedResponse = new EngineResponseContent(new ImageInspect(
        "sha256:1234", null, null, "parent", "", "", "",
        null, "", "", null, "", "", "", "",
        0, 0, new GraphDriverData("", [:]),
        new ImageInspectRootFS("", []),
        null
    ))

    when:
    task.inspect()

    then:
    1 * dockerClient.inspectImage("my.image:dev") >> expectedResponse
    and:
    task.imageInfo == expectedResponse
  }
}
