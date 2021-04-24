package de.gesellix.gradle.docker.tasks

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

class DockerRmiTask extends GenericDockerTask {

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

  @Inject
  DockerRmiTask(ObjectFactory objectFactory) {
    super(objectFactory)
    description = "Remove one or more images"

    imageId = objectFactory.property(String)
  }

  @TaskAction
  def rmi() {
    logger.info "docker rmi"
    getDockerClient().rmi(getImageId().get())
  }
}
