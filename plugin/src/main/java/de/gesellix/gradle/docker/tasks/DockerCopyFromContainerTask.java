package de.gesellix.gradle.docker.tasks;

import de.gesellix.docker.engine.EngineResponse;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.io.InputStream;

public class DockerCopyFromContainerTask extends GenericDockerTask {

  private final Property<String> container;

  @Input
  public Property<String> getContainer() {
    return container;
  }

  private final Property<String> sourcePath;

  @Input
  public Property<String> getSourcePath() {
    return sourcePath;
  }

  private EngineResponse<InputStream> content;

  @Internal
  public EngineResponse<InputStream> getContent() {
    return content;
  }

  @Inject
  public DockerCopyFromContainerTask(ObjectFactory objectFactory) {
    super(objectFactory);
    setDescription("Copy files/folders from a container to your host.");

    container = objectFactory.property(String.class);
    sourcePath = objectFactory.property(String.class);
  }

  @TaskAction
  public EngineResponse<InputStream> copyFromContainer() {
    getLogger().info("docker cp from container");
    return content = getDockerClient().getArchive(getContainer().get(), getSourcePath().get());
  }
}
