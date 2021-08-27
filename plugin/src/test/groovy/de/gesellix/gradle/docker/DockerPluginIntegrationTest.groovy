package de.gesellix.gradle.docker

import de.gesellix.docker.client.DockerClient
import de.gesellix.docker.client.DockerClientImpl
import de.gesellix.docker.client.LocalDocker
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.spockframework.util.Assert
import spock.lang.Ignore
import spock.lang.Requires
import spock.lang.Specification
import spock.lang.TempDir

@Requires({ LocalDocker.available() })
class DockerPluginIntegrationTest extends Specification {

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

  def "test info"() {
    given:
    buildFile << """
          task dockerInfo(type: de.gesellix.gradle.docker.tasks.DockerInfoTask) {
              doLast {
                  logger.lifecycle("request succeeded: " + (info.status.code == 200))
              }
          }
        """

    when:
    def result = GradleRunner.create()
        .withProjectDir(testProjectDir)
        .withArguments('dockerInfo', '--debug', '--info', '--stacktrace')
        .withPluginClasspath()
        .build()

    then:
    result.output.contains("request succeeded: true")
    result.task(':dockerInfo').outcome == TaskOutcome.SUCCESS
  }

  def "test pull"() {
    given:
    buildFile << """
          task dockerPull(type: de.gesellix.gradle.docker.tasks.DockerPullTask) {
              imageName = 'gesellix/testimage'
              tag = 'os-linux'
              doLast {
                  logger.lifecycle("testimage id: " + imageId)
              }
          }
        """

    when:
    def result = GradleRunner.create()
        .withProjectDir(testProjectDir)
        .withArguments('dockerPull')
        .withPluginClasspath()
        .build()

    then:
    result.output.contains("id: sha256:")
    result.task(':dockerPull').outcome == TaskOutcome.SUCCESS
  }

  @Ignore
  def "test pull with auth"() {
    given:
    buildFile << """
          task dockerPullPrivate(type: de.gesellix.gradle.docker.tasks.DockerPullTask) {
              authConfigPlain = dockerClient.readDefaultAuthConfig()
              imageName = 'gesellix/private-repo'
              tag = 'latest'
              doLast {
                  logger.lifecycle("testimage id: " + imageId)
              }
          }
        """

    when:
    def result = GradleRunner.create()
        .withProjectDir(testProjectDir)
        .withArguments('dockerPullPrivate')
        .withPluginClasspath()
        .build()

    then:
    result.output.contains("id: sha256:33ce5a2c7ad9915f26cc2263b8aee3be33832611795f2e3906104438b611b208")
    result.task(':dockerPullPrivate').outcome == TaskOutcome.SUCCESS
  }

  def "test push"() {
    given:
    def dockerClient = new DockerClientImpl()
    pull(dockerClient, "gesellix/testimage", "os-linux")
    tag(dockerClient, "gesellix/testimage:os-linux", "gesellix/example")

    buildFile << """
          task dockerPush(type: de.gesellix.gradle.docker.tasks.DockerPushTask) {
              repositoryName = 'gesellix/example'

              authConfig.set(new de.gesellix.docker.client.authentication.AuthConfig(
                    "username": "gesellix",
                    "password": "-yet-another-password-",
                    "email": "tobias@gesellix.de",
                    "serveraddress": "https://index.docker.io/v1/"))

              // don't access the official registry,
              // so that we don't try (and fail) to authenticate for each test execution.
              // for a real test, this line needs to be commented or removed, though.
              registry = 'example.com:5000'

              doLast {
                  logger.lifecycle("request succeeded: " + (info.status.code == 200))
              }
          }
        """

    when:
    GradleRunner.create()
        .withProjectDir(testProjectDir)
        .withArguments('dockerPush')
        .withPluginClasspath()
        .build()

    then:
    //pushResult.status ==~ "Pushing tag for rev \\[[a-z0-9]+\\] on \\{https://registry-1.docker.io/v1/repositories/gesellix/example/tags/latest\\}"
    //pushResult.error ==~ "Error: Status 401 trying to push repository gesellix/example: \"\""
    def exc = thrown(Exception)
    exc.message.contains("error:Get https://example.com:5000/v2/")

    cleanup:
    dockerClient.rmi("gesellix/example")
    dockerClient.rmi("example.com:5000/gesellix/example")
  }

  def "test run"() {
    given:
    buildFile << """
          task dockerRun(type: de.gesellix.gradle.docker.tasks.DockerRunTask) {
              containerConfiguration = ["Cmd"       : ["true"],
                                        "HostConfig": ["AutoRemove": true]]
              imageName = 'gesellix/testimage'
              tag = 'os-linux'
              containerName = "test-run"
              doLast {
                  logger.lifecycle("container id: " + result.container.content.Id)
                  logger.lifecycle("request successful: " + (result.status.status.code == 204))
              }
          }
        """

    when:
    def result = GradleRunner.create()
        .withProjectDir(testProjectDir)
        .withArguments('dockerRun')
        .withPluginClasspath()
        .build()

    then:
    result.output.contains("request successful: true")
    result.task(':dockerRun').outcome == TaskOutcome.SUCCESS

    cleanup:
    def dockerClient = new DockerClientImpl()
    dockerClient.stop('test-run')
    dockerClient.wait('test-run')
    dockerClient.rm('test-run')
  }

  def "test stop"() {
    given:
    def dockerClient = new DockerClientImpl()
    def runResult = dockerClient.run(
        'gesellix/testimage',
        ["Cmd"       : ["ping", "127.0.0.1"],
         "HostConfig": ["AutoRemove": true]],
        'os-linux',
        "test-stop")

    buildFile << """
          task dockerStop(type: de.gesellix.gradle.docker.tasks.DockerStopTask) {
              containerId = '${runResult.container.content.Id}'
              doLast {
                  logger.lifecycle("request successful: " + (result.status.code in [204, 304]))
              }
          }
        """

    when:
    def result = GradleRunner.create()
        .withProjectDir(testProjectDir)
        .withArguments('dockerStop')
        .withPluginClasspath()
        .build()

    then:
    result.output.contains("request successful: true")
    result.task(':dockerStop').outcome == TaskOutcome.SUCCESS

    cleanup:
    dockerClient.stop('test-stop')
    dockerClient.wait('test-stop')
    dockerClient.rm('test-stop')
  }

  def "test rm"() {
    given:
    def dockerClient = new DockerClientImpl()
    def runResult = dockerClient.run(
        'gesellix/testimage',
        ["Cmd": ["true"]],
        'os-linux',
        "test-rm")
    String runningContainerId = runResult.container.content.Id
    dockerClient.stop(runningContainerId)
    dockerClient.wait(runningContainerId)

    buildFile << """
          task dockerRm(type: de.gesellix.gradle.docker.tasks.DockerRmTask) {
              containerId = '$runningContainerId'
              doLast {
                  logger.lifecycle("request successful: " + (result.status.code == 204))
              }
          }
        """

    when:
    def result = GradleRunner.create()
        .withProjectDir(testProjectDir)
        .withArguments('dockerRm')
        .withPluginClasspath()
        .build()

    then:
    result.output.contains("request successful: true")
    result.task(':dockerRm').outcome == TaskOutcome.SUCCESS

    cleanup:
    dockerClient.stop('test-rm')
    dockerClient.wait('test-rm')
    dockerClient.rm('test-rm')
  }

  def "test start"() {
    given:
    def dockerClient = new DockerClientImpl()
    pull(dockerClient, "gesellix/testimage", "os-linux")
    def containerInfo = dockerClient.createContainer([
        "Image"     : "gesellix/testimage:os-linux",
        "Cmd"       : ["true"],
        "Name"      : "test-start",
        "HostConfig": ["AutoRemove": true]])

    buildFile << """
          task dockerStart(type: de.gesellix.gradle.docker.tasks.DockerStartTask) {
              containerId = '${containerInfo.content.Id}'
              doLast {
                  logger.lifecycle("request successful: " + (result.status.code == 204))
              }
          }
        """

    when:
    def result = GradleRunner.create()
        .withProjectDir(testProjectDir)
        .withArguments('dockerStart')
        .withPluginClasspath()
        .build()

    then:
    result.output.contains("request successful: true")
    result.task(':dockerStart').outcome == TaskOutcome.SUCCESS

    cleanup:
    dockerClient.stop('test-start')
    dockerClient.wait('test-start')
    dockerClient.rm('test-start')
  }

  def "test ps"() {
    given:
    def dockerClient = new DockerClientImpl()
    def uuid = UUID.randomUUID().toString()
    def cmd = "true || $uuid".toString()
    dockerClient.run(
        'gesellix/testimage',
        ["Cmd": [cmd]],
        'os-linux',
        "test-ps")

    buildFile << """
          task dockerPs(type: de.gesellix.gradle.docker.tasks.DockerPsTask) {
              doLast {
                  logger.lifecycle("request successful: " + (containers.status.code == 200))
                  boolean found = containers.content.findAll {
                      it.Names.first() == '/test-ps'
                  }.size() == 1
                  logger.lifecycle("found: " + found)
              }
          }
        """

    when:
    def result = GradleRunner.create()
        .withProjectDir(testProjectDir)
        .withArguments('dockerPs')
        .withPluginClasspath()
        .build()

    then:
    result.output.contains("found: true")
    result.task(':dockerPs').outcome == TaskOutcome.SUCCESS

    cleanup:
    dockerClient.stop('test-ps')
    dockerClient.wait('test-ps')
    dockerClient.rm('test-ps')
  }

  def "test images"() {
    given:
    def dockerClient = new DockerClientImpl()
    pull(dockerClient, "gesellix/testimage", "os-linux")
    tag(dockerClient, "gesellix/testimage:os-linux", "gesellix/images-list")

    buildFile << """
          task dockerImages(type: de.gesellix.gradle.docker.tasks.DockerImagesTask) {
              doLast {
                  logger.lifecycle("request successful: " + (images.status.code == 200))
                  boolean found = images.content.findAll {
                      it.RepoTags?.contains "gesellix/images-list:latest"
                  }.size() == 1
                  logger.lifecycle("found: " + found)
              }
          }
        """

    when:
    def result = GradleRunner.create()
        .withProjectDir(testProjectDir)
        .withArguments('dockerImages')
        .withPluginClasspath()
        .build()

    then:
    result.output.contains("found: true")
    result.task(':dockerImages').outcome == TaskOutcome.SUCCESS

    cleanup:
    dockerClient.rmi("gesellix/images-list:latest")
  }

  def "test run with data volumes"() {
    given:
    def hostDir = "/tmp"
    def dockerClient = new DockerClientImpl()

    pull(dockerClient, "gesellix/testimage", "os-linux")
    tag(dockerClient, "gesellix/testimage:os-linux", "gesellix/run-with-data-volumes")

    dockerClient.createContainer(
        [
            "Cmd"       : ["-"],
            "Image"     : "gesellix/run-with-data-volumes",
            "HostConfig": [
                "Binds"     : ["$hostDir:/data".toString()],
                "AutoRemove": true
            ],
        ], [
            name: "the-data-example"
        ])

    buildFile << """
          task dockerRun(type: de.gesellix.gradle.docker.tasks.DockerRunTask) {
              containerConfiguration = ["Cmd"       : ["true"],
                                        "HostConfig": ["VolumesFrom": ["the-data-example"],
                                                       "AutoRemove" : true]]
              imageName = 'gesellix/run-with-data-volumes'
              tag = 'latest'
              containerName = 'the-service-example'

              doLast {
                  logger.lifecycle("request successful: " + (result.status.status.success))
              }
          }
        """

    when:
    def result = GradleRunner.create()
        .withProjectDir(testProjectDir)
        .withArguments('dockerRun')
        .withPluginClasspath()
        .build()

    then:
    result.output.contains("request successful: true")
    result.task(':dockerRun').outcome == TaskOutcome.SUCCESS

    cleanup:
    dockerClient.stop("the-service-example")
    dockerClient.wait("the-service-example")
    dockerClient.rm("the-service-example")
    dockerClient.stop("the-data-example")
    dockerClient.wait("the-data-example")
    dockerClient.rm("the-data-example")
    dockerClient.rmi("gesellix/run-with-data-volumes:latest")
  }

  def "test volume create and remove"() {
    given:
    def hostDir = "/tmp"
    def dockerClient = new DockerClientImpl()

    pull(dockerClient, "gesellix/testimage", "os-linux")
    tag(dockerClient, "gesellix/testimage:os-linux", "gesellix/run-with-data-volumes")

    dockerClient.createContainer(
        [
            "Cmd"       : ["-"],
            "Image"     : "gesellix/run-with-data-volumes",
            "HostConfig": [
                "Binds"     : ["$hostDir:/data".toString()],
                "AutoRemove": true
            ],
        ], [
            name: "the-data-example"
        ])

    buildFile << """
          task dockerVolumeCreate(type: de.gesellix.gradle.docker.tasks.DockerVolumeCreateTask) {
              volumeConfig = [
                  Name      : "my-volume",
                  Driver    : "local",
                  DriverOpts: [:]
              ]

              doLast {
                  logger.lifecycle("task 1 successful: " + (response.status.code == 201))
              }
          }
          task dockerVolumeRm(type: de.gesellix.gradle.docker.tasks.DockerVolumeRmTask) {
              volumeName = "my-volume"

              doLast {
                  logger.lifecycle("task 2 successful: " + (response.status.code == 204))
              }
          }
        """

    when:
    def result = GradleRunner.create()
        .withProjectDir(testProjectDir)
        .withArguments('dockerVolumeCreate', 'dockerVolumeRm')
        .withPluginClasspath()
        .build()

    then:
    result.output.contains("task 1 successful: true")
    result.output.contains("task 2 successful: true")
    result.task(':dockerVolumeRm').outcome == TaskOutcome.SUCCESS
    result.task(':dockerVolumeCreate').outcome == TaskOutcome.SUCCESS

    cleanup:
    dockerClient.stop("the-data-example")
    dockerClient.wait("the-data-example")
    dockerClient.rm("the-data-example")
    try {
      dockerClient.rmVolume("my-volume")
    }
    catch (Exception ignored) {
    }
  }

  def "test swarm with services"() {
//    docker swarm init
//    docker network create -d overlay my-network
//    docker service create --name my-service --replicas 2 --network my-network -p 80:80/tcp nginx
    given:
    def dockerClient = new DockerClientImpl()

    buildFile << """
            task createNetwork(type: de.gesellix.gradle.docker.tasks.DockerNetworkCreateTask) {
                networkName = "my-network"
                networkConfig = [
                    Driver: "overlay",
                    "IPAM": [
                        "Driver": "default"
                    ]
                ]
            }

            def config = [
                "Name"        : "my-service",
                "TaskTemplate": [
                    "ContainerSpec": ["Image": "nginx"],
                    "RestartPolicy": ["Condition": "on-failure"]
                ],
                "Mode"        : ["Replicated": ["Replicas": 2]],
                "Networks"    : [["Target": "my-network"]],
                "EndpointSpec": [
                    "Ports": [
                        [
                            "Protocol"     : "tcp",
                            "TargetPort"   : 80,
                            "PublishedPort": 80
                        ]
                    ]
                ]
            ]
            task createService(type: de.gesellix.gradle.docker.tasks.DockerServiceCreateTask) {
                dependsOn createNetwork
                serviceConfig = config

                doLast {
                    logger.lifecycle("request successful: " + (response.status.code == 201))
                }
            }
        """

    def swarmCreated = false
    def dockerInfo = dockerClient.info().content
    if (dockerInfo.Swarm.LocalNodeState != "active") {
      buildFile << """
                def swarmConfig = [
                    "AdvertiseAddr"  : "127.0.0.1",
                    "ListenAddr"     : "0.0.0.0",
                    "ForceNewCluster": false
                ]
                task initSwarm(type: de.gesellix.gradle.docker.tasks.DockerSwarmInitTask) {
                    swarmconfig = swarmConfig
                }
                createNetwork.dependsOn initSwarm
            """
      swarmCreated = true
    }

    when:
    def result = GradleRunner.create()
        .withProjectDir(testProjectDir)
        .withArguments('createService')
        .withPluginClasspath()
        .build()

    then:
    result.output.contains("request successful: true")
    result.task(':createService').outcome == TaskOutcome.SUCCESS

    cleanup:
    dockerClient.rmService("my-service")
    dockerClient.rmNetwork("my-network")
    if (swarmCreated) {
      dockerClient.leaveSwarm([force: true])
    }
  }

  def "test certPath"() {
    given:
    buildFile << """
          task testTask(type: de.gesellix.gradle.docker.tasks.GenericDockerTask) {
              certPath.set("\${System.getProperty('user.home')}/.docker/machine/machines/default".toString())
              doFirst {
                  def version = getDockerClient().version()
                  ext.version = version
              }
              doLast {
                  logger.lifecycle("same version: " + (ext.version.content.ApiVersion == getDockerClient().version().content.ApiVersion))
              }
          }
        """

    when:
    def result = GradleRunner.create()
        .withProjectDir(testProjectDir)
        .withArguments('testTask')
        .withPluginClasspath()
        .build()

    then:
    result.output.contains("same version: true")
    result.task(':testTask').outcome == TaskOutcome.SUCCESS
  }

  void pull(DockerClient dockerClient, String image, String tag) {
    def createResponse = dockerClient.create([fromImage: image, tag: tag], [:])
    if (!createResponse.status.success) {
      println "create: ${createResponse}"
      Assert.fail("`docker pull $image:$tag` failed")
    }
  }

  void tag(DockerClient dockerClient, String from, String to) {
    def tagResponse = dockerClient.tag(from, to)
    if (!tagResponse.status.success) {
      println "tag: ${tagResponse}"
      Assert.fail("`docker tag $from $to` failed")
    }
  }
}
