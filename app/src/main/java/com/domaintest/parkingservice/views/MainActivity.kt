package com.domaintest.parkingservice.views

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.domaintest.parkingservice.domain.mqtt.MqttHelper
import com.domaintest.parkingservice.ui.theme.ParkingServiceTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    private var lastMessage: String = ""
    private lateinit var mMqttClient: MqttHelper
    // token: mqtt://holamundoestesi:Jf4poF137quTJJzj@holamundoestesi.cloud.shiftr.io
    private val topic_mqtt = "access"

    private val TAG = "MainActivity"


    private val msgSignIn = "vehicle in"
    private val msgSignOut = "vehicle out"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mMqttClient = MqttHelper(this)
        mMqttClient.getClientName()
        mMqttClient.connectBroker()
        setContent {
            ParkingServiceTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    mMqttClient.setOnMessageReceivedListener{
                        message ->
                        setMessage(message)
                    }

                    ParkingView(mqttClient = mMqttClient)
                }
            }
        }


    }

    private fun setMessage(message: String) {
        lastMessage = message
    }

    /* Cuando se cierre la app, desconectamos el cliente Mqtt*/
    override fun onDestroy() {
        super.onDestroy()
        mMqttClient.disconnect()
    }


    @Composable
    private fun ParkingView(mqttClient: MqttHelper) {

        var receivedMsg by remember { mutableStateOf("") }

        LaunchedEffect(mqttClient) {
            // Utilizamos LaunchedEffect para observar cambios en el MqttHelper
            while (true) {
                val newMessage = mqttClient.receiveMessage()
                receivedMsg = newMessage
                // Pausa para evitar un bucle infinito intensivo de CPU
                delay(1000)
                Log.d(TAG, "ParkingView: while: message: $newMessage")
            }
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Text(modifier = Modifier.padding(8.dp),
                text = "Parking Access Control", fontWeight = FontWeight.Bold,
                fontSize = 24.sp)

            Text(text = "Disponibles: ($receivedMsg)", fontWeight = FontWeight.Bold)

            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(PaddingValues(top = 50.dp, start = 20.dp, end = 20.dp)),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
                ) {
                // Button open barrier
                Button(modifier = Modifier.width(120.dp),
                    onClick = {
                        if(mqttClient.checkConnection()){
                            mqttClient.sendMessage(topic_mqtt, msgSignIn)
                        }
                }) {
                    Text(text = "Entrar")
            }

               Spacer(modifier = Modifier.padding(PaddingValues(start = 8.dp, end = 8.dp)))
                Button(modifier = Modifier.width(120.dp),
                    onClick = {
                        if(mqttClient.checkConnection()){
                            mqttClient.sendMessage(topic_mqtt, msgSignOut)
                        }
                }){
                    Text(text = "Salir")
                }
            }

        }
    }





}




