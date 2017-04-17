package cnt4514c.unfsymposiumnfcquiz;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Handler;
import android.os.Parcelable;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;

public class SuccessActivity extends AppCompatActivity {

    private Handler mHandler = new Handler();
    NfcAdapter nfcAdapter;
    PendingIntent pendingIntent;
    IntentFilter writeTagFilters[];
    Tag myTag;
    boolean writeMode;
    Integer qNum, qArrayLen, correctCount;
    String timeElapsed;
    TextView successMsg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_success);

        successMsg = (TextView) findViewById(R.id.successMsg);

        Vibrator vib = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        long[] patternTru = {0, 90, 75, 90, 75, 90, 75, 500};
        vib.vibrate(patternTru, -1);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            // Stop here, we definitely need NFC
            Toast.makeText(this, "This device doesn't support NFC.", Toast.LENGTH_LONG).show();
            finish();
        }
        readFromIntent(getIntent());

        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        tagDetected.addCategory(Intent.CATEGORY_DEFAULT);
        writeTagFilters = new IntentFilter[] { tagDetected };

        // Initially hide the content view.
        findViewById(R.id.nfc_image2).setVisibility(View.GONE);
        findViewById(R.id.nfc_image3).setVisibility(View.GONE);

        qNum = getIntent().getIntExtra("qNum", 0);
        qArrayLen = getIntent().getIntExtra("qArrayLen", 0);
        correctCount = getIntent().getIntExtra("correctCount", 0);
        timeElapsed = getIntent().getStringExtra("ttime");
    }

    @Override
    public void onBackPressed(){
        highlight(false);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus){
        super.onWindowFocusChanged(hasFocus);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    public void readFromIntent(Intent intent) {

        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            NdefMessage[] msgs = null;
            if (rawMsgs != null) {
                msgs = new NdefMessage[rawMsgs.length];
                for (int i = 0; i < rawMsgs.length; i++) {
                    msgs[i] = (NdefMessage) rawMsgs[i];
                }
            }
            buildTagViews(msgs);
        }
    }

    public void buildTagViews(NdefMessage[] msgs) {

        boolean result = false;
        Vibrator vib = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        long[] patternTru = {0, 100};
        long[] patternFal = {0, 100, 100, 100};

        if (msgs == null || msgs.length == 0) return;
        String text = "";
//        String tagId = new String(msgs[0].getRecords()[0].getType());
        byte[] payload = msgs[0].getRecords()[0].getPayload();
        String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16"; // Get the Text Encoding
        int languageCodeLength = payload[0] & 0063; // Get the Language Code, e.g. "en"
        // String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");

        try {
            // Get the Text
            text = new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);

            if(text.equals("START")){
                result = true;

                if(qNum.equals(qArrayLen)){
                    Intent congratsIntent = new Intent(SuccessActivity.this, CongratsActivity.class);
                    congratsIntent.putExtra("qArrayLen", qArrayLen);
                    congratsIntent.putExtra("correctCount", correctCount);
                    congratsIntent.putExtra("ttime", timeElapsed);
                    QuestionActivity.chron.stop();
                    //add any other data to pass to new activity
                    startActivity(congratsIntent);
                    finish();
                }
            }
            else{
                result = false;
            }

            highlight(result);

            if (result){
                vib.vibrate(patternTru, -1);
                mHandler.postDelayed(new Runnable(){
                    public void run(){
                        finish();
                    }
                }, 1500);
            }
            else{
                vib.vibrate(patternFal, -1);
            }
        } catch (UnsupportedEncodingException e) {
            Log.e("UnsupportedEncoding", e.toString());
        }
    }

    private void highlight(boolean result) {

        if (result){
            findViewById(R.id.nfc_image2).setVisibility(View.VISIBLE);

            mHandler.postDelayed(new Runnable(){
                public void run(){
                    findViewById(R.id.nfc_image2).setVisibility(View.GONE);
                }
            }, 1500);
        }
        else{
            findViewById(R.id.nfc_image3).setVisibility(View.VISIBLE);

            mHandler.postDelayed(new Runnable(){
                public void run(){
                    findViewById(R.id.nfc_image3).setVisibility(View.GONE);
                }
            }, 1500);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        readFromIntent(intent);
        if(NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())){
            myTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        WriteModeOff();

    }

    @Override
    public void onResume(){
        super.onResume();
        WriteModeOn();
    }

    /**********************************Enable Write********************************/
    private void WriteModeOn(){
        writeMode = true;
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, writeTagFilters, null);
    }
    /**********************************Disable Write*******************************/
    private void WriteModeOff(){
        writeMode = false;
        nfcAdapter.disableForegroundDispatch(this);
    }
}
