package cnt4514c.unfsymposiumnfcquiz;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Parcelable;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class QuestionActivity extends AppCompatActivity {

    NfcAdapter nfcAdapter;
    PendingIntent pendingIntent;
    IntentFilter writeTagFilters[];
    Tag myTag;
    TextView qText, aText1, aText2, aText3, aText4;
    boolean writeMode;
    Integer qNum = 0;
    Quiz quiz = new Quiz();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);

        qText = (TextView) findViewById(R.id.qText);
        aText1 = (TextView) findViewById(R.id.aText1);
        aText2 = (TextView) findViewById(R.id.aText2);
        aText3 = (TextView) findViewById(R.id.aText3);
        aText4 = (TextView) findViewById(R.id.aText4);

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

        initializeQuiz();
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

        } catch (UnsupportedEncodingException e) {
            Log.e("UnsupportedEncoding", e.toString());
        }
    }

    public void initializeQuiz(){

        addQuestion("What does NFC stand for?", "Near Field Communication", "Native File Cache","Network Firewall Communication", "Native Framework Cache");
        addQuestion("How many bits are in a byte?", "8", "32", "100", "16");
    }

    /**
     * @param qString //The question
     * @param aString //The correct answer
     * @param optString1 //Incorrect answer option 1
     * @param optString2 //Incorrect answer option 2
     * @param optString3 //Incorrect answer option 3
     */
    public void addQuestion(String qString, String aString, String optString1, String optString2, String optString3){
        Vibrator vib = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);

        Question q = new Question();
        Answer a = new Answer();
        QuestionOption qo = new QuestionOption();
        Integer n = 0;
        boolean idExists = true;

        while (idExists){
            for (Integer id : quiz.qIDs) {
                if (n.equals(id)){
                    n++;
                }
                else{
                    idExists = false;
                    vib.vibrate(1000);
                }
            }

        }

        quiz.qIDs.add(n);
        qText.setText(n.toString());
//
//        quiz.qIDs.add(n);
//
//        q.id = n;
//        q.txt = qString;
//        quiz.qArray.add(q);
//
//        a.id = n;
//        a.txt = aString;
//        quiz.aArray.add(a);
//
//        qo.id = n;
//        qo.option.add(optString1);
//        qo.option.add(optString2);
//        qo.option.add(optString3);
//        quiz.qOptionArray.add(qo);
    }

    public void nextQuestion(String tagText){
        Vibrator vib = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        String text = tagText;
//        if (ans.equals("A")){
//            vib.vibrate(200);
//        }
//        else{
//            vib.vibrate(1000);
//        }
    }

    public boolean checkAns(String q, String a){
        boolean result = false;

        if (q.equals(a)) {
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
