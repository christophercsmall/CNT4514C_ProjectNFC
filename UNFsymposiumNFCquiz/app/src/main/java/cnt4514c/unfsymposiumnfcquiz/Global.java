package cnt4514c.unfsymposiumnfcquiz;

import android.app.Application;

/**
 * Created by Chris4 on 4/17/2017.
 */

public class Global extends Application {

    public String name;
    public String email;

    public String getName(){
        return name;
    }

    public String getEmail(){
        return email;
    }

    public void setName(String name){
        this.name = name;
    }

    public void setEmail(String email){
        this.email = email;
    }
}
