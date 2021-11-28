package de.gesellix.gradle.docker.tasks;

import de.gesellix.docker.engine.EngineResponse;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;

public class DockerUnpauseTask extends GenericDockerTask {

  private final Property<String> containerId;

  @Input
  public Property<String> getContainerId() {
    return containerId;
  }

  @Inject
  public DockerUnpauseTask(ObjectFactory objectFactory) {
    super(objectFactory);
    setDescription("Unpause a paused container");

    containerId = objectFactory.property(String.class);
  }

  @TaskAction
  public void unpause() {
    getLogger().info("docker unpause");
    getDockerClient().unpause(getContainerId().get());
  }
}
