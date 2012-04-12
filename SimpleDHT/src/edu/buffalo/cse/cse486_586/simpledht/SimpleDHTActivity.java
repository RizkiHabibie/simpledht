package edu.buffalo.cse.cse486_586.simpledht;

import edu.buffalo.cse.cse486_586.simpledht.provider.MyContentProvider;
import edu.buffalo.cse.cse486_586.simpledht.provider.MyDataHelper;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class SimpleDHTActivity extends Activity {
	
	TextView tv = null;
	private int count = 0;
	private int getRecord = 0;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        tv = (TextView) findViewById(R.id.textView);
        tv.setText("Simple DHT Application");
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
    
    public void onInsertRecord(View view){
    	insertRecord(""+count,"test"+count);
    	count++;
    }
    
    public void onFetchRecord(View view){
    	fetchRecord(""+getRecord);
    	getRecord++;
    }
}