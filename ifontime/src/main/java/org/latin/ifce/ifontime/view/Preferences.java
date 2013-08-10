package org.latin.ifce.ifontime.view;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import org.latin.ifce.ifontime.R;

/**
 * Created by atila on 10/08/13.
 */
public class Preferences extends PreferenceActivity {

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      addPreferencesFromResource(R.xml.preferences);
   }
}
