package cnt4514c.unfsymposiumnfcquiz;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class FinalAnswerActivity extends AppCompatActivity {

    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_final_answer);
    }

    @Override
    public void onBackPressed(){
        highlight(false);
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
}
