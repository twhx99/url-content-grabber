import me.tongfei.progressbar.ProgressBar
import me.tongfei.progressbar.ProgressBarBuilder
import me.tongfei.progressbar.ProgressBarStyle
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.Options
import org.apache.tika.detect.DefaultDetector
import org.apache.tika.metadata.Metadata
import org.apache.tika.mime.MediaType
import java.io.*
import java.net.URL
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates


var urlToFetch by Delegates.notNull<URL>()
var timesToFetch by Delegates.notNull<Int>()
var timeToSleep by Delegates.notNull<Long>()

val folder = createFolder()
var exceptionCount = 0
val exceptionFile = File(folder, "exceptions.log")

fun main(args: Array<String>) {
    setProperties(args)
    for (fetch in getProgressBarIterable()) {
        try {
            writeFile(fetch, urlToFetch.openStream())
        } catch (ex: IOException) {
            handle(fetch, ex)
        }
        TimeUnit.MILLISECONDS.sleep(timeToSleep)
    }
    println("Done! Fetches failed: $exceptionCount")
    if (exceptionCount > 0) {
        println("The exceptions are logged in: $exceptionFile")
    }
}

fun writeFile(fetch: Int, stream: InputStream) {
    val (detectStream, writeStream) = stream.duplicate()
    val mediaType = detectMediaType(detectStream)
    generateFileName(fetch, mediaType).writeBytes(writeStream.readAllBytes())
}

fun generateFileName(fetch: Int, mediaType: MediaType) = when (FileType.detect(mediaType)) {
    FileType.Application -> File(folder, "fetch-$fetch.${mediaType.subtype}")
    else -> File(folder, "${mediaType.type}-$fetch.${mediaType.subtype}")
}


fun InputStream.duplicate(): Pair<InputStream, InputStream> {
    val baos = ByteArrayOutputStream()
    this.transferTo(baos)
    val firstClone: InputStream = ByteArrayInputStream(baos.toByteArray())
    val secondClone: InputStream = ByteArrayInputStream(baos.toByteArray())
    return Pair(firstClone, secondClone)
}

@Throws(IOException::class)
fun detectMediaType(stream: InputStream): MediaType = DefaultDetector().detect(stream, Metadata())

fun createFolder(): File {
    val timestamp: String = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)
    return File("results-$timestamp/").apply { mkdirs() }
}

fun getProgressBarIterable(): Iterable<Int> {
    val progressBarBuilder = ProgressBarBuilder().setTaskName("Fetching Data").setStyle(ProgressBarStyle.ASCII)
    return ProgressBar.wrap((1..timesToFetch).toList(), progressBarBuilder)
}

fun handle(fetch: Int, ex: Exception) {
    exceptionCount += 1
    val elements: Array<StackTraceElement> = ex.stackTrace
    exceptionFile.appendText(">> Fetch $fetch failed!")
    for (element in elements) {
        element.apply { exceptionFile.appendText("$fileName:$lineNumber>> $methodName()") }
    }
    exceptionFile.appendText("\n")
}

fun setProperties(args: Array<String>) {
    val cmd = DefaultParser().parse(getOptions(), args)
    urlToFetch = URL(cmd.getOptionValue("url"))
    timesToFetch = cmd.getOptionValue("times", "1").toInt()
    timeToSleep = cmd.getOptionValue("tts", "100").toLong()
}

fun getOptions(): Options {
    val options = Options()
    options.addRequiredOption("u", "url", true, "URL of JSON API Endpoint")
    options.addOption("t", "times", true, "Times to fetch the API")
    options.addOption("s", "tts", true, "Time to Sleep between Requests in Milliseconds")
    return options
}

enum class FileType(val mediaTypePrefix: String) {
    Application("application/"),
    Audio("audio/"),
    Image("image/"),
    Text("text/"),
    Video("video/");

    companion object {
        fun detect(mediaType: MediaType): FileType? {
            return values().firstOrNull { mediaType.toString().startsWith(it.mediaTypePrefix) }
        }
    }

}
