package de.gesellix.gradle.docker.tasks

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

class DockerTagTask extends GenericDockerTask {

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

  private Property<String> imageTag

  @Input
  Property<String> getImageTag() {
    return imageTag
  }

  /**
   * @deprecated This setter will be removed, please use the Property<> instead.
   * @see #getImageTag()
   */
  @Deprecated
  void setTag(String tag) {
    this.imageTag.set(tag)
  }

  @Inject
  DockerTagTask(ObjectFactory objectFactory) {
    super(objectFactory)
    description = "Tag an image into a repository"

    imageId = objectFactory.property(String)
    imageTag = objectFactory.property(String)
  }

  @TaskAction
  def tag() {
    logger.info "docker tag"
    return getDockerClient().tag(getImageId().get(), getImageTag().get())
  }
}
