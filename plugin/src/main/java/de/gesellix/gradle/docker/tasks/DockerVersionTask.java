package de.gesellix.gradle.docker.tasks;

import de.gesellix.docker.client.EngineResponseContent;
import de.gesellix.docker.remote.api.SystemVersion;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;
import org.gradle.work.DisableCachingByDefault;

import javax.inject.Inject;

@DisableCachingByDefault(because = "Delegate caching to Docker")
public class DockerVersionTask extends GenericDockerTask {

  private EngineResponseContent<SystemVersion> version;

  @Internal
  public EngineResponseContent<SystemVersion> getVersion() {
    return version;
  }

  @Inject
  public DockerVersionTask(ObjectFactory objectFactory) {
    super(objectFactory);
    setDescription("Show the Docker version information");
  }

  @TaskAction
  public void version() {
    getLogger().info("docker version");
    version = getDockerClient().version();
  }
}
