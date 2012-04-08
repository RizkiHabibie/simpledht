/**
 * 
 */
package edu.buffalo.cse.cse486_586.simpledht.comm;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import android.util.Log;

/**
 * @author sravan
 *
 */
public class ClientRunnable implements Runnable {

	private String serverAddr = "10.0.2.2";
	private int portNumber;
	private Socket client;
	
	public ClientRunnable(int portNumber){
		this.portNumber = portNumber;
	}
	
	/** 
	 * (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		try {

			InetAddress addr = InetAddress.getByName(serverAddr);

			client = new Socket(addr, portNumber);

			Log.d("CR","Connected to Server!!");
			/*UI_Handler.post(new Runnable() {
				@Override
				public void run() {
					activity.messageFromClient("Connected to Server!!");
				}
			});*/

		} catch (IOException e) {
			Log.d("CR","Error creating client socket");
			//e.printStackTrace();
		}
	}

}
