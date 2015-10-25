package com.rfid.smartconnect.smartconnect;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class BTActivity extends AppCompatActivity implements Runnable, View.OnClickListener {

    private ListView listViewPaired;
    private ListView listViewDetected;
    private TextView pairedBtn;
    private TextView availableBtn;
    private ImageView imgSearch, imgOn, imgDisc, imgOff;
    private ArrayAdapter<String> adapter, detectedAdapter;
    private List<String> arrayListpaired = new ArrayList<>();
    private List<BluetoothDevice> arrayListPairedBluetoothDevices;
    private List<BluetoothDevice> arrayListBluetoothDevices = new ArrayList<>();
    private UUID applicationUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private ProgressDialog mBluetoothConnectProgressDialog;
    private BluetoothSocket mBluetoothSocket;
    private BluetoothDevice bdDevice;
    private BluetoothClass bdClass;
    private String bt_device_address;
    private String bt_device_name;
    private BluetoothAdapter bluetoothAdapter;
    public static final int BOND_BONDED = 12;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bt);
        setupToolbar();
        initUi();
        setListeners();
        onClick(pairedBtn); // default click on paired button
        getDefaultPairedDevices();
        setAdapters();
        //bdDevice = getIntent().getStringExtra("device_name");
    }

    private void setAdapters() {
        adapter = new ArrayAdapter<String>(BTActivity.this, android.R.layout.simple_list_item_1, arrayListpaired);
        detectedAdapter = new ArrayAdapter<String>(BTActivity.this, android.R.layout.simple_list_item_single_choice);
        listViewDetected.setAdapter(detectedAdapter);
        detectedAdapter.notifyDataSetChanged();
        listViewPaired.setAdapter(adapter);
    }

    private void getDefaultPairedDevices() {
        arrayListpaired = new ArrayList<String>();
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        arrayListPairedBluetoothDevices = new ArrayList<BluetoothDevice>();
    }

    private void setupToolbar() {
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
    }

    private void setListeners() {
        pairedBtn.setOnClickListener(this);
        availableBtn.setOnClickListener(this);
        imgOn.setOnClickListener(this);
        imgSearch.setOnClickListener(this);
        imgDisc.setOnClickListener(this);
        imgOff.setOnClickListener(this);
        listViewDetected.setOnItemClickListener(listItemClicked);
        listViewPaired.setOnItemClickListener(listItemClickedOnPaired);
    }

    private void initUi() {
        pairedBtn = (TextView) findViewById(R.id.paired_btn);
        availableBtn = (TextView) findViewById(R.id.available_btn);
        listViewDetected = (ListView) findViewById(R.id.listViewDetected);
        listViewPaired = (ListView) findViewById(R.id.listViewPaired);
        imgSearch = (ImageView) findViewById(R.id.imgSearch);
        imgOn = (ImageView) findViewById(R.id.imgOn);
        imgDisc = (ImageView) findViewById(R.id.imgDisc);
        imgOff = (ImageView) findViewById(R.id.imgOff);
    }

    @Override
    protected void onStart() {
        super.onStart();
        getPairedDevices();
        bt_device_name = getIntent().getStringExtra("bt_device_name");
        bt_device_address = getIntent().getStringExtra("bt_device_address");
        checkforBond();
    }

    private void checkforBond(){
        if(bt_device_address!= null){
            bluetoothConnect(bt_device_name, bt_device_address);
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        bt_device_name = getIntent().getStringExtra("bt_device_name");
        bt_device_address = getIntent().getStringExtra("bt_device_address");
        checkforBond();
        //bluetoothConnect(bdDevice);
    }

    private void getPairedDevices() {
        arrayListPairedBluetoothDevices.clear();
        arrayListpaired.clear();
        Set<BluetoothDevice> pairedDevice = bluetoothAdapter.getBondedDevices();
        if (pairedDevice.size() > 0) {
            for (BluetoothDevice device : pairedDevice) {
                arrayListpaired.add(device.getName() + "\n" + device.getAddress());
                arrayListPairedBluetoothDevices.add(device);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private AdapterView.OnItemClickListener listItemClicked = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            bdDevice = arrayListBluetoothDevices.get(position);
            //bdClass = arrayListBluetoothDevices.get(position);
            Log.i("Log", "The device : " + bdDevice.toString());
            /*
             * here below we can do pairing without calling the callthread(), we can directly call the
             * connect(). but for the safer side we must usethe threading object.
             */
            //callThread();
            //connect(bdDevice);
            Boolean isBonded = false;
            try {
                isBonded = createBond(bdDevice);
                if (isBonded) {
                    //arrayListpaired.add(bdDevice.getName()+"\n"+bdDevice.getAddress());
                    //adapter.notifyDataSetChanged();
                    getPairedDevices();
                    adapter.notifyDataSetChanged();
                    if (bdDevice.getBondState() == BOND_BONDED) {
                        //write to NFC tag
                        Intent nfcintent = new Intent(BTActivity.this, MainActivity.class);
                        nfcintent.putExtra("bt_device", bdDevice);
                        //nfcintent.putExtra("bt_address", bdDevice.getAddress());
                        startActivity(nfcintent);
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }//connect(bdDevice);
            Log.i("Log", "The bond is created: " + isBonded);
        }
    };

    public void run() {
        try {
            mBluetoothSocket = bdDevice.createRfcommSocketToServiceRecord(applicationUUID);
            bluetoothAdapter.cancelDiscovery();
            mBluetoothSocket.connect();
            mHandler.sendEmptyMessage(0);
        } catch (IOException eConnectException) {
            Log.d("Log", "CouldNotConnectToSocket", eConnectException);
            closeSocket(mBluetoothSocket);
            return;
        }
    }

    private void closeSocket(BluetoothSocket nOpenSocket) {
        try {
            nOpenSocket.close();
            Log.d("Log", "SocketClosed");
        } catch (IOException ex) {
            Toast.makeText(BTActivity.this, "Device Not Connected", Toast.LENGTH_LONG).show();
            Log.d("Log", "CouldNotCloseSocket");
        }
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Toast.makeText(BTActivity.this, "Device Connected", Toast.LENGTH_LONG).show();
        }
    };

    private AdapterView.OnItemClickListener listItemClickedOnPaired = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            bdDevice = arrayListPairedBluetoothDevices.get(position);
            bluetoothConnect(bdDevice.getName(), bdDevice.getAddress());
        }
    };

    private void bluetoothConnect(String bt_device_name, String bt_device_address) {
        try {
            bdDevice = bluetoothAdapter.getRemoteDevice(bt_device_address);
            Log.v("Log", "Coming incoming address " + bt_device_address);
            Thread mBlutoothConnectThread = new Thread(BTActivity.this);
            mBlutoothConnectThread.start();
            /*
            Boolean removeBonding = removeBond(bdDevice);
            if(removeBonding) {
                arrayListpaired.remove(position);
                adapter.notifyDataSetChanged();
            }
            Log.i("Log", "Removed"+removeBonding);
            */
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*private void callThread() {
        new Thread(){
            public void run() {
                Boolean isBonded = false;
                try {
                    isBonded = createBond(bdDevice);
                    if(isBonded)
                    {
                        arrayListpaired.add(bdDevice.getName()+"\n"+bdDevice.getAddress());
                        adapter.notifyDataSetChanged();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }//connect(bdDevice);
                Log.i("Log", "The bond is created: "+isBonded);
            }
        }.start();
    }*/

    private Boolean connect(BluetoothDevice bdDevice) {
        Boolean bool = false;
        try {
            Log.i("Log", "service method is called ");
            Class cl = Class.forName("android.bluetooth.BluetoothDevice");
            Class[] par = {};
            Method method = cl.getMethod("createBond", par);
            Object[] args = {};
            bool = (Boolean) method.invoke(bdDevice);//, args);// this invoke creates the detected devices paired.
        } catch (Exception e) {
            Log.i("Log", "Inside catch of serviceFromDevice Method");
            e.printStackTrace();
        }
        return bool.booleanValue();
    }

    public boolean removeBond(BluetoothDevice btDevice) throws Exception {
        Class btClass = Class.forName("android.bluetooth.BluetoothDevice");
        Method removeBondMethod = btClass.getMethod("removeBond");
        Boolean returnValue = (Boolean) removeBondMethod.invoke(btDevice);
        return returnValue.booleanValue();
    }


    public boolean createBond(BluetoothDevice btDevice) throws Exception {
        Class class1 = Class.forName("android.bluetooth.BluetoothDevice");
        Method createBondMethod = class1.getMethod("createBond");
        Boolean returnValue = (Boolean) createBondMethod.invoke(btDevice);
        return returnValue.booleanValue();
    }

    private BroadcastReceiver myReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Message msg = Message.obtain();
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                Toast.makeText(context, "ACTION_FOUND", Toast.LENGTH_SHORT).show();
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                try {
                    //device.getClass().getMethod("setPairingConfirmation", boolean.class).invoke(device, true);
                    //device.getClass().getMethod("cancelPairingUserInput", boolean.class).invoke(device);
                } catch (Exception e) {
                    Log.i("Log", "Inside the exception: ");
                    e.printStackTrace();
                }

                if (arrayListBluetoothDevices.size() < 1) // this checks if the size of bluetooth device is 0,then add the
                {                                           // device to the arraylist.
                    detectedAdapter.add(device.getName() + "\n" + device.getAddress());
                    if (!(arrayListBluetoothDevices.contains(device))) {
                        arrayListBluetoothDevices.add(device);
                    }
                    detectedAdapter.notifyDataSetChanged();
                } else {
                    boolean flag = true;    // flag to indicate that particular device is already in the arlist or not
                    for (int i = 0; i < arrayListBluetoothDevices.size(); i++) {
                        if (device.getAddress().equals(arrayListBluetoothDevices.get(i).getAddress())) {
                            flag = false;
                        }
                    }
                    if (flag == true) {
                        detectedAdapter.add(device.getName() + "\n" + device.getAddress());
                        if (!arrayListBluetoothDevices.contains(device)) {
                            arrayListBluetoothDevices.add(device);
                        }
                        detectedAdapter.notifyDataSetChanged();
                    }
                }
            }
        }
    };

    private void startSearching() {
        Log.i("Log", "in the start searching method");
        IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        BTActivity.this.registerReceiver(myReceiver, intentFilter);
        bluetoothAdapter.startDiscovery();
    }

    private void onBluetooth() {
        if (!bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.enable();
            Log.i("Log", "Bluetooth is Enabled");
        }
    }

    private void offBluetooth() {
        if (bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.disable();
        }
    }

    private void makeDiscoverable() {
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);
        Log.i("Log", "Discoverable ");
    }

    private Handler handlerSearch = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 111:
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.available_btn:
                availableBtn.setActivated(true);
                pairedBtn.setActivated(false);
                listViewPaired.setVisibility(View.GONE);
                listViewDetected.setVisibility(View.VISIBLE);
                break;
            case R.id.paired_btn:
                availableBtn.setActivated(false);
                pairedBtn.setActivated(true);
                listViewPaired.setVisibility(View.VISIBLE);
                listViewDetected.setVisibility(View.GONE);
                break;
            case R.id.imgOn:
                onBluetooth();
                getPairedDevices();
                startSearching();
                break;
            case R.id.imgSearch:
                startSearching();
                break;
            case R.id.imgDisc:
                makeDiscoverable();
                break;
            case R.id.imgOff:
                offBluetooth();
                break;
            default:
                break;

        }
    }
}
