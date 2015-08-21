package de.gesellix.gradle.docker.models

import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import de.gesellix.docker.client.DockerClient

class DockerContainer {
    private static Logger logger = Logging.getLogger(DockerContainer)

    String name
    String imageName
    DockerClient client
    def config

    String id
    String imageId
    boolean running
    boolean exists

    DockerContainer(DockerClient client, String name, String imageName, def config) {
        this.client = client
        this.name = name
        this.imageName = imageName
        this.config = config

        // Force Image to be ImageName in config
        this.config.Image = imageName
    }

    /**
     * Inspect container
     *
     * @return container configuration or null
     */
    def inspect() {
        if(id == null) {
            def containers = client.ps([filters: [name: [name]]]).content.findAll {
                "/" + name in it.Names
            }
            if (containers.size() > 0) {
                id = containers[0].Id
            } else {
                id = null
                imageId = null
                running = false
                exists = false
                return null
            }
        }

        logger.info "Container[{}]: Inspecting container {}", name, id
        try {
            def response = client.inspectContainer(id)

            exists = true
            imageId = response.content.Image
            running = response.content.State.Running

            return response.content

        } catch(FileNotFoundException ignored) {
            id = null
            imageId = null
            running = false
            exists = false
        } catch(IllegalStateException ignored) {
            id = null
            imageId = null
            running = false
            exists = false
        }

        return null
    }

    /**
     * Create container
     */
    void create() {
        logger.lifecycle("Container[${name}]: Creating")
        def response = client.createContainer(config, [name: name])

        if (!response.status.success) {
            throw new IllegalStateException("docker create failed: ${response.status.text}")
        }

        id = response.content.Id

        inspect()
    }

    /**
     * Start container
     */
    void start() {
        logger.lifecycle("Container[${name}]: Starting")
        def response = client.startContainer(id)

        if (!response.status.success) {
            throw new IllegalStateException("docker start failed: ${response.status.text}")
        }

        inspect()
    }

    /**
     * Stop container
     */
    void stop() {
        logger.lifecycle("Container[${name}]: Stopping")
        def response = client.stop(id)

        if (!response.status.success) {
            throw new IllegalStateException("docker stop failed: ${response.status.text}")
        }

        inspect()
    }

    /**
     * Remove container
     */
    void remove() {
        logger.lifecycle("Container[${name}]: Removing")
        def response = client.rm(id)

        if (!response.status.success) {
            throw new IllegalStateException("docker rm failed: ${response.status.text}")
        }

        id = null
        imageId = null
        running = false
        exists = false
    }

    /**
     * Verify container is present
     */
    boolean isPresent() {
        boolean present = inspect() != null
        logger.info "Container[{}].isPresent() == {}", name, present

        return present
    }

    /**
     * Verify container is present and running
     * Note: does not check configuration matches
     */
    boolean isStarted() {
        boolean started = (inspect() != null) && running
        logger.info "Container[{}].isStarted() == {}", name, started

        return started
    }

    /**
     * Verify container is present, running and configuration matches
     */
    boolean isReloaded() {
        boolean good = checkReasonForReload() == null
        logger.info "Container[{}].isReloaded() == {}", name, good

        return good
    }

    /**
     * Restarted is always required for container
     */
    boolean isRestarted() {
        return false
    }

    /**
     * Verify container is stopped
     */
    boolean isStopped() {
        boolean stopped = (inspect() == null) || !running
        logger.info "Container[{}].isStopped() == {}", name, stopped

        return stopped
    }

    /**
     * Verify container does not exist
     */
    boolean isAbsent() {
        boolean absent = inspect() == null
        logger.info "Container[{}].isAbsent() == {}", name, absent

        return absent
    }

    /**
     * Verify container is present, and create if not
     * Note: does not check configuration matches
     *
     * @return changed
     */
    boolean ensurePresent() {
        logger.info "Container[{}].ensurePresent()", name

        if(!inspect()) {
            create()
            return true
        }

        logger.info "Container[{}]: already present.", name

        return false
    }

    /**
     * Verify container is present and running, and create and start if not
     * Note: does not check configuration matches
     *
     * @return changed
     */
    boolean ensureStarted() {
        logger.info "Container[{}].ensureStarted()", name

        boolean changed = false

        if(!inspect()) {
            create()
            changed = true
        }

        if (!running) {
            start()
            changed = true
        }

        if (!changed) {
            logger.info "Container[{}]: already running.", name
        }

        return changed
    }

    /**
     * Verify container is present, running and configuration matches, and
     * stop, remove, create and start if not
     *
     * @return changed
     */
    boolean ensureReloaded() {
        logger.info "Container[{}].ensureReloaded()", name

        String reason = checkReasonForReload()
        if (reason != null) {
            reload(reason)
            return true
        }

        logger.info "Container[{}]: is up-to-date and running, no reload necessary.", name
        return false
    }

    /**
     * Unconditionally restart or start the container
     *
     * @return changed (always true)
     */
    boolean ensureRestarted() {
        reload("Unconditional restart")
        return true
    }

    /**
     * Stops the container if it exists and is running
     *
     * @return changed
     */
    boolean ensureStopped() {
        if (!inspect()) {
            logger.info "Container[{}]: does not exist, no stop necessary.", name
            return false
        }

        if (running) {
            stop()
            return true
        }

        logger.info "Container[{}]: already stopped.", name
        return false
    }

    /**
     * Stops and removes the container if it exists
     *
     * @return changed
     */
    boolean ensureAbsent() {
        if (!inspect()) {
            logger.info "Container[{}]: does not exist, no removal necessary.", name
            return false
        }

        if (running) {
            stop()
        }
        remove()

        return true
    }

    /**
     * Check if reload of container is needed and return the reason
     *
     * @return reason for reload or null if no reload is needed
     */
    private String checkReasonForReload() {
        def current = inspect()

        if(!current) {
            return "Container does not exist"
        }
        if(!running) {
            return "Container is not running"
        }

        def response = client.inspectImage(imageName)

        if (!response.status.success) {
            return "Image does not exist locally, new pull request"
        }

        def image = response.content

        // Image (by identifier for newer image versions with same tag)
        if(current.Image != image.Id) {
            return "Image identifiers differ: ${current.Image} != ${image.Id}"
        }

        // Exposed Ports
        def expectedExposed = (image.ContainerConfig.ExposedPorts ?: [:]).keySet() +
                (config.ExposedPorts ?: [:]).keySet()
        def currentExposed = (current.Config.ExposedPorts ?: [:]).keySet()
        if (currentExposed != expectedExposed) {
            return "Exposed ports do not match: ${currentExposed} != ${expectedExposed}"
        }

        // Volumes
        def expectedVolumes = (image.ContainerConfig.Volumes ?: [:]).keySet() +
                (config.Volumes ?: [:]).keySet()
        def currentVolumes = (current.Config.Volumes ?: [:]).keySet()
        if (currentVolumes != expectedVolumes) {
            return "Volumes do not match: ${currentVolumes} != ${expectedVolumes}"
        }

        // Environment
        def expectedEnv = splitEnv((Collection<String>)image.ContainerConfig.Env) +
                splitEnv((Collection<String>)config.Env)
        def currentEnv = splitEnv((Collection<String>)current.Config.Env)
        if (currentEnv != expectedEnv) {
            return "Env does not match: ${currentEnv} != ${expectedEnv}"
        }

        // Entrypoint and Cmd

        def currentCmd = []
        def expectedCmd = []

        if (current.Config.Entrypoint) {
            currentCmd += current.Config.Entrypoint
        }
        if (current.Config.Cmd) {
            currentCmd += current.Config.Cmd
        }
        if (config.Entrypoint) {
            expectedCmd += config.Entrypoint
            expectedCmd += config.Cmd ?: []
        } else {
            expectedCmd += image.Config.Entrypoint ?: []
            expectedCmd += (config.Cmd ? config.Cmd : image.Config.Cmd ?: [])
        }

        if (currentCmd != expectedCmd) {
            return "Entrypoints and Cmd do not match: ${currentCmd} != ${expectedCmd}"
        }

        // -- Host Configuration

        // Binds
        if (current.HostConfig.Binds != config.HostConfig.Binds) {
            return "Binds do not match: ${current.HostConfig.Binds} != " +
                    "${config.HostConfig.Binds}"
        }

        // Port Bindings
        if (current.HostConfig.PortBindings != config.HostConfig.PortBindings) {
            return "Port Bindings do not match: ${current.HostConfig.PortBindings} != " +
                    "${config.HostConfig.PortBindings}"
        }

        // Links
        if (current.HostConfig.Links != config.HostConfig.Links) {
            return "Links do not match: ${current.HostConfig.Links} != " +
                    "${config.HostConfig.Links}"
        }

        // Privileged
        if (current.HostConfig.Privileged != config.HostConfig.Privileged) {
            return "Privileged does not match: ${current.HostConfig.Privileged} != " +
                    "${config.HostConfig.Privileged}"
        }

        // ExtraHosts
        if (current.HostConfig.ExtraHosts != config.HostConfig.ExtraHosts) {
            return "ExtraHosts do not match: ${current.HostConfig.ExtraHosts} != " +
                    "${config.HostConfig.ExtraHosts}"
        }

        return null
    }

    /**
     * Reload container
     */
    void reload(String reason) {
        // Method is public because it is mocked in tests

        logger.lifecycle("Container[${name}]: Reloading, ${reason}")
        inspect()

        if (running) {
            client.stop(id)
        }
        if (exists) {
            client.rm(id)
        }
        create()
        start()
    }

    /**
     * Split a list of environment variables ("name=value") to a map
     *
     * @param list of environment variables
     * @return map
     */
    private static Map<String,String> splitEnv(Collection<String> env) {
        if (!env) {
            return [:]
        }

        env.collectEntries { e->
            def parts = e.tokenize("=")
            [parts[0], parts[1]]
        }
    }
}
