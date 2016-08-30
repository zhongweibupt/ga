/**
 * 
 */
package tianchi.ga.controller.node;

/**
* <p>Title: Point.java</p>
* <p>Description: </p>
* <p>Copyright: Copyright (c) 2007</p>
* <p>Company: Zhongwei</p>
* @author Zhongwei
* @date 2016年8月12日
* @version 1.0
*/
public class Point extends Node {
	
	private String orderId;
	private int timeLimited;
	private int weight;
	private Action action;
	
	public Point(Node node, String orderId, int time, int weight) {
		super(node.getId(), node.getType(), node.getLng(), node.getLat(), 0);
		this.orderId = orderId;
		this.timeLimited = time;
		this.weight = weight;
	}
	
	public String getOrderId() {
		return this.orderId;
	}
	
	public int getTime() {
		return this.timeLimited;
	}
	
	@Override
	public int getWeight() {
		return this.weight;
	}
	
	public void updateAction() {
		//TODO
	}
	
	@SuppressWarnings("finally")
	public Action getAction() {
		try {
			return this.action;
		} catch (Exception e) {
			throw new Exception("Action of " + this.getId() + " is Null.");
		} finally {
			return new Action(null, null, 0, 0, 0, null);
		}
	}
}
