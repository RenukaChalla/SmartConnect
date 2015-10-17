package com.rfid.smartconnect.smartconnect;

import android.app.Activity;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.FormatException;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Set;


public class BluetoothActivity extends Activity {
    protected PendingIntent pendingIntent;
    private static String TAG = MainActivity.class.getSimpleName();
    private final static int REQUEST_ENABLE_BT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, this.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        setContentView(R.layout.activity_bluetooth);
        Button btnbluetooth = (Button) findViewById(R.id.bluetooth_btn);
        //final TextView message = (TextView) findViewById(R.id.edit_message);
        btnbluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleBluetooth();
            }
        });
        Button btnbluetoothDiscover = (Button) findViewById(R.id.discover_btn);
        btnbluetoothDiscover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                discoverBluetooth();
            }
        });
    }


    public void onNewIntent(Intent intent) { //
        Log.d(TAG, "onNewIntent");

        handleBluetooth();

    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String display = "New devices found are: ";
            TextView textView = (TextView) findViewById(R.id.title);
            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                //discovery starts, we can show progress dialog or perform other tasks
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                //discovery finishes, dismis progress dialog
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                //bluetooth device found
                BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                display += device.getName();
            }
            textView.setText(display);
        }
    };

    public void discoverBluetooth() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
        } else {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
            //DISCOVER THE DEVICES
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            IntentFilter filter = new IntentFilter();

            filter.addAction(BluetoothDevice.ACTION_FOUND);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

            registerReceiver(mReceiver, filter);
            adapter.startDiscovery();
        }
    }

    public void handleBluetooth() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
        } else {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
            if (pairedDevices.size() > 0) {
                String device_name = null;
                String display = "Paired devices are: \n";
                TextView textView = (TextView) findViewById(R.id.title);
                //ArrayAdapter mArrayAdapter = new ArrayAdapter<String>(this, device_name);
                for (BluetoothDevice device : pairedDevices) {
                    display = display + device.getName() + "  " + device.getAddress() + "\n";
                }
                textView.setText(display);
                //AcceptThread acceptThread = new AcceptThread();
            }
        }
    }

    public void onDestroy() {
        unregisterReceiver(mReceiver);

        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_bluetooth, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}



