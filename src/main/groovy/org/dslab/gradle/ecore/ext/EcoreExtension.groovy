package org.dslab.gradle.ecore.ext

import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty

/**
 * @author Adrian Rumpold (a.rumpold@ds-lab.org)
 */
class EcoreExtension {
    public static final String DEFAULT_BUILD_SCRIPT = 'build.xml'
    public static final String DEFAULT_MODEL_PATH = 'model'
    public static final String DEFAULT_OUTPUT_PATH = 'src-gen'

    final RegularFileProperty eclipsePath
    final RegularFileProperty buildScript
    final DirectoryProperty modelPath
    final DirectoryProperty outputDir

    final Project project

    EcoreExtension(Project project) {
        this.project = project

        this.eclipsePath = project.layout.fileProperty()

        this.buildScript = project.layout.fileProperty()
        buildScript.set(project.file(DEFAULT_BUILD_SCRIPT))

        this.modelPath = project.layout.directoryProperty()
        modelPath.set(project.file(DEFAULT_MODEL_PATH))

        this.outputDir = project.layout.directoryProperty()
        outputDir.set(project.file(DEFAULT_OUTPUT_PATH))

        project.logger.info("------- Task configuration --------")
        project.logger.info("eclipsePath: {}", eclipsePath)
        project.logger.info("buildScript: {}", buildScript)
        project.logger.info("modelPath: {}", modelPath)
        project.logger.info("outputDir: {}", outputDir)
    }
}
