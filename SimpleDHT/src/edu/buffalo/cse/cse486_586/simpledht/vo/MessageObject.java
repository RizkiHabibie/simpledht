/**
 * 
 */
package edu.buffalo.cse.cse486_586.simpledht.vo;

/**
 * @author sravan
 *
 */
public class MessageObject {
	private boolean isForwarded = false;
	private boolean isNewClient = false;
	private boolean isReply = false;
	private String nodeId;
	private String key;
	private String value;
	
	/**
	 * @param isForwarded
	 * @param isNewClient
	 * @param isReply
	 * @param isNodeId
	 * @param key
	 * @param value
	 */
	public MessageObject(boolean isForwarded, boolean isNewClient,
			boolean isReply, String isNodeId, String key, String value) {
		super();
		this.isForwarded = isForwarded;
		this.isNewClient = isNewClient;
		this.isReply = isReply;
		this.nodeId = isNodeId;
		this.key = key;
		this.value = value;
	}
	
	/**
	 * @return the isReply
	 */
	public boolean isReply() {
		return isReply;
	}
	/**
	 * @param isReply the isReply to set
	 */
	public void setReply(boolean isReply) {
		this.isReply = isReply;
	}
	/**
	 * @return the isForwarded
	 */
	public boolean isForwarded() {
		return isForwarded;
	}
	/**
	 * @param isForwarded the isForwarded to set
	 */
	public void setForwarded(boolean isForwarded) {
		this.isForwarded = isForwarded;
	}
	/**
	 * @return the isNewClient
	 */
	public boolean isNewClient() {
		return isNewClient;
	}
	/**
	 * @param isNewClient the isNewClient to set
	 */
	public void setNewClient(boolean isNewClient) {
		this.isNewClient = isNewClient;
	}
	/**
	 * @return the nodeId
	 */
	public String getNodeId() {
		return nodeId;
	}
	/**
	 * @param nodeId the isNodeId to set
	 */
	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}
	/**
	 * @return the key
	 */
	public String getKey() {
		return key;
	}
	/**
	 * @param key the key to set
	 */
	public void setKey(String key) {
		this.key = key;
	}
	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}
	/**
	 * @param value the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}
	
}
