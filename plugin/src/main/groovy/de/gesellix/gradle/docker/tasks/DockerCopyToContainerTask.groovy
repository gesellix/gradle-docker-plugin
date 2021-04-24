package de.gesellix.gradle.docker.tasks

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

class DockerCopyToContainerTask extends GenericDockerTask {

  @Input
  Property<String> container

  /**
   * @deprecated This setter will be removed, please use the Property<> instead.
   * @see #getContainer()
   */
  @Deprecated
  void setContainer(String container) {
    this.container.set(container)
  }

  @Input
  Property<String> targetPath

  /**
   * @deprecated This setter will be removed, please use the Property<> instead.
   * @see #getTargetPath()
   */
  @Deprecated
  void setTargetPath(String targetPath) {
    this.targetPath.set(targetPath)
  }

  @Input
  Property<InputStream> tarInputStream

  /**
   * @deprecated This setter will be removed, please use the Property<> instead.
   * @see #getTarInputStream()
   */
  @Deprecated
  void setTarInputStream(InputStream tarInputStream) {
    this.tarInputStream.set(tarInputStream)
  }

  @Inject
  DockerCopyToContainerTask(ObjectFactory objectFactory) {
    super(objectFactory)
    description = "Copy files/folders from your host to a container."

    container = objectFactory.property(String)
    targetPath = objectFactory.property(String)
    tarInputStream = objectFactory.property(InputStream)
  }

  @TaskAction
  def copyToContainer() {
    logger.info "docker cp to container"

    getDockerClient().putArchive(getContainer().get(), getTargetPath().get(), getTarInputStream().get())
  }
}
