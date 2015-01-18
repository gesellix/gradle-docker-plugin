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
    def task = project.task('testBuild', type: DockerBuildTask) {
      imageName = "buildTest"
//    buildContext = resource
      buildContextDirectory = new File(resource.toURI()).parentFile
    }
    task.tarOfBuildcontextTask.execute()

    when:
    task.execute()

    then:
    task.imageId ==~ "[a-z0-9]+"
  }

  def "test pull"() {
    given:
    def task = project.task('testPull', type: DockerPullTask) {
      imageName = 'busybox'
      tag = 'latest'
    }

    when:
    task.execute()

    then:
    task.imageId == '4986bf8c1536'
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

    def task = project.task('testPush', type: DockerPushTask) {
      repositoryName = 'gesellix/example'
//    authConfigPlain = authDetails
      authConfigEncoded = authConfig
      // don't access the official registry,
      // so that we don't try (and fail) to authenticate for each test execution.
      // for a real test, this line needs to be commented or removed, though.
      registry = 'example.com:5000'
    }

    when:
    task.execute()

    then:
    //pushResult.status ==~ "Pushing tag for rev \\[[a-z0-9]+\\] on \\{https://registry-1.docker.io/v1/repositories/gesellix/example/tags/latest\\}"
    //pushResult.error ==~ "Error: Status 401 trying to push repository gesellix/example: \"\""
    task.result =~ "Invalid registry endpoint https://example.com:5000/v1/: Get https://example.com:5000/v1/_ping: dial tcp \\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}:5000: i/o timeout. If this private registry supports only HTTP or HTTPS with an unknown CA certificate, please add `--insecure-registry example.com:5000` to the daemon's arguments. In the case of HTTPS, if you have access to the registry's CA certificate, no need for the flag; simply place the CA certificate at /etc/docker/certs.d/example.com:5000/ca.crt"
  }

  def "test run"() {
    given:
    def task = project.task('testRun', type: DockerRunTask) {
      dockerHost = DOCKER_HOST
      containerConfiguration = ["Cmd": ["true"]]
      imageName = 'busybox'
      tag = 'latest'
    }

    when:
    task.execute()

    then:
    task.result.container.Id ==~ "[a-z0-9]+"
    and:
    task.result.status == 204

    cleanup:
    new DockerClientImpl(dockerHost: DOCKER_HOST).rm(task.result.container.Id)
  }

  def "test stop"() {
    given:
    def runResult = new DockerClientImpl(dockerHost: DOCKER_HOST).run('busybox', ["Cmd": ["true"]], [:], 'latest')
    def task = project.task('testStop', type: DockerStopTask) {
      containerId = runResult.container.Id
    }

    when:
    task.execute()

    then:
    task.result == 204 || task.result == 304

    cleanup:
    new DockerClientImpl(dockerHost: DOCKER_HOST).rm(runResult.container.Id)
  }

  def "test rm"() {
    given:
    def dockerClient = new DockerClientImpl(dockerHost: DOCKER_HOST)
    def runResult = dockerClient.run('busybox', ["Cmd": ["true"]], [:], 'latest')
    def runningContainerId = runResult.container.Id
    dockerClient.stop(runningContainerId)
    dockerClient.wait(runningContainerId)
    def task = project.task('testRm', type: DockerRmTask) {
      containerId = runningContainerId
    }

    when:
    task.execute()

    then:
    task.result == 204
  }

  def "test start"() {
    given:
    def dockerClient = new DockerClientImpl(dockerHost: DOCKER_HOST)
    dockerClient.pull("busybox", "latest")
    def containerInfo = dockerClient.createContainer(["Image": "busybox:latest", "Cmd": ["true"]])
    def task = project.task('testStart', type: DockerStartTask) {
      containerId = containerInfo.Id
    }

    when:
    task.execute()

    then:
    task.result == 204

    cleanup:
    new DockerClientImpl(dockerHost: DOCKER_HOST).rm(containerInfo.Id)
  }

  def "test ps"() {
    given:
    def task = project.task('testPs', type: DockerPsTask) {
      dockerHost = DOCKER_HOST
    }
    def uuid = UUID.randomUUID().toString()
    def cmd = "true || $uuid".toString()
    def containerInfo = new DockerClientImpl(dockerHost: DOCKER_HOST).run('busybox', ["Cmd": [cmd]], [:], 'latest')

    when:
    task.execute()

    then:
    task.containers.findAll {
      it.Command == cmd
    }.size() == 1

    cleanup:
    new DockerClientImpl(dockerHost: DOCKER_HOST).rm(containerInfo.container.Id)
  }

  def "test images"() {
    given:
    def task = project.task('testImages', type: DockerImagesTask) {
      dockerHost = DOCKER_HOST
    }

    when:
    task.execute()

    then:
    task.images.findAll {
      it.RepoTags.contains "buildTest:latest"
    }.size() == 1
  }
}
