package de.gesellix.gradle.docker.tasks

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

class DockerRenameTask extends GenericDockerTask {

  @Input
  Property<String> containerId

  /**
   * @deprecated This setter will be removed, please use the Property<> instead.
   * @see #getContainerId()
   */
  @Deprecated
  void setContainerId(String containerId) {
    this.containerId.set(containerId)
  }

  @Input
  Property<String> newName

  /**
   * @deprecated This setter will be removed, please use the Property<> instead.
   * @see #getNewName()
   */
  @Deprecated
  void setNewName(String newName) {
    this.newName.set(newName)
  }

  @Internal
  def result

  @Inject
  DockerRenameTask(ObjectFactory objectFactory) {
    super(objectFactory)
    description = "Rename an existing container"

    containerId = objectFactory.property(String)
    newName = objectFactory.property(String)
  }

  @TaskAction
  def rename() {
    logger.info "docker rename"
    result = dockerClient.rename(getContainerId().get(), getNewName().get())
    return result
  }
}
