package de.gesellix.gradle.docker.tasks;

import de.gesellix.docker.client.EngineResponseContent;
import de.gesellix.docker.remote.api.ImageSummary;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.util.List;

public class DockerImagesTask extends GenericDockerTask {

  private EngineResponseContent<List<ImageSummary>> images;

  @Internal
  public EngineResponseContent<List<ImageSummary>> getImages() {
    return images;
  }

  @Inject
  public DockerImagesTask(ObjectFactory objectFactory) {
    super(objectFactory);
    setDescription("List images");
  }

  @TaskAction
  public EngineResponseContent<List<ImageSummary>> images() {
    getLogger().info("docker images");
    return images = getDockerClient().images();
  }
}
