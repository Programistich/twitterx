package twitterx.ai.google

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import twitterx.ai.api.AIProcessException
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Executor for running Python Gemini scripts as subprocess
 */
internal class PythonProcessExecutor(
    private val configuration: GoogleAIConfiguration
) {
    private val logger = LoggerFactory.getLogger(PythonProcessExecutor::class.java)
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
    }

    /**
     * Execute Python command and return parsed response
     */
    suspend fun execute(command: PythonCommand): Result<PythonResponse> = withContext(Dispatchers.IO) {
        try {
            val commandJson = json.encodeToString(command)

            val processBuilder = ProcessBuilder(
                configuration.pythonExecutable,
                configuration.scriptPath,
                commandJson
            )

            // Set working directory to project root
            processBuilder.directory(File("."))

            logger.debug("Executing Python command: {}", command.action)

            val process = processBuilder.start()

            // Wait for process completion with timeout
            val finished = process.waitFor(configuration.timeout, TimeUnit.MILLISECONDS)

            if (!finished) {
                process.destroyForcibly()
                return@withContext Result.failure(
                    AIProcessException("Python process timed out after ${configuration.timeout}ms")
                )
            }

            val result = PythonResult(
                exitCode = process.exitValue(),
                stdout = process.inputStream.bufferedReader().readText(),
                stderr = process.errorStream.bufferedReader().readText()
            )

            logger.debug("Python process completed with exit code: {}", result.exitCode)

            if (result.exitCode != 0) {
                logger.error("Python process failed with stderr: {}", result.stderr)
                return@withContext Result.failure(
                    AIProcessException("Python process failed with exit code ${result.exitCode}: ${result.stderr}")
                )
            }

            // Parse JSON response
            val response = try {
                json.decodeFromString<PythonResponse>(result.stdout)
            } catch (e: Exception) {
                logger.error("Failed to parse Python response: {}", result.stdout, e)
                return@withContext Result.failure(
                    AIProcessException("Failed to parse Python response: ${e.message}", e)
                )
            }

            if (!response.success) {
                return@withContext Result.failure(
                    mapPythonError(response.errorType, response.error ?: "Unknown error")
                )
            }

            Result.success(response)
        } catch (e: Exception) {
            logger.error("Error executing Python process", e)
            Result.failure(AIProcessException("Error executing Python process: ${e.message}", e))
        }
    }

    /**
     * Map Python error types to Kotlin exceptions
     */
    private fun mapPythonError(errorType: String?, errorMessage: String): Throwable {
        return when (errorType) {
            "AuthError" -> twitterx.ai.api.AIAuthException(errorMessage)
            "TimeoutError" -> twitterx.ai.api.AITimeoutException(errorMessage)
            "UsageLimitExceeded" -> twitterx.ai.api.AIUsageLimitException(errorMessage)
            "ModelInvalid" -> twitterx.ai.api.AIModelInvalidException(errorMessage)
            "TemporarilyBlocked" -> twitterx.ai.api.AITemporarilyBlockedException(errorMessage)
            "APIError" -> twitterx.ai.api.AIApiException(errorMessage)
            else -> twitterx.ai.api.AIApiException("$errorType: $errorMessage")
        }
    }
}
