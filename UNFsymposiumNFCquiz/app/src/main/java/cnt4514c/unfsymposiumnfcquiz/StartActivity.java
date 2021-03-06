package cnt4514c.unfsymposiumnfcquiz;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;
import java.io.UnsupportedEncodingException;

public class StartActivity extends AppCompatActivity {

    Global global;

    NfcAdapter nfcAdapter;
    PendingIntent pendingIntent;
    IntentFilter writeTagFilters[];
    Tag myTag;
    boolean writeMode;
    Integer readyCode;

    EditText name, email;
    String nameText, emailText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        global = (Global)getApplicationContext();

        name = (EditText) findViewById(R.id.name);
        email = (EditText) findViewById(R.id.email);

        name.setOnFocusChangeListener(new View.OnFocusChangeListener(){
            @Override
            public void onFocusChange(View v, boolean hasFocus){
                if (!hasFocus){
                    hideKeyboard(v);
                }
            }
        });

        email.setOnFocusChangeListener(new View.OnFocusChangeListener(){
            @Override
            public void onFocusChange(View v, boolean hasFocus){
                if (!hasFocus){
                    hideKeyboard(v);
                }
            }
        });

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            // Stop here, we definitely need NFC
            Toast.makeText(this, "This device doesn't support NFC.", Toast.LENGTH_LONG).show();
            finish();
        }

        readyCode = -1;
        readFromIntent(getIntent());

        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        tagDetected.addCategory(Intent.CATEGORY_DEFAULT);
        writeTagFilters = new IntentFilter[] { tagDetected };

        // Initially hide the content view.
        findViewById(R.id.nfc_image2).setVisibility(View.GONE);
    }

    public void hideKeyboard(View view){
        InputMethodManager imm = (InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
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
            readyCode = 0;
        }
        else{
            readyCode = 1;
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

            String nameText = name.getText().toString().trim();
            nameText = nameText.replaceAll(" ", "_").toLowerCase();
            String emailText = email.getText().toString().trim();
            emailText = emailText.replaceAll(" ", "_").toLowerCase();

            if (text.equals("START") && (!nameText.isEmpty() && !nameText.equals("") && nameText.length() > 0) && (readyCode.equals(0) || readyCode.equals(1))){

                if ((emailText.isEmpty() || emailText.equals("") || emailText.length() == 0)){
                    emailText = "-";
                }
                global.setName(nameText);
                global.setEmail(emailText);

                highlight();
                startActivity(new Intent(StartActivity.this, QuestionActivity.class));
                finish();
            }
            else {
                Toast.makeText(this, "Name is required.", Toast.LENGTH_LONG).show();
            }
        } catch (UnsupportedEncodingException e) {
            Log.e("UnsupportedEncoding", e.toString());
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

    private void highlight() {
        findViewById(R.id.nfc_image1).setVisibility(View.GONE);

        findViewById(R.id.nfc_image2).setVisibility(View.VISIBLE);


//        // Retrieve and cache the system's default "short" animation time.
//        animationDuration = getResources().getInteger(
//                android.R.integer.config_shortAnimTime);
//
//        // Set the content view to 0% opacity but visible, so that it is visible
//        // (but fully transparent) during the animation.
//        nfc_logo2View.setAlpha(0f);
//        nfc_logo2View.setVisibility(View.VISIBLE);
//
//        // Animate the content view to 100% opacity, and clear any animation
//        // listener set on the view.
//        nfc_logo2View.animate()
//                .alpha(1f)
//                .setDuration(animationDuration)
//                .setListener(null);

        // Animate the loading view to 0% opacity. After the animation ends,
        // set its visibility to GONE as an optimization step (it won't
        // participate in layout passes, etc.)
//        nfc_logo1View.animate()
//                .alpha(0f)
//                .setDuration(animationDuration)
//                .setListener(new AnimatorListenerAdapter() {
//                    @Override
//                    public void onAnimationEnd(Animator animation) {
//                        nfc_logo1View.setVisibility(View.GONE);
//                    }
//                });
    }


}
