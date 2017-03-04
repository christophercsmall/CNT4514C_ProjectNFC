package cnt4514c.unfsymposiumnfcquiz;

import android.support.v4.content.res.TypedArrayUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Quiz {
    public List<Question> qArray = new ArrayList<>();
    public List<Integer> qIDs = new ArrayList<>();
    public Integer currentQuestionNum;

    public List getQuestionIDs() {
        return this.qIDs;
    }

    public void addQuestion(String qString, String aString, String optString1, String optString2, String optString3){

        Question q = new Question();
        Integer n = 0;
        boolean idExists = true;

        while (idExists){
            if(this.qIDs.size() == 0){
                idExists = false;
            }
            for (Integer id : this.qIDs) {
                if (n.equals(id)){
                    n++;
                }
                else{
                    idExists = false;
                }
            }
        }

        this.qIDs.add(n);

        q.id = n;
        q.qTxt = qString;
        q.aTxt = aString;

        q.aOptions.add(optString1);
        q.aOptions.add(optString2);
        q.aOptions.add(optString3);
        q.aOptions.add(q.aTxt);

        this.qArray.add(q);
    }

    public void randomizeQuestions(){
        List<Question> qArrayOrd = qArray; // get ordered question array
        List<Question> qArrayRand = new ArrayList<>(); // the randomized questions
        Question randQ = new Question();
        Random rand = new Random();
        Integer randNum;

        // randomize question order
        for (Integer n = qArrayOrd.size(); n > 0; n--) {
            randNum = rand.nextInt(n);
            randQ = qArrayOrd.get(randNum); //get random question
            qArrayOrd.remove((int) randNum); // remove from next random selection
            randQ = randomizeOptions(randQ);
            qArrayRand.add(randQ);
        }
        // add replace ord question array with randomized array
        qArray = qArrayRand;
    }

    public Question randomizeOptions(Question qOrd){
        Question qRand = new Question();
        String optRand = "";
        List<String> optArrayOrd = qOrd.aOptions;
        List<String> optArrayRand = new ArrayList<>();
        Random rand = new Random();
        Integer randNum;

        for (Integer x = optArrayOrd.size(); x > 0; x--){
            randNum = rand.nextInt(x);
            optRand = optArrayOrd.get(randNum); // get random string option for this option array
            optArrayOrd.remove((int) randNum); // remove from next random selection
            optArrayRand.add(optRand); // add to random option array for this question
        }
        qRand.aOptions = optArrayRand;
        qRand.qTxt = qOrd.qTxt;
        qRand.aTxt = qOrd.aTxt;
        qRand.id = qOrd.id;

        return qRand;
    }

}
