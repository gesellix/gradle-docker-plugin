package de.gesellix.gradle.docker.tasks;

import de.gesellix.docker.engine.EngineResponse;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class DockerPullTask extends GenericDockerTask {

  private static final int RETRY_COUNT = 2;

  private final Property<String> imageName;

  @Input
  public Property<String> getImageName() {
    return imageName;
  }

  private final Property<String> imageTag;

  @Input
  @Optional
  public Property<String> getImageTag() {
    return imageTag;
  }

  private final Property<String> registry;

  @Input
  @Optional
  public Property<String> getRegistry() {
    return registry;
  }

  private String imageId;

  @Internal
  public String getImageId() {
    return imageId;
  }

  @Inject
  public DockerPullTask(ObjectFactory objectFactory) {
    super(objectFactory);
    setDescription("Pull an image or a repository from a Docker registry server");

    imageName = objectFactory.property(String.class);
    imageTag = objectFactory.property(String.class);
    registry = objectFactory.property(String.class);
  }

  @TaskAction
  public String pull() {
    getLogger().info("docker pull");

    Map<String, Object> query = new HashMap<>(3);
    query.put("fromImage", getImageName().get());
    query.put("tag", getImageTag().getOrNull());
    if (getRegistry().isPresent()) {
      query.put("fromImage", getRegistry().get() + "/" + getImageName().get());
    }

    Map<String, Object> options = new HashMap<>(1);
    options.put("EncodedRegistryAuth", getEncodedAuthConfig());

    EngineResponse response;
    int counter = 0;
    do {
      counter++;
      response = getDockerClient().create(query, options);
    } while(!response.getStatus().isSuccess() && counter <= RETRY_COUNT);

    if (response.getStatus().isSuccess()) {
      imageId = getDockerClient().findImageId(query.get("fromImage"), query.get("tag"));
    }
    else {
      imageId = null;
    }

    return imageId;
  }

  /**
   * @see #getImageName()
   * @deprecated This setter will be removed, please use the Property instead.
   */
  @Deprecated
  public void setImageName(String imageName) {
    this.imageName.set(imageName);
  }

  /**
   * @see #getImageTag()
   * @deprecated This setter will be removed, please use the Property instead.
   */
  @Deprecated
  public void setTag(String tag) {
    this.imageTag.set(tag);
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
