package cnt4514c.unfsymposiumnfcquiz;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class CongratsActivity extends AppCompatActivity {

    Integer correctCount, qArrayLen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_congrats);

        correctCount = getIntent().getIntExtra("correctCount", 0);
        qArrayLen = getIntent().getIntExtra("qArrayLen", 0);


        TextView quizScoreText = (TextView) findViewById(R.id.correctMsg);

        quizScoreText.setText(correctCount + " / " + qArrayLen);
    }
}
