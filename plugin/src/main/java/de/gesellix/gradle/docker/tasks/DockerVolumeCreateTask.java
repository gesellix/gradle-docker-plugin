package de.gesellix.gradle.docker.tasks;

import de.gesellix.docker.client.EngineResponseContent;
import de.gesellix.docker.remote.api.Volume;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.util.HashMap;

public class DockerVolumeCreateTask extends GenericDockerTask {

  private final MapProperty<String, Object> volumeConfig;

  @Input
  @Optional
  public MapProperty<String, Object> getVolumeConfig() {
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

    volumeConfig = objectFactory.mapProperty(String.class, Object.class);
  }

  @TaskAction
  public EngineResponseContent<Volume> createVolume() {
    getLogger().info("docker volume create");

    response = getDockerClient().createVolume(new HashMap<>(getVolumeConfig().get()));
    return response;
  }
}
