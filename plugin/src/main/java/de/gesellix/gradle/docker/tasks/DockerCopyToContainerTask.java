package de.gesellix.gradle.docker.tasks;

import de.gesellix.docker.engine.EngineResponse;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.io.InputStream;

public class DockerCopyToContainerTask extends GenericDockerTask {

  private final Property<String> container;

  @Input
  public Property<String> getContainer() {
    return container;
  }

  private final Property<String> targetPath;

  @Input
  public Property<String> getTargetPath() {
    return targetPath;
  }

  private final Property<InputStream> tarInputStream;

  @Input
  public Property<InputStream> getTarInputStream() {
    return tarInputStream;
  }

  @Inject
  public DockerCopyToContainerTask(ObjectFactory objectFactory) {
    super(objectFactory);
    setDescription("Copy files/folders from your host to a container.");

    container = objectFactory.property(String.class);
    targetPath = objectFactory.property(String.class);
    tarInputStream = objectFactory.property(InputStream.class);
  }

  @TaskAction
  public EngineResponse copyToContainer() {
    getLogger().info("docker cp to container");

    return getDockerClient().putArchive(getContainer().get(), getTargetPath().get(), getTarInputStream().get());
  }

  /**
   * @see #getContainer()
   * @deprecated This setter will be removed, please use the Property instead.
   */
  @Deprecated
  public void setContainer(String container) {
    this.container.set(container);
  }

  /**
   * @see #getTarInputStream()
   * @deprecated This setter will be removed, please use the Property instead.
   */
  @Deprecated
  public void setTarInputStream(InputStream tarInputStream) {
    this.tarInputStream.set(tarInputStream);
  }

  /**
   * @see #getTargetPath()
   * @deprecated This setter will be removed, please use the Property instead.
   */
  @Deprecated
  public void setTargetPath(String targetPath) {
    this.targetPath.set(targetPath);
  }
}
