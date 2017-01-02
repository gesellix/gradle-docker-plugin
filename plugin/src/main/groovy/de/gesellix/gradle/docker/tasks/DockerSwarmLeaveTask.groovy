package de.gesellix.gradle.docker.tasks

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction

class DockerSwarmLeaveTask extends DockerTask {

    @Input
    @Optional
    def query = [:]

    def response

    @TaskAction
    def leaveSwarm() {
        logger.info "docker swarm leave"

        response = getDockerClient().leaveSwarm(getQuery())
        return response
    }
}
