package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.util.UUID;

public class ConnectedActivity extends AppCompatActivity {

    public Button veneno;
    public Button hayHormigas;
    private static String address = null;
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
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

        address= extras.getString("Direccion_Bluethoot");

        btAdapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        //se realiza la conexion del Bluethoot crea y se conectandose a atraves de un socket
        try
        {
            btSocket = createBluetoothSocket(device);
        }
        catch (IOException e)
        {
            showToast( "La creacción del Socket fallo");
        }
        // Establish the Bluetooth socket connection.
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
                //insert code to deal with this
            }
        }
    }

    @SuppressLint("MissingPermission")
    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {

        return  device.createRfcommSocketToServiceRecord(BTMODULEUUID);
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    View.OnClickListener botonesListeners = new View.OnClickListener()
    {
        @SuppressLint("MissingPermission")
        @Override
        public void onClick(View v)
        {
            if(v.getId()==R.id.tirar_ven)
            {
                Intent intent;

                //se genera un Intent para poder lanzar la activity principal
                intent=new Intent(ConnectedActivity.this,TirarVen.class);

                //Se le agrega al intent los parametros que se le quieren pasar a la activyt principal
                //cuando se lanzado
                intent.putExtra("textoOrigen","Hola Mundo");

                //se inicia la activity principal
                startActivity(intent);
            }
            else if(v.getId()==R.id.hay_ants)
            {

            }
        }
    };
}