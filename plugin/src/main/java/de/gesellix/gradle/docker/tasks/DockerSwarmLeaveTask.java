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
import java.util.Map;

public class DockerSwarmLeaveTask extends GenericDockerTask {

  private final MapProperty<String, Object> query;

  @Input
  @Optional
  public MapProperty<String, Object> getQuery() {
    return query;
  }

  private EngineResponse response;

  @Internal
  public EngineResponse getResponse() {
    return response;
  }

  @Inject
  public DockerSwarmLeaveTask(ObjectFactory objectFactory) {
    super(objectFactory);
    setDescription("Leave the swarm");

    query = objectFactory.mapProperty(String.class, Object.class);
  }

  @TaskAction
  public EngineResponse leaveSwarm() {
    getLogger().info("docker swarm leave");

    response = getDockerClient().leaveSwarm(new HashMap<>(getQuery().get()));
    return response;
  }

  /**
   * @see #getQuery()
   * @deprecated This setter will be removed, please use the Property instead.
   */
  @Deprecated
  public void setQuery(Map<String, Object> query) {
    this.query.set(query);
  }
}
