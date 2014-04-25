package de.gesellix.gradle.docker

import groovyx.net.http.HTTPBuilder
import groovyx.net.http.HttpResponseDecorator
import org.codehaus.groovy.runtime.MethodClosure
import org.mortbay.util.ajax.JSON
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class DockerClientImpl implements DockerClient {

  private static Logger logger = LoggerFactory.getLogger(DockerClientImpl)

  def hostname
  def port

  DockerClientImpl(hostname = "172.17.42.1", port = 4243) {
    this.hostname = hostname
    this.port = port
  }

  @Override
  def pull(imageName) {
    logger.info "pull image '${imageName}'..."

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
