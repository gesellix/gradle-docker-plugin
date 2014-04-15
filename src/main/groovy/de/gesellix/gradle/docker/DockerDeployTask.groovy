package de.gesellix.gradle.docker

import groovyx.net.http.HTTPBuilder
import groovyx.net.http.HttpResponseDecorator
import org.codehaus.groovy.runtime.MethodClosure
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.mortbay.util.ajax.JSON
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

    def responseHandler = new ChunkedResponseHandler()
    def client = new HTTPBuilder("http://$hostname:$port/")
    client.handler.'200' = new MethodClosure(responseHandler, "handleResponse")
    client.post([path : "/images/create",
                 query: [fromImage: imageName]])
    logger.info("${responseHandler.lastResponseDetail}")
  }

  static class ChunkedResponseHandler {

    def completeResponse = ""

    def handleResponse(HttpResponseDecorator response) {
      new InputStreamReader(response.entity.content).each { chunk ->
        logger.debug("received chunk: '${chunk}'")
        completeResponse += chunk
      }
    }

    def getLastResponseDetail() {
      logger.debug("find last detail in: '${completeResponse}'")
      def lastResponseDetail = completeResponse.substring(completeResponse.lastIndexOf("}{") + 1)
      return JSON.parse(lastResponseDetail)
    }
  }
}
