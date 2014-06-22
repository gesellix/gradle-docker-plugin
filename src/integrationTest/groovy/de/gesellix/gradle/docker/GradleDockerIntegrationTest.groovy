package de.gesellix.gradle.docker

import de.gesellix.docker.client.DockerClientImpl
import de.gesellix.gradle.docker.tasks.*
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification

@Ignore("only for exploring the docker api")
class GradleDockerIntegrationTest extends Specification {

  @Shared
  Project project

  def setup() {
    project = ProjectBuilder.builder().withName('example').build()
  }

  def "test build"() {
    given:
    def resource = getClass().getResourceAsStream('build.tar')
    def task = project.task('testBuild', type: DockerBuildTask)
    task.imageName = "buildTest"
    task.buildContext = resource

    when:
    def buildResult = task.build()

    then:
    buildResult ==~ "[a-z0-9]+"
  }

  def "test pull"() {
    given:
    def task = project.task('testPull', type: DockerPullTask)
    task.imageName = 'busybox'
    task.tag = 'latest'

    when:
    def pullResult = task.pull()

    then:
    pullResult == 'a9eb17255234'
  }

  def "test run"() {
    given:
    def task = project.task('testRun', type: DockerRunTask)
    task.containerConfiguration = ["Cmd": ["true"]]
    task.imageName = 'busybox'
    task.tag = 'latest'

    when:
    def runResult = task.run()

    then:
    runResult.container.Id ==~ "[a-z0-9]+"
    and:
    runResult.status == 204
  }

  def "test stop"() {
    given:
    def task = project.task('testStop', type: DockerStopTask)
    def runResult = new DockerClientImpl().run(["Cmd": ["true"]], 'busybox', 'latest')
    task.containerId = runResult.container.Id

    when:
    def stopResult = task.stop()

    then:
    stopResult == 204
  }

  def "test deploy"() {
    given:
    def task = project.task('dockerDeploy', type: DockerDeployTask)
    task.imageName = 'scratch'

    when:
    def deployResult = task.deploy()

    then:
    deployResult == '511136ea3c5a'
  }
}
