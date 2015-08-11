package de.gesellix.gradle.docker.tasks

import de.gesellix.gradle.docker.models.DockerContainer
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

class DockerContainerTask extends DockerTask {
  private static Logger logger = Logging.getLogger(DockerContainerTask)

  enum State {

    PRESENT         // Ensures container exists
    , STARTED       // Ensures container exists and is running
    , RELOADED      // Ensures container is running and restarted if any configuration
                    //  is out of date
    , RESTARTED     // Ensures container is unconditionally restarted (or started)
    , STOPPED       // Ensures container is stopped
    , ABSENT        // Ensures container is stopped and removed
  }

  @Input
  State targetState = State.STARTED

  @Input
  String image
  @Input
  String containerName
  @Input
  @Optional
  String tag = ""

  /**
   * Publish a container᾿s port to the host
   * format: ip:hostPort:containerPort | ip::containerPort | hostPort:containerPort |
   * containerPort
   */
  @Input
  @Optional
  def ports = []

  /**
   * A list of environment variables in the form of VAR=value
   */
  @Input
  @Optional
  List<String> env = []

  /**
   * A list of links for the container. Each link entry should be of of the form
   * "container_name:alias".
   */
  @Input
  @Optional
  def links = [:]

  /**
   * Create a bind mount with: [host-dir]:[container-dir]:[rw|ro].
   * If "container-dir" is missing, then docker creates a new volume.
   */
  @Input
  @Optional
  def volumes = null

  /**
   * Gives the container full access to the host. Specified as a boolean value.
   */
  @Input
  @Optional
  boolean privileged = false

  /**
   * A list of blocking health checks to run against the container.
   *
   * [
   *   containerPort: container port, i.e. "80" or "80/tcp",
   *   type: http or tcp (default is tcp),
   *   timeout: timeout in seconds (default is 5)
   *   retries: number of retries (default is 15 tries)
   *   interval: interval between retries in seconds (default is 2)
   *   path: path element for http request
   * ]
   */
  @Input
  @Optional
  def healthChecks = []

  /**
   * A list of ["hostname:IP"] mappings to be added to the container's /etc/hosts file.
   */
  @Input
  @Optional
  def extraHosts = []

  boolean changed = false
  DockerContainer container

  DockerContainerTask() {
    description = "Manage the lifecycle of docker container"
    group = "Docker"

    //outputs.upToDateWhen { t -> t.changed == false }
  }

  @TaskAction
  def run() {
    def config = [:]

    config.Image = image
    config.HostConfig = [:]

    if (env) {
      config.Env = env
    }

    if (links) {
      config.HostConfig.Links = links
    }

    config.HostConfig.Privileged = privileged

    if(ports) {
      config.ExposedPorts = [:]
      config.HostConfig.PortBindings = config.HostConfig.PortBindings ?: [:]
      ports.each { p ->
        ArrayList<String> parts = p.tokenize(":")
        String container_port = parts.get(-1).toString()
        if(container_port.indexOf("/") == -1) {
          container_port = container_port + "/tcp"
        }

        if (config.HostConfig.PortBindings[container_port] == null) {
          config.HostConfig.PortBindings[container_port] = []
        }

        if(parts.size() == 1) {
          config.HostConfig.PortBindings[container_port] << [HostIp: "0.0.0.0", HostPort: ""]
        } else if (parts.size() == 2) {
          config.HostConfig.PortBindings[container_port] << [HostIp: "0.0.0.0",
                                                             HostPort: parts.get(0).toString()]
        } else if (parts.size() == 3) {
          if (parts.get(1)) {
            config.HostConfig.PortBindings[container_port] << [HostIp: parts.get(1).toString(),
                                                               HostPort: parts.get(0).toString()]
          } else {
            config.HostConfig.PortBindings[container_port] << [HostIp: "0.0.0.0",
                                                               HostPort: parts.get(0).toString()]
          }
        }
      }
    }

    if(volumes) {
      config.Volumes = [:]
      config.HostConfig.Binds = []
      volumes.each { v ->
        def parts = v.tokenize(":")

        if (parts.size() == 2) {
          config.Volumes[(String)parts.get(1)] = {}
          config.HostConfig.Binds << v
        } else if (parts.size() == 3) {
          if (!(parts.get(2) in ['ro', 'rw'])) {
            throw new IllegalArgumentException('bind needs to be either "ro" or "rw"')
          }
          config.Volumes[(String)parts.get(1)] = {}
          config.HostConfig.Binds << v
        } else if (parts.size() == 1) {
          config.Volumes[(String)parts.get(0)] = {}
        } else {
          throw new IllegalArgumentException("Malformed volume string: ${v}")
        }
      }
    }

    if (extraHosts) {
      config.HostConfig.ExtraHosts = extraHosts
    }

    container = new DockerContainer(getDockerClient(), containerName, image, config)

    switch(targetState) {
      case State.PRESENT:
        changed = container.present()
        break
      case State.STARTED:
        changed = container.started()
        doHealthChecks()
        break
      case State.RELOADED:
        changed = container.reloaded()
        doHealthChecks()
        break
      case State.RESTARTED:
        changed = container.restarted()
        doHealthChecks()
        break
      case State.STOPPED:
        changed = container.stopped()
        break
      case State.ABSENT:
        changed = container.absent()
        break
    }

    return !changed
  }

  def doHealthChecks() {
    if (healthChecks.size() == 0)
      return false

    String containerHost = new URI((String)dockerHost).getHost()

    logger.info "Running Health checks on host ${containerHost}"

    def current = container.inspect()

    if (!container.running) {
      throw new IllegalStateException("HealthCheck: Container is not running.")
    }

    healthChecks.each { h ->
      if (!h.containerPort)
        throw new IllegalArgumentException("ContainerPort is required.")

      if (h.containerPort.indexOf("/") == -1)
        h.containerPort = h.containerPort + "/tcp"

      h.type = h.type ?: "tcp"
      h.timeout = h.timeout ?: 5
      h.retries = h.retries ?: 15
      h.interval = h.interval ?: 2

      String[] p = current.HostConfig.PortBindings[(String)h.containerPort]

      if (!p)
        throw new IllegalArgumentException("Port \"${h.containerPort}\" is not bound to host.")

      int counter = 0

      switch(h.type) {
        case "tcp":
          def address = new InetSocketAddress(containerHost,
                                              (int)(p[0].HostPort.toInteger()))
          logger.info "HealthCheck/tcp: Connecting ${address}, (timeout ${h.timeout}, "+
                      "retries ${h.retries}, interval ${h.interval})"
          while(counter < (int)h.retries) {
            try {
              def s = new Socket()
              s.connect(address, (int)h.timeout)
              logger.info "HealthCheck/tcp: Container is healthy."
              s.close()
              return true
            } catch(SocketTimeoutException ignored) {
            } catch(IOException ignored) {
            }

            counter = counter + 1
            sleep ((int)h.interval*1000)
            logger.info "Attempt #${counter+1}"
          }

          break
        case "http":
          def url = new URL("http", containerHost,
                            (int)p[0].HostPort.toInteger(), (String)h.path)
          logger.info "HealthCheck/http: Connecting ${url}, (timeout ${h.timeout}, "+
                      "retries ${h.retries}, interval ${h.interval})"
          while(counter < (int)h.retries) {
            URLConnection s = null
            try {
              s = url.openConnection()
              s.setConnectTimeout(((int)h.timeout*1000))
              s.connect()
              if(((int)s.getResponseCode()/100) == 2) {
                logger.info "HealthCheck/http: Container is healthy: ${s.getResponseCode()}"
                return true
              }
            } catch(SocketTimeoutException ignored) {
            } catch(IOException ignored) {
            } finally {
              if (s)
                s.disconnect()
            }

            counter = counter + 1
            sleep ((int)h.interval*1000)
            logger.info "Attempt #${counter+1}"
          }

          break
        default:
          throw new IllegalArgumentException("Unsupported healthcheck type: ${h.type}")
      }

      throw new IllegalStateException("HealthCheck: Container not healthy.")
    }
  }
}
