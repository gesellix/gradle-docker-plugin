package de.gesellix.gradle.docker.worker

import de.gesellix.docker.client.builder.BuildContextBuilder
import groovy.util.logging.Slf4j
import org.gradle.workers.WorkAction

@Slf4j
abstract class BuildcontextArchiver implements WorkAction<BuildcontextArchiverWorkParameters> {

    @Override
    void execute() {
        File sourceDirectory = getParameters().sourceDirectory.getAsFile().get()
        File targetFile = getParameters().archivedTargetFile.getAsFile().get()
        log.info("archiving ${sourceDirectory} into ${targetFile}...")
        targetFile.parentFile.mkdirs()
        BuildContextBuilder.archiveTarFilesRecursively(sourceDirectory, targetFile)
        log.info("archiving finished")
    }
}
