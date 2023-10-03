package com.example.ledsbt;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.bluetooth.*;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;

import java.util.Set;

public class DispositivosVinculados extends AppCompatActivity {
    // Depuración de LOGCAT
    private static final String TAG = "DispositivosVinculados";
    // Declaración de ListView
    ListView IdLista;
    // String que se enviará a la actividad principal, MainActivity
    public static String EXTRA_DEVICE_ADDRESS = "device_address";
    private BluetoothAdapter mBtAdapter;
    private ArrayAdapter mPairedDevicesArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dispositivos_vinculados);
    }

    @Override
    public void onResume() {
        super.onResume();
        verificarEstadoBT(); // Inicializa la array de la lista de los dispositivos vinculados.
        mPairedDevicesArrayAdapter = new ArrayAdapter(this, R.layout.dispositivos_encontrados);
        IdLista = (ListView) findViewById(R.id.IdLista);
        IdLista.setAdapter(mPairedDevicesArrayAdapter);
        IdLista.setOnItemClickListener(mDeviceClickListener);
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();// Obtiene el adaptador local Bluetooth adapter
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
        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) { // Agrega un dispositivo emparejado al array
            for (BluetoothDevice device : pairedDevices) {
                mPairedDevicesArrayAdapter.add(device.getName() + "\n" +
                        device.getAddress());
            }
        }
    }

    // Configura un(on-click) para la lista
    private AdapterView.OnItemClickListener mDeviceClickListener = new
            AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView av, View v, int arg2, long arg3) {
                    // Obtener la direccion MAC del dispositivo, que son los últimos 17 caracteres en la vista
                    String info = ((TextView) v).getText().toString();
                    String address = info.substring(info.length() - 17);
                    finishAffinity();
                    // Realiza un intent para iniciar la siguiente actividad
                    // mientras toma un EXTRA_DEVICE_ADDRESS que es la dirección MAC.
                    Intent intend = new Intent(DispositivosVinculados.this, MainActivity.class);
                    intend.putExtra(EXTRA_DEVICE_ADDRESS, address);
                    startActivity(intend);
                }
            };

    private void verificarEstadoBT() { // Comprueba que el dispositivo tiene Bluetooth y que está encendido.
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBtAdapter == null) {
            Toast.makeText(getBaseContext(), "El dispositivo no soporta Bluetooth",
                    Toast.LENGTH_SHORT).show();
        } else {
            if (mBtAdapter.isEnabled()) {
                Log.d(TAG, "...Bluetooth activado ...");
            } else { // Solicita al usuario que active Bluetooth
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
}
