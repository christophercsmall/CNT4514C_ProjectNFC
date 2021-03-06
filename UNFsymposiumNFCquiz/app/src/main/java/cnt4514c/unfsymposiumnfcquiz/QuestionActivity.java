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
import android.os.SystemClock;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.TimeUnit;

public class QuestionActivity extends AppCompatActivity {

    private Handler mHandler = new Handler();
    NfcAdapter nfcAdapter;
    PendingIntent pendingIntent;
    IntentFilter writeTagFilters[];
    Tag myTag;
    TextView qText, aText1, aText2, aText3, aText4, qCounter, cCounter;
    static Chronometer chron;
    boolean writeMode;
    boolean firstCorrect = true;
    Quiz quiz = new Quiz();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        qText = (TextView) findViewById(R.id.qText);
        aText1 = (TextView) findViewById(R.id.aText1);
        aText2 = (TextView) findViewById(R.id.aText2);
        aText3 = (TextView) findViewById(R.id.aText3);
        aText4 = (TextView) findViewById(R.id.aText4);
        chron = (Chronometer) findViewById(R.id.chron);
        qCounter = (TextView) findViewById(R.id.qCounter);
        cCounter = (TextView) findViewById(R.id.cCounter);

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
        updateQuestionActivity();

        Vibrator vib = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        vib.vibrate(500);

        // Initially hide the content view.
        findViewById(R.id.nfc_image2).setVisibility(View.GONE);
        findViewById(R.id.nfc_image3).setVisibility(View.GONE);

        chron.setBase(SystemClock.elapsedRealtime());
        chron.setFormat("%s");
        chron.start();
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

    @Override
    public void onBackPressed(){
        highlight(false);
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
        //long[] patternTru = {0, 90, 75, 90, 75, 90, 75, 800};
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
            result = checkAns(text);
            highlight(result);

            if (result){
                long timeElapsed = SystemClock.elapsedRealtime() - chron.getBase();

                if (firstCorrect){
                    quiz.correctCount++;
                }

                String ttime = String.format("%02d:%02d", TimeUnit.MILLISECONDS.toMinutes(timeElapsed),
                                                         TimeUnit.MILLISECONDS.toSeconds(timeElapsed) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timeElapsed)));

                Intent successIntent = new Intent(QuestionActivity.this, SuccessActivity.class);
                successIntent.putExtra("qNum", quiz.currentQuestionNum);
                successIntent.putExtra("qArrayLen", quiz.qArray.size());
                successIntent.putExtra("correctCount", quiz.correctCount);
                successIntent.putExtra("ttime", ttime);
                //add any other data to pass to new activity
                startActivity(successIntent);

                mHandler.postDelayed(new Runnable(){
                    public void run(){
                        if(!quiz.currentQuestionNum.equals(quiz.qArray.size())){
                            updateQuestionActivity();
                        }
                        else{
                            finish();
                        }
                    }
                }, 1500);
            }
            else{
                vib.vibrate(patternFal, -1);
                firstCorrect = false;
                //maybe add random incorrect message
                Toast.makeText(this, "Wrong. Try Again.", Toast.LENGTH_SHORT).show();
            }
        } catch (UnsupportedEncodingException e) {
            Log.e("UnsupportedEncoding", e.toString());
        }
    }

    public void initializeQuiz(){
        quiz.currentQuestionNum = 0;
        quiz.correctCount = 0;
        quiz.addQuestion("What does NFC stand for?", "Near Field Communication", "Native File Cache","Network Firewall Communication", "Native Framework Cache");
        quiz.addQuestion("How many bits are in a byte?", "8", "32", "100", "16");
        quiz.addQuestion("At what frequency do NFC tags operate when transmitting data?", "13.56 megahertz", "125 kilohertz", "2.4 gigahertz", "800 megahertz");
        quiz.addQuestion("NFC tags are programmed to be 'Read-Only' when static lock bits are changed from 0 to 1.\n Previously written data can then be changed...", "never again", "by touching a strong magnet to erase all memory", "by using a specific frequency to reset lock bits", "by using pulsed voltage (similar to EEPROM)");
        quiz.addQuestion("The acronym 'NDEF' stands for...", "NFC Data Exchange Format", "Non-Dynamic Extensible Format", "Node Defined Electronic File", "Natively Distributed Electric Field");
        quiz.addQuestion("The Android Operating System began supporting NFC-enabled devices at what Android Version?", "2.3 (Gingerbread)", "4.0 (Ice Cream Sandwich)", "2.2 (Froyo)", "4.1 (Jelly Bean)");
        quiz.addQuestion("Which organization defines transmission protocols for communicating with contactless integrated circuits such as NFC tags?", "ISO/IEC", "IEEE", "NXP", "IETF");
        quiz.addQuestion("Mobile devices can send energy over radio waves to NFC tags to transmit messages.\n What is this method called?", "Electromagnetic Induction", "Bidirectional Transduction", "Piezoelectricity", "Electrostatic Transmission");
        quiz.addQuestion("When a mobile device attempts to write new data onto an NFC tag, the tag's old data is...", "overwritten up to the length of the new data", "rewritten behind the new data", "completely erased", "unchanged if there is enough unallocated storage");
        quiz.addQuestion("NFC tags can have either a 'wet' or 'dry' inlay.\n This refers to...", "the presence of an adhesive glue", "whether it can operate in moisture", "whether the antenna was formed in an electrolytic bath", "the tag's optimal environmental conditions");
        quiz.randomizeQuestions();
    }

    public void updateQuestionActivity(){
        //get question from array; index is referenced from object
        Question thisQ = quiz.qArray.get(quiz.currentQuestionNum);
        qText.setText(thisQ.qTxt);
        //later try to add dynamically sized options in activity
        aText1.setText(thisQ.aOptions.get(0));
        aText2.setText(thisQ.aOptions.get(1));
        aText3.setText(thisQ.aOptions.get(2));
        aText4.setText(thisQ.aOptions.get(3));
        qCounter.setText((quiz.currentQuestionNum + 1) + "/" + quiz.qArray.size());
        cCounter.setText(quiz.correctCount + "/" + quiz.qArray.size());
        firstCorrect = true; //reset for next question
    }

    public boolean checkAns(String tagContents){
        boolean result = false;

        if (tagContents.equals(quiz.qArray.get(quiz.currentQuestionNum).ansLetter)) {
            result = true;
            quiz.currentQuestionNum++;
        }
        return result;
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
