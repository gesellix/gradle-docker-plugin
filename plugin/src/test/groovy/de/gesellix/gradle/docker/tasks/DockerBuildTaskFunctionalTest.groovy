package de.gesellix.gradle.docker.tasks

import de.gesellix.docker.client.DockerClientImpl
import de.gesellix.docker.client.LocalDocker
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import spock.lang.Requires
import spock.lang.Specification
import spock.lang.TempDir

@Requires({ LocalDocker.available() })
class DockerBuildTaskFunctionalTest extends Specification {

  @TempDir
  File testProjectDir

  File buildFile

  // Also requires './gradlew :plugin:pluginUnderTestMetadata' to be run before performing the tests.
  def setup() {
    buildFile = new File(testProjectDir, 'build.gradle')
    buildFile << """
            plugins {
                id 'de.gesellix.docker'
            }
        """
  }

  def "can perform a build configured via config closure"() {
    given:
    URL dockerfile = getClass().getResource('/docker/Dockerfile')
    String baseDir = new File(dockerfile.toURI()).parentFile.absolutePath.replaceAll("\\${File.separator}", "/")
    String imageName = "gesellix/test-build:${UUID.randomUUID()}"

    buildFile << """
          task dockerBuild(type: de.gesellix.gradle.docker.tasks.DockerBuildTask) {
              buildContextDirectory = '$baseDir'
              imageName = '$imageName'
              doLast {
                  logger.lifecycle("Resulting image id: \${imageId}")
              }
          }
        """

    when:
    def result = GradleRunner.create()
        .withProjectDir(testProjectDir)
        .withArguments('dockerBuild', '--info', '--debug', '--stacktrace')
        .withPluginClasspath()
        .build()

    then:
    result.output.contains("Resulting image id: sha256:")
    result.task(":dockerBuild").outcome == TaskOutcome.SUCCESS

    cleanup:
    new DockerClientImpl().rmi(imageName)
  }

  def "can perform a build configured via task property setter"() {
    given:
    URL dockerfile = getClass().getResource('/docker/Dockerfile')
    String baseDir = new File(dockerfile.toURI()).parentFile.absolutePath.replaceAll("\\${File.separator}", "/")
    String imageName = "gesellix/test-build:${UUID.randomUUID()}"

    buildFile << """
          task dockerBuild(type: de.gesellix.gradle.docker.tasks.DockerBuildTask) {
              imageName = '$imageName'
              doFirst {
                  logger.lifecycle("buildContextDirectory: \${buildContextDirectory}")
              }
              doLast {
                  logger.lifecycle("Resulting image id: \${imageId}")
              }
          }
          dockerBuild.setBuildContextDirectory('$baseDir')
        """

    when:
    def result = GradleRunner.create()
        .withProjectDir(testProjectDir)
        .withArguments('dockerBuild', '--info', '--debug', '--stacktrace')
        .withPluginClasspath()
        .withDebug(true)
        .build()

    then:
    result.output.contains("Resulting image id: sha256:")
    result.task(":dockerBuild").outcome == TaskOutcome.SUCCESS

    cleanup:
    new DockerClientImpl().rmi(imageName)
  }

  def "accepts only task configs with at least one of buildContext or buildContextDirectory"() {
    given:
    buildFile << """
          task dockerBuild(type: de.gesellix.gradle.docker.tasks.DockerBuildTask) {
              buildContextDirectory = null
              buildContext = null
          }
        """

    when:
    GradleRunner.create()
        .withProjectDir(testProjectDir)
        .withArguments('dockerBuild')
        .withPluginClasspath()
        .build()

    then:
    Exception exception = thrown()
    exception.message.contains("Execution failed for task ':dockerBuild'.")
  }

  def "accepts exactly one of buildContext or buildContextDirectory"() {
    URL dockerfile = getClass().getResource('/docker/Dockerfile')
    String baseDir = new File(dockerfile.toURI()).parentFile.absolutePath.replaceAll("\\${File.separator}", "/")

    given:
    buildFile << """
          task dockerBuild(type: de.gesellix.gradle.docker.tasks.DockerBuildTask) {
              buildContextDirectory = '$baseDir'
              buildContext = new FileInputStream(File.createTempFile("docker", "test"))
          }
        """

    when:
    GradleRunner.create()
        .withProjectDir(testProjectDir)
        .withArguments('dockerBuild')
        .withPluginClasspath()
        .build()

    then:
    Exception exception = thrown()
    exception.message.contains("Execution failed for task ':dockerBuild'.")
  }
}
