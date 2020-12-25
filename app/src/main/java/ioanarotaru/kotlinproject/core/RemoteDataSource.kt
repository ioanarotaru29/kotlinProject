package ioanarotaru.kotlinproject.core

import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import ioanarotaru.kotlinproject.auth.data.AuthRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import okhttp3.*
import okio.ByteString
import org.json.JSONObject

object RemoteDataSource {
    val eventChannel = Channel<String>()

    init {
        val request = Request.Builder().url("ws://192.168.100.41:3000").build()
        val webSocket = OkHttpClient().newWebSocket(request, MyWebSocketListener())
    }

    private class MyWebSocketListener : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            Log.d("WebSocket", "onOpen")

            var obj = JSONObject(mapOf("type" to "authorization", "payload" to mapOf<String,String>(
                ("token" to Api.tokenInterceptor.token) as Pair<String, String>
            )))

            webSocket.send(obj.toString());
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            Log.d("WebSocket", "onMessage$text")
            runBlocking { eventChannel.send(text) }
        }

        override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
            Log.d("WebSocket", "onMessage bytes")
            output("Receiving bytes : " + bytes!!.hex())
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            Log.e("WebSocket", "onFailure", t)
            t.printStackTrace()
        }

        private fun output(txt: String) {
            Log.d("WebSocket", txt)
        }
    }
}