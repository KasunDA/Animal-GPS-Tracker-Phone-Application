package com.s3543757.alexanderknapik.animalsatrisk;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.regex.Pattern;

public class UDPTester extends Activity {

    //Variables used in the class
    private TextView txtUDP;            // Text to show the contents of the UDP packet.
    private TextView txtUDPAddress;     // Text to show the Address and socket listening to UDP packets
    private Discoverer mDiscoverer ;    // UDP broadcast thread in Discoverer.java
    private Handler handler ;           // Used by threads to wake up activity.

    String deviceReply ;                // device reply put in here by thread.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_udptester) ;
        txtUDP          = (TextView) findViewById(R.id.txtUDP);
        txtUDPAddress   = (TextView) findViewById(R.id.txtUDPAddress);

        //Set the textView to the IP address.
        txtUDPAddress.setText(getLocalIpAddress());

        handler = new Handler();
        txtUDP.setText("No activity");

        //--- create thread and start it running with SSID and PW of LAN to join.
        mDiscoverer = new Discoverer( this, this  ) ; // Create new thread
        mDiscoverer.start();
        txtUDP.setText( "Listening" ) ;
        //--- thread Discoverer now sending broadcast.
    }

    private Runnable myRunnable = new Runnable() { //--- wake up activity caused by guiWakeup().
        //@Override // should need this but causes an error.
        public void run() {
            //
            txtUDP.setText( deviceReply ) ;
            // if need to restart timer do so.
            handler.postDelayed(this, 100);
        }
    };

    void guiWakeup () { //--- called from thread which results in runnable above being called.
        handler.postDelayed(myRunnable, 0); // delay is zero.
    }

    //========================================================================
    //Get the IP address of the device.
    //This small section of code is provided by stack overflow users evertvandenbruel & beigleux
    //https://stackoverflow.com/questions/6064510/how-to-get-ip-address-of-the-device-from-code
    //Copyright (C) 2012
    private static String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
        return null;
    }
    //End contribution
}
