package de.gesellix.gradle.docker.tasks;

import de.gesellix.docker.remote.api.CreateImageInfo;
import de.gesellix.docker.remote.api.core.StreamCallback;
import org.gradle.api.GradleException;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class DockerPullTask extends GenericDockerTask {

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

  public Duration pullTimeout = Duration.of(10, ChronoUnit.MINUTES);

  @Internal
  public Duration getPullTimeout() {
    return pullTimeout;
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

    String imageName = getImageName()
        .map(i -> getRegistry().map(r -> r + "/" + i).getOrElse(i)).get();

    List<CreateImageInfo> infos = new ArrayList<>();
    CountDownLatch pullFinished = new CountDownLatch(1);

    getDockerClient().pull(
        new StreamCallback<CreateImageInfo>() {
          @Override
          public void onNext(CreateImageInfo element) {
            if (element != null) {
              getLogger().info(element.toString());
            }
            infos.add(element);
          }

          @Override
          public void onFailed(Exception e) {
            pullFinished.countDown();
          }

          @Override
          public void onFinished() {
            pullFinished.countDown();
          }
        },
        pullTimeout,
        imageName,
        getImageTag().getOrNull(),
        getEncodedAuthConfig()
    );
    try {
      pullFinished.await(pullTimeout.toMillis(), TimeUnit.MILLISECONDS);
    }
    catch (InterruptedException e) {
      throw new GradleException("Pull didn't finish before " + pullTimeout, e);
    }
    imageId = imageName + getImageTag().map(t -> ":" + t).getOrElse("");
    return imageId;
  }
}
