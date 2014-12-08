package de.gesellix.gradle.docker.tasks

import de.gesellix.docker.client.DockerClient
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification
import spock.lang.Unroll

class DockerPushTaskSpec extends Specification {

  def project
  def task
  def dockerClient = Mock(DockerClient)

  def setup() {
    project = ProjectBuilder.builder().build()
    task = project.task('dockerPush', type: DockerPushTask)
    task.dockerClient = dockerClient
  }

  @Unroll
  def "delegates to dockerClient with registry=#registry"() {
    given:
    def authDetails = ["username"     : "gesellix",
                       "password"     : "-yet-another-password-",
                       "email"        : "tobias@gesellix.de",
                       "serveraddress": "https://index.docker.io/v1/"]
    task.repositoryName = "repositoryName"
    task.registry = registry
    task.authConfigPlain = authDetails
//    task.authConfigEncoded = "--auth.base64--"

    when:
    task.execute()

    then:
    1 * dockerClient.encodeAuthConfig(authDetails) >> "--auth.base64--"

    then:
    1 * dockerClient.push("repositoryName", "--auth.base64--", registry)

    where:
    registry << [null, "registry.docker.io"]
  }
}
