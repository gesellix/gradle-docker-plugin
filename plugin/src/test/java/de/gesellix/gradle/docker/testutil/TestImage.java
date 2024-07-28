package de.gesellix.gradle.docker.testutil;

import de.gesellix.docker.client.DockerClient;

import java.util.Objects;

public class TestImage {

  private final DockerClient dockerClient;
  private final String repository;
  private final String tag;
  private final boolean isWindows;

  public TestImage(DockerClient dockerClient) {
    this.dockerClient = dockerClient;

    this.isWindows = Objects.requireNonNull(dockerClient.version().getContent().getOs()).equalsIgnoreCase("windows");
    this.repository = "gesellix/echo-server";
    this.tag = "2024-07-28T18-30-00";

    // TODO consider NOT calling prepare inside the constructor
    prepare();
  }

  public void prepare() {
    dockerClient.pull(null, null, getImageName(), getImageTag());
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
