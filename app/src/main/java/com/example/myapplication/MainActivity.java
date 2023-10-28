package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    public Button veneno;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        veneno=findViewById(R.id.tirar_ven);
        veneno.setOnClickListener(botonesListeners);
    }
    View.OnClickListener botonesListeners = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            if(v.getId()==R.id.tirar_ven)
            {
                    Intent intent;

                    //se genera un Intent para poder lanzar la activity principal
                    intent=new Intent(MainActivity.this,TirarVen.class);

                    //Se le agrega al intent los parametros que se le quieren pasar a la activyt principal
                    //cuando se lanzado
                    intent.putExtra("textoOrigen","Hola Mundo");

                    //se inicia la activity principal
                    startActivity(intent);
            }


        }
    };
}