package org.latin.ifce.ifontime;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;

import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.conn.HttpHostConnectException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.latin.ifce.ifontime.controller.RequestSchedule;
import org.latin.ifce.ifontime.model.HorarioHelper;
import org.latin.ifce.ifontime.view.Preferences;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MainActivity extends Activity {

   private Cursor model;
   private HorarioHelper helper = new HorarioHelper(this);
   private ListaAdapter adapter;
   private ListView lvHorarios;
   private TextView tvData;
   private int diaDaSemana;
   private Calendar calendar = Calendar.getInstance();
   private String data;

   private ProgressDialog dialog;
   private ProgressThread thread;
   private static final int PROGRESS_DIALOG = 0;

   private boolean hasErrors = false;
   private Exception error = null;

   private static final String DIA_DA_SEMANA = "diaDaSemana";
   private static final String CALENDARIO = "calendar";
   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);
      if(savedInstanceState != null) {
          diaDaSemana = savedInstanceState.getInt(DIA_DA_SEMANA);
          calendar.setTimeInMillis(savedInstanceState.getLong(CALENDARIO));
      } else {
          diaDaSemana = calendar.get(Calendar.DAY_OF_WEEK);
      }
      load();
   }

   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      getMenuInflater().inflate(R.menu.main, menu);
      return true;
   }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(CALENDARIO,calendar.getTime().getTime());
        outState.putInt(DIA_DA_SEMANA, diaDaSemana);
    }

    @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      if (item.getItemId() == R.id.action_load_schedules) {
         if (isNetworkAvailable()) {
            clickLoadSchedules();
            return true;
         }
         error(new Exception(getString(R.string.network_not_available_err)));
         return false;
      } else if (item.getItemId() == R.id.action_preferences) {
         startActivity(new Intent(this, Preferences.class));
         return true;
      }
      return super.onOptionsItemSelected(item);
   }

   private void showLoadingSchedulesDialog(Bundle args) {
      dialog = new ProgressDialog(this);
      dialog.setProgressStyle(PROGRESS_DIALOG);
      dialog.setMessage(getResources().getString(R.string.loading_schedules_text));
      thread = new ProgressThread(handler, args.getString("hash"));
      thread.start();
      dialog.show();
      Log.d("MainActivity", "starting thread ...");
   }

   private void clickLoadSchedules() {
       AlertDialog.Builder alert = new AlertDialog.Builder(this);
       alert.setTitle(R.string.load_schedules);
       final EditText input = new EditText(this);
       input.setHint(R.string.code_text);
       alert.setView(input);

       alert.setPositiveButton(R.string.ok_text, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int whichButton) {
            String value = input.getText().toString();

            if( (value == null) || ("".equals(value))) {
               Toast.makeText(MainActivity.this, R.string.code_not_informed_err, Toast.LENGTH_LONG).show();
            } else {
               Bundle args = new Bundle();
               args.putString("hash", value);
               showLoadingSchedulesDialog(args);
            }
          }
       });
       alert.show();
   }

   private void loadSchedules() {
      Log.i("MainActivity", "loading schedules ...");
      if (validate()) {
         data = new SimpleDateFormat("EEEE\nd, MMM").format(calendar.getTime());
         tvData.setText(data);

         model = helper.list(diaDaSemana);
         startManagingCursor(model);
         adapter = new ListaAdapter(model);
         lvHorarios.setAdapter(adapter);
      }

   }

   private void sendRequest(String hash) {
      try {
         SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
         String host = preferences.getString("pref_host", "10.50.40.22");
         String port = preferences.getString("pref_port", "8080");

         RequestSchedule request = new RequestSchedule(host, port);
         JSONObject json = request.getAnswer(hash);

         String err = json.getString("error");
         if (! "".equals(err)) {
            Log.d("[DEBUG]", err);
            hasErrors = true;
            error = new Exception(err);
            return;
         }

         int deleted = helper.deleteAll();
         Log.d("MainActivity", "deleting " + deleted + " row(s)...");

         JSONArray rows = json.getJSONArray("rows");
         for (int i = 0; i < rows.length(); i++) {
            JSONObject row = rows.getJSONObject(i);
            ContentValues values = new ContentValues();
            values.put("disciplina", row.getString("disciplina"));
            values.put("horario_inicio", row.getString("horario_inicio"));
            values.put("horario_fim", row.getString("horario_fim"));
            values.put("sala", row.getString("sala"));
            values.put("professor", row.getString("professor"));
            values.put("dia_da_semana", row.getInt("dia_da_semana"));

            helper.create(values);
            Log.i("MainActivity", "inserting " + i + " value(s)");
         }
      } catch (HttpHostConnectException e) {
         hasErrors = true;
         error = new Exception(getString(R.string.access_err));
      } catch (JSONException e) {
         Log.e("RequestSchedule", e.getMessage(), e);
         hasErrors = true;
         error = e;
      } catch (Exception e) {
         Log.e("RequestSchedule", e.getMessage(), e);
         hasErrors = true;
         error = e;
      }
   }

   public boolean validate() {
      if (hasErrors) {
         error(error);
         hasErrors = false;
         return false;
      }
      return true;
   }

   public boolean isNetworkAvailable() {
      ConnectivityManager cm = (ConnectivityManager)
              getSystemService(Context.CONNECTIVITY_SERVICE);
      NetworkInfo networkInfo = cm.getActiveNetworkInfo();
      // if no network is available networkInfo will be null
      // otherwise check if we are connected
      if (networkInfo != null && networkInfo.isConnected()) {
         return true;
      }
      return false;
   }

   private static class HorarioHolder {
      private TextView professor, horario, local, disciplina;

      public HorarioHolder(View linha) {
         professor = (TextView) linha.findViewById(R.id.tvProf);
         horario = (TextView) linha.findViewById(R.id.tvHorario);
         local = (TextView) linha.findViewById(R.id.tvLocal);
         disciplina = (TextView) linha.findViewById(R.id.tvDisciplina);
      }

      public void popularForm(Cursor c) {
         disciplina.setText(c.getString(1));
         String horarioFormatado = c.getString(2) + " - " + c.getString(3);
         horario.setText(horarioFormatado);
         local.setText(c.getString(4));
         professor.setText(c.getString(5));
      }
   }

   private class ListaAdapter extends CursorAdapter {

      public ListaAdapter(Cursor c) {
         super(MainActivity.this, c);
      }

      @Override
      public View newView(Context cntxt, Cursor cursor, ViewGroup vg) {
         LayoutInflater inflater = getLayoutInflater();
         View linha = inflater.inflate(R.layout.activity_main_item, vg, false);
         HorarioHolder holder = new HorarioHolder(linha);
         linha.setTag(holder);
         return linha;
      }

      @Override
      public void bindView(View view, Context cntxt, Cursor cursor) {
         HorarioHolder holder = (HorarioHolder) view.getTag();
         holder.popularForm(cursor);
      }
   }

   private void load() {
      lvHorarios = (ListView) findViewById(R.id.lvHorarios);
      tvData = (TextView) findViewById(R.id.tvData);
      loadSchedules();

   }

   final Handler handler = new Handler() {
      public void handleMessage(Message msg) {
         boolean done = msg.getData().getBoolean("done");
         if (done) {
            if (dialog.isShowing()) {
               dialog.dismiss();
            }
            loadSchedules();
         }
      }
   };

   private class ProgressThread extends Thread {
      Handler handler = null;
      String hash;

      public ProgressThread(Handler handler, String hash) {
         this.handler = handler;
         this.hash = hash;
      }

      @Override
      public void run() {
         sendRequest(hash);

         Message message = handler.obtainMessage();
         Bundle bundle = new Bundle();
         bundle.putBoolean("done", true);
         message.setData(bundle);
         handler.sendMessage(message);
      }
   }

    public void onBackButtonClick(View v) {
        if(diaDaSemana == 2) {
           diaDaSemana = 6;
           calendar.add(calendar.DAY_OF_YEAR, -3);
        } else if (diaDaSemana == 1) {
           diaDaSemana = 6;
           calendar.add(calendar.DAY_OF_YEAR, -2);
        } else {
            diaDaSemana--;
            calendar.add(calendar.DAY_OF_YEAR, -1);
        }

        load();
    }

    public void onNextButtonClick(View v) {
        if(diaDaSemana == 6) {
            diaDaSemana = 2;
            calendar.add(calendar.DAY_OF_YEAR, 3);
        } else if (diaDaSemana == 7) {
           diaDaSemana = 2;
           calendar.add(calendar.DAY_OF_YEAR, 2);
        } else {
            diaDaSemana++;
            calendar.add(calendar.DAY_OF_YEAR, 1);
        }

        load();
    }

   public void error(Exception e) {
      AlertDialog.Builder dialog = new AlertDialog.Builder(this);
      dialog.setTitle(R.string.error_occurred);
      dialog.setMessage(e.getMessage());
      dialog.setNeutralButton(R.string.ok_text, null);
      dialog.show();
   }
}
