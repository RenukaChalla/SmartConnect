package com.rfid.smartconnect.smartconnect;

//import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
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
public class MainActivity extends AppCompatActivity {

    private static String TAG = MainActivity.class.getSimpleName();
    protected NfcAdapter adapter;
    protected PendingIntent pendingIntent;
    IntentFilter writeTagFilters[];
    boolean writeMode;
    Tag mytag;
    Context ctx;
    private TextView mTextView;
    BluetoothDevice bdDevice;
    private String device_name;
    private  String device_address;
    private StringTokenizer bdtkn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate");
        mTextView = (TextView) findViewById(R.id.title3);
        // initialize NFC
        adapter = NfcAdapter.getDefaultAdapter(this);
        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        tagDetected.addCategory(Intent.CATEGORY_DEFAULT);
        writeTagFilters = new IntentFilter[] { tagDetected };

    }

    @Override
    public void onPause(){
        super.onPause();
        Log.d(TAG, "onPause");
        WriteModeOff();
    }

    @Override
    public void onStart() {
        super.onStart();
        bdDevice = getIntent().getParcelableExtra("bt_device");
        Log.d(TAG, "onStart");
    }

    @Override
    public void onResume(){
        super.onResume();
        bdDevice = getIntent().getParcelableExtra("bt_device");
        Log.d(TAG, "onResume");
        WriteModeOn();
    }

    private void WriteModeOn(){
        writeMode = true;
        adapter.enableForegroundDispatch(this, pendingIntent, writeTagFilters, null);
    }

    private void WriteModeOff(){
        writeMode = false;
        adapter.disableForegroundDispatch(this);
    }

    // Function to read from the tag
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

    private NdefRecord readRecordN(int n) {

        Ndef ndef = Ndef.get(mytag);
        if (ndef == null) {
            // NDEF is not supported by this Tag.
            return null;
        }

        NdefMessage ndefMessage = ndef.getCachedNdefMessage();

        NdefRecord[] records = ndefMessage.getRecords();
        return records[n];
    }


    // Function for writing to tag
    private void write(Tag tag) throws IOException, FormatException {

        NdefRecord[] records = { createRecord(/* TODO:"headsetBluetoothName" */) };
        /*TODO: 2 conditions for write: 1 when headset paired for 1st time: NdefRecord[] records = { createRecord(headset_name),
         createRecord(1), createRecord(phoneName); }
         2 when headset_name found in paired device: NdefRecord[] records = { createRecord(headset_name),record_freq(),
         createRecord(phoneName) };*/

        NdefMessage  message = new NdefMessage(records);
        // Get an instance of Ndef for the tag.
        Ndef ndef = Ndef.get(tag);
        // Enable I/O
        ndef.connect();
        // Write the message
        ndef.writeNdefMessage(message);
        writeMode = false;
        // Close the connection
        ndef.close();
    }

    // Function to create record for tag
    private NdefRecord createRecord(/*TODO: String bluetoothName*/) throws UnsupportedEncodingException {
        // PackageManager pm = getPackageManager();
        // System.getProperty("os.version");

        String lang       = "en";
        String text = bdDevice.getName()+","+bdDevice.getAddress();//"renu,F0:B4:79:08:BE:93"/* bluetoothName */;
        byte[] textBytes  = text.getBytes();
        byte[] langBytes  = lang.getBytes("US-ASCII");
        int    langLength = langBytes.length;
        int    textLength = textBytes.length;

        byte[] payload    = new byte[1 + langLength + textLength];

        // set status byte (see NDEF spec for actual bits)
        payload[0] = (byte) langLength;

        // copy langbytes and textbytes into payload
        System.arraycopy(langBytes, 0, payload, 1, langLength);
        System.arraycopy(textBytes, 0, payload, 1 + langLength, textLength);

        NdefRecord recordNFC = new NdefRecord(NdefRecord.TNF_WELL_KNOWN,  NdefRecord.RTD_TEXT,  new byte[0], payload);

        return recordNFC;
    }

    private NdefRecord record_freq() throws UnsupportedEncodingException {
        String lang = "en";
        String freq = null;
        int frequency = 0;
        // record[0]=headset name, record[1]=frequency of use, record[2..]=phone's bluetooth name
        NdefRecord freq_block = readRecordN(1);

        if (freq_block.getTnf() == NdefRecord.TNF_WELL_KNOWN && Arrays.equals(freq_block.getType(), NdefRecord.RTD_TEXT)) {
            try {
                frequency = tryParse(readRecordText(freq_block));
                //  frequency = parseInt(readRecordText(freq_block));
                if(frequency == 0) {
                    frequency += 1;
                }
                else {
                    frequency += 1;
                }
                freq = String.valueOf(frequency);
            } catch (UnsupportedEncodingException e) {
                Log.e(TAG, "Unsupported Encoding", e);
            }
        }
        String text = freq;
        byte[] textBytes  = text.getBytes();
        byte[] langBytes  = lang.getBytes("US-ASCII");
        int    langLength = langBytes.length;
        int    textLength = textBytes.length;

        byte[] payload    = new byte[1 + langLength + textLength];

        // set status byte (see NDEF spec for actual bits)
        payload[0] = (byte) langLength;

        // copy langbytes and textbytes into payload
        System.arraycopy(langBytes, 0, payload, 1, langLength);
        System.arraycopy(textBytes, 0, payload, 1 + langLength, textLength);

        NdefRecord recordNFC = new NdefRecord(NdefRecord.TNF_WELL_KNOWN,  NdefRecord.RTD_TEXT,  new byte[0], payload);

        return recordNFC;
    }

    public static int tryParse(String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    private String readRecordText(NdefRecord record) throws UnsupportedEncodingException{

        byte[] payload = record.getPayload();

        // Get the Language Code
        int languageCodeLength = payload[0] & 0063;

        // Get the Text
        return new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1);
    }

    @Override
    protected void onNewIntent(Intent intent){
        Log.d(TAG, "onNewIntent");
        ctx=this;
        if(NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())){
            TextView textView = (TextView) findViewById(R.id.title2);
            textView.setText("Hello NFC tag!");
            //   Toast.makeText(ctx, ctx.getString(R.string.hello_tag), Toast.LENGTH_LONG ).show();
            mytag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            // Read tag
            new NdefReaderTask().execute(mytag);
            //   readMessages(intent);
            Toast.makeText(this, this.getString(R.string.ok_detection) + mytag.toString(), Toast.LENGTH_LONG ).show();

            // write to tag
            /*try {
                write(mytag);
                Toast.makeText(ctx, ctx.getString(R.string.ok_writing), Toast.LENGTH_LONG ).show();
            } catch (IOException e) {
                Toast.makeText(ctx, ctx.getString(R.string.error_writing), Toast.LENGTH_LONG ).show();
                e.printStackTrace();
            } catch (FormatException e) {
                Toast.makeText(ctx, ctx.getString(R.string.error_writing) , Toast.LENGTH_LONG ).show();
                e.printStackTrace();
            }*/
        }
    }

    // Phone's Bluetooth name
    private String getLocalBluetoothName(){
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        String name = mBluetoothAdapter.getName();
        //  String address = mBluetoothAdapter.getAddress();
        if(name == null){
            Log.d(TAG, "Name is null!");
            name = mBluetoothAdapter.getAddress();
        }
        return name;
    }


    // Activate device vibrator for 500 ms
    private void vibrate() {
        Log.d(TAG, "vibrate");

        Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibe.vibrate(500);
    }


    // Background task for reading the data. Do not block the UI thread while reading.

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

            byte[] payload = record.getPayload();
            // Get the Text Encoding
            String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16";
            int languageCodeLength = payload[0] & 0063;
            // String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");
            // e.g. "en"
            // Get the Text
            String stuff = new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1);
            //   mTextView.setText("Tag says: %s" + stuff);
            bdtkn = new StringTokenizer(stuff,",");
            device_name = bdtkn.nextToken();
            device_address = bdtkn.nextToken();
            Intent btintent = new Intent(MainActivity.this, BTActivity.class);
            btintent.putExtra("bt_device_name", device_name);
            btintent.putExtra("bt_device_address", device_address);
            startActivity(btintent);
            return new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1);
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                mTextView.setText("Tag says: " + result);
            }
        }
    }
}
