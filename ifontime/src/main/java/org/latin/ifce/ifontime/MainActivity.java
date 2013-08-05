package org.latin.ifce.ifontime;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.Menu;

import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;
import org.latin.ifce.ifontime.model.HorarioHelper;

public class MainActivity extends Activity {

    private Cursor model;
    private HorarioHelper helper = new HorarioHelper(this);
    private ListaAdapter adapter;
    private ListView lvHorarios;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        carregar();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    private static class HorarioHolder{
        private TextView professor, horario, local, disciplina;

        public HorarioHolder(View linha) {
            professor = (TextView) linha.findViewById(R.id.tvProf);
            horario = (TextView) linha.findViewById(R.id.tvHorario);
            local = (TextView) linha.findViewById(R.id.tvLocal);
            disciplina = (TextView) linha.findViewById(R.id.tvDisciplina);
        }

        public void popularForm(Cursor c) {
            disciplina.setText(c.getString(1));
            professor.setText(c.getString(1));
        }

        /*
        disciplina, horario_inicio, horario_fim, ");
      builder.append("sala, professor, dia_da_semana ");
         */

    }

    private class ListaAdapter extends CursorAdapter{

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

    private void carregar(){
        lvHorarios = (ListView) findViewById(R.id.lvHorarios);
        model = helper.listar("1");
        startManagingCursor(model);
        adapter = new ListaAdapter(model);
        lvHorarios.setAdapter(adapter);
    }
    
}
