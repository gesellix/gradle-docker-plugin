package de.gesellix.gradle.docker;

import de.gesellix.docker.authentication.AuthConfig;
import org.gradle.api.Project;

import java.net.Proxy;

/**
 * Provides project extension class for configuring default values for all Docker tasks.
 */
public class DockerPluginExtension {

  private final Project project;

  /**
   * Docker host url. Will default to the system property {@code docker.host} if that is provided,
   * otherwise it will try to use the environment variable {@code DOCKER_HOST}. Failing that it will be
   * null.
   */
  private String dockerHost;

  private String certPath;

  private Proxy proxy;
  private AuthConfig authConfig;

  public DockerPluginExtension(Project project) {
    this.project = project;
    dockerHost = System.getProperty("docker.host", System.getenv("DOCKER_HOST"));
    certPath = System.getProperty("docker.cert.path", System.getenv("DOCKER_CERT_PATH"));
  }

  /**
   * Allows for the setting of the path where certificates can be found.
   *
   * @param path Any path object that can be resolved via {@code project.file ( )}
   */
  public void setCertPath(String path) {
    this.certPath = path;
  }

  /**
   * Returns the certificate path as an absolute path to the certificate.
   * If {@code certPath} was not configured it will attempt to check for system property
   * {@code docker.cert.path} first and then for the {@code DOCKER_CERT_PATH} environment variable.
   *
   * @return Absolute path as a string or null.
   */
  public String getCertPath() {
    if (certPath != null && !certPath.isEmpty()) {
      return project.file(certPath).getAbsolutePath();
    }
    return null;
  }

  public String getDockerHost() {
    return dockerHost;
  }

  public void setDockerHost(String dockerHost) {
    this.dockerHost = dockerHost;
  }

  public Proxy getProxy() {
    return proxy;
  }

  public void setProxy(Proxy proxy) {
    this.proxy = proxy;
  }

  public AuthConfig getAuthConfig() {
    return authConfig;
  }

  public void setAuthConfig(AuthConfig authConfig) {
    this.authConfig = authConfig;
  }
}
