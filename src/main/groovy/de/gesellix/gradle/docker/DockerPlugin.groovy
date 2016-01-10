package de.gesellix.gradle.docker

import de.gesellix.gradle.docker.tasks.DockerTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.slf4j.Logger
import org.slf4j.LoggerFactory

public class DockerPlugin implements Plugin<Project> {

    def Logger logger = LoggerFactory.getLogger(DockerPlugin)

    static final String EXTENSION_NAME = 'docker'

    @Override
    void apply(Project project) {
//    project.plugins.apply(BasePlugin)

        logger.debug "ensure '${EXTENSION_NAME}' extension exists"
        def extension = project.extensions.findByName(EXTENSION_NAME) ?: project.extensions.create(EXTENSION_NAME, DockerPluginExtension, project)
    }
}
