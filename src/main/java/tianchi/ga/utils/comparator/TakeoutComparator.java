/**
 * 
 */
package tianchi.ga.utils.comparator;

import java.util.Comparator;

import tianchi.ga.controller.route.Takeout;

/**
* <p>Title: TakeoutComparator.java</p>
* <p>Description: </p>
* <p>Copyright: Copyright (c) 2007</p>
* <p>Company: Zhongwei</p>
* @author Zhongwei
* @date 2016年8月16日
* @version 1.0
*/
public class TakeoutComparator implements Comparator<Takeout> {
	public int compare(Takeout o1, Takeout o2) {
		if(o1.getShop().getId().compareTo(o2.getShop().getId()) > 0) {
			return 1;
		} else if(o1.getShop().getId().compareTo(o2.getShop().getId()) < 0) {
			return -1;
		} else {
			if(o1.getPickupTime() > o2.getPickupTime()) {
				return 1;
			} else if(o1.getPickupTime() < o2.getPickupTime()) {
				return -1;
			} else {
				if(o1.getWeight() < o2.getWeight()) {
					return 1;
				} else if(o1.getWeight() > o2.getWeight()) {
					return -1;
				} else {
					if(o1.getDeliveryTime() > o2.getDeliveryTime()) {
						return 1;
					} else if(o1.getDeliveryTime() == o2.getDeliveryTime()) {
						return 0;
					} else {
						return -1;
					}
				}
			}
		}
	}
}