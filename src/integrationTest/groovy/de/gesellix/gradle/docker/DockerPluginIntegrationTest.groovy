package de.gesellix.gradle.docker

import de.gesellix.docker.client.DockerClientImpl
import de.gesellix.gradle.docker.tasks.*
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification

@Ignore("only for exploring the docker api")
class DockerPluginIntegrationTest extends Specification {

  @Shared
  Project project

  def DOCKER_HOST = "http://172.17.42.1:4243/"

  def setup() {
    project = ProjectBuilder.builder().withName('example').build()
    project.apply plugin: 'docker'
    project.docker.dockerHost = DOCKER_HOST
  }

  def "test build"() {
    given:
//    def resource = getClass().getResourceAsStream('build.tar')
    def resource = getClass().getResource('/docker/Dockerfile')
    def task = project.task('testBuild', type: DockerBuildTask)
    task.imageName = "buildTest"
//    task.buildContext = resource
    task.buildContextDirectory = new File(resource.toURI()).parentFile

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

  def "test push"() {
    given:
    def authDetails = ["username"     : "gesellix",
                       "password"     : "-yet-another-password-",
                       "email"        : "tobias@gesellix.de",
                       "serveraddress": "https://index.docker.io/v1/"]
    def authConfig = new DockerClientImpl().encodeAuthConfig(authDetails)

    def task = project.task('testPush', type: DockerPushTask)
    task.repositoryName = 'gesellix/example'
//    task.authConfigPlain = authDetails
    task.authConfigEncoded = authConfig

    when:
    def pushResult = task.push()

    then:
    pushResult.status ==~ "Pushing tag for rev \\[[a-z0-9]+\\] on \\{https://registry-1.docker.io/v1/repositories/gesellix/example/tags/latest\\}"
  }

  def "test run"() {
    given:
    def task = project.task('testRun', type: DockerRunTask)
    task.dockerHost = DOCKER_HOST
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
    def runResult = new DockerClientImpl(dockerHost: DOCKER_HOST).run(["Cmd": ["true"]], 'busybox', 'latest')
    task.containerId = runResult.container.Id

    when:
    def stopResult = task.stop()

    then:
    stopResult == 204
  }

  def "test ps"() {
    given:
    def task = project.task('testPs', type: DockerPsTask)
    task.dockerHost = DOCKER_HOST
    def uuid = UUID.randomUUID().toString()
    def cmd = "true || $uuid".toString()
    new DockerClientImpl(dockerHost: DOCKER_HOST).run(["Cmd": [cmd]], 'busybox', 'latest')

    when:
    def psResult = task.ps()

    then:
    psResult.findAll {
      it.Command == cmd
    }.size() == 1
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
