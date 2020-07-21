package de.stjj.backend.utils

import de.stjj.backend.models.UploadedFile
import java.io.File
import java.nio.file.Paths

const val DATA_DIR_ENV_VAR = "DATA_DIR"

fun getFileForUploadedFile(uploadedFile: UploadedFile): File = Paths.get(System.getenv(DATA_DIR_ENV_VAR), "uploads", uploadedFile.id.value + (uploadedFile.mimeType?.extension ?: "")).toFile()
fun getUserImageFile(id: Int): File = Paths.get(System.getenv(DATA_DIR_ENV_VAR), "user-images", id.toString()).toFile()
