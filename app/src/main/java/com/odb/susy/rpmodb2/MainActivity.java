package com.odb.susy.rpmodb2;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.pires.obd.commands.engine.RPMCommand;
import com.github.pires.obd.commands.protocol.EchoOffCommand;
import com.github.pires.obd.commands.protocol.LineFeedOffCommand;
import com.github.pires.obd.commands.protocol.SelectProtocolCommand;
import com.github.pires.obd.commands.protocol.TimeoutCommand;
import com.github.pires.obd.enums.ObdProtocols;

import java.io.IOException;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private final String ODB_ADDRESS = "00:1D:A5:68:98:8C";
    private final String UUID_STRING = "00001101-0000-1000-8000-00805F9B34FB";
    private final int TIME_RECIVE_COMAND = 10;

    BluetoothSocket socket;
    BluetoothAdapter bluetoothAdapter;
    BluetoothDevice device;

    TextView rpmText;

    public void setUI(){
        rpmText = (TextView) findViewById(R.id.main_rpm_text);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setUI();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                connect();
            }
        },500);

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



    public void connect(){

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        device = bluetoothAdapter.getRemoteDevice(ODB_ADDRESS);
        UUID uuid = UUID.fromString(UUID_STRING);


        try {
            socket  = device.createInsecureRfcommSocketToServiceRecord(uuid);
            socket.connect();
            protocolsODB2();

            RPMCommand rpmcommand = new RPMCommand();

            while (!Thread.currentThread().isInterrupted())
            {
                rpmcommand.run(socket.getInputStream(), socket.getOutputStream());
                // TODO handle commands result
                String stringRPM = rpmcommand.getFormattedResult();
                System.out.println(">>"+rpmcommand.getFormattedResult());
                setTextView(rpmText,stringRPM);
            }

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error in method connect", Toast.LENGTH_SHORT).show();
            rpmText.setText("Error");

        } catch (InterruptedException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error in method interrupt connect", Toast.LENGTH_SHORT).show();
            rpmText.setText("Error");


        }

    }


    //CONNECTION ODB2
    private void protocolsODB2(){
        try{
            new EchoOffCommand().run(socket.getInputStream(), socket.getOutputStream());
            new LineFeedOffCommand().run(socket.getInputStream(), socket.getOutputStream());
            new TimeoutCommand(TIME_RECIVE_COMAND).run(socket.getInputStream(), socket.getOutputStream());
            new SelectProtocolCommand(ObdProtocols.AUTO).run(socket.getInputStream(), socket.getOutputStream());
        }catch (Exception e){
            Toast.makeText(this, "Error in protocols method", Toast.LENGTH_SHORT).show();
        }
    }






/*

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
*/




}
