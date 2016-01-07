package de.gesellix.gradle.docker

import de.gesellix.gradle.docker.tasks.DockerTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.slf4j.Logger
import org.slf4j.LoggerFactory

public class DockerPlugin implements Plugin<Project> {

    def Logger logger = LoggerFactory.getLogger(DockerPlugin)

    final def EXTENSION_NAME = 'docker'

    @Override
    void apply(Project project) {
//    project.plugins.apply(BasePlugin)

        logger.debug "ensure '${EXTENSION_NAME}' extension exists"
        def extension = project.extensions.findByName(EXTENSION_NAME) ?: project.extensions.create(EXTENSION_NAME, DockerPluginExtension, project)

        project.tasks.withType(DockerTask) { task ->
            logger.debug "apply '${EXTENSION_NAME}' extension config to $task"
            task.dockerHost = extension.dockerHost
            task.certPath = extension.getCertPath()
            task.proxy = extension.proxy
            task.authConfigPlain = extension.authConfigPlain
            task.authConfigEncoded = extension.authConfigEncoded
        }
    }
}
