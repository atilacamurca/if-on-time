package org.latin.ifce.ifontime;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

import org.latin.ifce.ifontime.model.HorarioHelper;

public class MainActivity extends Activity {

   HorarioHelper model = new HorarioHelper(getApplicationContext());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
}
