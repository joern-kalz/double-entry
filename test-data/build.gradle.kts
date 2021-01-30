import java.time.LocalDate
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE

tasks.register("build") {
    doLast {
        val output = File(projectDir, "src/backup.template.json").readText()
            .replace(Regex("\\{(\\d+) months ago\\}")) {
                val monthsAgo = it.groupValues[1].toLong()
                LocalDate.now().minusMonths(monthsAgo).format(ISO_LOCAL_DATE)
            }
            .replace(Regex("\\{(\\d+) days ago\\}")) {
                val daysAgo = it.groupValues[1].toLong()
                LocalDate.now().minusDays(daysAgo).format(ISO_LOCAL_DATE)
            }

        mkdir(buildDir)
        val file = File(buildDir, "backup.json")
        file.createNewFile()
        file.writeText(output)
    }
}