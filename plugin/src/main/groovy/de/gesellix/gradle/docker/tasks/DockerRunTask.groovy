package de.gesellix.gradle.docker.tasks

import de.gesellix.docker.client.EnvFileParser
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction

class DockerRunTask extends GenericDockerTask {

  @Input
  def imageName
  @Input
  @Optional
  def tag = ""
  @Input
  @Optional
  def containerName = ""
  /**
   * Accepts a list of port mappings with the following pattern: `hostPort:containerPort`.
   * More sophisticated patterns are only supported via plain containerConfig.
   */
  @Input
  @Optional
  def ports = []
  @Input
  @Optional
  def containerConfiguration = [:]
  /**
   * @deprecated use #containerConfiguration.HostConfig
   */
  @Input
  @Optional
  @Deprecated
  def hostConfiguration = [:]
  @Input
  @Optional
  def env = []
  @Input
  @Optional
  def environmentFiles = []

  @Internal
  def envFileParser = new EnvFileParser()

  @Internal
  def result

  DockerRunTask() {
    description = "Run a command in a new container"
    group = "Docker"
  }

  @TaskAction
  def run() {
    logger.info "docker run"
    if (getHostConfiguration() != [:]) {
      throw new UnsupportedOperationException("please use `containerConfiguration.HostConfig`!")
    }
    def containerConfig = getActualContainerConfig()
    result = getDockerClient().run(
        getAsString(getImageName()),
        containerConfig,
        getAsString(getTag()),
        getAsString(getContainerName()),
        getAuthConfig())
    return result
  }

  static String getAsString(def closureOrString) {
    if (closureOrString instanceof Closure) {
      return (closureOrString as Closure)()
    }
    else {
      return closureOrString?.toString()
    }
  }

  @Internal
  def getActualContainerConfig() {
    def containerConfig = getContainerConfiguration() ?: [:]
    containerConfig.HostConfig = containerConfig.HostConfig ?: [:]
    if (getEnvironmentFiles()) {
      containerConfig.Env = containerConfig.Env ?: []
      getEnvironmentFiles().each { File file ->
        def parsedEnv = envFileParser.parse(file)
        containerConfig.Env.addAll(parsedEnv)
      }
    }
    if (getEnv()) {
      containerConfig.Env = containerConfig.Env ?: []
      getEnv().each {
        containerConfig.Env += it
      }
    }
    if (getPorts()) {
      containerConfig.ExposedPorts = containerConfig.ExposedPorts ?: [:]
      containerConfig.HostConfig.PortBindings = containerConfig.HostConfig.PortBindings ?: [:]
      getPorts().each { String portMapping ->
        // format: ip:hostPort:containerPort | ip::containerPort | hostPort:containerPort | containerPort
        def splittedPortMapping = portMapping.split(":")
        if (splittedPortMapping.size() != 2) {
          throw new UnsupportedOperationException("please use the plain `containerConfig.ExposedPorts and containerConfig.HostConfig.PortBindings` properties")
        }
        def hostPort = splittedPortMapping[0]
        def containerPort = "${splittedPortMapping[1]}/tcp".toString()
        containerConfig.ExposedPorts[containerPort] = [:]
        containerConfig.HostConfig.PortBindings[containerPort] = [[HostIp  : "0.0.0.0",
                                                                   HostPort: hostPort]]
      }
    }
    logger.info "effective container config: ${containerConfig}"
    return containerConfig
  }
}
