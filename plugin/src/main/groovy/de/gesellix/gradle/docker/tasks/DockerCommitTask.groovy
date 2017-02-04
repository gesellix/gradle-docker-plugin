package de.gesellix.gradle.docker.tasks

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction

class DockerCommitTask extends DockerTask {

    @Input
    def containerId
    @Input
    def tag
    @Input
    def repo
    @Optional
    @Input
    def author
    @Optional
    @Input
    def comment
    @Input
    @Optional
    def pauseContainer
    @Optional
    @Input
    def changes

    DockerCommitTask() {
        description = "Commit changes to a container"
        group = "Docker"
    }

    @TaskAction
    def commit() {
        logger.info "docker commit changes to container"
        return getDockerClient().commit(getContainerId(),[
                                          repo   : getRepo(),
                                          tag    : getTag(),
                                          comment: getComment(),
                                          author : getAuthor(),
                                          changes: getChanges(),
                                          pause  : getPauseContainer()
                                      ])
    }
}
