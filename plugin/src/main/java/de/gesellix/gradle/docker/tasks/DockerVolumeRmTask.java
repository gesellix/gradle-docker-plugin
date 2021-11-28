package de.gesellix.gradle.docker.tasks;

import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;

public class DockerVolumeRmTask extends GenericDockerTask {

  private final Property<String> volumeName;

  @Input
  public Property<String> getVolumeName() {
    return volumeName;
  }

  @Inject
  public DockerVolumeRmTask(ObjectFactory objectFactory) {
    super(objectFactory);
    setDescription("Remove a volume");

    volumeName = objectFactory.property(String.class);
  }

  @TaskAction
  public void rmVolume() {
    getLogger().info("docker volume rm");
    getDockerClient().rmVolume(getVolumeName().get());
  }
}
