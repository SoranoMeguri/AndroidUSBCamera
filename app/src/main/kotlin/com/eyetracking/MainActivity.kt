package com.eyetracking

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.eyetracking.camera.FrameCallback
import com.eyetracking.camera.USBCameraManager
import com.eyetracking.server.MJPEGServer

class MainActivity : AppCompatActivity() {
    private lateinit var cameraManager: USBCameraManager
    private lateinit var mjpegServer: MJPEGServer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        cameraManager = USBCameraManager(this)
        mjpegServer = MJPEGServer(port = 8888)

        cameraManager.setFrameCallback(object : FrameCallback {
            override fun onFrameReceived(jpegData: ByteArray, width: Int, height: Int, timestamp: Long) {
                mjpegServer.publishFrame(jpegData)
            }
        })

        cameraManager.initialize()
        mjpegServer.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        mjpegServer.stop()
        cameraManager.destroy()
    }
}
