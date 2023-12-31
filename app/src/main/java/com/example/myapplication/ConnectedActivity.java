package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;
import java.util.UUID;


public class ConnectedActivity extends AppCompatActivity implements SensorEventListener{

    private final static float ACC = 30;
    public Button veneno;
    public Button hayHormigas;
    public Button salir;
    public Button humedad;
    public TextView txtEstado;
    private static String address = null;
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private ConnectedThread mConnectedThread;
    final int handlerState = 0;
    Handler bluetoothIn;
    private StringBuilder recDataString = new StringBuilder();
    private int notificationId = 1;
    private static final String CHANNEL_ID = "1000";
    private SensorManager accelerometer;


    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent=getIntent();
        Bundle extras=intent.getExtras();

        setContentView(R.layout.activity_connected);

        veneno=findViewById(R.id.tirar_ven);
        veneno.setOnClickListener(botonesListeners);

        hayHormigas = findViewById(R.id.hayAnts);
        hayHormigas.setOnClickListener(botonesListeners);

        salir = findViewById(R.id.salir);
        salir.setOnClickListener(botonesListeners);

        humedad = findViewById(R.id.hay_humedad);
        humedad.setOnClickListener(botonesListeners);

        txtEstado = findViewById(R.id.txtEstado);

        address= extras.getString("Direccion_Bluethoot");

        btAdapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        bluetoothIn = Handler_Msg_Hilo_Principal();

        accelerometer = (SensorManager) getSystemService(SENSOR_SERVICE);

        try
        {
            btSocket = createBluetoothSocket(device);
        }
        catch (IOException e)
        {
            showToast( "La creacción del Socket fallo");
        }
        try
        {
            btSocket.connect();
        }
        catch (IOException e)
        {
            try
            {
                btSocket.close();
            }
            catch (IOException e2)
            {
            }
        }
        mConnectedThread = new ConnectedThread(btSocket);
        mConnectedThread.start();
        mConnectedThread.write("I");

        registerSenser();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent event)
    {

        int sensorType = event.sensor.getType();

        float[] values = event.values;

        if (sensorType == Sensor.TYPE_ACCELEROMETER)
        {
            if ((Math.abs(values[0]) > ACC || Math.abs(values[1]) > ACC || Math.abs(values[2]) > ACC)) {
                showToast("Se esta sacudiendo el telefono");
                mConnectedThread.write("R");
            }
        }
    }

    @SuppressLint("NewApi")
    @Override
    protected void onStop() {
        super.onStop();
        mConnectedThread.write("E");
    }




    private void registerSenser()
    {
        accelerometer.registerListener(this, accelerometer.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void unregisterSenser()
    {
        accelerometer.unregisterListener(this);
    }


    private Handler Handler_Msg_Hilo_Principal ()
    {
        return new Handler() {
            @SuppressLint("HandlerLeak")
            public void handleMessage(android.os.Message msg)
            {
                if (msg.what == handlerState)
                {
                    String readMessage = (String) msg.obj;
                    showToast(readMessage);
                    recDataString.append(readMessage);
                    int endOfLineIndex = recDataString.indexOf("\r\n");
                    showNotification(readMessage);
                    processMessage(readMessage);

                    if (endOfLineIndex > 0)
                    {
                        String dataInPrint = recDataString.substring(0, endOfLineIndex);

                        recDataString.delete(0, recDataString.length());
                    }
                }
            }
        };

    }

    private void processMessage(String msg)  {
        if(msg.contains("enen")) {
            txtEstado.setText("Debe recargar el veneno");
        }
        else if(msg.contains("S"))
        {
            txtEstado.setText("Hay hormigas");
        }
        else if(msg.contains("N")) {
            txtEstado.setText("No hay hormigas");
        }
        else if(msg.contains("H")) {
            txtEstado.setText("Humedad alta");
        }
        else if(msg.contains("L")) {
            txtEstado.setText("Humedad baja");
        }
    }


    @SuppressLint("MissingPermission")
    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {

        return  device.createRfcommSocketToServiceRecord(BTMODULEUUID);
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    @SuppressLint("MissingPermission")
    private void showNotification(String data)
    {
        if(data.isEmpty())
            return;
        createChannel();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle("Evento de Arduino")
                .setContentText("Mensaje leido: " + data)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(notificationId++, builder.build());
    }

    private void createChannel()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = CHANNEL_ID;
            String description = CHANNEL_ID;
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    //H hay Hormigas? --> S/N
    //R tirar veneno
    //C consultar humedad --> L/H
    //I iniciar  --> A, todo ok
    //E salir
    View.OnClickListener botonesListeners = new View.OnClickListener()
    {
        @SuppressLint("MissingPermission")
        @Override
        public void onClick(View v)
        {
            if(v.getId()==R.id.tirar_ven)
            {
                mConnectedThread.write("R");
            }
            else if(v.getId()==R.id.hayAnts)
            {
                mConnectedThread.write("H");
            }
            else if(v.getId()==R.id.hay_humedad)
            {
                mConnectedThread.write("C");
            }
            else if(v.getId()==R.id.salir)
            {
                mConnectedThread.write("E");
                finish();
            }
        }
    };

    private class ConnectedThread extends Thread
    {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket)
        {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try
            {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run()
        {
            byte[] buffer = new byte[256];
            int bytes;

            while (true)
            {
                try
                {
                    bytes = mmInStream.read(buffer);
                    String readMessage = new String(buffer, 0, bytes);

                    bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }


        public void write(String input) {
            byte[] msgBuffer = input.getBytes();
            try {
                mmOutStream.write(msgBuffer);
            } catch (IOException e) {
                showToast("La conexion fallo");
                finish();

            }
        }
    }

}