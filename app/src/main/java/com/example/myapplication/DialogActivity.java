package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;


import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import kotlin.contracts.Returns;

public class DialogActivity extends Activity
{

    private Button btnOK, btnCancelar;
    private EditText txtDestino;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog);

        btnOK = (Button) findViewById(R.id.btnOK);
        btnCancelar = (Button) findViewById(R.id.btnCancelar);
        txtDestino=(EditText)findViewById(R.id.txtDestino);

        btnOK.setOnClickListener(botonesListeners);
        btnCancelar.setOnClickListener(botonesListeners);


        //se crea un objeto Bundle para poder recibir los parametros enviados por la activity Inicio
        //al momeento de ejecutar stratActivity
        Intent intent=getIntent();
        Bundle extras=intent.getExtras();
        String texto= extras.getString("textoOrigen");
        txtDestino.setText(texto);

    }


    private View.OnClickListener botonesListeners = new View.OnClickListener()
    {
        public void onClick(View v) {

            if(v.getId()==R.id.btnOK) {
                Toast.makeText(getApplicationContext(), "Boton OK presionado", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            if(v.getId() == R.id.btnCancelar) {
                Toast.makeText(getApplicationContext(), "Boton Cancelar presionado", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            Toast.makeText(getApplicationContext(),"Error en Listener de botones",Toast.LENGTH_LONG).show();


        }

    };
}
