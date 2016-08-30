/**
 * 
 */
package tianchi.ga.controller.route;

import tianchi.ga.controller.node.Node;
import tianchi.ga.controller.node.Point;

/**
* <p>Title: Takeout.java</p>
* <p>Description: </p>
* <p>Copyright: Copyright (c) 2007</p>
* <p>Company: Zhongwei</p>
* @author Zhongwei
* @date 2016年8月15日
* @version 1.0
*/
public class Takeout {
	private String takeoutId;
	private int pickupTime;
	private int deliveryTime;
	private int weight;
	
	private Point shop;
	private Point spot;
	
	public Takeout(String takeoutId, Node shop, Node spot, int pickupTime, int deliveryTime, int weight) {
		this.takeoutId = takeoutId;
		this.pickupTime = pickupTime;
		this.deliveryTime = deliveryTime;
		this.weight = weight;
		
		this.shop = new Point(shop, takeoutId, this.pickupTime, weight);
		this.spot = new Point(spot, takeoutId, this.deliveryTime, weight);
	}
	
	public String getId() {
		return this.takeoutId;
	}

	public Point getSpot() {
		return this.spot;
	}

	public Point getShop() {
		return this.shop;
	}
	
	public int getPickupTime() {
		return this.pickupTime;
	}
	
	public int getDeliveryTime() {
		return this.deliveryTime;
	}
	
	public int getWeight() {
		return this.weight;
	}
}
