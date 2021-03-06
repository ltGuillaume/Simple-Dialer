package com.simplemobiletools.dialer.helpers

import android.annotation.SuppressLint
import android.content.Context
import android.provider.CallLog.Calls
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.PERMISSION_READ_CALL_LOG
import com.simplemobiletools.commons.helpers.ensureBackgroundThread
import com.simplemobiletools.dialer.models.RecentCall

class RecentsHelper(private val context: Context) {
    @SuppressLint("MissingPermission")
    fun getRecentCalls(callback: (ArrayList<RecentCall>) -> Unit) {
        ensureBackgroundThread {
            val recentCalls = ArrayList<RecentCall>()
            if (!context.hasPermission(PERMISSION_READ_CALL_LOG)) {
                callback(recentCalls)
                return@ensureBackgroundThread
            }

            val uri = Calls.CONTENT_URI
            val projection = arrayOf(
                Calls._ID,
                Calls.NUMBER,
                Calls.CACHED_NAME,
                Calls.CACHED_PHOTO_URI,
                Calls.DATE,
                Calls.DURATION,
                Calls.TYPE
            )

            val sortOrder = "${Calls._ID} DESC LIMIT 100"

            var previousRecentCallFrom = ""
            context.queryCursor(uri, projection, sortOrder = sortOrder) { cursor ->
                val id = cursor.getIntValue(Calls._ID)
                val number = cursor.getStringValue(Calls.NUMBER)
                val name = cursor.getStringValue(Calls.CACHED_NAME) ?: number
                val photoUri = cursor.getStringValue(Calls.CACHED_PHOTO_URI) ?: ""
                val startTS = (cursor.getLongValue(Calls.DATE) / 1000L).toInt()
                val duration = cursor.getIntValue(Calls.DURATION)
                val type = cursor.getIntValue(Calls.TYPE)
                val recentCall = RecentCall(id, number, name, photoUri, startTS, duration, type)

                if ("$number$name" != previousRecentCallFrom) {
                    recentCalls.add(recentCall)
                }

                previousRecentCallFrom = "$number$name"
            }

            callback(recentCalls)
        }
    }
}
