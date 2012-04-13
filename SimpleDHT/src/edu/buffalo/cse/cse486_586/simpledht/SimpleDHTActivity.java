package edu.buffalo.cse.cse486_586.simpledht;

import edu.buffalo.cse.cse486_586.simpledht.provider.MyContentProvider;
import edu.buffalo.cse.cse486_586.simpledht.provider.MyDataHelper;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class SimpleDHTActivity extends Activity {

	TextView tv = null;
	private int count = 0;
	private int getRecord = 0;
	private Handler handler = null;
	private final String _INFO_TEXT_ = "Simple Key-Value Storage\n" +
			"---------------------------\n" +
			"1) 'Delete Dump' deletes Local Data\n" +
			"2) Insert with an existing key updates the data\n" +
			"*  If only single node in system, stores everything in local";
	

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		tv = (TextView) findViewById(R.id.textView);
		tv.setText(_INFO_TEXT_);
		handler = new Handler();
		init();
	}

	private void init(){
		MyContentProvider.nodeID = getNodePortNumber();
	}

	public String getNodePortNumber(){
		TelephonyManager tel = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
		return tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
	}

	public Uri insertRecord(String key, String value){
		ContentValues cv = new ContentValues();
		cv.put(MyDataHelper.COLUMN_KEY, key);
		cv.put(MyDataHelper.COLUMN_VALUE, value);
		//MyContentProvider cp = new MyContentProvider();

		Uri ret_Uri = getContentResolver().insert(MyContentProvider.CONTENT_URI, cv);

		/*String provider_key = I_Uri.getLastPathSegment();
		Cursor result = getContentResolver().query(MyContentProvider.CONTENT_URI, null, provider_key, null, null);
		try{
			if(result.moveToFirst())
				Log.d("S_DHT", "result_in_activity: "+result.getString(1));
		}
		catch(Exception e){
			Log.d("S_DHT", "Exception: Activity : "+e.getMessage());
		}*/
		return ret_Uri;
	}
	
	public int deleteRecords(String key){
		
		int ret_val = getContentResolver().delete(MyContentProvider.CONTENT_URI, key,null);
		return ret_val;
	}

	public void fetchRecord(String key){

		//String provider_key = I_Uri.getLastPathSegment();
		Cursor result = getContentResolver().query(MyContentProvider.CONTENT_URI, null, key, null, null);
		try{
			if(result.moveToFirst()){
				Log.d("S_DHT", "Result_in_activity: "+result.getString(1));
				tv.setText("Value: "+result.getString(1));
			}
		}
		catch(Exception e){
			Log.d("S_DHT", "Exception: Activity : "+e.getMessage());
		}
	}

	public Cursor getRecords(String key){
		//String provider_key = I_Uri.getLastPathSegment();
		return getContentResolver().query(MyContentProvider.CONTENT_URI, null, key, null, null);
	}

	public void appendTextInView(String message){
		tv.append(message);
	}
	
	public void setTextInView(String message){
		tv.setText(message);
	}
	
	/**
	 * Used to insert & query records with 1 second delay.
	 * @author sravan
	 *
	 */
	private class TestRunnable implements Runnable{
		private Handler handler = null;
		
		public TestRunnable(Handler handler) {
			super();
			this.handler = handler;
		}

		@Override
		public void run() {
			handler.post(new Runnable() {
				@Override
				public void run() {
					setTextInView("Inserting records:\n");
				}
			});	
			for(int i=0;i<10;i++){
				insertRecord(i+"","test"+i);
				try{
					Thread.sleep(1000);
				}
				catch(InterruptedException e){
					Log.d("S_DHT", "Exception in TestRunnable.run(): "+e.getMessage());
				}
			}
			
			handler.post(new Runnable() {
				@Override
				public void run() {
					setTextInView("Queries from DHT: <key : value>\n" +
					  "-------------------------------\n");
				}
			});
			
			for(int i=0;i<10;i++){
				Cursor result = getRecords(""+i);
				try{
					String tempStr = "";
					while(result.moveToNext()){
						tempStr += result.getString(0) + " : " + result.getString(1) + "\n"; 
						Log.d("S_DHT", "Result_in_activity: "+result.getString(1));
					}
					final String resultString = tempStr;
					handler.post(new Runnable() {
						@Override
						public void run() {
							appendTextInView(resultString);
						}
					});	
				}
				catch(Exception e){
					Log.d("S_DHT", "Exception: Activity : "+e.getMessage());
				}
				try{
					Thread.sleep(1000);
				}
				catch(InterruptedException e){
					Log.d("S_DHT", "Exception in TestRunnable.run(): "+e.getMessage());
				}
			}
		}

	}
	
	private class DumpRunnable implements Runnable{
		private Handler handler = null;

		public DumpRunnable(Handler handler){
			this.handler = handler;
		}
		
		@Override
		public void run() {
			Cursor result = getRecords(null);
			try{
				String tempStr = "";
				while(result.moveToNext()){
					tempStr += result.getString(0) + " : " + result.getString(1) + "\n"; 
					Log.d("S_DHT", "Result_in_activity: "+result.getString(1));
				}
				final String resultString = tempStr;
				handler.post(new Runnable() {
					@Override
					public void run() {
						appendTextInView(resultString);
					}
				});	
			}
			catch(Exception e){
				Log.d("S_DHT", "Exception: Activity: "+e.getMessage());
			}
		}
		
	}

	public void onTestClick(View view){
		setTextInView("Queries from DHT: <key : value>\n" +
					  "-------------------------------\n");
		(new Thread(new TestRunnable(handler))).start();
		//count++;
	}

	public void onDumpClick(View view){
		setTextInView("Local Data Dump: <key : value>\n" +
					  "------------------------------\n");
		(new Thread(new DumpRunnable(handler))).start();
		//count++;
	}

	public void onInsertRecord(View view){
		insertRecord(""+count,"test"+count);
		count++;
	}

	public void onFetchRecord(View view){
		fetchRecord(""+getRecord);
		getRecord++;
	}
	
	public void onDeleteDumpClick(View view){
		int no_of_records = deleteRecords(null);
		setTextInView("No of records Deleted: " + no_of_records);
	}
}