package com.rch.telpoprinter;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.common.apiutil.CommonException;
import com.common.apiutil.moneybox.MoneyBox;
import com.common.apiutil.pos.RS232Reader;
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


    void doPrint1() throws CommonException {
        UsbThermalPrinter printer=new UsbThermalPrinter(this);
        printer.EscPosCommandExe(new byte[]{0x1b,0x40});
        byte resul= printer.EscPosCommandExe(new byte[]{0x10,4,1});


        int rep=10;
        byte[] cmd= new byte[3];
        cmd[0]=27;
        cmd[1]=33;
        cmd[2]=16;

        printer.EscPosCommandExe(cmd);

        for (int i=0;i<rep;i++) {
            printer.EscPosCommandExe("double H\n".getBytes());
        }

        cmd[2]=32;

        printer.EscPosCommandExe(cmd);

        for (int i=0;i<rep;i++) {
            printer.EscPosCommandExe("double W\n".getBytes());
        }

        cmd[2]=32+16;

        printer.EscPosCommandExe(cmd);

        for (int i=0;i<rep;i++) {
            printer.EscPosCommandExe("double WH\n".getBytes());
        }

        cmd[2]=0;

        printer.EscPosCommandExe(cmd);

        for (int i=0;i<rep;i++) {
            printer.EscPosCommandExe("normal\n".getBytes());
        }

    }

    void doPrint() {
        UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);

        ProbeTable customTable = new ProbeTable();
        customTable.addProduct(0x28e9, 0x28d, ChromeCcdSerialDriver.class);
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
            //   port.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);


            byte[] cmd= new byte[3];
            cmd[0]=0x1b;
            cmd[1]=0x40;
            port.write(cmd, 1000);


            port.write("-\n".getBytes(), 1000);

            dle_eot(port,1);
            port.write("-\n".getBytes(), 1000);
            dle_eot(port,2);
            port.write("-\n".getBytes(), 1000);
            dle_eot(port,3);
            port.write("-\n".getBytes(), 1000);
            dle_eot(port,4);




            cmd= new byte[3];
            cmd[0]=27;
            cmd[1]=33;
            cmd[2]=16;



            for (int i=0;i<rep;i++) {
                port.write("double H\n".getBytes(), 1000);

            }
            port.write(new byte[]{0x0a}, 1000);

            cmd= new byte[3];
            cmd[0]=27;
            cmd[1]=33;
            cmd[2]=32;
            port.write(cmd, 1000);



            for (int i=0;i<rep;i++)
                port.write("double w\n".getBytes(), 1000);
            port.write(new byte[]{0x0a}, 1000);


            cmd= new byte[3];
            cmd[0]=27;
            cmd[1]=33;
            cmd[2]=16+32;
            port.write(cmd, 1000);



            for (int i=0;i<rep;i++)
                port.write("double wh\n".getBytes(), 1000);
            port.write(new byte[]{0x0a}, 1000);

            cmd= new byte[3];
            cmd[0]=27;
            cmd[1]=33;
            cmd[2]=0;
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

        } catch (IOException e) {
            e.printStackTrace();
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
                try {

                    doPrint1();
                } catch (CommonException e) {
                    e.printStackTrace();
                }

                 doPrint();
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


    void dle_eot(UsbSerialPort port, int n) throws IOException {
        byte[] cmd = new byte[3];
        cmd[0] = 16;
        cmd[1] = 04;
        cmd[2] = (byte) n;
        port.write(cmd, 1000);


        byte[] response = new byte[1];
        response[0]= (byte) 0xee;

        int retLen = port.read(response, 500);


        if (retLen >= 1)
            Log.d("", Integer.toHexString(response[0]));
        else {
            Log.d("", "NO Answer "+Integer.toHexString(response[0]));

        }
    }


    void ds()
    {

    }
}