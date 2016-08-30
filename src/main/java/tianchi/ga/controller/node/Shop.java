/**
 * 
 */
package tianchi.ga.controller.node;

import java.util.ArrayList;
import java.util.List;

import tianchi.ga.controller.route.Takeout;

/**
* <p>Title: Shop.java</p>
* <p>Description: </p>
* <p>Copyright: Copyright (c) 2007</p>
* <p>Company: Zhongwei</p>
* @author Zhongwei
* @date 2016年8月15日
* @version 1.0
*/
public class Shop extends Node {
	private List<Takeout> takeouts;
	
	public Shop(String id, String type, double lng, double lat, int weight) {
		super(id, type, lng, lat, weight);
		this.takeouts = new ArrayList<Takeout>();
	}

	public void updateTakeouts(List<Takeout> takeouts) {
		this.takeouts = takeouts;
	}

	public List<Takeout> getTakeouts() {
		return this.takeouts;
	}
}
