package de.gesellix.gradle.docker

import de.gesellix.docker.client.DockerClientImpl
import de.gesellix.gradle.docker.tasks.*
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.IgnoreIf
import spock.lang.Shared
import spock.lang.Specification

@IgnoreIf({ !System.env.DOCKER_HOST })
class DockerPluginIntegrationTest extends Specification {

  @Shared
  Project project

//  def defaultDockerHost = "unix:///var/run/docker.sock"
  def defaultDockerHost = System.env.DOCKER_HOST?.replaceFirst("tcp://", "http://")
  def DOCKER_HOST = defaultDockerHost

  def setup() {
    project = ProjectBuilder.builder().withName('example').build()
    project.apply plugin: 'de.gesellix.docker'
    project.docker.dockerHost = DOCKER_HOST
  }

  def "test build"() {
    given:
//    def resource = getClass().getResourceAsStream('build.tar')
    def resource = getClass().getResource('/docker/Dockerfile')
    def task = project.task('testBuild', type: DockerBuildTask)
    task.configure {
      imageName = "buildTest"
//    buildContext = resource
      buildContextDirectory = new File(resource.toURI()).parentFile
    }
    task.tarOfBuildcontextTask.execute()

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
    pullResult == 'e72ac664f4f0'
  }

  def "test push"() {
    given:
    def authDetails = ["username"     : "gesellix",
                       "password"     : "-yet-another-password-",
                       "email"        : "tobias@gesellix.de",
                       "serveraddress": "https://index.docker.io/v1/"]
    def dockerClient = new DockerClientImpl(dockerHost: DOCKER_HOST)
    def authConfig = dockerClient.encodeAuthConfig(authDetails)
    dockerClient.pull("scratch")
    dockerClient.tag("scratch", "gesellix/example")

    def task = project.task('testPush', type: DockerPushTask)
    task.repositoryName = 'gesellix/example'
//    task.authConfigPlain = authDetails
    task.authConfigEncoded = authConfig
    // don't access the official registry,
    // so that we don't try (and fail) to authenticate for each test execution.
    // for a real test, this line needs to be commented or removed, though.
    task.registry = 'example.com:5000'

    when:
    def pushResult = task.push()

    then:
    //pushResult.status ==~ "Pushing tag for rev \\[[a-z0-9]+\\] on \\{https://registry-1.docker.io/v1/repositories/gesellix/example/tags/latest\\}"
    //pushResult.error ==~ "Error: Status 401 trying to push repository gesellix/example: \"\""
    pushResult ==~ "Invalid Registry endpoint: Get http://example.com:5000/v1/_ping: dial tcp \\d{1,3}.\\d{1,3}.\\d{1,3}.\\d{1,3}:5000: i/o timeout"
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
    def runResult = new DockerClientImpl(dockerHost: DOCKER_HOST).run('busybox', ["Cmd": ["true"]], [:], 'latest')
    task.containerId = runResult.container.Id

    when:
    def stopResult = task.stop()

    then:
    stopResult == 204
  }

  def "test rm"() {
    given:
    def task = project.task('testRm', type: DockerRmTask)
    def dockerClient = new DockerClientImpl(dockerHost: DOCKER_HOST)
    def runResult = dockerClient.run('busybox', ["Cmd": ["true"]], [:], 'latest')
    def containerId = runResult.container.Id
    dockerClient.stop(containerId)
    dockerClient.wait(containerId)
    task.containerId = containerId

    when:
    def rmResult = task.rm()

    then:
    rmResult == 204
  }

  def "test start"() {
    given:
    def task = project.task('testStart', type: DockerStartTask)
    def dockerClient = new DockerClientImpl(dockerHost: DOCKER_HOST)
    dockerClient.pull("busybox", "latest")
    def containerInfo = dockerClient.createContainer(["Image": "busybox:latest", "Cmd": ["true"]])
    task.containerId = containerInfo.Id

    when:
    def startResult = task.start()

    then:
    startResult == 204
  }

  def "test ps"() {
    given:
    def task = project.task('testPs', type: DockerPsTask)
    task.dockerHost = DOCKER_HOST
    def uuid = UUID.randomUUID().toString()
    def cmd = "true || $uuid".toString()
    new DockerClientImpl(dockerHost: DOCKER_HOST).run('busybox', ["Cmd": [cmd]], [:], 'latest')

    when:
    def psResult = task.ps()

    then:
    psResult.findAll {
      it.Command == cmd
    }.size() == 1
  }

  def "test images"() {
    given:
    def task = project.task('testImages', type: DockerImagesTask)
    task.dockerHost = DOCKER_HOST

    when:
    def imagesResult = task.images()

    then:
    imagesResult.findAll {
      it.RepoTags.contains "buildTest:latest"
    }.size() == 1
  }
}
