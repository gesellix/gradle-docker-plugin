package de.gesellix.gradle.docker

import de.gesellix.docker.client.DockerClientImpl
import de.gesellix.docker.client.LocalDocker
import de.gesellix.docker.remote.api.ContainerCreateRequest
import de.gesellix.docker.remote.api.HostConfig
import de.gesellix.docker.remote.api.LocalNodeState
import de.gesellix.docker.remote.api.core.ClientException
import de.gesellix.gradle.docker.testutil.TestImage
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import spock.lang.Ignore
import spock.lang.Requires
import spock.lang.Specification
import spock.lang.TempDir

@Requires({ LocalDocker.available() })
class DockerPluginIntegrationTest extends Specification {

  TestImage testImage

  @TempDir
  File testProjectDir

  File buildFile

  // Also requires './gradlew :plugin:pluginUnderTestMetadata' to be run before performing the tests.
  def setup() {
    testImage = new TestImage(new DockerClientImpl())
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
                  logger.lifecycle("request succeeded: " + (info.content instanceof de.gesellix.docker.remote.api.SystemInfo))
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
              imageName = '${testImage.imageName}'
              imageTag = '${testImage.imageTag}'
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
    result.output.contains("id: ${testImage.imageWithTag}")
    result.task(':dockerPull').outcome == TaskOutcome.SUCCESS
  }

  @Ignore
  def "test pull with auth"() {
    given:
    buildFile << """
          task dockerPullPrivate(type: de.gesellix.gradle.docker.tasks.DockerPullTask) {
              authConfig = dockerClient.readDefaultAuthConfig()
              imageName = 'gesellix/private-repo'
              imageTag = 'latest'
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
    dockerClient.tag(testImage.imageWithTag, "gesellix/example")

    buildFile << """
          task dockerPush(type: de.gesellix.gradle.docker.tasks.DockerPushTask) {
              repositoryName = 'gesellix/example'

              authConfig.set(new de.gesellix.docker.authentication.AuthConfig(
                    "username": "gesellix",
                    "password": "-yet-another-password-",
                    "email": "tobias@gesellix.de",
                    "serveraddress": "https://index.docker.io/v1/"))

              // don't access the official registry,
              // so that we don't try (and fail) to authenticate for each test execution.
              // for a real test, this line needs to be commented or removed, though.
              registry = 'example.com:5000'

              doLast {
                  logger.lifecycle("request succeeded")
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
    Exception exc = thrown(Exception)
    exc.message.readLines().find { it.matches(".*error=Get \"?https://example.com:5000/v2/.*") }

    cleanup:
    dockerClient.rmi("gesellix/example")
    dockerClient.rmi("example.com:5000/gesellix/example")
  }

  def "test run"() {
    given:
    buildFile << """
          task dockerRun(type: de.gesellix.gradle.docker.tasks.DockerRunTask) {
              containerConfiguration = new de.gesellix.docker.remote.api.ContainerCreateRequest().tap {
                hostConfig = new de.gesellix.docker.remote.api.HostConfig().tap {
                  autoRemove = true
                }
              }
              imageName = '${testImage.imageName}'
              imageTag = '${testImage.imageTag}'
              containerName = "test-run"
              doLast {
                  logger.lifecycle("container id: " + result.content.id)
                  logger.lifecycle("request successful: " + (result.content instanceof de.gesellix.docker.remote.api.ContainerCreateResponse))
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
    try { dockerClient.stop('test-run') } catch (ClientException ignored) {}
    try { dockerClient.wait('test-run') } catch (ClientException ignored) {}
    try { dockerClient.rm('test-run') } catch (ClientException ignored) {}
  }

  def "test stop"() {
    given:
    def dockerClient = new DockerClientImpl()
    def runResult = dockerClient.run(
        new ContainerCreateRequest().tap {
          image = testImage.imageWithTag
          hostConfig = new HostConfig().tap {
            autoRemove = true
          }
        },
        "test-stop")

    buildFile << """
          task dockerStop(type: de.gesellix.gradle.docker.tasks.DockerStopTask) {
              containerId = '${runResult.content.id}'
              doLast {
                  logger.lifecycle("request successful")
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
    result.output.contains("request successful")
    result.task(':dockerStop').outcome == TaskOutcome.SUCCESS

    cleanup:
    try { dockerClient.stop('test-stop') } catch (ClientException ignored) {}
    try { dockerClient.wait('test-stop') } catch (ClientException ignored) {}
    try { dockerClient.rm('test-stop') } catch (ClientException ignored) {}
  }

  def "test rm"() {
    given:
    def dockerClient = new DockerClientImpl()
    def runResult = dockerClient.run(
        new ContainerCreateRequest().tap {
          image = testImage.imageWithTag
        },
        "test-rm")
    String runningContainerId = runResult.content.id
    dockerClient.stop(runningContainerId)
    dockerClient.wait(runningContainerId)

    buildFile << """
          task dockerRm(type: de.gesellix.gradle.docker.tasks.DockerRmTask) {
              containerId = '$runningContainerId'
              doLast {
                  logger.lifecycle("request successful")
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
    result.output.contains("request successful")
    result.task(':dockerRm').outcome == TaskOutcome.SUCCESS

    cleanup:
    try { dockerClient.stop('test-rm') } catch (ClientException ignored) {}
    try { dockerClient.wait('test-rm') } catch (ClientException ignored) {}
    try { dockerClient.rm('test-rm') } catch (ClientException ignored) {}
  }

  def "test start"() {
    given:
    def dockerClient = new DockerClientImpl()
    def containerInfo = dockerClient.createContainer(
        new ContainerCreateRequest().tap {
          image = testImage.imageWithTag
          hostConfig = new HostConfig().tap {
            autoRemove = true
          }
        },
        "test-start")

    buildFile << """
          task dockerStart(type: de.gesellix.gradle.docker.tasks.DockerStartTask) {
              containerId = '${containerInfo.content.id}'
              doLast {
                  logger.lifecycle("request successful")
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
    result.output.contains("request successful")
    result.task(':dockerStart').outcome == TaskOutcome.SUCCESS

    cleanup:
    dockerClient.stop('test-start')
    try { dockerClient.wait('test-start') } catch (ClientException ignored) {}
    try { dockerClient.rm('test-start') } catch (ClientException ignored) {}
  }

  def "test ps"() {
    given:
    def dockerClient = new DockerClientImpl()
    dockerClient.run(
        new ContainerCreateRequest().tap {
          image = testImage.imageWithTag
        },
        "test-ps")

    buildFile << """
          task dockerPs(type: de.gesellix.gradle.docker.tasks.DockerPsTask) {
              doLast {
                  logger.lifecycle("request successful: " + (containers.content instanceof java.util.List))
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
    dockerClient.tag(testImage.imageWithTag, "gesellix/images-list")

    buildFile << """
          task dockerImages(type: de.gesellix.gradle.docker.tasks.DockerImagesTask) {
              doLast {
                  logger.lifecycle("request successful: " + (images.content instanceof java.util.List<de.gesellix.docker.remote.api.ImageSummary>))
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
    def hostDir = testProjectDir
    def containerDir = testImage.isWindows() ? "C:/data" : "/data"
    def dockerClient = new DockerClientImpl()

    dockerClient.tag(testImage.imageWithTag, "gesellix/run-with-data-volumes")

    dockerClient.createContainer(
        new ContainerCreateRequest().tap {
          image = "gesellix/run-with-data-volumes"
          hostConfig = new HostConfig().tap {
            autoRemove = true
            binds = ["$hostDir:$containerDir".toString()]
          }
        },
        "the-data-example")

    buildFile << """
          task dockerRun(type: de.gesellix.gradle.docker.tasks.DockerRunTask) {
              containerConfiguration = new de.gesellix.docker.remote.api.ContainerCreateRequest().tap {
//                image = "gesellix/run-with-data-volumes:latest"
                hostConfig = new de.gesellix.docker.remote.api.HostConfig().tap {
                  autoRemove = true
                  volumesFrom = ["the-data-example"]
                }
              }
              imageName = "gesellix/run-with-data-volumes"
              imageTag.set("latest")
              containerName = "the-service-example"

              doLast {
                  logger.lifecycle("request successful: " + (result.content instanceof de.gesellix.docker.remote.api.ContainerCreateResponse))
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
    try { dockerClient.stop("the-service-example") } catch (ClientException ignored) {}
    try { dockerClient.wait("the-service-example") } catch (ClientException ignored) {}
    try { dockerClient.rm("the-service-example") } catch (ClientException ignored) {}
    try { dockerClient.stop("the-data-example") } catch (ClientException ignored) {}
    try { dockerClient.wait("the-data-example") } catch (ClientException ignored) {}
    try { dockerClient.rm("the-data-example") } catch (ClientException ignored) {}
    dockerClient.rmi("gesellix/run-with-data-volumes:latest")
  }

  def "test volume create and remove"() {
    given:
    def hostDir = testProjectDir
    def containerDir = testImage.isWindows() ? "C:/data" : "/data"
    def dockerClient = new DockerClientImpl()

    dockerClient.tag(testImage.imageWithTag, "gesellix/run-with-data-volumes")

    dockerClient.createContainer(
        new ContainerCreateRequest().tap {
          image = "gesellix/run-with-data-volumes"
          hostConfig = new HostConfig().tap {
            autoRemove = true
            binds = ["$hostDir:$containerDir".toString()]
          }
        },
        "the-data-example")

    buildFile << """
          task dockerVolumeCreate(type: de.gesellix.gradle.docker.tasks.DockerVolumeCreateTask) {
              volumeConfig = [
                  Name      : "my-volume",
                  Driver    : "local",
                  DriverOpts: [:]
              ]

              doLast {
                  logger.lifecycle("task 1 successful: " + (response.content instanceof de.gesellix.docker.remote.api.Volume))
              }
          }
          task dockerVolumeRm(type: de.gesellix.gradle.docker.tasks.DockerVolumeRmTask) {
              volumeName = "my-volume"

              doLast {
                  logger.lifecycle("task 2 successful: " + (true))
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
    dockerClient.rmi("gesellix/run-with-data-volumes:latest")
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

            def config = new de.gesellix.docker.remote.api.ServiceCreateRequest().tap {
              name = "my-service"
              taskTemplate = new de.gesellix.docker.remote.api.TaskSpec().tap {
                containerSpec = new de.gesellix.docker.remote.api.TaskSpecContainerSpec().tap {
                  image = "nginx"
                }
                restartPolicy = new de.gesellix.docker.remote.api.TaskSpecRestartPolicy().tap {
                  condition = de.gesellix.docker.remote.api.TaskSpecRestartPolicy.Condition.OnMinusFailure
                }
              }
              mode = new de.gesellix.docker.remote.api.ServiceSpecMode().tap {
                replicated = new de.gesellix.docker.remote.api.ServiceSpecModeReplicated(2)
              }
              networks = [
                  new de.gesellix.docker.remote.api.NetworkAttachmentConfig().tap {
                    target = "my-network"
                  }
              ]
              endpointSpec = new de.gesellix.docker.remote.api.EndpointSpec().tap {
                ports = [
                    new de.gesellix.docker.remote.api.EndpointPortConfig().tap {
                      protocol = de.gesellix.docker.remote.api.EndpointPortConfig.Protocol.Tcp
                      targetPort = 80
                      publishedPort = 80
                    }
                ]
              }
            }
            task createService(type: de.gesellix.gradle.docker.tasks.DockerServiceCreateTask) {
                dependsOn createNetwork
                serviceConfig = config

                doLast {
                    logger.lifecycle("request successful: " + (response.content instanceof de.gesellix.docker.remote.api.ServiceCreateResponse))
                }
            }
        """

    def swarmCreated = false
    def dockerInfo = dockerClient.info().content
    if (dockerInfo.swarm.localNodeState != LocalNodeState.Active) {
      buildFile << """
                def swarmConfig = new de.gesellix.docker.remote.api.SwarmInitRequest().tap {
                  advertiseAddr = "127.0.0.1"
                  listenAddr = "0.0.0.0"
                  forceNewCluster = false
                }
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
      dockerClient.leaveSwarm(true)
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
}
