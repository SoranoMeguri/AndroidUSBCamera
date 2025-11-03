package com.eyetracking.server

import io.ktor.http.ContentType
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.delay
import java.util.concurrent.atomic.AtomicReference

class MJPEGServer(private val port: Int = 8888) {
    private var server: ApplicationEngine? = null
    private val currentFrame: AtomicReference<ByteArray> = AtomicReference()

    fun start() {
        server = embeddedServer(Netty, port) {
            routing {
                get("/stream/camera1") {
                    call.respondMJPEGStream()
                }
                get("/health") {
                    call.respondText("OK", ContentType.Text.Plain)
                }
            }
        }.start(wait = false)
    }

    fun publishFrame(jpegData: ByteArray) {
        currentFrame.set(jpegData)
    }

    private suspend fun ApplicationCall.respondMJPEGStream() {
        response.headers.append("Content-Type", "multipart/x-mixed-replace; boundary=--BOUNDARY")
        response.headers.append("Connection", "keep-alive")
        response.headers.append("Cache-Control", "no-cache, no-store, must-revalidate")
        response.headers.append("Pragma", "no-cache")
        response.headers.append("Expires", "0")

        respondOutputStream {
            while (true) {
                val frame = currentFrame.get()
                if (frame != null) {
                    write("--BOUNDARY\r\n".toByteArray())
                    write("Content-Type: image/jpeg\r\n".toByteArray())
                    write("Content-Length: ${frame.size}\r\n\r\n".toByteArray())
                    write(frame)
                    write("\r\n".toByteArray())
                    flush()
                }
                delay(33) // ~30 FPS
            }
        }
    }

    fun stop() {
        server?.stop(1000, 5000)
    }
}
