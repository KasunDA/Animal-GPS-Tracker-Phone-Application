/* Click on Plus to see program description.
  
PURPOSE - run a thread to send a broadcast and receive a single reply. 
          Used for device discovery.

   NOTE - From Android 3 must run network tasks in a thread or async task.
          Will also work in Android 2.
        - Discovery terminated by timeout or client device reception.  
        - This code assumes a single replying device is on the wifi.
        - There are 5 permissions that must be added to the manifest to
          enable access to wifi, broadcasting, and changing wifi state.
        - This thread may access data and functions in the activity that
          created it but it may not call functions that effect any views,  
          doing so results in a run time error.
          The calling activity may access data and functions of this thread.
        - Items that must be accessible outside the class do not have a
          modifier (eg private) so they are only accessible within the package.
          
FURTHER - to turn the hotspot on and off see       
            http://stackoverflow.com/questions/6394599/android-turn-on-off-wifi-hotspot-programmatically
          to change SSID and password
            http://stackoverflow.com/questions/7221712/cant-set-wificonfiguration-when-enabling-wifi-hotspot-using-setwifiapenabled 
*/
 package com.pjr;
 

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

import android.app.Activity;
import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

class Discoverer extends Thread {
	
//------ Variables	
	  private static final String TAG = "Discovery";    // filter for logcat
	  private static final int DISCOVERY_PORT = 50010;
	  private static final int TIMEOUT_MS = 1000;
	  
      private WifiManager mWifi;
      private Context context;       // of activity that calls this class.
      private String LAN_SSID ;
      private String LAN_PW ;
      private String broadcastData ; // sent to all devices on LAN.
      private String deviceData ;    // Reply from a single device.
      private MainActivity callingActivity ;
      
      


//====== Constructor ========================================================
      
	  Discoverer(String SSID, String PW, Context activityContext, Activity A) { 
		 //--- Capture all parameters. 
		 LAN_SSID = SSID ;
		 LAN_PW = PW ; 
		 context = activityContext ;
		 broadcastData = LAN_SSID + "," + LAN_PW + "\0";
		 deviceData = "Did not even receive own broadcast, wifi down?" ;
		 callingActivity = (MainActivity)A ; // This is a pointer to the main activity.
	  }
	  
	  

//====== Run ================================================================
	  
	  public void run() { //--- executes when thread started.
		
	    try {	   
	      //--- get access to wifi and grab socket.	
		  Log.d(TAG, "Started thread.");
	      mWifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);    
	      DatagramSocket socket = new DatagramSocket(DISCOVERY_PORT);
	      socket.setBroadcast(true);
	      socket.setSoTimeout(TIMEOUT_MS);

	      //--- send broadcast and wait for reply.
	      sendDiscoveryRequest(socket);
	      listenForResponses(socket);	  
	      socket.close();
	      
	      //--- write communications result into data of calling activity, then wake up main activity and stop.
	      callingActivity.deviceReply = deviceData  ; // write data into main activity data element.
	      callingActivity.guiWakeup() ;               // Cause main activity member to be called by OS.
	    } 
	    catch (IOException e) {
	      Log.e(TAG, "Could not send discovery request", e); 
	      callingActivity.deviceReply = "Send or receive of UDP failed." ;
	      callingActivity.guiWakeup() ; 
	    }
	    
	  }

//=========================================================================
	  /** 
	   * Send a broadcast UDP packet containing data for clients.
	   * 
	   * @throws IOException
	   */
	  private void sendDiscoveryRequest(DatagramSocket socket) throws IOException {
		Log.d(TAG, "Sending data " + broadcastData);
	    DatagramPacket packet = new DatagramPacket(broadcastData.getBytes(), broadcastData.length(),
	        getBroadcastAddress(), DISCOVERY_PORT);
	    socket.send(packet);
	  }

//=========================================================================	  
//	  
// For wifi get broadcast address,  for hotspot this gets reported incorrectly
// so just used the fixed value 192.168.43.255.  
	
  private InetAddress getBroadcastAddress() throws IOException {
  	    
    String IP = "" ;
    byte[] quads = new byte[4];
    
    if( isApOn( context) ) { // hotspot active so broadcast fixed.
      IP = "192.168.43.255" ;	
	  quads[0] = (byte) 192 ;  	
	  quads[1] = (byte) 168 ;  	
	  quads[2] = (byte) 43 ;  	
	  quads[3] = (byte) 255 ;  	
	}
	else { // assume wifi active not hotspot.
		DhcpInfo dhcp = mWifi.getDhcpInfo();
		if (dhcp == null) {
		  Log.d(TAG, "Could not get dhcp info");
		  return null;
		}
        int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
	    int x ;
	    for (int k = 0; k < 4; k++){
	      x =  ((broadcast >> k * 8) & 0xFF);
	      IP = IP + x + "." ;
	      quads[k] = (byte) x ;
	    }  
	}
	    
	    Log.d(TAG, "Broadcast IP: " + InetAddress.getByAddress(quads).getHostAddress()) ;
	    return InetAddress.getByAddress(quads);
}

	  
//==========================================================================
//check whether wifi hotspot on or off
	  public static boolean isApOn(Context context) {
	      WifiManager wifimanager = (WifiManager) context.getSystemService(context.WIFI_SERVICE);     
	      try {
	          Method method = wifimanager.getClass().getDeclaredMethod("isWifiApEnabled");
	          method.setAccessible(true);
	          return (Boolean) method.invoke(wifimanager);
	      }
	      catch (Throwable ignored) {}
	      return false;
	  }
	  
	  
	  
//==========================================================================
	  /**
	   * Listen on socket for responses, timing out after TIMEOUT_MS
	   * 
	   * @param socket
	   *          socket on which the announcement request was sent
	   * @throws IOException
	   */
	  private void listenForResponses(DatagramSocket socket) throws IOException {
	    byte[] buf = new byte[1024];
	    try {
	      while (true) {
	        DatagramPacket packet = new DatagramPacket(buf, buf.length); 
	        socket.receive(packet);
	        String s = new String(packet.getData(), 0, packet.getLength());
	        //--- assume reply packet is different to sent packet.
	        if ( s.equals(broadcastData) ) {
			   Log.d(TAG, "Received own broadcast: " + s);
			   deviceData = "Received own broadcast, wifi working, no device." ;
	        }	
	        else {
		       Log.d(TAG, "Received device response: " + s);	
		       deviceData = s ;
		       return ; // stop listening as only expect one device reply.
	        }	
	      }
	    } catch (SocketTimeoutException e) { //--- timeout exits the listener if no client.
	      Log.d(TAG, "Receive timeout.");
	      socket.close();
	    }
	  }

//========================================================================
	  
	  String discoveryReply() {  // not needed but shows main activity can access thread function.
		  return( deviceData);	  
	  }
	   
}


