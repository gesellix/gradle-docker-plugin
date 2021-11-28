package de.gesellix.gradle.docker.tasks;

import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;

public class DockerNetworkRmTask extends GenericDockerTask {

  private final Property<String> networkName;

  @Input
  public Property<String> getNetworkName() {
    return networkName;
  }

  private final Property<Boolean> ignoreError;

  @Input
  @Optional
  public Property<Boolean> getIgnoreError() {
    return ignoreError;
  }

  @Inject
  public DockerNetworkRmTask(ObjectFactory objectFactory) {
    super(objectFactory);
    setDescription("Remove a network");

    networkName = objectFactory.property(String.class);
    ignoreError = objectFactory.property(Boolean.class);
    ignoreError.convention(false);
  }

  @TaskAction
  public void rmNetwork() {
    getLogger().info("docker network rm");

    try {
      getDockerClient().rmNetwork(getNetworkName().get());
    }
    catch (Exception e) {
      if (!ignoreError.get()) {
        throw new RuntimeException(e);
      }
      else {
        if (getLogger().isInfoEnabled()) {
          getLogger().warn("docker network rm " + getNetworkName().get() + " failed", e);
        }
        else {
          getLogger().warn("docker network rm " + getNetworkName().get() + " failed");
        }
      }
    }
  }
}
