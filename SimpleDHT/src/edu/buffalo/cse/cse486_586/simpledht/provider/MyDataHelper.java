/**
 * 
 */
package edu.buffalo.cse.cse486_586.simpledht.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * @author sravan
 *
 */
public class MyDataHelper extends SQLiteOpenHelper {
	
	public final static String TABLE_NAME = "KeyLookup";
	public final static String COLUMN_KEY = "provider_key";
	public final static String COLUMN_VALUE = "provider_value";
	
	public final String CREATE_TABLE_SQL = "CREATE TABLE " +TABLE_NAME +
											" (" +
											"	" + COLUMN_KEY + " TEXT PRIMARY KEY," +
											"	" + COLUMN_VALUE + " TEXT" +
											");";

	public final String DROP_TABLE_SQL = "DROP TABLE IF EXISTS " + TABLE_NAME;

	public MyDataHelper(Context context, String name, 
						CursorFactory factory,int version) {
		super(context, name, factory, version);
		
	}

	/* (non-Javadoc)
	 * @see android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite.SQLiteDatabase)
	 */
	@Override
	public void onCreate(SQLiteDatabase sqlDb) {
		sqlDb.execSQL(DROP_TABLE_SQL);
		sqlDb.execSQL(CREATE_TABLE_SQL);
	}

	/* (non-Javadoc)
	 * @see android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database.sqlite.SQLiteDatabase, int, int)
	 */
	@Override
	public void onUpgrade(SQLiteDatabase sqlDb, int oldVersion, int newVersion) {
		sqlDb.execSQL(DROP_TABLE_SQL);
		onCreate(sqlDb);
	}

}
