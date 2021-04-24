package de.gesellix.gradle.docker.tasks

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

class DockerNetworkRmTask extends GenericDockerTask {

  @Input
  Property<String> networkName

  /**
   * @deprecated This setter will be removed, please use the Property<> instead.
   * @see #getNetworkName()
   */
  @Deprecated
  void setNetworkName(String networkName) {
    this.networkName.set(networkName)
  }

  @Input
  @Optional
  Property<Boolean> ignoreError

  /**
   * @deprecated This setter will be removed, please use the Property<> instead.
   * @see #getIgnoreError()
   */
  @Deprecated
  void setIgnoreError(boolean ignoreError) {
    this.ignoreError.set(ignoreError)
  }

  @Internal
  def response

  @Inject
  DockerNetworkRmTask(ObjectFactory objectFactory) {
    super(objectFactory)
    description = "Remove a network"

    networkName = objectFactory.property(String)
    ignoreError = objectFactory.property(Boolean)
    ignoreError.convention(false)
  }

  @TaskAction
  def rmNetwork() {
    logger.info "docker network rm"

    try {
      response = getDockerClient().rmNetwork(getNetworkName().get())
    }
    catch (Exception e) {
      if (!ignoreError.get()) {
        throw new RuntimeException(e)
      }
      else {
        if (logger.isInfoEnabled()) {
          logger.warn("docker network rm ${networkName} failed", e)
        }
        else {
          logger.warn("docker network rm ${networkName} failed")
        }
        response = null
      }
    }
  }
}
