package com.example.bussolasensores

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.BatteryManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.ImageView

class MainActivity : AppCompatActivity() {

    private lateinit var bussola : Sensor
    private lateinit var acelerometro : Sensor
    private lateinit var sensorManager: SensorManager
    private lateinit var listener: SensorEventListener

    private var ultimoGrau = 0f
    private var vlrsBussola = FloatArray(3)
    private var vlrsGravidade = FloatArray(3)
    private var angulosDeOrientacao = FloatArray(3)
    private var matrixDeRotacao = FloatArray(9)



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var imageView : ImageView = findViewById(R.id.imageView)

        /** Listando os sensores **/
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val sensores : List<Sensor> = sensorManager.getSensorList(Sensor.TYPE_ALL)
        sensores.forEach{ sensor ->
            Log.i("SENSORES", sensor.toString())
        }

        /** Pegar um sensor específico **/
        acelerometro = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        bussola = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        if(bussola != null){
            Log.i("SENSORES", "O dispositivo possui bússola!")
        } else {
            Log.i("SENSORES", "O dispositivo não possui bússola!")
        }

        /** Criar o SensorEventListener **/

        listener = object : SensorEventListener{
            override fun onSensorChanged(event: SensorEvent?) {
                when(event?.sensor?.type){
                    Sensor.TYPE_ACCELEROMETER -> {
                        vlrsGravidade = event.values.clone()
                        var x = event.values[0]
                        var y = event.values[1]
                        var z = event.values[2]
                        Log.i("SENSORES", "Sensor.TYPE_ACCELEROMETER -> x = $x, y = $y, z = $z")
                    }
                    Sensor.TYPE_MAGNETIC_FIELD -> {
                        vlrsBussola = event.values.clone()
                        //Log.i("SENSORES", "Sensor.TYPE_MAGNETIC_FIELD")

                    }
                }
                SensorManager.getRotationMatrix(matrixDeRotacao, null, vlrsGravidade, vlrsBussola)
                SensorManager.getOrientation(matrixDeRotacao, angulosDeOrientacao)

                val radiano: Float = angulosDeOrientacao[0]
                val grauAtual = (Math.toDegrees(radiano.toDouble()) + 360).toFloat() % 360

                var rotacionar = RotateAnimation(
                    ultimoGrau, -grauAtual,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f
                )
                rotacionar.duration = 250
                rotacionar.fillAfter = true

                imageView.startAnimation(rotacionar)
                ultimoGrau = -grauAtual
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            }
        }
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(listener, acelerometro, SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(listener, bussola, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(listener)
    }

}