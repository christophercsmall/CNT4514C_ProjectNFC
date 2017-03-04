package cnt4514c.unfsymposiumnfcquiz;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Chris4 on 2/28/2017.
 */

public class Question {
    public Integer id;
    public String qTxt;
    public String aTxt;
    public List<String> aOptions = new ArrayList<>();

    public String getqTxt(){
        return this.qTxt;
    }

    public String getaTxt(){
        return this.aTxt;
    }

}
