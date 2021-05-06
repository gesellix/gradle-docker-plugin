package de.gesellix.gradle.docker.tasks;

import de.gesellix.docker.engine.EngineResponse;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;

public class DockerImagesTask extends GenericDockerTask {

  private EngineResponse images;

  @Internal
  public EngineResponse getImages() {
    return images;
  }

  @Inject
  public DockerImagesTask(ObjectFactory objectFactory) {
    super(objectFactory);
    setDescription("List images");
  }

  @TaskAction
  public EngineResponse images() {
    getLogger().info("docker images");
    return images = getDockerClient().images();
  }
}
