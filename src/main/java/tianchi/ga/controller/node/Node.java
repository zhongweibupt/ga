/**
 * 
 */
package tianchi.ga.controller.node;

/**
* <p>Title: Node.java</p>
* <p>Description: </p>
* <p>Copyright: Copyright (c) 2007</p>
* <p>Company: Zhongwei</p>
* @author Zhongwei
* @date 2016年8月12日
* @version 1.0
*/
public class Node {
	private String id;
	private String type;
	private double lng;		
	private double lat;		
	protected int weight;
	
	public Node(String id, String type, double lng, double lat, int weight) {
		this.id = id;
		this.type = type;
		this.lng = lng;
		this.lat = lat;
		this.weight = weight;
	}
	
	public String getId() {
		return this.id;
	}
	
	public String getType() {
		return this.type;
	}
	
	public double getLng() {
		return this.lng;
	}
	
	public double getLat() {
		return this.lat;
	}
	
	public int getWeight() {
		return this.weight;
	}
	
	public void updateWeight(int weight) {
		this.weight = weight;
	}
}
