package de.gesellix.gradle.docker.tasks

import de.gesellix.docker.client.EnvFileParser
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class DockerRunTask extends AbstractDockerTask {

  private static Logger logger = LoggerFactory.getLogger(DockerRunTask)

  @Input
  def imageName
  @Input
  @Optional
  def tag
  @Input
  @Optional
  def containerName
  @Input
  @Optional
  def containerConfiguration
  @Input
  @Optional
  def hostConfiguration
  @Input
  @Optional
  def environmentFiles

  def envFileParser = new EnvFileParser()

  def result

  DockerRunTask() {
    description = "Run a command in a new container"
    group = "Docker"
  }

  @TaskAction
  def run() {
    logger.info "docker run"
    def containerConfig = getContainerConfiguration() ?: [:]
    containerConfig.HostConfig = getHostConfiguration() ?: containerConfig.HostConfig
    if (getEnvironmentFiles()) {
      containerConfig.Env = containerConfig.Env ?: []
      getEnvironmentFiles().each {
        def parsedEnv = envFileParser.parse(it)
        containerConfig.Env.addAll(parsedEnv)
      }
      logger.info "effective container.env: ${containerConfig.Env}"
    }

    result = getDockerClient().run(getImageName(), containerConfig, getTag(), getContainerName())
    return result
  }
}
