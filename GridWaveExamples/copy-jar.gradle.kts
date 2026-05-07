tasks.register("copyJar") {
    val targetDirs = listOf(
        rootProject.file("run/mods"),
        rootProject.file("build/libs"),
    )

    doFirst {
        val libsDir = layout.buildDirectory.dir("libs").get().asFile
        val jars = libsDir.listFiles { it.extension == "jar" }?.toList() ?: emptyList()
        val newest = jars.maxByOrNull { it.lastModified() }

        // Delete old GridWaveExamples jars
        jars.forEach { if (it != newest && it.name.contains(project.name)) it.delete() }

        // Clean target directories
        targetDirs.forEach { dir ->
            if (dir.exists()) {
                dir.listFiles()?.forEach { f -> if (f.name.contains(project.name)) f.delete() }
            } else dir.mkdirs()
        }

        // Copy jars to all target directories
        jars.forEach { jar ->
            targetDirs.forEach { dir ->
                copy {
                    from(jar)
                    into(dir)
                }
            }
        }
    }
}

tasks.named("jar") {
    finalizedBy("copyJar")  // run copyJar automatically after jar
}