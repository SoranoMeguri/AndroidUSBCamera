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
package com.jiangdg.demo.utils

import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbEndpoint
import android.hardware.usb.UsbInterface
import android.os.Build

/**
 * USB Device Info Utilities
 */
object DeviceUtils {

    fun getDeviceDetailString(device: UsbDevice): String {
        val sb = StringBuilder()

        sb.append("Device Name: ${device.deviceName}\n")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            sb.append("Product Name: ${device.productName}\n")
            sb.append("Manufacturer Name: ${device.manufacturerName}\n")
            sb.append("Serial Number: ${device.serialNumber}\n")
        }
        sb.append("Device ID: ${device.deviceId}\n")
        sb.append("Vendor ID: ${device.vendorId} (0x${String.format("%04X", device.vendorId)})\n")
        sb.append("Product ID: ${device.productId} (0x${String.format("%04X", device.productId)})\n")
        sb.append("Class: ${device.deviceClass}\n")
        sb.append("Subclass: ${device.deviceSubclass}\n")
        sb.append("Protocol: ${device.deviceProtocol}\n")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            sb.append("Version: ${device.version}\n")
        }
        sb.append("Interface Count: ${device.interfaceCount}\n")

        sb.append("\n--- Interfaces ---\n")
        for (i in 0 until device.interfaceCount) {
            val intf = device.getInterface(i)
            sb.append("\nInterface #$i:\n")
            sb.append("  ID: ${intf.id}\n")
            sb.append("  Class: ${intf.interfaceClass} (${getInterfaceClassName(intf.interfaceClass)})\n")
            sb.append("  Subclass: ${intf.interfaceSubclass}\n")
            sb.append("  Protocol: ${intf.interfaceProtocol}\n")
            sb.append("  Name: ${intf.name}\n")
            sb.append("  Endpoint Count: ${intf.endpointCount}\n")

            for (j in 0 until intf.endpointCount) {
                val ep = intf.getEndpoint(j)
                sb.append("    Endpoint #$j:\n")
                sb.append("      Address: ${ep.address}\n")
                sb.append("      Number: ${ep.endpointNumber}\n")
                sb.append("      Direction: ${if (ep.direction == UsbConstants.USB_DIR_IN) "IN" else "OUT"}\n")
                sb.append("      Type: ${getEndpointTypeName(ep.type)}\n")
                sb.append("      Attributes: ${ep.attributes}\n")
                sb.append("      Max Packet Size: ${ep.maxPacketSize}\n")
                sb.append("      Interval: ${ep.interval}\n")
            }
        }

        return sb.toString()
    }

    private fun getInterfaceClassName(cls: Int): String {
        return when (cls) {
            UsbConstants.USB_CLASS_APP_SPEC -> "Application Specific"
            UsbConstants.USB_CLASS_AUDIO -> "Audio"
            UsbConstants.USB_CLASS_CDC_DATA -> "CDC Data"
            UsbConstants.USB_CLASS_COMM -> "Communications"
            UsbConstants.USB_CLASS_CONTENT_SEC -> "Content Security"
            UsbConstants.USB_CLASS_CSCID -> "Content Smart Card"
            UsbConstants.USB_CLASS_HID -> "HID"
            UsbConstants.USB_CLASS_HUB -> "Hub"
            UsbConstants.USB_CLASS_MASS_STORAGE -> "Mass Storage"
            UsbConstants.USB_CLASS_MISC -> "Miscellaneous"
            UsbConstants.USB_CLASS_PER_INTERFACE -> "Per Interface"
            UsbConstants.USB_CLASS_PHYSICAL -> "Physical"
            UsbConstants.USB_CLASS_PRINTER -> "Printer"
            UsbConstants.USB_CLASS_STILL_IMAGE -> "Still Image"
            UsbConstants.USB_CLASS_VENDOR_SPEC -> "Vendor Specific"
            UsbConstants.USB_CLASS_VIDEO -> "Video"
            UsbConstants.USB_CLASS_WIRELESS_CONTROLLER -> "Wireless Controller"
            else -> "Unknown($cls)"
        }
    }

    private fun getEndpointTypeName(type: Int): String {
        return when (type) {
            UsbConstants.USB_ENDPOINT_XFER_BULK -> "Bulk"
            UsbConstants.USB_ENDPOINT_XFER_CONTROL -> "Control"
            UsbConstants.USB_ENDPOINT_XFER_INT -> "Interrupt"
            UsbConstants.USB_ENDPOINT_XFER_ISOC -> "Isochronous"
            else -> "Unknown($type)"
        }
    }
}