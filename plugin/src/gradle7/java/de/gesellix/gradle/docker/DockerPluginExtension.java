package de.gesellix.gradle.docker;

import de.gesellix.docker.client.authentication.AuthConfig;
import org.gradle.api.provider.Property;

import java.net.Proxy;

abstract public class DockerPluginExtension {

  abstract public Property<String> getDockerHost();

  abstract public Property<String> getCertPath();

  abstract public Property<Proxy> getProxy();

  abstract public Property<AuthConfig> getAuthConfig();
}
