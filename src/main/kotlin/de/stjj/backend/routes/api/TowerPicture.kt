package de.stjj.backend.routes.api

import de.stjj.backend.utils.dataDir
import de.stjj.backend.utils.towerPictureSecret
import io.jooby.Kooby
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

private val picturePath: Path = File(dataDir).resolve("./tower-picture").toPath()

fun Kooby.towerPictureRoutes() {
    post("/tower-picture/$towerPictureSecret") {
        val file = ctx.file("file")
        Files.move(file.path(), picturePath)
        file.destroy()
    }

    get("/tower-picture") {
        ctx.send(picturePath)
    }
}
