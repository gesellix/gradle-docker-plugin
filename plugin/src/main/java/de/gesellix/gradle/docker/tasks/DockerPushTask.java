package de.gesellix.gradle.docker.tasks;

import de.gesellix.docker.remote.api.PushImageInfo;
import de.gesellix.docker.remote.api.core.Cancellable;
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
import java.util.stream.Collectors;

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

  public Duration pushTimeout = Duration.of(10, ChronoUnit.MINUTES);

  @Internal
  public Duration getPushTimeout() {
    return pushTimeout;
  }

  @Inject
  public DockerPushTask(ObjectFactory objectFactory) {
    super(objectFactory);
    setDescription("Push an image or a repository to a Docker registry server");

    repositoryName = objectFactory.property(String.class);
    registry = objectFactory.property(String.class);
  }

  @TaskAction
  public void push() {
    getLogger().info("docker push");
    List<PushImageInfo> infos = new ArrayList<>();
    CountDownLatch pushFinished = new CountDownLatch(1);
    StreamCallback<PushImageInfo> callback = new StreamCallback<PushImageInfo>() {

      private Cancellable cancellable;

      @Override
      public void onStarting(Cancellable cancellable) {
        this.cancellable = cancellable;
      }

      @Override
      public void onNext(PushImageInfo element) {
        getLogger().info(element != null ? element.toString() : null);
        infos.add(element);
      }

      @Override
      public void onFailed(Exception e) {
        getLogger().error("Push failed", e);
        pushFinished.countDown();
        cancellable.cancel();
      }

      @Override
      public void onFinished() {
        getLogger().info("Push finished");
        pushFinished.countDown();
      }
    };
    getDockerClient().push(callback, pushTimeout, getRepositoryName().get(), getEncodedAuthConfig(), getRegistry().getOrNull());
    try {
      getLogger().debug("Waiting " + pushTimeout + " for the build to finish...");
      pushFinished.await(pushTimeout.toMillis(), TimeUnit.MILLISECONDS);

      List<PushImageInfo> errors = infos.stream()
          .filter(i -> i.getError() != null)
          .collect(Collectors.toList());
      if (!errors.isEmpty()) {
        throw new GradleException("Push failed: " + errors.stream().findFirst().get());
      }
    }
    catch (InterruptedException e) {
      getLogger().error("Push didn't finish before timeout of " + pushFinished, e);
    }
  }
}
