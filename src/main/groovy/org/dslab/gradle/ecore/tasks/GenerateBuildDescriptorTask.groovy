/*
 * Copyright 2018 Adriano
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
