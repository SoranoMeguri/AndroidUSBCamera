package com.eyetracking.camera

import android.content.Context
import android.hardware.usb.UsbDevice
import android.util.Log
import com.jiangdg.ausbc.MultiCameraClient
import com.jiangdg.ausbc.callback.IDeviceConnectCallBack
import com.jiangdg.ausbc.callback.IPreviewDataCallBack
import com.jiangdg.ausbc.camera.CameraUVC
import com.jiangdg.ausbc.camera.bean.CameraRequest
import com.jiangdg.usb.USBMonitor

interface FrameCallback {
    fun onFrameReceived(jpegData: ByteArray, width: Int, height: Int, timestamp: Long)
}

class USBCameraManager(private val context: Context) {
    private lateinit var multiCameraClient: MultiCameraClient
    private var cameraDevice: MultiCameraClient.ICamera? = null
    private var frameCallback: FrameCallback? = null

    companion object {
        private const val TAG = "USBCameraManager"
        private const val PREVIEW_WIDTH = 640
        private const val PREVIEW_HEIGHT = 480
    }

    fun initialize() {
        multiCameraClient = MultiCameraClient(context, deviceConnectCallBack)
        multiCameraClient.register()
    }

    private val deviceConnectCallBack = object : IDeviceConnectCallBack {
        override fun onAttachDev(device: UsbDevice?) {
            Log.d(TAG, "onAttachDev: device attached")
            device?.let { multiCameraClient.requestPermission(it) }
        }

        override fun onConnectDev(device: UsbDevice?, ctrlBlock: USBMonitor.UsbControlBlock?) {
            Log.d(TAG, "onConnectDev: device connected")
            device?.let {
                openCamera(it, ctrlBlock)
            }
        }

        override fun onDisConnectDec(device: UsbDevice?, ctrlBlock: USBMonitor.UsbControlBlock?) {
            Log.d(TAG, "onDisConnectDev: device disconnected")
            closeCamera()
        }

        override fun onDetachDec(device: UsbDevice?) {
            Log.d(TAG, "onDetachDec: device detached")
            closeCamera()
        }

        override fun onCancelDev(device: UsbDevice?) {
            Log.d(TAG, "onCancelDev: permission denied")
        }
    }

    fun openCamera(usbDevice: UsbDevice, ctrlBlock: USBMonitor.UsbControlBlock?) {
        if (cameraDevice != null) {
            closeCamera()
        }
        val camera = CameraUVC(context, usbDevice).apply {
            setUsbControlBlock(ctrlBlock)
        }
        camera.openCamera(null,
            CameraRequest.Builder()
                .setPreviewWidth(PREVIEW_WIDTH)
                .setPreviewHeight(PREVIEW_HEIGHT)
                .setPreviewFormat(CameraRequest.PreviewFormat.FORMAT_MJPEG)
                .create()
        )
        camera.addPreviewDataCallBack(previewDataCallBack)
        cameraDevice = camera
    }

    private val previewDataCallBack = object: IPreviewDataCallBack {
        override fun onPreviewData(data: ByteArray?, width: Int, height: Int, format: IPreviewDataCallBack.DataFormat) {
            if (format == IPreviewDataCallBack.DataFormat.JPEG && data != null) {
                frameCallback?.onFrameReceived(data, width, height, System.currentTimeMillis())
                Log.v(TAG, "Frame received, size: ${data.size}")
            }
        }
    }

    fun setFrameCallback(callback: FrameCallback) {
        frameCallback = callback
    }

    fun closeCamera() {
        cameraDevice?.closeCamera()
        cameraDevice = null
    }

    fun destroy() {
        closeCamera()
        multiCameraClient.unRegister()
        multiCameraClient.destroy()
    }
}
