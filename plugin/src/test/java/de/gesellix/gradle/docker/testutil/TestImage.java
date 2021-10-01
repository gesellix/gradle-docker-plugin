package de.gesellix.gradle.docker.testutil;

import de.gesellix.docker.client.DockerClient;

import java.util.Map;
import java.util.Objects;

public class TestImage {

  private final DockerClient dockerClient;
  private final String repository;
  private final String tag;
  private final boolean isWindows;

  public TestImage(DockerClient dockerClient) {
    this.dockerClient = dockerClient;

    this.isWindows = Objects.requireNonNull(((Map) dockerClient.version().getContent()).get("Os").toString()).equalsIgnoreCase("windows");
    this.repository = "gesellix/echo-server";
    this.tag = isWindows ? "os-windows" : "os-linux";

    // TODO consider NOT calling prepare inside the constructor
    prepare();
  }

  public void prepare() {
    dockerClient.pull(getImageName(), getImageTag());
  }

  public boolean isWindows() {
    return isWindows;
  }

  public String getImageWithTag() {
    return getImageName() + ":" + getImageTag();
  }

  public String getImageName() {
    return repository;
  }

  public String getImageTag() {
    return tag;
  }
}
