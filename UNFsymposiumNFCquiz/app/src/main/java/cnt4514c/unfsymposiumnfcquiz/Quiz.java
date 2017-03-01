package cnt4514c.unfsymposiumnfcquiz;

import java.util.ArrayList;
import java.util.List;

public class Quiz {
    public List<Question> qArray = new ArrayList<>();
    public List<Answer> aArray = new ArrayList<>();
    public List<QuestionOption> qOptionArray = new ArrayList<>();
    public List<Integer> qIDs = new ArrayList<>();

    public List addIDtoList(Integer id) {
        qIDs.add(id);
        return qIDs;
    }

    public List getQuestionIDs() {
        return qIDs;
    }
}
