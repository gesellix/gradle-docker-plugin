package de.gesellix.gradle.docker.tasks;

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
  public void tag() {
    getLogger().info("docker tag");
    getDockerClient().tag(getImageId().get(), getImageTag().get());
  }
}
