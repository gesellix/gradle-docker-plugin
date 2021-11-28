package de.gesellix.gradle.docker.tasks;

import de.gesellix.docker.engine.EngineResponse;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.util.HashMap;

public class DockerVolumesTask extends GenericDockerTask {

  private final MapProperty<String, Object> query;

  @Input
  @Optional
  public MapProperty<String, Object> getQuery() {
    return query;
  }

  private EngineResponse volumes;

  @Internal
  public EngineResponse getVolumes() {
    return volumes;
  }

  @Inject
  public DockerVolumesTask(ObjectFactory objectFactory) {
    super(objectFactory);
    setDescription("List volumes from all volume drivers");

    query = objectFactory.mapProperty(String.class, Object.class);
  }

  @TaskAction
  public void volumes() {
    getLogger().info("docker volume ls");
    volumes = getDockerClient().volumes(new HashMap<>(getQuery().get()));
  }
}
