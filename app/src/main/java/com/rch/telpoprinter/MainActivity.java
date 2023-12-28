package com.rch.telpoprinter;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.common.apiutil.CommonException;
import com.common.apiutil.moneybox.MoneyBox;
import com.common.apiutil.pos.RS232Reader;
import com.common.apiutil.powercontrol.PowerControl;
import com.common.apiutil.printer.UsbThermalPrinter;
import com.hoho.android.usbserial.driver.ChromeCcdSerialDriver;
import com.hoho.android.usbserial.driver.ProbeTable;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;


import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    MoneyBox moneyBox;
    RS232Reader rs232Reader;
    TextView printerTV;
    TextView dsTV;
    UsbThermalPrinter printer=new UsbThermalPrinter(this);

    static final int BOLD=8;


    void doPrintByUsbSerialPrinter() throws CommonException {
      printer.EscPosCommandExe(new byte[]{0x1b,0x40});


       dleEot1(printer, (byte) 1);
        dleEot1(printer, (byte) 2);
        dleEot1(printer, (byte) 3);
        dleEot1(printer, (byte) 4);




        int rep=10;
        byte[] cmd= new byte[3];
        cmd[0]=27;
        cmd[1]=33;
        cmd[2]=16+BOLD;

        printer.EscPosCommandExe(cmd);

        for (int i=0;i<rep;i++) {
                printer.EscPosCommandExe("double H\n".getBytes());

        }





        cmd[2]=32+BOLD;

        printer.EscPosCommandExe(cmd);

        for (int i=0;i<rep;i++) {
            printer.EscPosCommandExe("double W\n".getBytes());
        }

        cmd[2]=32+16+BOLD;

        printer.EscPosCommandExe(cmd);

        for (int i=0;i<rep;i++) {
            printer.EscPosCommandExe("double WH\n".getBytes());
        }

        cmd[2]=BOLD;

        printer.EscPosCommandExe(cmd);

        for (int i=0;i<rep;i++) {
            printer.EscPosCommandExe("normal\n".getBytes());
        }

        //CUT  !!!!!!!! Non fa niente
        printer.EscPosCommandExe(new byte[]{29,86,1});


    }

    void doPrintRawUsbSerial() throws CommonException {

 //       printer.EscPosCommandExe(new byte[]{0x1b,0x40});
        UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);

        ProbeTable customTable = new ProbeTable();
        customTable.addProduct(0x28e9, 0x28d, ChromeCcdSerialDriver.class);
     //   customTable.addProduct(0x154f, 0x154f, ChromeCcdSerialDriver.class);
     //   customTable.addProduct(0x1fc9, 0x2016, ChromeCcdSerialDriver.class);



        UsbSerialProber prober = new UsbSerialProber(customTable);

        List<UsbSerialDriver> availableDrivers = prober.findAllDrivers(manager);
        if (availableDrivers.isEmpty())
            return;

        UsbSerialDriver driver = availableDrivers.get(0);
        UsbDeviceConnection connection = manager.openDevice(driver.getDevice());
        if (connection == null) {
            // add UsbManager.requestPermission(driver.getDevice(), ..) handling here
            return;
        }

        UsbSerialPort port = driver.getPorts().get(0); // Most devices have just one port (port 0)
        try {
            int rep=10;
            port.open(connection);


            // initialize printer ESC @
            byte[] cmd= new byte[2];
            cmd[0]=0x1b;
            cmd[1]=0x40;
            port.write(cmd, 1000);


            // reset automatic status
//            cmd= new byte[3];
//            cmd[0]=29;
//            cmd[1]=97;
//            cmd[2]=0;
//
//            port.write(cmd, 1000);


//            dleEot(port,2);
//            gsR(port,49);
//            gsR(port,50);
//            gsR(port,52);

//            escV(port);





            cmd= new byte[3];
            cmd[0]=27;
            cmd[1]=33;
            cmd[2]=16+BOLD;
            port.write(cmd, 1000);


            for (int i=0;i<rep;i++) {
                port.write("double H\n".getBytes(), 1000);

            }
            port.write(new byte[]{0x0a}, 1000);


            cmd= new byte[3];
            cmd[0]=27;
            cmd[1]=33;
            cmd[2]=32+BOLD;
            port.write(cmd, 1000);



            for (int i=0;i<rep;i++)
                port.write("double w\n".getBytes(), 1000);
            port.write(new byte[]{0x0a}, 1000);


            cmd= new byte[3];
            cmd[0]=27;
            cmd[1]=33;
            cmd[2]=16+32+BOLD;
            port.write(cmd, 1000);



            for (int i=0;i<rep;i++) {


                port.write("double wh\n".getBytes(), 1000);
            }
            port.write(new byte[]{0x0a}, 1000);

            cmd= new byte[3];
            cmd[0]=27;
            cmd[1]=33;
            cmd[2]=BOLD;
            port.write(cmd, 1000);

            for (int i=0;i<rep;i++)
                port.write("normal\n".getBytes(), 1000);
            port.write(new byte[]{0x0a}, 1000);

            //CUT
            cmd[0]=29;
            cmd[1]=86;
            cmd[2]=1;
            port.write(cmd, 1000);

            port.write(new byte[]{0x0c}, 1000);

        } catch (IOException  e) {
            e.printStackTrace();
        } finally {
            connection.close();
        }

    }



    void moneybox()
    {
        MoneyBox.open();
        try {
            Thread.sleep(250);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        MoneyBox.close();
    }

    void sendSerial()
    {
        rs232Reader.rsSend("the quick brown fox jumps over......\n".getBytes(StandardCharsets.UTF_8));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Intent intent=new Intent();
        intent.setAction("android.intent.action.cert.white.list ");
        intent.putExtra("white_list","com.rch.telpoprinter");
        sendBroadcast(intent);

        File f= new File(getFilesDir().getAbsolutePath()+"/mnt");
        f.mkdir();
        Button button = findViewById(R.id.print_button);


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                printerTV.setText("");


                    Thread thread= new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                doPrintRawUsbSerial();
                                //doPrintByUsbSerialPrinter();

                            } catch (CommonException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    thread.start();

            }
        });

        Button button1 = findViewById(R.id.money_box_button);

        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moneybox();
            }
        });

        Button button2 = findViewById(R.id.serial_button);

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendSerial();
            }
        });


        Button button3= findViewById(R.id.ds_button);
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                readDS();
            }
        });




        printerTV= findViewById(R.id.textViewPrinter);
        dsTV= findViewById(R.id.textViewDS);

//        UsbManager manager = (UsbManager) getSystemService(USB_SERVICE);
//
//        HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
//        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
//        while(deviceIterator.hasNext()){
//            UsbDevice device = deviceIterator.next();
//            UsbDeviceConnection connection = manager.openDevice(device);
//            Log.d("","");
//        }


//        SDKUtil.getInstance(this).initSDK();
//
//        if (!SystemUtil.isInstallServiceApk()) {
//            Log.d("tagg", "API 调用 >> 系统反射");
//        }else {
//            Log.d("tagg", "API 调用 >> 服务APK");
//        }

//        rs232Reader= new RS232Reader(this);
//
//        int ret=rs232Reader.rsOpen(CommonConstants.RS232Type.RS232_1, 9600);
//
//        rs232Reader.setRSReaderListener(new IRSReaderListener() {
//            @Override
//            public void onRecvData(byte[] bytes) {
//                ((TextView)findViewById(R.id.textView)).setText(new String(bytes));
//            }
//        });


    }

    void dleEot1(UsbThermalPrinter printer,byte n) throws CommonException {
        int ret= printer.EscPosCommandExe(new byte[]{0x10,4,n});
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                printerTV.setText(printerTV.getText().toString() + "\n" + "DLE EOT "+n+ ": 0X"+Integer.toHexString(ret));
            }
        });
    }

    void dleEot(UsbSerialPort port, int n) throws IOException {
        byte[] cmd = new byte[3];
        cmd[0] = 16;
        cmd[1] = 04;
        cmd[2] = (byte) n;
        port.write(cmd, 1000);


        byte[] response = new byte[1];
        response[0]= (byte) 0xee;



        int retLen = port.read(response, 5000);







        if (retLen >= 1) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    printerTV.setText(printerTV.getText().toString() + "\n" + "DLE EOT "+n+ ": 0X"+Integer.toHexString(response[0]));
                }
            });
            Log.d("", Integer.toHexString(response[0]));
        }
        else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    printerTV.setText(printerTV.getText().toString() + "\n"+ "DLE EOT "+n+ ":" + "NO Answer "+ Integer.toHexString(response[0]));
                }
            });

            Log.d("", "NO Answer "+Integer.toHexString(response[0]));

        }
    }

    void gsR(UsbSerialPort port, int n) throws IOException {
        byte[] cmd = new byte[3];
        cmd[0] = 29;
        cmd[1] = 114;
        cmd[2] = (byte) n;
        port.write(cmd, 1000);


        byte[] response = new byte[1];
        response[0]= (byte) 0xee;



        int retLen = port.read(response, 5000);







        if (retLen >= 1) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    printerTV.setText(printerTV.getText().toString() + "\n" + "GS R "+n+ ": 0X"+Integer.toHexString(response[0]));
                }
            });
            Log.d("", Integer.toHexString(response[0]));
        }
        else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    printerTV.setText(printerTV.getText().toString() + "\n"+ "GS R "+n+ ":" + "NO Answer "+ Integer.toHexString(response[0]));
                }
            });

            Log.d("", "NO Answer "+Integer.toHexString(response[0]));

        }
    }

    void escV(UsbSerialPort port) throws IOException {
        byte[] cmd = new byte[2];
        cmd[0] = 27;
        cmd[1] = 118;

        port.write(cmd, 1000);


        byte[] response = new byte[1];
        response[0]= (byte) 0xee;



        int retLen = port.read(response, 5000);







        if (retLen >= 1) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    printerTV.setText(printerTV.getText().toString() + "\n" + "ESC V "+ ": 0X"+Integer.toHexString(response[0]));
                }
            });
            Log.d("", Integer.toHexString(response[0]));
        }
        else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    printerTV.setText(printerTV.getText().toString() + "\n"+ "ESC V "+ ":" + "NO Answer "+ Integer.toHexString(response[0]));
                }
            });

            Log.d("", "NO Answer "+Integer.toHexString(response[0]));

        }
    }



    void readDS()
    {
        PowerControl powerControl = new PowerControl(this);
       if ( powerControl.getDialSwitchStatus()==0)
           dsTV.setText("OFF");
       else
           dsTV.setText("ON");
    }
}