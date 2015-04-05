package de.gesellix.gradle.docker.tasks

import de.gesellix.docker.client.DockerClient
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class DockerCleanupTaskSpec extends Specification {

  def project
  def task
  def dockerClient = Mock(DockerClient)

  def setup() {
    project = ProjectBuilder.builder().build()
    task = project.task('dockerCleanup', type: DockerCleanupTask)
    task.dockerClient = dockerClient
  }

  def "removes exited containers"() {
    given:
    task.shouldKeepContainer = { container ->
      container.Names.any { String name ->
        name.replaceAll("^/", "").matches(".*data.*")
      }
    }

    when:
    task.execute()

    then:
    1 * dockerClient.ps([filters: [status: "exited"]]) >> [
        content: [
            [Command: "ping 127.0.0.1",
             Id     : "container-id-1",
             Image  : "gesellix/docker-client-testimage:latest",
             Names  : ["/agitated_bardeen"],
             Status : "Exited (137) 13 minutes ago"],
            [Command: "ping 127.0.0.1",
             Id     : "container-id-2",
             Image  : "gesellix/docker-client-testimage:latest",
             Names  : ["/my_data"],
             Status : "Exited (137) 13 minutes ago"]
        ]
    ]
    then:
    1 * dockerClient.rm("container-id-1")
    and:
    0 * dockerClient.rm("container-id-2")
    and:
    1 * dockerClient.images([filters: [dangling: true]]) >> [:]
  }

  def "removes dangling images"() {
    when:
    task.execute()

    then:
    1 * dockerClient.ps([filters: [status: "exited"]]) >> [:]
    and:
    1 * dockerClient.images([filters: [dangling: true]]) >> [
        content: [
            [Created    : 1420075526,
             Id         : "image-id-1",
             ParentId   : "f62feddc05dc67da9b725361f97d7ae72a32e355ce1585f9a60d090289120f73",
             RepoTags   : ["<none>": "<none>"],
             Size       : 0,
             VirtualSize: 188299119]
        ]
    ]
    then:
    1 * dockerClient.rmi("image-id-1")
  }
}
