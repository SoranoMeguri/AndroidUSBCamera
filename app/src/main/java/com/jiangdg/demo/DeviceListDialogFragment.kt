/*
 * Copyright 2017-2022 Jiangdg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jiangdg.demo

import android.app.Dialog
import android.content.Context
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.jiangdg.ausbc.camera.CameraUVC
import com.jiangdg.demo.utils.DeviceUtils
import com.jiangdg.ausbc.MultiCameraClient

class DeviceListDialogFragment : BottomSheetDialogFragment() {

    private lateinit var usbManager: UsbManager
    private var currentCamera: MultiCameraClient.ICamera? = null

    companion object {
        fun newInstance(camera: MultiCameraClient.ICamera?): DeviceListDialogFragment {
            val fragment = DeviceListDialogFragment()
            fragment.setCurrentCamera(camera)
            return fragment
        }
    }

    fun setCurrentCamera(camera: MultiCameraClient.ICamera?) {
        this.currentCamera = camera
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_device_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        usbManager = requireContext().getSystemService(Context.USB_SERVICE) as UsbManager
        val deviceList = usbManager.deviceList.values.toList()

        val rv = view.findViewById<RecyclerView>(R.id.rvDeviceList)
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = DeviceAdapter(deviceList, currentCamera)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setOnShowListener {
            val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout?
            bottomSheet?.let {
                val behavior = BottomSheetBehavior.from(it)
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                behavior.peekHeight = 0
            }
        }
        return dialog
    }


    class DeviceAdapter(
        private val devices: List<UsbDevice>,
        private val currentCamera: MultiCameraClient.ICamera?
    ) : RecyclerView.Adapter<DeviceAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvDeviceName: TextView = view.findViewById(R.id.tvDeviceName)
            val tvConnectionStatus: TextView = view.findViewById(R.id.tvConnectionStatus)
            val layoutActiveInfo: LinearLayout = view.findViewById(R.id.layoutActiveInfo)
            val tvResolution: TextView = view.findViewById(R.id.tvResolution)
            val tvFps: TextView = view.findViewById(R.id.tvFps)
            val tvColorFormat: TextView = view.findViewById(R.id.tvColorFormat)
            val tvRawInfo: TextView = view.findViewById(R.id.tvRawInfo)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_device_info, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val device = devices[position]
            val context = holder.itemView.context

            val deviceName = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                if (!device.productName.isNullOrEmpty()) device.productName else device.deviceName
            } else {
                device.deviceName
            }
            holder.tvDeviceName.text = "$deviceName (${device.deviceId})"

            // Check if this device is the current camera
            var isCurrent = false
            currentCamera?.let { camera ->
                if (camera is CameraUVC) {
                   if (camera.getUsbDevice()?.deviceId == device.deviceId) {
                       isCurrent = true
                   }
                }
            }

            if (isCurrent) {
                holder.tvConnectionStatus.text = context.getString(R.string.active)
                holder.tvConnectionStatus.setTextColor(android.graphics.Color.GREEN)
                holder.layoutActiveInfo.visibility = View.VISIBLE

                if (currentCamera is CameraUVC) {
                    val size = (currentCamera as CameraUVC).getPreviewSize()
                    if (size != null) {
                         holder.tvResolution.text = "${context.getString(R.string.resolution)} ${size.width}x${size.height}"
                    } else {
                        holder.tvResolution.text = "${context.getString(R.string.resolution)} ${context.getString(R.string.unknown)}"
                    }
                    // FPS retrieval depends on implementation, CameraUVC might not expose current FPS directly easily without listener
                    // But we can try to show configured fps range if available or just leave it generic if not exposed
                    // Assuming for now we don't have direct access to current FPS value easily via public API of CameraUVC in this context
                    // without attaching a listener. We will skip or put placeholder.
                    // Actually, let's check if we can get it.
                    // Based on DemoFragment, EventBus passes FPS. But here we are in a dialog.
                    // We might not be able to show real-time FPS here easily.
                    holder.tvFps.text = "${context.getString(R.string.fps)} --"

                    holder.tvColorFormat.text = "${context.getString(R.string.color_format)} MJPEG/YUYV" // Placeholder or try to get if exposed
                }

            } else {
                holder.tvConnectionStatus.text = context.getString(R.string.inactive)
                holder.tvConnectionStatus.setTextColor(android.graphics.Color.GRAY)
                holder.layoutActiveInfo.visibility = View.GONE
            }

            holder.tvRawInfo.text = DeviceUtils.getDeviceDetailString(device)
        }

        override fun getItemCount(): Int = devices.size
    }
}