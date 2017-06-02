package com.odb.susy.rpmodb2;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
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

    int MAX_RPM = 2000 ;

    Context context;

    BluetoothAdapter btAdapter;
    BluetoothDevice device;
    UUID uuid;
    BluetoothSocket socket;

    RelativeLayout contentColor;
    RelativeLayout outRPM;
    TextView labelGetRPMtext;
    TextView labelRPM;

    public void setUI(){
        contentColor = (RelativeLayout) findViewById(R.id.main_content_color);
        outRPM = (RelativeLayout) findViewById(R.id.outRPM);
        labelGetRPMtext = (TextView) findViewById(R.id.main_rpm_text);
        labelRPM = (TextView) findViewById(R.id.main_label_rpm);

        setUpFonts();
    }

    public void setUpFonts(){
        Typeface font_regular = Typeface.createFromAsset(context.getAssets(), "regular.ttf");
        labelGetRPMtext.setTypeface(font_regular);
        labelRPM.setTypeface(font_regular);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;

        setUI();
        //getDeviceList();
        connect();
        contentColor.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                configurationAlertDialog();
                return true;
            }
        });

    }

    //CONNECTION ODB2

    public void connect(){

        try {
            btAdapter = BluetoothAdapter.getDefaultAdapter();
            device = btAdapter.getRemoteDevice("00:1D:A5:68:98:8C");
            uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
            socket = device.createInsecureRfcommSocketToServiceRecord(uuid);

            socket.connect();

            if(protocolsODB2()){
                RPM();
            }

            Toast.makeText(this, "Connected and load", Toast.LENGTH_SHORT).show();


        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error in method connect", Toast.LENGTH_SHORT).show();
            labelGetRPMtext.setText("Error");

        }

    }

    private boolean protocolsODB2(){
        try {
            new EchoOffCommand().run(socket.getInputStream(), socket.getOutputStream());
            new LineFeedOffCommand().run(socket.getInputStream(), socket.getOutputStream());
            new TimeoutCommand(100).run(socket.getInputStream(), socket.getOutputStream());
            new SelectProtocolCommand(ObdProtocols.AUTO).run(socket.getInputStream(), socket.getOutputStream());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error in protocol method", Toast.LENGTH_SHORT).show();
            return false;
        } catch (InterruptedException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error in protocol method", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private void RPM(){
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
                    int rpm = engineRpmCommand.getRPM();
                    System.out.println(rpm);
                    setTextView(labelGetRPMtext,String.valueOf(rpm));
                    bacgroundColor(rpm);

                }
            }
        });
        t.start();
    }

    public void bacgroundColor(int rpm){
        int RPM = rpm;
        if(RPM <= MAX_RPM/2){
            setRelativeLayoutVisibilit(outRPM,false);
            setRelativeLayoutVisibilit(contentColor,true);

            System.out.println((RPM*255)/(MAX_RPM/2));
            setOutColor(contentColor,(RPM*255)/(MAX_RPM/2),255,0);
        }


        if(RPM >= MAX_RPM/2 && RPM <= MAX_RPM){
            setRelativeLayoutVisibilit(outRPM,false);
            setRelativeLayoutVisibilit(contentColor,true);

            System.out.println((RPM*255)/(MAX_RPM/2));
            setOutColor(contentColor,255,254-(RPM*255)/(MAX_RPM/2),0);
        }

        if(RPM > MAX_RPM ){
            setRelativeLayoutVisibilit(outRPM,true);
            setRelativeLayoutVisibilit(contentColor,false);

            Thread outRevTrhead = new Thread(new Runnable() {
                @Override
                public void run() {
                    boolean exit = false;
                    while (!exit){
                        for (int a = 0 ; a <= 10; a ++ ){
                            try {
                                setOutColor(outRPM,255,255,255);
                                Thread.currentThread().sleep(200);
                                setOutColor(outRPM,255,0,0);
                                Thread.currentThread().sleep(200);

                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                }
            });
            outRevTrhead.start();




        }


    }

    //SET OUT DATA THREADS

    public void setTextView(final TextView textView, final String str){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView.setText(str);
            }
        });
    }

    public void setOutColor(final RelativeLayout relativeLayout, final int red, final int green, final int blue){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int selectedColor = Color.rgb(red, green, blue);
                relativeLayout.setBackgroundColor(selectedColor);
            }
        });
    }

    public void setRelativeLayoutVisibilit(final RelativeLayout relativelayot, final Boolean visibility){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(visibility){
                    relativelayot.setVisibility(View.VISIBLE);
                } else {
                     relativelayot.setVisibility(View.GONE);
                }
            }
        });
    }

    //ALERTDIALOG

    public void configurationAlertDialog(){


        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);

        View view = getLayoutInflater().inflate( R.layout.configuration_mode, null );
        Button btEconomic = (Button) view.findViewById(R.id.dialog_economic_button);
        Button btNormal = (Button) view.findViewById(R.id.dialog_normal_button);
        Button btSport = (Button) view.findViewById(R.id.dialog_sport_button);

        alertDialog.setView(view);

        final AlertDialog dialog = alertDialog.create();
        dialog.show();

        btEconomic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MAX_RPM = 2000;
                Toast.makeText(context, "Revoluciones de corte " + MAX_RPM, Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        btNormal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MAX_RPM = 3000;
                Toast.makeText(context, "Revoluciones de corte " + MAX_RPM, Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        btSport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MAX_RPM = 5000;
                Toast.makeText(context, "Revoluciones de corte " + MAX_RPM, Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

    }




/*
    public void getDeviceList(){
        BluetoothAdapter mBlurAdapter= BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = mBlurAdapter.getBondedDevices();

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

    }
*/


    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
