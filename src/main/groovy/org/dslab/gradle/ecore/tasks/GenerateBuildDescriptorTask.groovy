package org.dslab.gradle.ecore.tasks

import groovy.text.XmlTemplateEngine
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

/**
 * @author Adrian Rumpold (a.rumpold@ds-lab.org)
 */
class GenerateBuildDescriptorTask extends DefaultTask {
    @OutputFile
    final RegularFileProperty descriptor = newOutputFile()

    @TaskAction
    def generateDescriptor() {
        logger.info("----- generateDescriptor() -----")
        logger.info("descriptor: {}", descriptor)

        descriptor.asFile.get().withWriter {
            def pkg = [
                    nsUri   : "http://www.ds-lab.org/t3/metamodel/eda",
                    name    : "org.dslab.t3.metamodel.emf",
                    nsPrefix: "EDA"
            ]

            // TODO: Automatically discover pairs of Ecore models and genmodel
            def model = [
                    ecore   : 'eda/eda.ecore',
                    genmodel: 'eda/eda.genmodel'
            ]

            URL template = getClass().getResource("/templates/build.xml")
            def engine = new XmlTemplateEngine().createTemplate(template)

            def binding = [
                    package: pkg,
                    project: project,
                    model  : model
            ]
            def rendered = engine.make(binding)
            it.write(rendered)
        }
    }
}
