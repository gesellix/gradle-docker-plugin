package de.gesellix.gradle.docker.tasks;

import de.gesellix.docker.engine.EngineResponse;
import de.gesellix.docker.remote.api.ContainerWaitResponse;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;

public class DockerWaitTask extends GenericDockerTask {

  private final Property<String> containerId;

  @Input
  public Property<String> getContainerId() {
    return containerId;
  }

  private final Property<Boolean> ignoreError;

  @Input
  @Optional
  public Property<Boolean> getIgnoreError() {
    return ignoreError;
  }

  private EngineResponse<ContainerWaitResponse> result;

  @Internal
  public EngineResponse<ContainerWaitResponse> getResult() {
    return result;
  }

  @Inject
  public DockerWaitTask(ObjectFactory objectFactory) {
    super(objectFactory);
    setDescription("Block until a container stops, then print its exit code.");

    containerId = objectFactory.property(String.class);
    ignoreError = objectFactory.property(Boolean.class);
    ignoreError.convention(false);
  }

  @TaskAction
  public EngineResponse<ContainerWaitResponse> awaitStop() {
    getLogger().info("docker wait");

    try {
      result = getDockerClient().wait(getContainerId().get());
    }
    catch (Exception e) {
      if (!ignoreError.get()) {
        throw new RuntimeException(e);
      }
      else {
        if (getLogger().isInfoEnabled()) {
          getLogger().warn("docker container wait " + getContainerId().get() + " failed", e);
        }
        else {
          getLogger().warn("docker container wait " + getContainerId().get() + " failed");
        }
      }
    }
    return result;
  }
}
