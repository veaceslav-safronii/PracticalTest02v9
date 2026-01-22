package ro.pub.cs.systems.eim.practicaltest02v9

import android.util.Log
import java.io.IOException
import java.net.ServerSocket
import java.util.HashMap

class ServerThread(private val port: Int) : Thread() {
    private var serverSocket: ServerSocket? = null

    override fun run() {
        try {
            serverSocket = ServerSocket(port)
            Log.v("[SERVER THREAD]", "Server started on port $port...")


            while (!currentThread().isInterrupted) {
                Log.i("[SERVER THREAD]", "Waiting for client...")
                val socket = serverSocket!!.accept()
                Log.i(
                    "[SERVER THREAD]",
                    "Client connected with IP: " + socket.inetAddress + ":" + socket.port
                )

                val communicationThread = CommunicationThread(this, socket)
                communicationThread.start()
            }
        } catch (e: IOException) {
            Log.e("[SERVER THREAD]", "ServerSocket failed on port $port", e)
        }
    }

    fun stopThread() {
        interrupt()
        try {
            serverSocket?.close()
            Log.v("[SERVER THREAD]", "Server stopped...")
        } catch (e: IOException) {
            Log.e("[SERVER THREAD]", "An exception has occurred: " + e.message)
        }
    }
}