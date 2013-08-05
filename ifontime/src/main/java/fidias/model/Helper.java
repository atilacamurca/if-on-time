package fidias.model;

import org.latin.ifce.ifontime.R;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class Helper extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "if-on-time.db";
	private static final int VERSION = 1;
	private static final String DROP_IF_EXISTS = "DROP TABLE IF EXISTS ";
	protected Context context;

	public Helper(Context context) {
		super(context, DATABASE_NAME, null, VERSION);
        this.context = context;
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(context.getString(R.string.sql_create_table_horarios));
      db.execSQL(context.getString(R.string.sql_insert_default));
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int odlVersion, int newVersion) {
		
	}
	
	/**
	 * insert a new row in the database.
	 * insere uma nova coluna no banco de dados.
	 * @param table
	 * @param nullColumnRack
	 * @param values
	 */
	public long create(String table, String nullColumnRack, ContentValues values) {
		return getWritableDatabase().insert(table, nullColumnRack, values);
	}
	
	/**
	 * delete an existing row in the database.
	 * deleta uma coluna existente no banco de dados.
	 * @param table
	 * @param id
	 * @return
	 */
	public int delete(String table, String id) {
		String whereClause = "_id = ?";
		String[] whereArgs = {id};
		return getWritableDatabase().delete(table, whereClause, whereArgs);
	}
	
	/**
	 * update existing row in the database.
	 * atualiza uma coluna existente no banco de dados.
	 * @param table
	 * @param id
	 * @param values
	 */
	public int update(String table, String id, ContentValues values) {
		String whereClause = "_id = ?";
		String[] whereArgs = {id};
		return getWritableDatabase().update(table, values, whereClause, whereArgs);
	}
}
