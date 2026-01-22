package ro.pub.cs.systems.eim.practicaltest02v9

import android.util.Log
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.HttpURLConnection
import java.net.Socket
import java.net.URL

class CommunicationThread(
    private val serverThread: ServerThread,
    private val socket: Socket
) : Thread() {

    override fun run() {
        var connection: HttpURLConnection? = null

        try {
            val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
            val printWriter = PrintWriter(socket.getOutputStream(), true)

            val raw = reader.readLine()
            Log.d("[COMMUNICATION THREAD]", "Received raw: $raw")

            if (raw.isNullOrBlank()) {
                printWriter.println("ERROR: empty request")
                return
            }

            val parts = raw.split(",")
            if (parts.size < 2) {
                printWriter.println("ERROR: expected format word,minLetters")
                return
            }

            val word = parts[0].trim()
            val letters = parts[1].trim().toIntOrNull()
            if (word.isEmpty() || letters == null) {
                printWriter.println("ERROR: invalid word or minLetters")
                return
            }

            val url = "http://www.anagramica.com/all/$word"
            connection = (URL(url).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 5000
                readTimeout = 5000
            }

            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            if (stream == null) {
                printWriter.println("ERROR: HTTP $code")
                return
            }

            val response = StringBuilder()
            BufferedReader(InputStreamReader(stream)).use { responseReader ->
                var line: String?
                while (responseReader.readLine().also { line = it } != null) {
                    response.append(line)
                }
            }

            if (code !in 200..299) {
                printWriter.println("ERROR: HTTP $code ${response.toString()}")
                return
            }

            val jsonResponse = JSONObject(response.toString())
            val allAnagrams = jsonResponse.optJSONArray("all")
            if (allAnagrams == null) {
                printWriter.println("ERROR: missing field 'all'")
                return
            }

            val filtered = mutableListOf<String>()
            for (i in 0 until allAnagrams.length()) {
                val anagram = allAnagrams.optString(i, "")
                if (anagram.length >= letters) filtered.add(anagram)
            }

            val result = if (filtered.isEmpty()) "No results" else filtered.joinToString(",")
            printWriter.println(result)
            Log.i("[COMMUNICATION THREAD]", "Sent to client: $result")

        } catch (e: Exception) {
            Log.e("[COMMUNICATION THREAD]", "Exception: ${e.message}", e)
            try {
                PrintWriter(socket.getOutputStream(), true).println("ERROR: ${e.message}")
            } catch (_: Exception) { }
        } finally {
            try {
                connection?.disconnect()
            } catch (_: Exception) { }
            try {
                socket.close()
                Log.i("[COMMUNICATION THREAD]", "Connection closed")
            } catch (_: Exception) { }
        }
    }
}
