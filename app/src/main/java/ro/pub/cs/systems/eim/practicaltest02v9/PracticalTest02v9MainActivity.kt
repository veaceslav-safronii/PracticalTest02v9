package ro.pub.cs.systems.eim.practicaltest02v9

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class PracticalTest02v9MainActivity : AppCompatActivity() {
    private lateinit var serverPortEditText: EditText
    private lateinit var clientWordEditText: EditText
    private lateinit var clientLettersEditText: EditText
    private lateinit var urlEditText: EditText
    private lateinit var serverResponseTextView: TextView
    private lateinit var connectButton: Button
    private lateinit var requestButton: Button

    private var serverThread : ServerThread? = null

    public val api : String = "http://www.anagramica.com/all/:"




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_practical_test02v9_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        serverPortEditText = findViewById(R.id.server_port_edit_text)
        clientWordEditText = findViewById(R.id.client_word_edit_text)
        clientLettersEditText = findViewById(R.id.client_letters_edit_text)
        serverResponseTextView = findViewById(R.id.server_response_text_view)
        connectButton = findViewById(R.id.connect_button)
        requestButton = findViewById(R.id.request_button)

        connectButton.setOnClickListener {
            val serverPort = serverPortEditText.text.toString()
            if (serverPort.isEmpty()) {
                Toast.makeText(
                    applicationContext,
                    "[MAIN ACTIVITY] Server port should be filled!",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            serverThread = ServerThread(serverPort.toInt())
            serverThread?.start()
            Toast.makeText(
                applicationContext,
                "[MAIN ACTIVITY] Server starting on port $serverPort",
                Toast.LENGTH_SHORT
            ).show()
        }

        requestButton.setOnClickListener {
            val clientWord = clientWordEditText.text.toString()
            val clientLetters = clientLettersEditText.text.toString()
            val serverPort = serverPortEditText.text.toString()

            if (serverThread == null || !serverThread!!.isAlive) {
                Toast.makeText(
                    applicationContext,
                    "[MAIN ACTIVITY] There is no server to connect to!",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener

            }

            if (clientWord.isEmpty() || clientLetters.isEmpty()) {
                Toast.makeText(
                    applicationContext,
                    "[MAIN ACTIVITY] Address, port and URL should be filled!",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val clientThread = ClientThread(
                serverPort.toInt(),
                clientWord,
                clientLetters.toInt(),
                serverResponseTextView
            )
            clientThread.start()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serverThread?.stopThread()
    }
}