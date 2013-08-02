package org.latin.ifce.ifontime.model;

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

   public Cursor listar(String diaDaSemana) {
      StringBuilder builder = new StringBuilder();
      builder.append("SELECT _id, disciplina, horario_inicio, horario_fim, ");
      builder.append("sala, professor, dia_da_semana ");
      builder.append("FROM horarios WHERE dia_da_semana = ? ");
      return getReadableDatabase().rawQuery(builder.toString(), new String[]{diaDaSemana});
   }
}
