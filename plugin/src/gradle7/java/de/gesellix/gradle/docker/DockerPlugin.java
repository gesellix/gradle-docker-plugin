package de.gesellix.gradle.docker;

import de.gesellix.docker.engine.DockerEnv;
import de.gesellix.gradle.docker.tasks.GenericDockerTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.provider.ProviderFactory;

import java.net.Proxy;

public class DockerPlugin implements Plugin<Project> {

  public static final String EXTENSION_NAME = "docker";

  @Override
  public void apply(Project project) {
    DockerPluginExtension extension = project.getExtensions().create(EXTENSION_NAME, DockerPluginExtension.class, project);
    ProviderFactory providers = project.getProviders();
    extension.getDockerHost().convention(providers.systemProperty("docker.host")
                                             .orElse(providers.environmentVariable("DOCKER_HOST"))
                                             .orElse(new DockerEnv().getDockerHost()));
    extension.getCertPath().convention(providers.systemProperty("docker.cert.path")
                                           .orElse(providers.environmentVariable("DOCKER_CERT_PATH"))
                                           .getOrNull());
    extension.getProxy().convention(Proxy.NO_PROXY);

    project.getTasks().withType(GenericDockerTask.class).configureEach(task -> {
      task.getDockerHost().convention(extension.getDockerHost());
      task.getCertPath().convention(extension.getCertPath());
      task.getProxy().convention(extension.getProxy());
      task.getAuthConfig().convention(extension.getAuthConfig());
    });
  }
}
