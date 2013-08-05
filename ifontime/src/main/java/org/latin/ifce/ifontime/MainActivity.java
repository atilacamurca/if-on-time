package org.latin.ifce.ifontime;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;

import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.latin.ifce.ifontime.model.HorarioHelper;

import java.util.Calendar;

public class MainActivity extends Activity {

   private Cursor model;
   private HorarioHelper helper = new HorarioHelper(this);
   private ListaAdapter adapter;
   private ListView lvHorarios;
   private int diaDaSemana;

   private ProgressDialog dialog;
   private ProgressThread thread;
   private static final int PROGRESS_DIALOG = 0;

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);

      Calendar calendar = Calendar.getInstance();
      diaDaSemana = calendar.get(Calendar.DAY_OF_WEEK);

      carregar();
   }

   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      // Inflate the menu; this adds items to the action bar if it is present.
      getMenuInflater().inflate(R.menu.main, menu);
      return true;
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      if (item.getItemId() == R.id.action_load_schedules) {
         clickLoadSchedules();
         return true;
      }
      return super.onOptionsItemSelected(item);
   }

   @Override
   protected Dialog onCreateDialog(int id) {
      switch (id) {
         case PROGRESS_DIALOG:
            dialog = new ProgressDialog(this);
            dialog.setProgressStyle(PROGRESS_DIALOG);
            dialog.setMessage(getResources().getString(R.string.loading_schedules_text));
            thread = new ProgressThread(handler);
            thread.start();
            return dialog;
         default:
            return null;
      }
   }

   private void clickLoadSchedules() {
      showDialog(PROGRESS_DIALOG);
   }

   private void loadSchedules() {
      Log.i("MainActivity", "loading schedules ...");
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

   private void carregar() {
      lvHorarios = (ListView) findViewById(R.id.lvHorarios);
      model = helper.listar(diaDaSemana);
      startManagingCursor(model);
      adapter = new ListaAdapter(model);
      lvHorarios.setAdapter(adapter);
   }

   final Handler handler = new Handler() {
      public void handleMessage(Message msg) {
         boolean done = msg.getData().getBoolean("done");
         if (done) {
            if (dialog.isShowing()) {
               dismissDialog(PROGRESS_DIALOG);
            }
            loadSchedules();
         }
      }
   };

   private class ProgressThread extends Thread {
      Handler handler = null;

      public ProgressThread(Handler handler) {
         this.handler = handler;
      }

      @Override
      public void run() {
         try {
            Thread.sleep(3000);
            Message message = handler.obtainMessage();
            Bundle bundle = new Bundle();
            bundle.putBoolean("done", true);
            message.setData(bundle);
            handler.sendMessage(message);
         } catch (InterruptedException e) {
            Log.e("MainActivity", e.getMessage());
         }
      }
   }
}
