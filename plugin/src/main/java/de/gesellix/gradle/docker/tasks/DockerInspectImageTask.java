package de.gesellix.gradle.docker.tasks;

import de.gesellix.docker.client.EngineResponseContent;
import de.gesellix.docker.remote.api.ImageInspect;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;

public class DockerInspectImageTask extends GenericDockerTask {

  private final Property<String> imageId;

  @Input
  public Property<String> getImageId() {
    return imageId;
  }

  private EngineResponseContent<ImageInspect> imageInfo;

  @Internal
  public EngineResponseContent<ImageInspect> getImageInfo() {
    return imageInfo;
  }

  @Inject
  public DockerInspectImageTask(ObjectFactory objectFactory) {
    super(objectFactory);
    setDescription("Return low-level information on image");

    imageId = objectFactory.property(String.class);
  }

  @TaskAction
  public void inspect() {
    getLogger().info("docker inspect");
    imageInfo = getDockerClient().inspectImage(getImageId().get());
  }
}
