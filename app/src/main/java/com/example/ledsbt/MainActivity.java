package com.example.ledsbt;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.Intent;

import java.util.UUID;

import android.bluetooth.*;
import android.content.pm.PackageManager;
import android.os.*;
import android.view.View;
import android.widget.*;

import java.io.*;

public class MainActivity extends AppCompatActivity {
    EditText edtTextoOut;
    ImageButton btnEnviar, btnAdelante, btnIzquierda, btnStop, btnDerecha, btnReversa;
    TextView tvtMensaje;
    Button btnDesconectar;
    Handler bluetoothIn;
    final int handlerState = 0;
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private StringBuilder DataStringIN = new StringBuilder();
    private ConnectedThread MyConexionBT;
    // Identificador único de servicio - SPP UUID
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    // String para la dirección MAC
    private static String address = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bluetoothIn = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == handlerState) {
                    char MyCaracter = (char) msg.obj;
                    if (MyCaracter == 'a') {
                        tvtMensaje.setText("ACELERANDO");
                    }
                    if (MyCaracter == 'i') {
                        tvtMensaje.setText("GIRO IZQUIERDA");
                    }
                    if (MyCaracter == 'd') {
                        tvtMensaje.setText("GIRO DERECHA");
                    }
                    if (MyCaracter == 'r') {
                        tvtMensaje.setText("RETROCEDIENDO");
                    }
                    if (MyCaracter == 's') {
                        tvtMensaje.setText("DETENIDO");
                    }
                }
            }
        };
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        verificarEstadoBT();
        edtTextoOut = findViewById(R.id.edtTextoOut);
        btnEnviar = findViewById(R.id.btnEnviar);
        btnAdelante = findViewById(R.id.btnAdelante);
        btnIzquierda = findViewById(R.id.btnIzquierda);
        btnStop = findViewById(R.id.btnStop);
        btnDerecha = findViewById(R.id.btnDerecha);
        btnReversa = findViewById(R.id.btnReversa);
        tvtMensaje = findViewById(R.id.tvtMensaje);
        btnDesconectar = findViewById(R.id.btnDesconectar);
        btnEnviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { // GetDat es cualquier dato String
                String GetDat = edtTextoOut.getText().toString();
                MyConexionBT.write(GetDat); // ENVIA GetDat AL PUERTO SERIE
            }
        });
        btnAdelante.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyConexionBT.write("A");
            }
        });
        btnIzquierda.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyConexionBT.write("I");
            }
        });
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyConexionBT.write("S");
            }
        });
        btnDerecha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyConexionBT.write("D");
            }
        });
        btnReversa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyConexionBT.write("R");
            }
        });
        btnDesconectar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (btSocket != null) {
                    try {
                        btSocket.close();
                    } catch (IOException e) {
                        Toast.makeText(getBaseContext(), "ERROR",
                                Toast.LENGTH_SHORT).show();
                        ;
                    }
                }
                finish();
            }
        });
    }



    //MODIFICAR ESTE FRAGMENTO, ¿NECESITA EL NULL?
    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws
            IOException {
        // Crea una conexion de salida segura para el dispositivo usando el servicio UUID
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return device.createRfcommSocketToServiceRecord(BTMODULEUUID);
        }

        return null;
    }




    @Override
    public void onPause() {
        super.onPause();
        try { // Cuando se sale de la aplicación esta parte permite que no se deje abierto el socket
            btSocket.close();
        } catch (IOException e2) {
        }
    }

    private void verificarEstadoBT() {
        if (btAdapter == null) { // Comprueba que el Bluetooth esté disponible y solicita se active si está desactivado
            Toast.makeText(getBaseContext(), "El dispositivo no soporta bluetooth",
                    Toast.LENGTH_LONG).show();
        } else {
            if (btAdapter.isEnabled()) {
            } else {
                Intent enableBtIntent = new
                        Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }
    private class ConnectedThread extends Thread{ // Clase que permite crear el evento de  conexión
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        public ConnectedThread(BluetoothSocket socket){
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try{
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch(IOException e){}
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }
        public void run(){
            byte[] byte_in = new byte[1];
            while(true){ // Se mantiene en modo escucha para determinar el ingreso de datos
                try{
                    mmInStream.read(byte_in);
                    char ch = (char) byte_in[0];
                    bluetoothIn.obtainMessage(handlerState, ch).sendToTarget();
                } catch(IOException e){
                    break;
                }
            }
        }
        public void write(String input){ // Envío de trama
            try{
                mmOutStream.write(input.getBytes());
            } catch(IOException e){ // si no es posible enviar datos se cierra la conexión
                Toast.makeText(getBaseContext(), "La conexión falló",
                        Toast.LENGTH_LONG).show();
            }
        }
    }
}
