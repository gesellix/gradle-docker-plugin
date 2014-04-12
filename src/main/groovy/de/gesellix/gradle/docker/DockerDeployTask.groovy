package de.gesellix.gradle.docker

import groovyx.net.http.RESTClient
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class DockerDeployTask extends DefaultTask {

  private static Logger logger = LoggerFactory.getLogger(DockerDeployTask)

  DockerDeployTask() {
  }

  @TaskAction
  def deploy() {
    logger.info "running deploy..."

    def imageName = "scratch"
    def hostname = "172.17.42.1"
    def port = 4243

    def client = new RESTClient("http://$hostname:$port/")
    def response = client.post([path : "/images/create",
                                query: [fromImage: imageName]])
    if (!response.isSuccess()) {
      throw new RuntimeException("Failed to pull image '$imageName' on host '$hostname'.")
    }

//    logger.info("response: ${response.entity.content}")
    logger.info("response: ${response.responseData}")
  }
}
