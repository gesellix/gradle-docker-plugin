package de.gesellix.gradle.docker.tasks;

import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;

public class DockerRmiTask extends GenericDockerTask {

  private final Property<String> imageId;

  @Input
  public Property<String> getImageId() {
    return imageId;
  }

  @Inject
  public DockerRmiTask(ObjectFactory objectFactory) {
    super(objectFactory);
    setDescription("Remove one or more images");

    imageId = objectFactory.property(String.class);
  }

  @TaskAction
  public void rmi() {
    getLogger().info("docker rmi");
    getDockerClient().rmi(getImageId().get());
  }

  /**
   * @see #getImageId()
   * @deprecated This setter will be removed, please use the Property instead.
   */
  @Deprecated
  public void setImageId(String imageId) {
    this.imageId.set(imageId);
  }
}
