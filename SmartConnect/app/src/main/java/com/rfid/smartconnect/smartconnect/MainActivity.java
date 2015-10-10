package com.rfid.smartconnect.smartconnect;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.ndeftools.Message;
import org.ndeftools.Record;
import org.ndeftools.externaltype.AndroidApplicationRecord;
import android.annotation.SuppressLint;

import android.app.Activity;
import android.app.PendingIntent;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.FormatException;
import android.nfc.tech.NdefFormatable;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.os.Parcelable;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

@SuppressLint({ "ParserError", "ParserError" })
public class MainActivity extends ActionBarActivity {

    private static String TAG = MainActivity.class.getSimpleName();
    private final static int REQUEST_ENABLE_BT = 1;
    protected NfcAdapter nfcAdapter;
    protected PendingIntent nfcPendingIntent;
    protected NfcAdapter adapter;
    protected PendingIntent pendingIntent;
    IntentFilter writeTagFilters[];
    boolean writeMode;
    Tag mytag;
    Context ctx;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // initialize NFC
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        nfcPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, this.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        Log.d(TAG, "onResume");
        setContentView(R.layout.activity_main);
        ctx=this;
        Button btnWrite = (Button) findViewById(R.id.write_btn);
        final TextView message = (TextView)findViewById(R.id.edit_message);
        btnWrite.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                try {
                    if(mytag==null){
                        Toast.makeText(ctx, ctx.getString(R.string.error_detected), Toast.LENGTH_LONG ).show();
                    }else{
                        write(message.getText().toString(),mytag);
                        Toast.makeText(ctx, ctx.getString(R.string.ok_writing), Toast.LENGTH_LONG ).show();
                    }
                } catch (IOException e) {
                    Toast.makeText(ctx, ctx.getString(R.string.error_writing), Toast.LENGTH_LONG ).show();
                    e.printStackTrace();
                } catch (FormatException e) {
                    Toast.makeText(ctx, ctx.getString(R.string.error_writing) , Toast.LENGTH_LONG ).show();
                    e.printStackTrace();
                }
            }
        });

        adapter = NfcAdapter.getDefaultAdapter(this);
        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        tagDetected.addCategory(Intent.CATEGORY_DEFAULT);
        writeTagFilters = new IntentFilter[] { tagDetected };

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        enableForegroundMode();
    }

    @Override
    protected void onPause() {
        super.onResume();
        Log.d(TAG, "onPause");
        disableForegroundMode();
    }
    private void write(String text, Tag tag) throws IOException, FormatException {

        NdefRecord[] records = { createRecord(text) };
        NdefMessage  message = new NdefMessage(records);
        // Get an instance of Ndef for the tag.
        Ndef ndef = Ndef.get(tag);
        // Enable I/O
        ndef.connect();
        // Write the message
        ndef.writeNdefMessage(message);
        // Close the connection
        ndef.close();
    }
    @Override
    public void onNewIntent(Intent intent) { //
        Log.d(TAG, "onNewIntent");
        // check for NFC related actions
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
            TextView textView = (TextView) findViewById(R.id.title);
            textView.setText("Hello NFC tag!");
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            new NdefReaderTask().execute(tag);
            readMessages(intent);
            handleBluetooth();
            mytag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            Toast.makeText(this, this.getString(R.string.ok_detection) + mytag.toString(), Toast.LENGTH_LONG ).show();
        }
    }

    private NdefRecord createRecord(String text) throws UnsupportedEncodingException {
        String lang       = "en";
        byte[] textBytes  = text.getBytes();
        byte[] langBytes  = lang.getBytes("US-ASCII");
        int    langLength = langBytes.length;
        int    textLength = textBytes.length;
        byte[] payload    = new byte[1 + langLength + textLength];

        // set status byte (see NDEF spec for actual bits)
        payload[0] = (byte) langLength;

        // copy langbytes and textbytes into payload
        System.arraycopy(langBytes, 0, payload, 1,              langLength);
        System.arraycopy(textBytes, 0, payload, 1 + langLength, textLength);

        NdefRecord recordNFC = new NdefRecord(NdefRecord.TNF_WELL_KNOWN,  NdefRecord.RTD_TEXT,  new byte[0], payload);

        return recordNFC;
    }

    public void readMessages(Intent intent) {
        Parcelable[] messages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        if (messages != null) {
            Log.d(TAG, "Found " + messages.length + " NDEF messages");
            vibrate(); // signal found messages :-)
            for (int i = 0; i < messages.length; i++) {
                try {
                    List<Record> records = new org.ndeftools.Message((NdefMessage) messages[i]);
                    Log.d(TAG, "Found " + records.size() + " records in message " + i);
                    for (int k = 0; k < records.size(); k++) {
                        Log.d(TAG, " Record #" + k + " is of class " + records.get(k).getClass().getSimpleName());
                        Record record = records.get(k);
                        TextView textView = (TextView) findViewById(R.id.title);
                        //textView.setText(record.text.value);
                        if (record instanceof AndroidApplicationRecord) {
                            AndroidApplicationRecord aar = (AndroidApplicationRecord) record;
                            Log.d(TAG, "Package is " + aar.getPackageName());
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Problem parsing message", e);
                }

            }
        }
    }

    /**
     * Activate device vibrator for 500 ms
     */
    private void vibrate() {
        Log.d(TAG, "vibrate");

        Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibe.vibrate(500);
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
                //ArrayAdapter mArrayAdapter = new ArrayAdapter<String>(this, device_name);
                for (BluetoothDevice device : pairedDevices) {
                    TextView textView = (TextView) findViewById(R.id.title);
                    textView.setText(device.getName() + "\n" + device.getAddress());
                }
                //AcceptThread acceptThread = new AcceptThread();

            }
        }
    }

    public void enableForegroundMode() {
        Log.d(TAG, "enableForegroundMode");
        // foreground mode gives the current active application priority for reading scanned tags
        writeMode = true;
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        IntentFilter[] writeTagFilters = new IntentFilter[]{tagDetected};
        nfcAdapter.enableForegroundDispatch(this, nfcPendingIntent, writeTagFilters, null);
    }

    public void disableForegroundMode() {
        Log.d(TAG, "disableForegroundMode");
        writeMode = false;
        nfcAdapter.disableForegroundDispatch(this);
    }

    private class NdefReaderTask extends AsyncTask<Tag, Void, String> {
        @Override
        protected String doInBackground(Tag... params) {
            Tag tag = params[0];
            Ndef ndef = Ndef.get(tag);
            if (ndef == null) {
                // NDEF is not supported by this Tag.
                return null;
            }
            NdefMessage ndefMessage = ndef.getCachedNdefMessage();
            NdefRecord[] records = ndefMessage.getRecords();
            for (NdefRecord ndefRecord : records) {
                if (ndefRecord.getTnf() == NdefRecord.TNF_WELL_KNOWN && Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_TEXT)) {
                    try {
                        return readText(ndefRecord);
                    } catch (UnsupportedEncodingException e) {
                        Log.e(TAG, "Unsupported Encoding", e);
                    }
                }
            }
            return null;
        }

        private String readText(NdefRecord record) throws UnsupportedEncodingException {
        /*
         * See NFC forum specification for "Text Record Type Definition" at 3.2.1
         *
         * http://www.nfc-forum.org/specs/
         *
         * bit_7 defines encoding
         * bit_6 reserved for future use, must be 0
         * bit_5..0 length of IANA language code
         */
            byte[] payload = record.getPayload();
            String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16";
            int languageCodeLength = payload[0] & 0063;
            // String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");
            // e.g. "en"
            // Get the Text
            return new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
        }
        private class AcceptThread extends Thread {
            private final BluetoothServerSocket mmServerSocket;

            public AcceptThread() {
                // Use a temporary object that is later assigned to mmServerSocket,
                // because mmServerSocket is final
                BluetoothServerSocket tmp = null;
                try {
                    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                    Method getUuidsMethod = BluetoothAdapter.class.getDeclaredMethod("getUuids", null);

                    ParcelUuid[] uuids = (ParcelUuid[]) getUuidsMethod.invoke(mBluetoothAdapter, null);
                    TextView textView = (TextView) findViewById(R.id.title);
                    UUID MY_UUID = UUID.randomUUID();
                    String NAME = "My_NAME";
                    for (ParcelUuid uuid : uuids) {
                        textView.setText("UUID: " + uuid.getUuid().toString());
                        MY_UUID = uuid.getUuid();
                    }
                    // MY_UUID is the app's UUID string, also used by the client code
                    tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
                } catch (Exception e) {
                }
                mmServerSocket = tmp;
            }

            public void run() {
                BluetoothSocket socket = null;
                // Keep listening until exception occurs or a socket is returned
                while (true) {
                    try {
                        socket = mmServerSocket.accept();
                        // If a connection was accepted
                        if (socket != null) {
                            // Do work to manage the connection (in a separate thread)
                            //manageConnectedSocket(socket);
                            mmServerSocket.close();
                            break;
                        }
                    } catch (IOException e) {
                        break;
                    }
                }
            }

            /**
             * Will cancel the listening socket, and cause the thread to finish
             */
            public void cancel() {
                try {
                    mmServerSocket.close();
                } catch (IOException e) {
                }
            }
        }
    }
}
