package cnt4514c.unfsymposiumnfcquiz;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;

public class CongratsActivity extends AppCompatActivity {

    NfcAdapter nfcAdapter;
    PendingIntent pendingIntent;
    IntentFilter writeTagFilters[];
    Tag myTag;
    boolean writeMode;
    Integer correctCount, qArrayLen;
    String name, email, ttime, score;
    Global global;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_congrats);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        TextView quizScoreText = (TextView) findViewById(R.id.correctMsg);
        TextView timeText = (TextView) findViewById(R.id.timeMsg);

        global = (Global) getApplicationContext();
        name = global.getName();
        email = global.getEmail();
        ttime = getIntent().getStringExtra("ttime");

        correctCount = getIntent().getIntExtra("correctCount", 0);
        qArrayLen = getIntent().getIntExtra("qArrayLen", 0);

        score = (correctCount + " / " + qArrayLen);

        quizScoreText.setText(score);
        timeText.setText(ttime);

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
    }

    @Override
    public void onBackPressed(){

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

            if(text.equals("SUBMIT") && paramsValid(name, ttime, score)){

                //get vars and add to uri

                Intent postScoreIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://n00931863.sytes.net?name=" +name+ "&email=" +email+ "&ttime=" +ttime+ "&score=" +score));
                startActivity(postScoreIntent);

                finish();
            }
            else{
            }

        } catch (UnsupportedEncodingException e) {
            Log.e("UnsupportedEncoding", e.toString());
        }
    }

    public boolean paramsValid(String nm, String tt, String sc){
        boolean result = false;

        if((!nm.isEmpty() && nm.length() > 0 && !nm.equals(""))
        && (!tt.isEmpty() && tt.length() > 0 && !tt.equals(""))
        && (!sc.isEmpty() && sc.length() > 0 && !sc.equals("")))
        {
            result = true;
        }

        return result;
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
