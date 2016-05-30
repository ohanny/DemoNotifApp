package fr.icodem.demonotifapp;

import android.app.Activity;
import android.os.Bundle;

/**
 * The activity displayed when the
 * notification is selected
 */
public class ViewEventActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_event);
    }
}
