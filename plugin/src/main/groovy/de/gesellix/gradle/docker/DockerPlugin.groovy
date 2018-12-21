package de.gesellix.gradle.docker

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

class DockerPlugin implements Plugin<Project> {

    private static final Logger logger = Logging.getLogger(DockerPlugin)

    static final String EXTENSION_NAME = 'docker'

    @Override
    void apply(Project project) {
//    project.plugins.apply(BasePlugin)

        logger.debug "ensure '${EXTENSION_NAME}' extension exists"
        project.extensions.findByName(EXTENSION_NAME) ?: project.extensions.create(EXTENSION_NAME, DockerPluginExtension, project)
    }
}
