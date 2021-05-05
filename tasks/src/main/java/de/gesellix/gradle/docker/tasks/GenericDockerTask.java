package de.gesellix.gradle.docker.tasks;

import de.gesellix.docker.client.DockerClient;
import de.gesellix.docker.client.DockerClientImpl;
import de.gesellix.docker.client.authentication.AuthConfig;
import de.gesellix.docker.engine.DockerEnv;
import org.gradle.api.DefaultTask;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Optional;

import javax.inject.Inject;
import java.net.Proxy;

import static java.net.Proxy.NO_PROXY;

public class GenericDockerTask extends DefaultTask {

  Property<String> dockerHost;

  Property<String> certPath;

  Property<Proxy> proxy;

  Property<AuthConfig> authConfig;

  private DockerClient dockerClient;

  @Inject
  public GenericDockerTask(ObjectFactory objectFactory) {
    dockerHost = objectFactory.property(String.class);
    certPath = objectFactory.property(String.class);
    proxy = objectFactory.property(Proxy.class);
    authConfig = objectFactory.property(AuthConfig.class);
    setGroup("Docker");
  }

  @Input
  @Optional
  public Property<String> getDockerHost() {
    return dockerHost;
  }

  @Input
  @Optional
  public Property<String> getCertPath() {
    return certPath;
  }

  @Input
  @Optional
  public Property<Proxy> getProxy() {
    return proxy;
  }

  @Input
  @Optional
  public Property<AuthConfig> getAuthConfig() {
    return authConfig;
  }

  /**
   * @see #authConfig
   * @deprecated
   */
  @Deprecated
  public void setAuthConfigPlain(AuthConfig authConfig) {
    this.authConfig.set(authConfig);
  }

  /**
   * @see #authConfig
   * @deprecated
   */
  @Deprecated
  public void setAuthConfigEncoded(String ignored) {
    throw new UnsupportedOperationException("please use the authConfig method instead");
  }

  void setDockerClient(DockerClient dockerClient) {
    this.dockerClient = dockerClient;
  }

  @Internal
  public DockerClient getDockerClient() {
    if (dockerClient == null) {
      if (dockerHost.isPresent() || certPath.isPresent()) {
        DockerEnv dockerEnv = new DockerEnv();
        if (dockerHost.isPresent()) {
          dockerEnv.setDockerHost(dockerHost.get());
        }

        if (certPath.isPresent()) {
          dockerEnv.setCertPath(getProject().file(certPath.get()).getAbsolutePath());
        }

        dockerClient = new DockerClientImpl(dockerEnv, proxy.getOrElse(NO_PROXY));
      }
      else {
        dockerClient = new DockerClientImpl();
      }
    }

    return dockerClient;
  }

  @Internal
  public String getEncodedAuthConfig() {
    return authConfig.map((AuthConfig a) -> getDockerClient().encodeAuthConfig(a)).getOrElse("");
  }
}
