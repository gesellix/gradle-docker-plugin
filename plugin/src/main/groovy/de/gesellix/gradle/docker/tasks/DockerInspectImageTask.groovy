package de.gesellix.gradle.docker.tasks

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

class DockerInspectImageTask extends GenericDockerTask {

  @Input
  Property<String> imageId

  /**
   * @deprecated This setter will be removed, please use the Property<> instead.
   * @see #getImageId()
   */
  @Deprecated
  void setImageId(String imageId) {
    this.imageId.set(imageId)
  }

  @Internal
  def imageInfo

  @Inject
  DockerInspectImageTask(ObjectFactory objectFactory) {
    super(objectFactory)
    description = "Return low-level information on image"

    imageId = objectFactory.property(String)
  }

  @TaskAction
  def inspect() {
    logger.info "docker inspect"

    imageInfo = getDockerClient().inspectImage(getImageId().get())
  }
}
