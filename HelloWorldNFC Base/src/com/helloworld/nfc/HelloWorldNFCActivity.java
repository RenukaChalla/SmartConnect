package com.helloworld.nfc;

import java.util.List;

import org.ndeftools.Message;
import org.ndeftools.Record;
import org.ndeftools.externaltype.AndroidApplicationRecord;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.Vibrator;
import android.util.Log;
import android.widget.TextView;


public class HelloWorldNFCActivity extends Activity {
    /** Called when the activity is first created. */
	
	private static String TAG = HelloWorldNFCActivity.class.getSimpleName();
	
	protected NfcAdapter nfcAdapter;
    protected PendingIntent nfcPendingIntent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
    	Log.d(TAG, "onResume");

        setContentView(R.layout.main);
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	
    	Log.d(TAG, "onResume");
    	
    	// enableForegroundMode();
    }
    
    @Override
    protected void onPause() { 
    	super.onResume();

    	Log.d(TAG, "onPause");

    	// disableForegroundMode();
    }

	@Override
	public void onNewIntent(Intent intent) { //
		Log.d(TAG, "onNewIntent");

		
	}

	/** 
	 * Activate device vibrator for 500 ms 
	 * */
	
	private void vibrate() {
		Log.d(TAG, "vibrate");

		Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE) ;
		vibe.vibrate(500);
	}


}