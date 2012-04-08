/**
 * 
 */
package edu.buffalo.cse.cse486_586.simpledht.comm;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import android.util.Log;

/**
 * 
 * @author sravan
 *
 */
public class ServerRunnable implements Runnable {

	private int portNumber = 10000;
	private ServerSocket server = null;
	private Socket client = null;
	private String nodeId = null;
	
	public ServerRunnable(int portNumber,String nodeId){
		this.portNumber = portNumber;
		this.nodeId = nodeId;
	}
	
	/**
	 * 
	 * (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		try{
			server = new ServerSocket(portNumber);
			//IS_SERVER_RUNNING = true;

			Log.d("SR", "Server at " + nodeId + "binded to port " + portNumber);
			//Log.d(node.toString(), "Manually redirect traffic to port 9090.");
			while(true){
				try {

					/*UI_Handler.post(new Runnable() {
						public void run() {
							activity.messsageFromServer("Waiting for Client to connect!");
						}
					});*/

					client = server.accept();
					Log.d("SR","Client Connected!!");
					/*UI_Handler.post(new Runnable() {
						public void run() {
							activity.messsageFromServer("Client Connected!!");
						}
					});*/
					

				}
				catch(IOException e){
					Log.d("SR", "Server generated an exception");
				}
			}
			//boolean isClientAlive = true;

		} catch (IOException e) {
			Log.d("SR","Error Creating Socket.");
			e.printStackTrace();
		}

	}
}
