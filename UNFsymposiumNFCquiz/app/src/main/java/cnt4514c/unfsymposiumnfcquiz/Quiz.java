package cnt4514c.unfsymposiumnfcquiz;

import java.util.ArrayList;
import java.util.List;

public class Quiz {
    public List<Question> qArray = new ArrayList<>();
    public List<Integer> qIDs = new ArrayList<>();

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

        this.qArray.add(q);
    }

}
