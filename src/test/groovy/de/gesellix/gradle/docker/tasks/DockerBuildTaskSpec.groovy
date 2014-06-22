package de.gesellix.gradle.docker.tasks

import de.gesellix.docker.client.DockerClient
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class DockerBuildTaskSpec extends Specification {

  def project
  def task
  def dockerClient = Mock(DockerClient)

  def setup() {
    project = ProjectBuilder.builder().build()
    task = project.task('dockerBuild', type: DockerBuildTask)
    task.dockerClient = dockerClient
  }

  def "delegates to dockerClient with buildContextDirectory"() {
    URL dockerfile = getClass().getResource('/docker/Dockerfile')
    def baseDir = new File(dockerfile.toURI()).parentFile

    given:
    task.buildContextDirectory = baseDir
    task.imageName = "imageName"

    when:
    task.build()

    then:
    1 * dockerClient.build(_ as InputStream) >> "4711"

    then:
    1 * dockerClient.tag("4711", "imageName")
  }

  def "delegates to dockerClient with buildContext"() {
    def inputStream = new FileInputStream(File.createTempFile("docker", "test"))

    given:
    task.buildContext = inputStream
    task.imageName = "imageName"

    when:
    task.build()

    then:
    1 * dockerClient.build(inputStream) >> "4711"

    then:
    1 * dockerClient.tag("4711", "imageName")
  }
}
