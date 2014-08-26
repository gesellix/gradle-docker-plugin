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

  def "depends on tar task to archive buildContextDirectory"() {
    URL dockerfile = getClass().getResource('/docker/Dockerfile')
    def baseDir = new File(dockerfile.toURI()).parentFile

    given:
    task.buildContextDirectory = baseDir
    task.imageName = "user/imageName"

    when:
    task.configure()

    then:
    project.getTasksByName("tarOfBuildcontext", false).size() == 1

    and:
    task.dependsOn.any { it == project.getTasksByName("tarOfBuildcontext", false).first() }
  }

  def "tar of buildContextDirectory contains buildContextDirectory"() {
    URL dockerfile = getClass().getResource('/docker/Dockerfile')
    def baseDir = new File(dockerfile.toURI()).parentFile

    given:
    task.buildContextDirectory = baseDir
    task.imageName = "user/imageName"

    when:
    task.configure()

    then:
    def tarOfBuildcontextTask = project.getTasksByName("tarOfBuildcontext", false).first()
    tarOfBuildcontextTask.destinationDir == new File("${tarOfBuildcontextTask.getTemporaryDir()}")

    and:
    tarOfBuildcontextTask.inputs.files.asPath == project.fileTree(baseDir).asPath
  }

  // TODO this should become an integration test, so that the 'task dependsOn tarOfBuildcontext' also works
  def "delegates to dockerClient with tar of buildContextDirectory as buildContext"() {
    URL dockerfile = getClass().getResource('/docker/Dockerfile')
    def baseDir = new File(dockerfile.toURI()).parentFile

    given:
    task.buildContextDirectory = baseDir
    task.imageName = "user/imageName"
    task.dockerClient = dockerClient
    task.configure()
    def tarOfBuildcontextTask = project.getTasksByName("tarOfBuildcontext", false).first()
    tarOfBuildcontextTask.execute()

    when:
    task.execute()

    then:
    1 * dockerClient.build({ FileInputStream })
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

  // TODO this should become an integration test
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

  // TODO this should become an integration test
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

  def "normalizedImageName should match [a-z0-9-_.]"() {
    expect:
    task.getNormalizedImageName() ==~ "[a-z0-9-_\\.]+"
  }
}
