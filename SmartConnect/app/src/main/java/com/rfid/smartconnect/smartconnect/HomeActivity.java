package com.rfid.smartconnect.smartconnect;

import android.app.PendingIntent;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.Vibrator;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.ndeftools.Record;
import org.ndeftools.externaltype.AndroidApplicationRecord;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnbind;
    private Button btnpair;
    private static String TAG = HomeActivity.class.getSimpleName();
    private TextView mTextView;
    private BluetoothDevice bdDevice;
    private String device_name;
    private String device_address;
    private StringTokenizer bdtkn;
    private Tag mytag;
    private Context ctx;
    protected NfcAdapter adapter;
    protected PendingIntent pendingIntent;
    IntentFilter writeTagFilters[];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Log.d(TAG, "onCreate");
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mTextView = (TextView) findViewById(R.id.txtview);
        adapter = NfcAdapter.getDefaultAdapter(this);
        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        tagDetected.addCategory(Intent.CATEGORY_DEFAULT);
        adapter = NfcAdapter.getDefaultAdapter(this);
        writeTagFilters = new IntentFilter[]{tagDetected};
        btnbind = (Button) findViewById(R.id.btnbind);
        btnbind.setOnClickListener(this);
        btnpair = (Button) findViewById(R.id.btnpair);
        btnpair.setOnClickListener(this);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        adapter.disableForegroundDispatch(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        bdDevice = getIntent().getParcelableExtra("bt_device");
        Log.d(TAG, "onStart");
    }

    @Override
    public void onResume() {
        super.onResume();
        bdDevice = getIntent().getParcelableExtra("bt_device");
        Log.d(TAG, "onResume");
        adapter.enableForegroundDispatch(this, pendingIntent, writeTagFilters, null);
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

    private String readRecordText(NdefRecord record) throws UnsupportedEncodingException {

        byte[] payload = record.getPayload();

        // Get the Language Code
        int languageCodeLength = payload[0] & 0063;

        // Get the Text
        return new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.d(TAG, "onNewIntent");
        ctx = this;
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {

            //   Toast.makeText(ctx, ctx.getString(R.string.hello_tag), Toast.LENGTH_LONG ).show();
            mytag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            // Read tag
            new NdefReaderTask().execute(mytag);
            //   readMessages(intent);
            Toast.makeText(this, this.getString(R.string.ok_detection) + mytag.toString(), Toast.LENGTH_LONG).show();
        }
    }

    // Activate device vibrator for 500 ms
    private void vibrate() {
        Log.d(TAG, "vibrate");

        Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibe.vibrate(500);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnbind:
                Intent btintent = new Intent(this, BTActivity.class);
                startActivity(btintent);
                break;
            case R.id.btnpair:
                Intent btintent1 = new Intent(this, BTActivity.class);
                startActivity(btintent1);
                break;
        }
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
            String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16";
            int languageCodeLength = payload[0] & 0063;
            String stuff = new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1);
            bdtkn = new StringTokenizer(stuff, ",");
            device_name = bdtkn.nextToken();
            device_address = bdtkn.nextToken();
            Intent btintent = new Intent(HomeActivity.this, BTActivity.class);
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
