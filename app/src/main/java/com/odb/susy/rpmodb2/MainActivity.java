package com.odb.susy.rpmodb2;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ListViewCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.pires.obd.commands.engine.RPMCommand;
import com.github.pires.obd.commands.protocol.EchoOffCommand;
import com.github.pires.obd.commands.protocol.LineFeedOffCommand;
import com.github.pires.obd.commands.protocol.SelectProtocolCommand;
import com.github.pires.obd.commands.protocol.TimeoutCommand;
import com.github.pires.obd.commands.temperature.AmbientAirTemperatureCommand;
import com.github.pires.obd.enums.ObdProtocols;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getDeviceList();


    }


    public void getDeviceList(){
        BluetoothAdapter mBlurAdapter= BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = mBlurAdapter.getBondedDevices();

        ListView listView = (ListView) findViewById(R.id.listview);
        final ArrayList<String> values = new ArrayList<>();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, values);

        TextView num = (TextView) findViewById(R.id.number);
        num.setText(String.valueOf(pairedDevices.size()));

        if (pairedDevices.isEmpty()) {
            Log.e("DeviceActivity ",
                    "Device not founds");
            return ;
        }


        String name="";
        for (BluetoothDevice devices : pairedDevices) {

            /*
            name = name + "Device : address : " + devices.getAddress() + " name :"
                    + devices.getName() + "\n";
            */

            values.add(devices.getAddress());

        }

        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(MainActivity.this, values.get(position), Toast.LENGTH_SHORT).show();
                connect(values.get(position));
            }
        });
    }


    public void connect(String string){
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();

        BluetoothDevice device = btAdapter.getRemoteDevice(string);

        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");


        try {
            BluetoothSocket socket = device.createInsecureRfcommSocketToServiceRecord(uuid);
            socket.connect();
            Toast.makeText(this, "Ha conectado", Toast.LENGTH_SHORT).show();


            try{
                new RPMCommand().getRPM();
            }catch (Exception e){
                System.out.println("ERROR DE RPM;");
            }





        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error de conexion", Toast.LENGTH_SHORT).show();
        }
    }



}
