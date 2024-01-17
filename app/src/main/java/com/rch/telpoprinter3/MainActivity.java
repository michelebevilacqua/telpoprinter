package com.rch.telpoprinter3;

import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

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
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    MoneyBox moneyBox;
    RS232Reader rs232Reader;
    TextView printerTV;
    TextView dsTV;
    UsbThermalPrinter printer=new UsbThermalPrinter(this);

    static final int BOLD=8;

    UsbManager usbManager;

    UsbDevice printerDevice;

    UsbSerialDriver driver;

    boolean askUsbPermission =true;

    static final int P_TELPO=0;
    static final int P_SNBC=1;
    static final int P_PRINT=2;

    static final int printerT= P_TELPO;


    Byte[] emulateFirmwareLine(String s, int size)
    {
        ArrayList<Byte> fLine= new ArrayList<Byte>();

        fLine.add((byte) 27);
        fLine.add((byte) 33);
        fLine.add((byte) (size));
        for (Byte bs:s.getBytes())
            fLine.add(bs);
        fLine.add((byte) '\n');
        fLine.add((byte) 27);
        fLine.add((byte) 33);
        fLine.add((byte) 0);

        return  fLine.toArray( new Byte[fLine.size()]);

    }

    void
    doPrintByUsbSerialPrinter() throws CommonException {
      printer.EscPosCommandExe(new byte[]{0x1b,0x40});



        dleEot1(printer, (byte) 2);



      PrintableDocument printableDocument= new PrintableDocument();

        BitmapPrinter bitmapPrinter= new BitmapPrinter(this);


     boolean BLOCK_PRINT=false;

     if (BLOCK_PRINT) {

         for (int i = 0; i < 10; i++) {
             int size=0;
             printableDocument.addLine(emulateFirmwareLine("12345678901234567890123456789012345678901234567", size));
             printableDocument.addLine(emulateFirmwareLine("abcdefghilabcdefghilabcdefghilabcdefghilabcdefg".toUpperCase(), size));
             size=16;
             printableDocument.addLine(emulateFirmwareLine("12345678901234567890123456789012345678901234567", size));
             printableDocument.addLine(emulateFirmwareLine("abcdefghilabcdefghilabcdefghilabcdefghilabcdefg", size));
             size=32;
             printableDocument.addLine(emulateFirmwareLine("123456789012345678901234", size));
             printableDocument.addLine(emulateFirmwareLine("abcdefghilabcdefghilabcd".toUpperCase(), size));
             size=32+16;
             printableDocument.addLine(emulateFirmwareLine("123456789012345678901234", size));
             printableDocument.addLine(emulateFirmwareLine("abcdefghilabcdefghipqrst", size));
         }


         bitmapPrinter.textAsBitmap(printableDocument);


         printer.printLogo(bitmapPrinter.getImage(), false);
      //   printer.printLogoRaw(bitmapPrinter.getPixels(), BitmapPrinter.LINE_DOT_WIDTH, bitmapPrinter.getImage().getHeight());

     }
     else {


         printableDocument.clearLines();
         printableDocument.addLine(emulateFirmwareLine("ABCaqp78901234567890123456789012345678901234567",0 ));
         bitmapPrinter.textAsBitmap(printableDocument);


         for (int i = 0; i < 10; i++) {



            if (printer.checkStatus()!=0)
                break;

             printer.printLogo(bitmapPrinter.getImage(), false);
//             printer.printLogoRaw(bitmapPrinter.getPixels(), BitmapPrinter.LINE_DOT_WIDTH, bitmapPrinter.getImage().getHeight());

         }
     }



        for (int i=0;i<10;i++)
            printer.EscPosCommandExe("\n".getBytes());

//        int rep=10;
//        byte[] cmd= new byte[3];
//        cmd[0]=27;
//        cmd[1]=33;
//        cmd[2]=16+BOLD;
//
//
//        printer.checkStatus();
//        printer.EscPosCommandExe(cmd);
//
//        for (int i=0;i<rep;i++) {
//                printer.EscPosCommandExe("double H\n".getBytes());
//
//        }
//




//        cmd[2]=32+BOLD;
//
//        printer.EscPosCommandExe(cmd);
//
//        for (int i=0;i<rep;i++) {
//            printer.EscPosCommandExe("double W\n".getBytes());
//        }
//
//        cmd[2]=32+16+BOLD;
//
//        printer.EscPosCommandExe(cmd);
//
//        for (int i=0;i<rep;i++) {
//            printer.EscPosCommandExe("double WH\n".getBytes());
//        }
//
//        cmd[2]=BOLD;
//
//        printer.EscPosCommandExe(cmd);
//
//        for (int i=0;i<rep;i++) {
//            printer.EscPosCommandExe("normal\n".getBytes());
//        }

        //CUT  !!!!!!!! Non fa niente
        printer.EscPosCommandExe(new byte[]{29,86,1});


    }

    void doPrintRawUsbSerial() throws CommonException {


        UsbDeviceConnection connection = usbManager.openDevice(printerDevice);
        if (connection == null) {
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




            port.write(new byte[]{27,33,0}, 1000);
            port.write("012345678901234567890123456789012345678901234567".getBytes(), 1000);
//            port.write(new byte[]{27,33,0}, 1000);
//            for (int i=0;i<20;i++)  port.write(new byte[]{'a'}, 1000);

            for (int i=0;i<10;i++)  port.write(new byte[]{'\n'}, 1000);


//            cmd= new byte[3];
//            cmd[0]=27;
//            cmd[1]=33;
//            cmd[2]=16+BOLD;
//            port.write(cmd, 1000);
//
//
//            for (int i=0;i<rep;i++) {
//                port.write("double H\n".getBytes(), 1000);
//
//            }
//            port.write(new byte[]{0x0a}, 1000);
//
//
//            cmd= new byte[3];
//            cmd[0]=27;
//            cmd[1]=33;
//            cmd[2]=32+BOLD;
//            port.write(cmd, 1000);
//
//
//
//            for (int i=0;i<rep;i++)
//                port.write("double w\n".getBytes(), 1000);
//            port.write(new byte[]{0x0a}, 1000);
//
//
//            cmd= new byte[3];
//            cmd[0]=27;
//            cmd[1]=33;
//            cmd[2]=16+32+BOLD;
//            port.write(cmd, 1000);
//
//
//
//            for (int i=0;i<rep;i++) {
//
//
//                port.write("double wh\n".getBytes(), 1000);
//            }
//            port.write(new byte[]{0x0a}, 1000);
//
//            cmd= new byte[3];
//            cmd[0]=27;
//            cmd[1]=33;
//            cmd[2]=BOLD;
//            port.write(cmd, 1000);
//
//            for (int i=0;i<rep;i++)
//                port.write("normal\n".getBytes(), 1000);
//            port.write(new byte[]{0x0a}, 1000);
//
//
//
//            port.write(new byte[]{(byte)0xa0,(byte)0xa1,(byte)0xa2,(byte)0xa3,0x0a},1000);

            //CUT
            cmd= new byte[3];
            cmd[0]=29;
            cmd[1]=86;
            cmd[2]=1;
            port.write(cmd, 1000);

            port.write(new byte[]{0x0c}, 1000);
            port.close();

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


        ((TextView)findViewById(R.id.textViewPackage)).setText(getApplicationContext().getPackageName());


//        Intent intent=new Intent();
//        intent.setAction("android.intent.action.cert.white.list");
//        intent.putExtra("white_list","com.rch.telpoprinter1");
//        sendBroadcast(intent);

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
                                //doPrintRawUsbSerial();
                                doPrintByUsbSerialPrinter();

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




        ToggleButton toggleButton= findViewById(R.id.toggleButton);

        toggleButton.setTextOn("ASK USB Perm.");
        toggleButton.setTextOff("Do not ASK USB Perm.");

        toggleButton.setChecked(askUsbPermission);

        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
              askUsbPermission= b;
            }
        });



        printerTV= findViewById(R.id.textViewPrinter);
        dsTV= findViewById(R.id.textViewDS);

//        UsbManager usbManager = (UsbManager) getSystemService(USB_SERVICE);
//
//        HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
//        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
//        while(deviceIterator.hasNext()){
//            UsbDevice device = deviceIterator.next();
//            UsbDeviceConnection connection = usbManager.openDevice(device);
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

        usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

        String ACTION_USB_PERMISSION =
                "com.android.example.USB_PERMISSION";
         BroadcastReceiver usbReceiver = new BroadcastReceiver() {

            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (ACTION_USB_PERMISSION.equals(action)) {
                    synchronized (this) {
                        UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                        if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                            if(device != null){
                                //call method to set up device communication
                                printerDevice=device;
                            }
                        }
                        else {
                            Log.d("", "permission denied for device " + device);
                        }
                    }
                }
            }
        };


        PendingIntent permissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), PendingIntent.FLAG_MUTABLE);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        registerReceiver(usbReceiver, filter);



        ProbeTable customTable = new ProbeTable();
        if (printerT==P_TELPO)
            switch (printerT) {
                case P_TELPO:
                    customTable.addProduct(0x28e9, 0x28d, ChromeCcdSerialDriver.class);
                    break;
                case P_SNBC:
                    customTable.addProduct(0x154f, 0x154f, ChromeCcdSerialDriver.class);
                    break;
                case P_PRINT:
                default:
                    customTable.addProduct(0x1fc9, 0x2016, ChromeCcdSerialDriver.class);
            }


        UsbSerialProber prober = new UsbSerialProber(customTable);

        List<UsbSerialDriver> availableDrivers = prober.findAllDrivers(usbManager);
        if (availableDrivers.isEmpty())
            return;

        driver = availableDrivers.get(0);

        if (askUsbPermission)
        usbManager.requestPermission(driver.getDevice(), permissionIntent);
        else
            printerDevice= driver.getDevice();;


    }

    void dleEot1(UsbThermalPrinter printer,byte n) throws CommonException {
        int ret1 = printer.checkStatus();
        int ret= printer.EscPosCommandExe(new byte[]{0x10,4,n});


        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                printerTV.setText(printerTV.getText().toString() + "\n" + "DLE EOT "+n+ ": 0X"+Integer.toHexString(ret)+ ": 0X"+Integer.toHexString(ret1));
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