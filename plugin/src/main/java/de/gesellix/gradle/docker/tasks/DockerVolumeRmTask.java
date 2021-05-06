package de.gesellix.gradle.docker.tasks;

import de.gesellix.docker.engine.EngineResponse;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;

public class DockerVolumeRmTask extends GenericDockerTask {

  private final Property<String> volumeName;

  @Input
  public Property<String> getVolumeName() {
    return volumeName;
  }

  private EngineResponse response;

  @Internal
  public EngineResponse getResponse() {
    return response;
  }

  @Inject
  public DockerVolumeRmTask(ObjectFactory objectFactory) {
    super(objectFactory);
    setDescription("Remove a volume");

    volumeName = objectFactory.property(String.class);
  }

  @TaskAction
  public EngineResponse rmVolume() {
    getLogger().info("docker volume rm");

    response = getDockerClient().rmVolume(getVolumeName().get());
    return response;
  }

  /**
   * @see #getVolumeName()
   * @deprecated This setter will be removed, please use the Property instead.
   */
  @Deprecated
  public void setVolumeName(String volumeName) {
    this.volumeName.set(volumeName);
  }
}
