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
    task.imageName = "user/imageName"

    when:
    task.build()

    then:
    1 * dockerClient.build(_ as InputStream) >> "4711"

    then:
    1 * dockerClient.tag("4711", "user/imageName")

    and:
    new File(task.outputs.files.asPath) == new File("${task.getTemporaryDir()}/buildContext_user_imageName")
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

    and:
    task.outputs.files.isEmpty()
  }

  def "accepts only task configs with at least one of buildContext or buildContextDirectory"() {
    given:
    task.buildContextDirectory = null
    task.buildContext = null

    when:
    task.execute()

    then:
    Exception exception = thrown()
    exception.message == "Execution failed for task ':dockerBuild'."
    and:
    exception.cause.message ==~ "assert getBuildContext\\(\\)\n\\s{7}\\|\n\\s{7}null"
  }

  def "accepts exactly one of buildContext or buildContextDirectory"() {
    URL dockerfile = getClass().getResource('/docker/Dockerfile')
    def baseDir = new File(dockerfile.toURI()).parentFile
    def inputStream = new FileInputStream(File.createTempFile("docker", "test"))

    given:
    task.buildContextDirectory = baseDir
    task.buildContext = inputStream

    when:
    task.execute()

    then:
    Exception exception = thrown()
    exception.message == "Execution failed for task ':dockerBuild'."
    and:
    exception.cause.message ==~ "assert !getBuildContext\\(\\)\n\\s{7}\\|\\|\n\\s{7}\\|java.io.FileInputStream@\\w+\n\\s{7}false"
  }
}
