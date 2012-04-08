package edu.buffalo.cse.cse486_586.simpledht;

import edu.buffalo.cse.cse486_586.simpledht.provider.MyContentProvider;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.widget.TextView;

public class SimpleDHTActivity extends Activity {
	
	TextView tv = null;
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
}