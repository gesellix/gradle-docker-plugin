package de.gesellix.gradle.docker.tasks;

import de.gesellix.docker.client.EngineResponseContent;
import de.gesellix.docker.remote.api.Volume;
import de.gesellix.docker.remote.api.VolumeCreateOptions;

import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;

public class DockerVolumeCreateTask extends GenericDockerTask {

  private final Property<VolumeCreateOptions> volumeConfig;

  @Input
  @Optional
  public Property<VolumeCreateOptions> getVolumeConfig() {
    return volumeConfig;
  }

  private EngineResponseContent<Volume> response;

  @Internal
  public EngineResponseContent<Volume> getResponse() {
    return response;
  }

  @Inject
  public DockerVolumeCreateTask(ObjectFactory objectFactory) {
    super(objectFactory);
    setDescription("Create a volume");

    volumeConfig = objectFactory.property(VolumeCreateOptions.class);
  }

  @TaskAction
  public EngineResponseContent<Volume> createVolume() {
    getLogger().info("docker volume create");

    response = getDockerClient().createVolume(volumeConfig.get());
    return response;
  }
}
