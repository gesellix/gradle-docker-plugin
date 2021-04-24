package de.gesellix.gradle.docker.tasks

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

class DockerPushTask extends GenericDockerTask {

  @Input
  Property<String> repositoryName

  /**
   * @deprecated This setter will be removed, please use the Property<> instead.
   * @see #getRepositoryName()
   */
  @Deprecated
  void setRepositoryName(String repositoryName) {
    this.repositoryName.set(repositoryName)
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
  def result

  @Inject
  DockerPushTask(ObjectFactory objectFactory) {
    super(objectFactory)
    description = "Push an image or a repository to a Docker registry server"

    repositoryName = objectFactory.property(String)
    registry = objectFactory.property(String)
  }

  @TaskAction
  def push() {
    logger.info "docker push"
    result = dockerClient.push(getRepositoryName().get(), getEncodedAuthConfig(), getRegistry().getOrNull())
    return result
  }
}
