package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class TirarVen extends AppCompatActivity {

    private Button btnTirar, btnCancelarVen;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tirar_ven);
        btnTirar = (Button) findViewById(R.id.btnTirarVen);
        btnCancelarVen = (Button) findViewById(R.id.btnCancelarVen);
        btnTirar.setOnLongClickListener(botonesLongListeners);
        btnCancelarVen.setOnClickListener(botonesListeners);


    }

    private View.OnClickListener botonesListeners = new View.OnClickListener()
    {
        public void onClick(View v) {

            if(v.getId()==R.id.btnTirarVen) {
                Toast.makeText(getApplicationContext(), "Mantenga presionado", Toast.LENGTH_SHORT).show();
                return;
            }
            if(v.getId() == R.id.btnCancelarVen) {
                Toast.makeText(getApplicationContext(), " Cancelar Tirado", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            Toast.makeText(getApplicationContext(),"Error en Listener de botones",Toast.LENGTH_LONG).show();


        }

    };
    private View.OnLongClickListener botonesLongListeners = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            if (v.getId() == R.id.btnTirarVen) {
                Toast.makeText(getApplicationContext(), "Mantenga presionado", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        }

        @Override
        public boolean onLongClickUseDefaultHapticFeedback(@NonNull View v) {
            return View.OnLongClickListener.super.onLongClickUseDefaultHapticFeedback(v);
        }
        /*@Override
        public void onLongClick(View v) {
            if (v.getId() == R.id.btnTirarVen) {
                Toast.makeText(getApplicationContext(), "Mantenga presionado", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        ;*/
    };
}