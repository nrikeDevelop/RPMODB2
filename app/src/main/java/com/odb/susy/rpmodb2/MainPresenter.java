package com.odb.susy.rpmodb2;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.widget.Toast;

import com.github.pires.obd.commands.engine.RPMCommand;
import com.github.pires.obd.commands.protocol.EchoOffCommand;
import com.github.pires.obd.commands.protocol.LineFeedOffCommand;
import com.github.pires.obd.commands.protocol.SelectProtocolCommand;
import com.github.pires.obd.commands.protocol.TimeoutCommand;
import com.github.pires.obd.enums.ObdProtocols;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by susy on 4/06/17.
 */

public class MainPresenter {

    BluetoothAdapter btAdapter;
    BluetoothDevice device;
    UUID uuid;
    BluetoothSocket socket;

    MainView mainView;

    Context context;

    public MainPresenter(MainView mainView, Context context) {
        this.mainView = mainView;
        this.context = context;
    }

    public void connect(){

        try {
            btAdapter = BluetoothAdapter.getDefaultAdapter();
            device = btAdapter.getRemoteDevice("00:1D:A5:68:98:8C");
            uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
            socket = device.createInsecureRfcommSocketToServiceRecord(uuid);

            socket.connect();

            try {
                new EchoOffCommand().run(socket.getInputStream(), socket.getOutputStream());
                new LineFeedOffCommand().run(socket.getInputStream(), socket.getOutputStream());
                new TimeoutCommand(100).run(socket.getInputStream(), socket.getOutputStream());
                new SelectProtocolCommand(ObdProtocols.AUTO).run(socket.getInputStream(), socket.getOutputStream());

                //TODO INFO GET CAR
                RPM();

            } catch (IOException e) {
                e.printStackTrace();
                mainView.showToastMessage("Error in protocol odb");
            } catch (InterruptedException e) {
                e.printStackTrace();
                mainView.showToastMessage("Error in protocol odb");
            }

            mainView.showToastMessage("Conected");

        } catch (IOException e) {
            e.printStackTrace();
            mainView.showToastMessage("Error in socket connection");

            //Toast.makeText(this, "Error in method connect", Toast.LENGTH_SHORT).show();
            //labelGetRPMtext.setText("Error");

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
                    //System.out.println(rpm);
                    mainView.RPM(rpm);

                }
            }
        });
        t.start();
    }

}
