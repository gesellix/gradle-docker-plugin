package de.gesellix.gradle.docker.tasks;

import de.gesellix.docker.client.authentication.AuthConfig;
import groovy.lang.Closure;
import org.gradle.api.Task;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class DockerPublishTask extends GenericDockerTask {

  private final DirectoryProperty buildContextDirectory;

  @InputDirectory
  @Optional
  public DirectoryProperty getBuildContextDirectory() {
    return buildContextDirectory;
  }

  private final Property<InputStream> buildContext;

  @Input
  @Optional
  public Property<InputStream> getBuildContext() {
    return buildContext;
  }

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

  private final MapProperty<String, String> targetRegistries;

  @Input
  @Optional
  public MapProperty<String, String> getTargetRegistries() {
    return targetRegistries;
  }

  private final Property<String> imageNameWithTag;

  @Internal
  public Property<String> getImageNameWithTag() {
    return imageNameWithTag;
  }

  @Inject
  public DockerPublishTask(ObjectFactory objectFactory) {
    super(objectFactory);
    setDescription("builds and publishes an image");

    buildContextDirectory = objectFactory.directoryProperty();
    buildContext = objectFactory.property(InputStream.class);
    imageName = objectFactory.property(String.class);
    imageTag = objectFactory.property(String.class);
    imageNameWithTag = objectFactory.property(String.class);
    targetRegistries = objectFactory.mapProperty(String.class, String.class);
  }

  /**
   * @deprecated remove the Groovy Dependency
   */
  @Deprecated
  @Override
  public @NotNull Task configure(@NotNull Closure closure) {
    final DockerPublishTask configuredTask = (DockerPublishTask) super.configure(closure);

    imageNameWithTag.set(configuredTask.getImageName().get());
    if (configuredTask.getImageTag().isPresent()) {
      imageNameWithTag.set(configuredTask.getImageName().get() + ":" + configuredTask.getImageTag().get());
    }

    final Task buildImageTask = configureBuildImageTask();
    configuredTask.getDependsOn().forEach((Object parentTaskDependency) -> {
      if (!buildImageTask.equals(parentTaskDependency)) {
        buildImageTask.mustRunAfter(parentTaskDependency);
      }
    });
    configuredTask.dependsOn(buildImageTask);

    if ((getTargetRegistries().getOrElse(new HashMap<>())).isEmpty()) {
      getLogger().warn("No targetRegistries configured, image won't be pushed to any registry.");
    }
    getTargetRegistries().get().forEach((String registryName, String targetRegistry) -> {
      Task pushTask = createUniquePushTask(registryName, targetRegistry, getImageNameWithTag(), getAuthConfig());
      pushTask.dependsOn(buildImageTask);
      configuredTask.dependsOn(pushTask);
    });

    return configuredTask;
  }

  private static String capitalize(String s) {
    return s.isEmpty() ? "" : "" + Character.toUpperCase(s.charAt(0)) + s.substring(1);
  }

  public Task createUniquePushTask(final String registryName, final String targetRegistry, final Property<String> imageNameWithTag, final Property<AuthConfig> authConfig) {
    DockerPushTask pushTask = createUniqueTask(DockerPushTask.class, "pushImageTo" + capitalize(registryName) + "For" + capitalize(this.getName()));
    pushTask.getRepositoryName().set(imageNameWithTag);
    pushTask.getRegistry().set(targetRegistry);
    pushTask.getAuthConfig().set(authConfig);

    DockerRmiTask rmiTask = createUniqueTask(DockerRmiTask.class, "rmi" + capitalize(registryName) + "ImageFor" + capitalize(this.getName()));
    rmiTask.getImageId().set(targetRegistry + "/" + imageNameWithTag.get());
    pushTask.finalizedBy(rmiTask);
    return pushTask;
  }

  private Task configureBuildImageTask() {
    String buildImageTaskName = "buildImageFor" + capitalize(this.getName());
    DockerBuildTask buildImageTask = createUniqueTask(DockerBuildTask.class, buildImageTaskName);
    buildImageTask.getImageName().set(DockerPublishTask.this.getImageNameWithTag());
    buildImageTask.getBuildContext().set(DockerPublishTask.this.getBuildContext());
    buildImageTask.getBuildContextDirectory().set(DockerPublishTask.this.getBuildContextDirectory());
    return buildImageTask;
  }

  public <T> T createUniqueTask(final Class<T> taskType, final String name) {
    final Set<Task> existingTasks = getProject()
        .getTasks()
        .stream()
        .filter((Task t) -> taskType.isInstance(t) && t.getName().equals(name))
        .collect(Collectors.toSet());
    if (existingTasks.size() > 1) {
      throw new IllegalStateException("unique task expected, found " + existingTasks.size() + ".");
    }

    if (!existingTasks.isEmpty()) {
      return (T) existingTasks.stream().findFirst().get();
    }
    else {
      Map<String, Serializable> args = new HashMap<>(2);
      args.put("type", taskType);
      args.put("group", getGroup());
      return (T) getProject().task(args, name);
    }
  }

  @TaskAction
  public void publish() {
    getLogger().info("running publish...");
  }

  /**
   * @see #getBuildContextDirectory()
   * @deprecated This setter will be removed, please use the Property instead.
   */
  @Deprecated
  public void setBuildContextDirectory(File buildContextDirectory) {
    this.buildContextDirectory.set(buildContextDirectory);
  }

  /**
   * @see #getBuildContextDirectory()
   * @deprecated This setter will be removed, please use the Property instead.
   */
  @Deprecated
  public void setBuildContextDirectory(String buildContextDirectory) {
    this.buildContextDirectory.set(getProject().file(buildContextDirectory));
  }

  /**
   * @see #getBuildContext()
   * @deprecated This setter will be removed, please use the Property instead.
   */
  @Deprecated
  public void setBuildContext(InputStream buildContext) {
    this.buildContext.set(buildContext);
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
  public void setImageTag(String imageTag) {
    this.imageTag.set(imageTag);
  }

  /**
   * @see #getTargetRegistries()
   * @deprecated This setter will be removed, please use the Property instead.
   */
  @Deprecated
  public void setTargetRegistries(Map<String, String> targetRegistries) {
    this.targetRegistries.set(targetRegistries);
  }
}
