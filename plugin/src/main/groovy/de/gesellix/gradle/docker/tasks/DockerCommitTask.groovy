package de.gesellix.gradle.docker.tasks

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

class DockerCommitTask extends GenericDockerTask {

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
  Property<String> repo

  /**
   * @deprecated This setter will be removed, please use the Property<> instead.
   * @see #getContainerId()
   */
  @Deprecated
  void setRepo(String repo) {
    this.repo.set(repo)
  }

  @Optional
  @Input
  Property<String> tag

  /**
   * @deprecated This setter will be removed, please use the Property<> instead.
   * @see #getContainerId()
   */
  @Deprecated
  void setTag(String tag) {
    this.tag.set(tag)
  }

  @Optional
  @Input
  Property<String> author

  /**
   * @deprecated This setter will be removed, please use the Property<> instead.
   * @see #getContainerId()
   */
  @Deprecated
  void setAuthor(String author) {
    this.author.set(author)
  }

  @Optional
  @Input
  Property<String> comment

  /**
   * @deprecated This setter will be removed, please use the Property<> instead.
   * @see #getContainerId()
   */
  @Deprecated
  void setComment(String comment) {
    this.comment.set(comment)
  }

  @Input
  @Optional
  Property<Boolean> pauseContainer

  /**
   * @deprecated This setter will be removed, please use the Property<> instead.
   * @see #getContainerId()
   */
  @Deprecated
  void setPauseContainer(Boolean pauseContainer) {
    this.pauseContainer.set(pauseContainer)
  }

  @Optional
  @Input
  Property<String> changes

  /**
   * @deprecated This setter will be removed, please use the Property<> instead.
   * @see #getContainerId()
   */
  @Deprecated
  void setChanges(String changes) {
    this.changes.set(changes)
  }

  @Inject
  DockerCommitTask(ObjectFactory objectFactory) {
    super(objectFactory)
    description = "Commit changes to a container"

    containerId = objectFactory.property(String)
    repo = objectFactory.property(String)
    tag = objectFactory.property(String)
    author = objectFactory.property(String)
    comment = objectFactory.property(String)
    changes = objectFactory.property(String)
    pauseContainer = objectFactory.property(Boolean)
    pauseContainer.convention(true)
  }

  @TaskAction
  def commit() {
    logger.info "docker commit"
    return getDockerClient().commit(getContainerId().get(), [
        repo   : getRepo().get(),
        tag    : getTag().getOrNull(),
        comment: getComment().getOrNull(),
        author : getAuthor().getOrNull(),
        changes: getChanges().getOrNull(),
        pause  : getPauseContainer().getOrElse(true)
    ])
  }
}
