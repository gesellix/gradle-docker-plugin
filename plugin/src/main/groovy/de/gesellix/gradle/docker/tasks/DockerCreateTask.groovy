package de.gesellix.gradle.docker.tasks

import de.gesellix.docker.client.EnvFileParser
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

class DockerCreateTask extends GenericDockerTask {

  @Input
  Property<String> imageName

  /**
   * @deprecated This setter will be removed, please use the Property<> instead.
   * @see #getImageName()
   */
  @Deprecated
  void setImageName(String imageName) {
    this.imageName.set(imageName)
  }

  @Input
  @Optional
  Property<String> tag

  /**
   * @deprecated This setter will be removed, please use the Property<> instead.
   * @see #getTag()
   */
  @Deprecated
  void setTag(String tag) {
    this.tag.set(tag)
  }

  @Input
  @Optional
  Property<String> containerName

  /**
   * @deprecated This setter will be removed, please use the Property<> instead.
   * @see #getContainerName()
   */
  @Deprecated
  void setContainerName(String containerName) {
    this.containerName.set(containerName)
  }

  /**
   * Accepts a list of port mappings with the following pattern: `hostPort:containerPort`.
   * More sophisticated patterns are only supported via plain containerConfig.
   */
  @Input
  @Optional
  ListProperty<String> ports

  /**
   * @deprecated This setter will be removed, please use the Property<> instead.
   * @see #getPorts()
   */
  @Deprecated
  void setPorts(List<String> ports) {
    this.ports.set(ports)
  }

  @Input
  @Optional
  MapProperty<String, Object> containerConfiguration

  /**
   * @deprecated This setter will be removed, please use the Property<> instead.
   * @see #getContainerConfiguration()
   */
  @Deprecated
  void setContainerConfiguration(Map<String, Object> containerConfiguration) {
    this.containerConfiguration.set(containerConfiguration)
  }

  @Input
  @Optional
  ListProperty<String> env

  /**
   * @deprecated This setter will be removed, please use the Property<> instead.
   * @see #getEnv()
   */
  @Deprecated
  void setEnv(List<String> env) {
    this.env.set(env)
  }

  @Input
  @Optional
  ListProperty<File> environmentFiles

  /**
   * @deprecated This setter will be removed, please use the Property<> instead.
   * @see #getEnvironmentFiles()
   */
  @Deprecated
  void setEnvironmentFiles(List<File> environmentFiles) {
    this.environmentFiles.set(environmentFiles)
  }

  @Internal
  EnvFileParser envFileParser = new EnvFileParser()

  @Internal
  def result

  @Inject
  DockerCreateTask(ObjectFactory objectFactory) {
    super(objectFactory)
    description = "Create a new container"

    imageName = objectFactory.property(String)
    tag = objectFactory.property(String)
    tag.convention("")
    containerName = objectFactory.property(String)
    containerName.convention("")
    ports = objectFactory.listProperty(String)
    containerConfiguration = objectFactory.mapProperty(String, Object)
    env = objectFactory.listProperty(String)
    environmentFiles = objectFactory.listProperty(File)
  }

  @TaskAction
  def create() {
    logger.info "docker create"
    Map<String, ?> containerConfig = getActualContainerConfig()
    result = getDockerClient().createContainer(containerConfig, [name: getContainerName().getOrElse("")])
    return result
  }

  @Internal
  Map<String, ?> getActualContainerConfig() {
    Map<String, Object> containerConfig = new HashMap<>(getContainerConfiguration().getOrElse([:]))
    containerConfig.Image = "${getImageName().get()}${getTag().get() ? ":${getTag().get()}" : ""}"
    containerConfig.HostConfig = containerConfig.HostConfig ?: [:]
    if (getEnvironmentFiles().get()) {
      containerConfig.Env = containerConfig.Env ?: []
      getEnvironmentFiles().get().each { File file ->
        def parsedEnv = envFileParser.parse(file)
        containerConfig.Env.addAll(parsedEnv)
      }
      logger.info "effective container.env: ${containerConfig.Env}"
    }
    if (getEnv().get()) {
      containerConfig.Env = containerConfig.Env ?: []
      getEnv().get().each {
        containerConfig.Env += it
      }
    }
    if (getPorts().get()) {
      containerConfig.ExposedPorts = containerConfig.ExposedPorts ?: [:]
      containerConfig.HostConfig.PortBindings = containerConfig.HostConfig.PortBindings ?: [:]
      getPorts().get().each { String portMapping ->
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
    return containerConfig
  }
}
