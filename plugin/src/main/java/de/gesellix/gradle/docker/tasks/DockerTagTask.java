package de.gesellix.gradle.docker.tasks;

import de.gesellix.docker.engine.EngineResponse;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;

public class DockerTagTask extends GenericDockerTask {

  private final Property<String> imageId;

  @Input
  public Property<String> getImageId() {
    return imageId;
  }

  private final Property<String> imageTag;

  @Input
  public Property<String> getImageTag() {
    return imageTag;
  }

  @Inject
  public DockerTagTask(ObjectFactory objectFactory) {
    super(objectFactory);
    setDescription("Tag an image into a repository");

    imageId = objectFactory.property(String.class);
    imageTag = objectFactory.property(String.class);
  }

  @TaskAction
  public EngineResponse tag() {
    getLogger().info("docker tag");
    return getDockerClient().tag(getImageId().get(), getImageTag().get());
  }

  /**
   * @see #getImageId()
   * @deprecated This setter will be removed, please use the Property instead.
   */
  @Deprecated
  public void setImageId(String imageId) {
    this.imageId.set(imageId);
  }

  /**
   * @see #getImageTag()
   * @deprecated This setter will be removed, please use the Property instead.
   */
  @Deprecated
  public void setTag(String tag) {
    this.imageTag.set(tag);
  }
}
