tasks.register("copyNodeEditor") {
    val targetDir = file("src/main/resources/Client/NodeEditor/Workspaces/HytaleGenerator Java")
    val sourceBase = file(System.getenv("APPDATA") + "/Hytale/install/release/package/game/latest/Client/NodeEditor/Workspaces/HytaleGenerator Java")
    val itemsToCopy = listOf("_Workspace.json", "GridWave")

    doFirst {
        // Delete existing content in target
        if (targetDir.exists()) {
            targetDir.listFiles()?.forEach { it.deleteRecursively() }
        } else {
            targetDir.mkdirs()
        }

        // Copy each item
        itemsToCopy.forEach { name ->
            val sourceItem = File(sourceBase, name)
            if (sourceItem.exists()) {
                copy {
                    from(sourceItem)
                    into(if (sourceItem.isDirectory) File(targetDir, name) else targetDir)
                }
            }
        }
    }
}

tasks.named("jar") {
    dependsOn("copyNodeEditor")  // run copyNodeEditor before jar
}