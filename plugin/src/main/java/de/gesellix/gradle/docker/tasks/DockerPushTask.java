package de.gesellix.gradle.docker.tasks;

import de.gesellix.docker.engine.EngineResponse;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;

public class DockerPushTask extends GenericDockerTask {

  private final Property<String> repositoryName;

  @Input
  public Property<String> getRepositoryName() {
    return repositoryName;
  }

  private final Property<String> registry;

  @Input
  @Optional
  public Property<String> getRegistry() {
    return registry;
  }

  @Internal
  private EngineResponse result;

  public EngineResponse getResult() {
    return result;
  }

  @Inject
  public DockerPushTask(ObjectFactory objectFactory) {
    super(objectFactory);
    setDescription("Push an image or a repository to a Docker registry server");

    repositoryName = objectFactory.property(String.class);
    registry = objectFactory.property(String.class);
  }

  @TaskAction
  public EngineResponse push() {
    getLogger().info("docker push");
    result = getDockerClient().push(getRepositoryName().get(), getEncodedAuthConfig(), getRegistry().getOrNull());
    return result;
  }

  /**
   * @see #getRepositoryName()
   * @deprecated This setter will be removed, please use the Property instead.
   */
  @Deprecated
  public void setRepositoryName(String repositoryName) {
    this.repositoryName.set(repositoryName);
  }

  /**
   * @see #getRegistry()
   * @deprecated This setter will be removed, please use the Property instead.
   */
  @Deprecated
  public void setRegistry(String registry) {
    this.registry.set(registry);
  }
}
