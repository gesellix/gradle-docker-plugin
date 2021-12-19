package de.gesellix.gradle.docker.tasks;

import de.gesellix.docker.remote.api.core.Cancellable;
import de.gesellix.docker.remote.api.core.Frame;
import de.gesellix.docker.remote.api.core.StreamCallback;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class DockerLogsTask extends GenericDockerTask {

  private final Property<String> containerId;

  @Input
  public Property<String> getContainerId() {
    return containerId;
  }

  public Duration logsTimeout = Duration.of(10, ChronoUnit.MINUTES);

  @Internal
  public Duration getLogsTimeout() {
    return logsTimeout;
  }

  private final MapProperty<String, Object> logOptions;

  @Input
  @Optional
  public MapProperty<String, Object> getLogOptions() {
    return logOptions;
  }

  @Inject
  public DockerLogsTask(ObjectFactory objectFactory) {
    super(objectFactory);

    setDescription("Fetch the logs of a container");

    containerId = objectFactory.property(String.class);
    logOptions = objectFactory.mapProperty(String.class, Object.class);
    logOptions.convention(new HashMap<>());
    logOptions.put("follow", false);
  }

  @TaskAction
  public void logs() {
    getLogger().info("docker logs {}", containerId.get());

    CountDownLatch logsFinished = new CountDownLatch(1);
    StreamCallback<Frame> callback = new StreamCallback<Frame>() {
      Cancellable cancellable;

      @Override
      public void onStarting(Cancellable cancellable) {
        this.cancellable = cancellable;
      }

      @Override
      public void onNext(Frame frame) {
        if (frame != null) {
          getLogger().info(frame.toString());
        }
      }

      @Override
      public void onFailed(Exception e) {
        getLogger().error("failed", e);
        logsFinished.countDown();
        cancellable.cancel();
      }

      @Override
      public void onFinished() {
        getLogger().info("finished");
        logsFinished.countDown();
      }
    };

    getDockerClient().logs(
        containerId.get(),
        logOptions.getOrNull(),
        callback,
        logsTimeout);
    try {
      getLogger().debug("Following the logs for  " + logsTimeout + "...");
      logsFinished.await(logsTimeout.toMillis(), TimeUnit.MILLISECONDS);
    }
    catch (InterruptedException ignored) {
    }
  }
}
