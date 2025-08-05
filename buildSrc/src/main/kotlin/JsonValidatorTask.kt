@file:Suppress("UnstableApiUsage")

import com.github.erosb.jsonsKema.FormatValidationPolicy
import com.github.erosb.jsonsKema.ItemsValidationFailure
import com.github.erosb.jsonsKema.JsonParser
import com.github.erosb.jsonsKema.SchemaLoader
import com.github.erosb.jsonsKema.ValidationFailure
import com.github.erosb.jsonsKema.Validator
import com.github.erosb.jsonsKema.ValidatorConfig
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.problems.ProblemGroup
import org.gradle.api.problems.ProblemId
import org.gradle.api.problems.ProblemReporter
import org.gradle.api.problems.Problems
import org.gradle.api.problems.Severity
import org.gradle.api.tasks.InputFile
import javax.inject.Inject

abstract class JsonValidatorTask @Inject constructor(
    problems: Problems
) : DefaultTask() {
    init {
        group = "verification"
        description = "Validates the JSON files against the schema."
    }

    private val problemGroup = ProblemGroup.create("json-validation", "JSON Validation")

    private val problemReporter: ProblemReporter = problems.reporter

    @get:InputFile
    abstract val inputFile: RegularFileProperty

    @get:InputFile
    abstract val schemaFile: RegularFileProperty

    @org.gradle.api.tasks.TaskAction
    fun validate() {
        val jsonFile = inputFile.get().asFile
        val schemaFile = schemaFile.get().asFile

        logger.lifecycle("Validating JSON file in ${jsonFile.name} against schema ${schemaFile.name}")
        val jsonValue = JsonParser(jsonFile.inputStream()).parse()

        val schema = SchemaLoader(schemaFile.readText()).load()
        val validator = Validator.create(schema, ValidatorConfig(FormatValidationPolicy.ALWAYS))

        val failure = validator.validate(jsonValue)

        if (failure == null) {
            logger.lifecycle("Validation successful: ${jsonFile.name} is valid according to the schema.")
        } else {
            val rootFailures = buildList {
                val failureQueue = ArrayDeque<ValidationFailure>()
                failureQueue.add(failure)
                while (failureQueue.isNotEmpty()) {
                    val currentFailure = failureQueue.removeFirst()
                    if (currentFailure.causes.isEmpty()) {
                        add(currentFailure)
                    } else {
                        failureQueue.addAll(currentFailure.causes)
                    }
                }
            }

            val failureId = ProblemId.create("json-validation-failure", "JSON Validation Failure", problemGroup)

            rootFailures.forEach { failure ->
                problemReporter.report(failureId) {
                    severity(Severity.ERROR)
                    details(failure.message)
                    lineInFileLocation(
                        jsonFile.absolutePath,
                        failure.instance.location.lineNumber,
                        failure.instance.location.position
                    )
                }
            }
            throw RuntimeException(failure.toString())
        }
    }
}