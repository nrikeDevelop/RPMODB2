package com.odb.susy.rpmodb2;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.pires.obd.commands.engine.RPMCommand;
import com.github.pires.obd.commands.protocol.EchoOffCommand;
import com.github.pires.obd.commands.protocol.LineFeedOffCommand;
import com.github.pires.obd.commands.protocol.SelectProtocolCommand;
import com.github.pires.obd.commands.protocol.TimeoutCommand;
import com.github.pires.obd.enums.ObdProtocols;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {




    TextView rpmText;

    public void setUI(){
        rpmText = (TextView) findViewById(R.id.main_rpm_text);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setUI();

        getDeviceList();
        rpmText.setText("Connecting...");
    }

    public void setTextView(final TextView textView, final String str){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView.setText(str);
            }
        });
    }



    public void connect(String string){


        try {
            BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();

            BluetoothDevice device = btAdapter.getRemoteDevice(string);

            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

            final BluetoothSocket socket = device.createInsecureRfcommSocketToServiceRecord(uuid);

            socket.connect();

            new EchoOffCommand().run(socket.getInputStream(), socket.getOutputStream());

            new LineFeedOffCommand().run(socket.getInputStream(), socket.getOutputStream());

            new TimeoutCommand(100).run(socket.getInputStream(), socket.getOutputStream());

            new SelectProtocolCommand(ObdProtocols.AUTO).run(socket.getInputStream(), socket.getOutputStream());

            Toast.makeText(this, "Connected and load", Toast.LENGTH_SHORT).show();

            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    RPMCommand engineRpmCommand = new RPMCommand();
                    while (!Thread.currentThread().isInterrupted())
                    {
                        try {
                            engineRpmCommand.run(socket.getInputStream(), socket.getOutputStream());

                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        String rpm = engineRpmCommand.getFormattedResult();
                        // TODO handle commands result
                        System.out.println(rpm);
                        setTextView(rpmText,rpm);

                    }
                }
            });
            t.start();




        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error in method connect", Toast.LENGTH_SHORT).show();
            rpmText.setText("Error");

        } catch (InterruptedException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error protocol", Toast.LENGTH_SHORT).show();
        }

    }


    //CONNECTION ODB2
    private void protocolsODB2(){

    }








    public void getDeviceList(){
        BluetoothAdapter mBlurAdapter= BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = mBlurAdapter.getBondedDevices();

        ListView listView = (ListView) findViewById(R.id.listview);
        final ArrayList<String> values = new ArrayList<>();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, values);



        if (pairedDevices.isEmpty()) {
            Log.e("DeviceActivity ",
                    "Device not founds");
            return ;
        }


        String name="";
        for (BluetoothDevice devices : pairedDevices) {


            name = name + "Device : address : " + devices.getAddress() + " name :"
                    + devices.getName() ;

            System.out.println(name);

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





}
