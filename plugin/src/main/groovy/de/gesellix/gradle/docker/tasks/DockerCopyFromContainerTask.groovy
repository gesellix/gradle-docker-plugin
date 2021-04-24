package de.gesellix.gradle.docker.tasks

import de.gesellix.docker.engine.EngineResponse
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

class DockerCopyFromContainerTask extends GenericDockerTask {

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
  Property<String> sourcePath

  /**
   * @deprecated This setter will be removed, please use the Property<> instead.
   * @see #getSourcePath()
   */
  @Deprecated
  void setSourcePath(String sourcePath) {
    this.sourcePath.set(sourcePath)
  }

  @Internal
  EngineResponse content

  @Inject
  DockerCopyFromContainerTask(ObjectFactory objectFactory) {
    super(objectFactory)
    description = "Copy files/folders from a container to your host."

    container = objectFactory.property(String)
    sourcePath = objectFactory.property(String)
  }

  @TaskAction
  def copyFromContainer() {
    logger.info "docker cp from container"

    content = getDockerClient().getArchive(getContainer().get(), getSourcePath().get())
  }
}
