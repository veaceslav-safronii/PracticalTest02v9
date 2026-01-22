package ro.pub.cs.systems.eim.practicaltest02v9

import android.util.Log
import android.widget.TextView
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket

class ClientThread(
    private val port: Int,
    private val word: String,
    private val letters: Int,
    private val responseTextView: TextView
) : Thread() {
    override fun run() {
        try {
            val socket = Socket("localhost", port)
            val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
            val writer = PrintWriter(socket.getOutputStream(), true)

            writer.println("$word,$letters")

            val serverResponse = reader.readLine()

            responseTextView.post {
                responseTextView.text = serverResponse
            }

            socket.close()
            Log.d("[CLIENT THREAD]", "Socket closed")

        } catch (e: Exception) {
            Log.e("[CLIENT THREAD]", "An exception has occurred: " + e.message)
        }
    }
}
