/* Click on + to show Introduction.
 
   PURPOSE - simple program to UDP broadcast to all hosts on a LAN and 
             look for a reply.
             
   DETAILS - From Android 3 onward all networking must be in a thread, this
             also works for Android 2.
             
           - The thread is placed in a separate file.  
             To create a new file right click on the package name src/com.*
             and select New->Other-Java-class.
             
           - The standard ways to communicate between an activity and a 
             thread include localBroadcastManager, intents, handlers, and more.
             These are good and make the activity and handler more independent
             of each other and avoid thread programming problems.
             
             This code uses the ability of classes in the same package to access
             each other's functions and data.
             An activity and thread can change each other's data freely but take 
             care with async problems as the order of access can be anything.
             An activity can call a thread function safely, again apart 
             from async issues.
             If a thread tries to call an activity function that affects views then
             a run time error occurs.  A simple handler can solve this problem as
             seen at the bottom of this code.  When the thread wants to wake up 
             the activity it calls guiWakeup(), and 1ms later or less the operating 
             system calls "runnable" in the context of the activity so changes to 
             the GUI views can be made.
 */

package com.pjr;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;



public class MainActivity extends Activity {
	
//------ Variables ---------------------------------------------------
	private EditText text_SSID;      // GUI widgets.       
	private EditText text_PW;            
	private EditText text_Result;         
	private Discoverer mDiscoverer ; // UDP broadcast thread in Discoverer.java
	         String deviceReply ;     // device reply put in here by thread.
	private Handler handler ;        // Used by threads to wake up activity.
	
	
//=========================================================================================	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main) ;
        text_SSID   = (EditText) findViewById(R.id.editText_SSID) ;
        text_PW     = (EditText) findViewById(R.id.editText_PW) ;
    	text_Result = (EditText) findViewById(R.id.editText_Result) ;
    	handler = new Handler();
    	text_Result.setText("") ;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    
 //====================================================================================  
    
    public void onMyClick(View view) {//--- function named in OnClick property of button
    	//--- create thread and start it running with SSID and PW of LAN to join.
    	mDiscoverer = new Discoverer( text_SSID.getText().toString(), // Create new thread.
    			                      text_PW.getText().toString(), this, this  ) ; 
    	mDiscoverer.start();            	
    	text_Result.setText( "Sending broadcast out ..." ) ;
    	//--- thread Discoverer now sending broadcast.   	
    }
 
    public void onQuit(View view) {
    	finish() ;
    }
 
    
//======================================================================================
    
    private Runnable myRunnable = new Runnable() { //--- wake up activity caused by guiWakeup().
    	   //@Override // should need this but causes an error.
    	   public void run() {
    	      //
    		   text_Result.setText( deviceReply ) ;  
    	      // if need to restart timer do so.
    	      //handler.postDelayed(this, 100);
    	   }
    	};
    	
    void guiWakeup () { //--- called from thread which results in runnable above being called.
    	handler.postDelayed(myRunnable, 0); // delay is zero.	 
    }
    
}

