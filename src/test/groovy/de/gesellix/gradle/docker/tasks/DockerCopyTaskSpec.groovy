package de.gesellix.gradle.docker.tasks

import de.gesellix.docker.client.DockerClient
import org.apache.commons.io.IOUtils
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class DockerCopyTaskSpec extends Specification {

  def project
  def task
  def dockerClient = Mock(DockerClient)

  def setup() {
    project = ProjectBuilder.builder().build()
    task = project.task('dockerCopy', type: DockerCopyTask)
    task.dockerClient = dockerClient
  }

  def "delegates to dockerClient and saves result"() {
    given:
    task.containerId = "4711"
    task.filename = "/file.txt"

    when:
    task.execute()

    then:
    1 * dockerClient.copyFile("4711", "/file.txt") >> "file-content".bytes
    and:
    task.content == "file-content".bytes
  }

  def "delegates to dockerClient and writes to targetFile"() {
    given:
    task.containerId = "4711"
    task.filename = "/file.txt"
    def tempFile = File.createTempFile("docker-cp", "txt")
    tempFile.deleteOnExit()
    task.targetFilename = tempFile.absolutePath

    when:
    task.execute()

    then:
    1 * dockerClient.copyFile("4711", "/file.txt") >> "file-content".bytes

    and:
    def inputStream = new FileInputStream(tempFile)
    IOUtils.toString(inputStream) == "file-content"
    IOUtils.closeQuietly(inputStream)
  }
}
