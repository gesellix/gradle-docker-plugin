package de.gesellix.gradle.docker.tasks

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

class DockerPullTask extends GenericDockerTask {

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

  private Property<String> imageTag

  @Input
  @Optional
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

  @Input
  @Optional
  Property<String> registry

  /**
   * @deprecated This setter will be removed, please use the Property<> instead.
   * @see #getRegistry()
   */
  @Deprecated
  void setRegistry(String registry) {
    this.registry.set(registry)
  }

  @Internal
  String imageId

  @Inject
  DockerPullTask(ObjectFactory objectFactory) {
    super(objectFactory)
    description = "Pull an image or a repository from a Docker registry server"

    imageName = objectFactory.property(String)
    imageTag = objectFactory.property(String)
    registry = objectFactory.property(String)
  }

  @TaskAction
  def pull() {
    logger.info "docker pull"

    Map query = [fromImage: getImageName().get(),
                 tag      : getImageTag().getOrNull()]
    if (getRegistry().present) {
      query.fromImage = "${getRegistry().get()}/${getImageName().get()}".toString()
    }

    Map options = [EncodedRegistryAuth: getEncodedAuthConfig()]

    def response = dockerClient.create(query, options)
    if (response.status.success) {
      imageId = dockerClient.findImageId(query.fromImage, query.tag)
    }
    else {
      imageId = null
    }
    return imageId
  }
}
