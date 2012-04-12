/**
 * 
 */
package edu.buffalo.cse.cse486_586.simpledht.provider;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import edu.buffalo.cse.cse486_586.simpledht.vo.MessageObject;
import edu.buffalo.cse.cse486_586.simpledht.vo.MessageType;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

/**
 * @author sravan
 *
 */
public class MyContentProvider extends ContentProvider {

	public static final String AUTHORITY = "edu.buffalo.cse.cse486_586.simpledht.provider";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);

	private static final String CENTRAL_SERVER_ID = "5554";
	private static final int SERVER_BIND_PORT = 10000;
	private static final int SERVER_CONNECT_PORT = 11108;
	private static final String SERVER_ADDRESS = "10.0.2.2";
	private static final String __NODE_ID__ = "__NODE_ID__";
	//private CountDownLatch blocker = null;
	private ArrayBlockingQueue<String> uriBlockingQueue = new ArrayBlockingQueue<String>(5);
	private ArrayBlockingQueue<Map<String, String>> cursorBlockingQueue = new ArrayBlockingQueue<Map<String, String>>(5);

	private Node current_Node = new Node();

	private boolean isWindows = false;

	public static String nodeID;

	private MyDataHelper dataHelper;
	private String database_name = "KeyValueStore";

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {

		return 0;
	}

	@Override
	public String getType(Uri uri) {

		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		//MessageObject msg = new M
		//msgFromExistingNode()
		String key = values.getAsString(MyDataHelper.COLUMN_KEY);
		String value = values.getAsString(MyDataHelper.COLUMN_VALUE);

		// forwared request, happens only in case of new_req. (fwded req doesn't come here.
		if(!isHashBetweenHashes(current_Node.predecessor.hashValue, current_Node.hashValue, genHash(key))){
			sendMessageToOtherNode(nodeID,resolvePortNumber(current_Node.successor.node_id), MessageType.INSERT_RECORD ,key, value);
			try {
				String ret_uri = uriBlockingQueue.take();
				if(ret_uri == null){
					Log.d("S_DHT", "Exception: INSERT_RETURN_VALUE_IS_NULL");
					return null;
				}
				return Uri.parse(ret_uri);
			} catch (InterruptedException e) {
				Log.d("S_DHT", "Exception: "+e.getMessage());
				e.printStackTrace();
				return null;
			}
		}



		SQLiteDatabase db = dataHelper.getWritableDatabase();

		/*String key = values.getAsString(MyDataHelper.COLUMN_KEY);
		if(key.equals("0"))
			dataHelper.onCreate(db);*/

		long id = db.insert(MyDataHelper.TABLE_NAME, null, values);
		if(id > 0){
			Uri ret_Uri = ContentUris.withAppendedId(MyContentProvider.CONTENT_URI, id);
			getContext().getContentResolver().notifyChange(ret_Uri, null);
			return ret_Uri;
		}
		return null;
	}

	/**
	 * This method is expected to be started on Application Start.
	 * 
	 */
	@Override
	public boolean onCreate() {
		dataHelper = new MyDataHelper(getContext(),
				database_name,
				null,
				1);

		// Instead of calling init directly, introduce a delay, onCreate must be finished
		(new Thread(new InitRunnable())).start();
		Log.d("S_DHT","MCP: "+"MyContentProvider.onCreate(): Begin");
		//return false;
		return true;
	}

	private class InitRunnable implements Runnable{
		@Override
		public void run() {
			try{
				Thread.sleep(2000);
			}
			catch(Exception e){
				e.printStackTrace();
			}
			init();
		}

	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {

		String key = selection;

		// forwared request, happens only in case of new_req. (fwded req doesn't come here.
		if(!isHashBetweenHashes(current_Node.predecessor.hashValue, current_Node.hashValue, genHash(key))){
			sendMessageToOtherNode(nodeID,resolvePortNumber(current_Node.successor.node_id), MessageType.MSG_REQUEST ,key, "");
			try {
				Map<String, String> ret_uri = cursorBlockingQueue.take();
				if(ret_uri == null){
					Log.d("S_DHT", "Exception: INSERT_RETURN_VALUE_IS_NULL");
					return null;
				}
				MatrixCursor cursor = new MatrixCursor(new String[]{MyDataHelper.COLUMN_KEY, MyDataHelper.COLUMN_VALUE});
				for(String k:ret_uri.keySet()){
					cursor.addRow(new String[]{k,ret_uri.get(k)});
				}
				return cursor;
			} catch (InterruptedException e) {
				Log.d("S_DHT", "Exception: "+e.getMessage());
				e.printStackTrace();
				return null;
			}
		}


		SQLiteQueryBuilder qBuilder = new SQLiteQueryBuilder();
		qBuilder.setTables(MyDataHelper.TABLE_NAME);
		if(selection != null){
			//qBuilder.appendWhere("provider_key = " + selection + "");
			selection = MyDataHelper.COLUMN_KEY + " = '" + selection + "'";
		}

		SQLiteDatabase db = dataHelper.getReadableDatabase();

		//Cursor cursor = qBuilder.query(db, projection, "provider_key = '" + selection + "'", null, null, null, null);
		Cursor cursor = qBuilder.query(db, projection, selection , null, null, null, null);
		cursor.setNotificationUri(getContext().getContentResolver(), uri);

		return cursor;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// IMPLEMENT_THIS
		return 0;
	}

	private class Node{

		public String node_id;
		public String hashValue;
		public Node successor;
		public Node predecessor;

		public Node(){

		}

		public Node(String node_id, String hashValue){
			this.node_id = node_id;
			this.hashValue = hashValue;
		}

		public void set(String node_id, String hashValue, Node successor,
				Node predecessor) {
			this.node_id = node_id;
			this.hashValue = hashValue;
			this.successor = successor;
			this.predecessor = predecessor;
		}
		public void set(String node_id, String hashValue) {
			this.node_id = node_id;
			this.hashValue = hashValue;
		}

		public void setSuccessor(Node successor) {
			this.successor = successor;
		}

		public void setPredecessor(Node predecessor) {
			this.predecessor = predecessor;
		}
	}

	private int getServerPort(){
		if(isWindows){
			return (resolvePortNumber(nodeID) - 1000);
		}
		return SERVER_BIND_PORT;
	}

	private void init(){		
		// Every node will have a server.
		Thread serverThread = new Thread(new ServerRunnable(getServerPort(), nodeID));
		current_Node.set(nodeID, genHash(nodeID));
		current_Node.successor = current_Node;
		current_Node.predecessor = current_Node;
		Log.d("S_DHT","INIT: "+nodeID);
		try{
			Thread.sleep(2000);
			serverThread.start();
		}
		catch(InterruptedException e){
			e.printStackTrace();
		}
		if(!nodeID.equals(CENTRAL_SERVER_ID)){ // If not 5554. (New Node JOIN.)
			Thread newConnection = new Thread(
					new ClientRunnable(nodeID, SERVER_CONNECT_PORT,true, false, false, false, MessageType.NEW_NODE_JOIN, "", ""));
			newConnection.start();
		}
	}

	private void storeMessage(String msg){
		ContentValues cv = new ContentValues();
		cv.put(MyDataHelper.COLUMN_KEY, 0 + "");
		cv.put(MyDataHelper.COLUMN_VALUE, msg);
		//MyContentProvider cp = new MyContentProvider();

		Uri I_Uri = this.insert(MyContentProvider.CONTENT_URI, cv);
		String provider_key = I_Uri.getLastPathSegment();
		Cursor result = this.query(MyContentProvider.CONTENT_URI, null, "0", null, null);
		if(result != null && result.getCount() > 0 ){
			result.moveToFirst();
			Log.d("S_DHT", "MCP: "+result.getString(0) + ": " + result.getString(1));
		}
		/*try{
			if(result.getCount() > 0)
				result.moveToPosition(msgObj.getSeq_id());
			Log.d("CP_Log", result.getString(1));
		}
		catch(Exception e){
			Log.d("CP_Log", e.getMessage());
		}*/
	}



	private class MessageInserter implements Runnable{

		@Override
		public void run() {
			storeMessage("test0");
		}

	}

	private synchronized void sendMessageToOtherNode(String node_id,int port_number, MessageType messageType, String l_key, String l_value){
		// Intentionally created new objects.
		String key = new String(l_key);
		String value = new String(l_value);
		//if(!nodeID.equals(CENTRAL_SERVER_ID)){ // If not 5554. (New Node JOIN.)
		Thread newConnection = new Thread(
				new ClientRunnable(node_id, port_number,false, false, false, false, messageType, key, value));
		newConnection.start();
		//}
	}

	private synchronized void sendMessageToOtherNode(String node_id,int port_number, MessageType messageType, String l_key, String l_value, String uri){
		// Intentionally created new objects.
		String key = new String(l_key);
		String value = new String(l_value);
		//if(!nodeID.equals(CENTRAL_SERVER_ID)){ // If not 5554. (New Node JOIN.)
		Thread newConnection = new Thread(
				new ClientRunnable(node_id, port_number,false, false, false, false, messageType, key, value, uri));
		newConnection.start();
		//}
	}

	private synchronized void sendMessageToOtherNode(String node_id,int port_number, MessageType messageType, String l_key, String l_value, Map<String,String> records){
		// Intentionally created new objects.
		String key = new String(l_key);
		String value = new String(l_value);
		//if(!nodeID.equals(CENTRAL_SERVER_ID)){ // If not 5554. (New Node JOIN.)
		Thread newConnection = new Thread(
				new ClientRunnable(node_id, port_number,false, false, false, false, messageType, key, value, records));
		newConnection.start();
		//}
	}

	int resolvePortNumber(String node_id){
		return Integer.parseInt(node_id)*2;
	}

	public void printAdjacentNodes(){
		Log.d("S_DHT",nodeID+" --> Successor: "+current_Node.successor.node_id + ",  Predecessor: " + current_Node.predecessor.node_id );
	}

	/**
	 * Assumption that two node_id's doens't have the same hash value.
	 * @param socket
	 * @param msg
	 */
	private void newClientSocket(Socket socket, MessageObject msg){
		Log.d("S_DHT","MCP: "+"in newClientSocket()");
		if(nodeID.equals(CENTRAL_SERVER_ID)){
			if(current_Node.hashValue.equals(current_Node.successor.hashValue)){
				// Only one node in system. // ACCEPT REQUEST
				//Node succ = current_Node.successor;
				sendMessageToOtherNode(nodeID,resolvePortNumber(msg.getNodeId()), MessageType.NEW_NODE_ACCEPT ,current_Node.successor.node_id,current_Node.successor.hashValue);
				//current_Node.predecessor = new Node(msg.getNodeId(), genHash(msg.getNodeId())); -- msg from node is expected (or) no change required
				current_Node.successor = new Node(msg.getNodeId(), genHash(msg.getNodeId()));
			}
			else if(isHashBetweenHashes(current_Node.hashValue, current_Node.successor.hashValue, genHash(msg.getNodeId()))){
				//Node
				sendMessageToOtherNode(nodeID,resolvePortNumber(msg.getNodeId()), MessageType.NEW_NODE_ACCEPT ,current_Node.successor.node_id,current_Node.successor.hashValue);
				current_Node.successor = new Node(msg.getNodeId(), genHash(msg.getNodeId()));
			}
			else{
				sendMessageToOtherNode(nodeID,resolvePortNumber(msg.getNodeId()), MessageType.CONNECT_NEXT_NODE ,current_Node.successor.node_id,current_Node.successor.hashValue);
			}
			msg.setNewClient(false);

		}
		else{
			// curr & succ will not be equal
			// CHECK if is between hashes (curr, succ) ACCEPT, else CONNECT_SUCC
			if(isHashBetweenHashes(current_Node.hashValue, current_Node.successor.hashValue, genHash(msg.getNodeId()))){
				//Node
				sendMessageToOtherNode(nodeID,resolvePortNumber(msg.getNodeId()), MessageType.NEW_NODE_ACCEPT ,current_Node.successor.node_id,current_Node.successor.hashValue);
				current_Node.successor = new Node(msg.getNodeId(), genHash(msg.getNodeId()));
			}
			else{
				sendMessageToOtherNode(nodeID,resolvePortNumber(msg.getNodeId()), MessageType.CONNECT_NEXT_NODE ,current_Node.successor.node_id,current_Node.successor.hashValue);
			}
		}
		printAdjacentNodes();
	}

	// 0-100
	public boolean isHashBetweenHashes(String current, String next, String find){
		if(hashCompare(next, current) == 0 || hashCompare(next, find) == 0 
				|| hashCompare(find, current) == 0){
			return false;
		}
		else if(hashCompare(next, current) > 0){
			if(hashCompare(find, current) > 0 && hashCompare(next, find) > 0){
				return true;
			}
			else{
				return false;
			}
		}
		else{ // 90,10
			if(hashCompare(find, current) > 0){ // 91
				return true;
			}
			else if(hashCompare(next, find) > 0){ // 9
				return true;
			}
			else{ // other
				return false;
			}
		}
	}

	public int hashCompare(String current, String other){
		int curLength = current.length();
		int otherLen = other.length();
		int retValue = 0;
		while(otherLen > 0 && curLength > 0){
			if( current.charAt(curLength-1) == other.charAt(otherLen-1))
				retValue = retValue + (current.charAt(curLength-1) - other.charAt(otherLen-1));
			else
				retValue = (current.charAt(curLength-1) - other.charAt(otherLen-1));

			otherLen--;
			curLength--;
		}
		if(curLength == 0 && otherLen == 0){
			return retValue;
		}
		else if(curLength > 0){
			return 1;
		}
		else if(otherLen > 0){
			return -1;
		}
		Log.d("S_DHT","HASH:"+ " This must not be printed");
		return 0;
	}

	private void msgFromExistingNode(MessageObject msg){
		Log.d("S_DHT","MCP: "+"in msgFromExistingNode()");
		if(msg.getMessageType() == MessageType.NEW_NODE_ACCEPT){
			current_Node.successor = new Node(msg.getKey(),msg.getValue());
			current_Node.predecessor = new Node(msg.getNodeId(),genHash(msg.getNodeId()));
			sendMessageToOtherNode(nodeID,resolvePortNumber(current_Node.successor.node_id), MessageType.SET_NEW_PREDECESSOR ,current_Node.successor.node_id,current_Node.successor.hashValue);
		}
		else if(msg.getMessageType() == MessageType.CONNECT_NEXT_NODE){
			sendMessageToOtherNode(nodeID,resolvePortNumber(msg.getKey()), MessageType.NEW_NODE_JOIN ,current_Node.successor.node_id,current_Node.successor.hashValue);
		}
		else if(msg.getMessageType() == MessageType.INSERT_RECORD){
			String keyHashValue = genHash(msg.getKey());
			if((hashCompare(current_Node.hashValue, keyHashValue) == 0) || isHashBetweenHashes(current_Node.predecessor.hashValue, current_Node.hashValue, keyHashValue) ){
				// FIND MESSAGE AND REPLY TO SOURCE, REQUIRE_SOURCE_ADDRESS
				// IF CURRENT NODE, REPLY DIRECTLY
				ContentValues cv = new ContentValues();
				cv.put(MyDataHelper.COLUMN_KEY, msg.getKey());
				cv.put(MyDataHelper.COLUMN_VALUE, msg.getValue());
				//MyContentProvider cp = new MyContentProvider();

				Uri reply_Uri = this.insert(MyContentProvider.CONTENT_URI, cv);
				sendMessageToOtherNode(msg.getNodeId(),resolvePortNumber(msg.getNodeId()), MessageType.INSERT_REPLY ,"","",reply_Uri.toString());
				//ELSE -> reply to source.
			}
			else{
				//msg.getNodeId() -> should & will store the request's actual node.
				sendMessageToOtherNode(msg.getNodeId(),resolvePortNumber(current_Node.successor.node_id), MessageType.INSERT_RECORD ,msg.getKey(),msg.getValue());
				//FORWARD TO NEXT NODE. (ATTACHING SOURCE NODE_ID)
			}
		}
		else if(msg.getMessageType() == MessageType.MSG_REQUEST){
			String keyHashValue = genHash(msg.getKey());
			if((hashCompare(current_Node.hashValue, keyHashValue) == 0) || isHashBetweenHashes(current_Node.predecessor.hashValue, current_Node.hashValue, keyHashValue) ){
				// FIND MESSAGE AND REPLY TO SOURCE, REQUIRE_SOURCE_ADDRESS
				// IF CURRENT NODE, REPLY DIRECTLY

				Cursor cursor = this.query(MyContentProvider.CONTENT_URI,null,msg.getKey(),null,null);

				Map<String, String> records = new HashMap<String, String>();
				if(cursor != null){
					while(cursor.moveToNext()){
						records.put(cursor.getString(0), cursor.getString(1));
					}
				}

				sendMessageToOtherNode(msg.getNodeId(),resolvePortNumber(msg.getNodeId()), MessageType.MSG_REPLY ,"","",records);
				//ELSE -> reply to source.
			}
			else{
				//msg.getNodeId() -> should & will store the request's actual node.
				sendMessageToOtherNode(msg.getNodeId(),resolvePortNumber(current_Node.successor.node_id), MessageType.MSG_REQUEST ,msg.getKey(),msg.getValue());
				//FORWARD TO NEXT NODE. (ATTACHING SOURCE NODE_ID)
			}
		}
		else if(msg.getMessageType() == MessageType.MSG_REPLY){
			//DELIVER MESSAGE TO CLIENT
			try {
				cursorBlockingQueue.put(msg.getRecords());
			} catch (InterruptedException e) {
				Log.d("S_DHT", "Exception: cursorBlockingQueue.put() failed: "+e.getMessage());
				e.printStackTrace();
			}
		}
		else if(msg.getMessageType() == MessageType.INSERT_REPLY){
			//DELIVER MESSAGE TO CLIENT
			try {
				uriBlockingQueue.put(msg.getUri());
			} catch (InterruptedException e) {
				Log.d("S_DHT", "Exception: uriBlockingQueue.put() failed: "+e.getMessage());
				e.printStackTrace();
			}
		}
		else if(msg.getMessageType() == MessageType.SET_NEW_PREDECESSOR){
			current_Node.predecessor = new Node(msg.getNodeId(),genHash(msg.getNodeId()));
		}
		else if(msg.getMessageType() == MessageType.INSERT_RECORD){
			if(hashCompare(current_Node.hashValue, genHash(msg.getKey())) == 0 || isHashBetweenHashes(current_Node.hashValue, current_Node.successor.hashValue, genHash(msg.getKey()))){
				// PERFORM INSERT OR UPDATE
			}
			else{
				sendMessageToOtherNode(nodeID,resolvePortNumber(current_Node.successor.node_id), MessageType.INSERT_RECORD ,msg.getKey(),msg.getValue());
			}
		}
		else{
			Log.d("S_DHT","EXCEPTION: "+ "Invalid message received.");
		}
		printAdjacentNodes();
	}

	private class ServerRunnable implements Runnable {

		private int portNumber = 10000;
		private ServerSocket server = null;
		private Socket client = null;
		private String nodeId = null;

		public ServerRunnable(int i_portNumber,String s_nodeId){
			this.portNumber = i_portNumber;
			this.nodeId = s_nodeId;
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

				Log.d("S_DHT","SR: "+ "Server at " + nodeId + " binded to port " + portNumber);
				while(true){
					try {
						client = server.accept();
						Log.d("S_DHT","SR: "+"Client Connected!!");

						// Read a message immediately after connection.
						ObjectInputStream ois = new ObjectInputStream(client.getInputStream());
						MessageObject msg = null;
						try {
							msg = (MessageObject)ois.readObject();
							System.out.println(msg);
							if( msg.getMessageType() == MessageType.NEW_NODE_JOIN){
								//check if new_node
								Log.d("S_DHT","SR: "+"Calling: newClientSocket()");
								newClientSocket(client,msg);
								//if msg existing_node
							}
							else {
								msgFromExistingNode(msg);
							}

						} catch (ClassNotFoundException e) {
							e.printStackTrace();
							Log.d("S_DHT","EXCEPTION: "+"ServerRunnable.run(): "+e.getMessage());
						}

					}
					catch(IOException e){
						Log.d("S_DHT","SR: " + "Server generated an exception");
					}
				}
				//boolean isClientAlive = true;

			} catch (IOException e) {
				Log.d("S_DHT","SR: "+"Error Creating Socket.");
				e.printStackTrace();
			}
		}
	}


	/**
	 * @author sravan
	 *
	 */
	public class ClientRunnable implements Runnable{

		private String node_id;
		private int port_number;
		private Socket client;

		private String ip_addr = MyContentProvider.SERVER_ADDRESS;

		private boolean isNewClient = false;
		private boolean isForwarded = false;
		private boolean isReply = false;
		private boolean connectToNext = false;
		private MessageType messageType;
		private String key = "";
		private String value = "";
		private MessageObject msgObj;
		private String uri;
		private Map<String, String> records;


		/**
		 * @param node_id
		 * @param port_number
		 * @param isNewClient
		 * @param isForwarded
		 * @param isReply
		 * @param connectToNext
		 * @param key
		 * @param value
		 */
		public ClientRunnable(String node_id,int port_number, boolean isNewClient, 
				boolean isForwarded, boolean isReply, boolean connectToNext, MessageType messageType, String key, String value){
			this.node_id = node_id;
			this.port_number = port_number;
			this.isForwarded = isForwarded;
			this.isNewClient = isNewClient;
			this.isReply = isReply;
			this.connectToNext = connectToNext;
			this.messageType = messageType;
			this.key = key;
			this.value= value;
		}

		public ClientRunnable(String node_id,int port_number, boolean isNewClient, 
				boolean isForwarded, boolean isReply, boolean connectToNext, MessageType messageType, String key, String value, String uri){
			this.node_id = node_id;
			this.port_number = port_number;
			this.isForwarded = isForwarded;
			this.isNewClient = isNewClient;
			this.isReply = isReply;
			this.connectToNext = connectToNext;
			this.messageType = messageType;
			this.key = key;
			this.value= value;
			this.uri = uri;
		}

		public ClientRunnable(String node_id,int port_number, boolean isNewClient, 
				boolean isForwarded, boolean isReply, boolean connectToNext, MessageType messageType, String key, String value, Map<String,String> records){
			this.node_id = node_id;
			this.port_number = port_number;
			this.isForwarded = isForwarded;
			this.isNewClient = isNewClient;
			this.isReply = isReply;
			this.connectToNext = connectToNext;
			this.messageType = messageType;
			this.key = key;
			this.value= value;
			this.records = records;
		}

		public void run(){
			try {
				InetAddress addr = InetAddress.getByName(ip_addr);
				client = new Socket(addr, port_number);
				msgObj = new MessageObject(isForwarded, isNewClient, isReply, connectToNext, messageType, node_id, key, value);
				msgObj.setUri(uri);
				msgObj.setRecords(records);
				Log.d("S_DHT","CR: "+"Connected to Server!!");
				(new ObjectOutputStream(client.getOutputStream())).writeObject(msgObj);

			} catch (IOException e) {
				Log.d("S_DHT","EXCEPTION: "+"Error creating client socket: "+e.getMessage());
				//e.printStackTrace();
			}

		}

	}


	private String genHash(String input){
		MessageDigest sha1;
		try {
			sha1 = MessageDigest.getInstance("SHA-1");
			byte[] sha1Hash = sha1.digest(input.getBytes());
			Formatter formatter = new Formatter();
			for (byte b : sha1Hash) {
				formatter.format("%02x", b);
			}
			return formatter.toString();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		}		
	}

}
