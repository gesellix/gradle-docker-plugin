package de.gesellix.gradle.docker.worker

import de.gesellix.docker.client.builder.BuildContextBuilder
import groovy.util.logging.Slf4j

import javax.inject.Inject

@Slf4j
class BuildcontextArchiver implements Runnable {

    File sourceDirectory
    File targetFile

    @Inject
    BuildcontextArchiver(File sourceDirectory, File targetFile) {
        this.sourceDirectory = sourceDirectory
        this.targetFile = targetFile
    }

    @Override
    void run() {
        log.info("archiving ${sourceDirectory} into ${targetFile}...")
        targetFile.parentFile.mkdirs()
        BuildContextBuilder.archiveTarFilesRecursively(sourceDirectory, targetFile)
        log.info("archiving finished")
    }
}
