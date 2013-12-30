package org.latin.ifce.ifontime.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import fidias.model.Helper;

/**
 * Created by atila on 02/08/13.
 */
public class HorarioHelper extends Helper {

   public HorarioHelper(Context context) {
      super(context);
   }

   public Cursor list(int diaDaSemana) {
      StringBuilder builder = new StringBuilder();
      builder.append("SELECT _id, disciplina, horario_inicio, horario_fim, ");
      builder.append("sala, professor, dia_da_semana ");
      builder.append("FROM horarios WHERE dia_da_semana = ? ");
      builder.append("ORDER BY horario_inicio ");
      return getReadableDatabase().rawQuery(builder.toString(),
              new String[]{
                      String.valueOf(diaDaSemana)
              });
   }

   public int deleteAll() {
      return getWritableDatabase().delete("horarios", null, null);
   }

   public void create(ContentValues values) {
      create("horarios", null, values);
   }

   public boolean existsAnySchedule() {
      StringBuilder builder = new StringBuilder();
      builder.append("SELECT COUNT(*) as count FROM horarios ");
      Cursor c = getReadableDatabase().rawQuery(builder.toString(), new String[]{});
      c.moveToFirst();
      return (c.getInt(c.getColumnIndex("count")) > 0 ? true : false);
   }
}
