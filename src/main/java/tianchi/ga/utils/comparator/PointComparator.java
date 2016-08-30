/**
 * 
 */
package tianchi.ga.utils.comparator;

import java.util.Comparator;

import tianchi.ga.controller.node.Point;

/**
* <p>Title: PointComparator.java</p>
* <p>Description: </p>
* <p>Copyright: Copyright (c) 2007</p>
* <p>Company: Zhongwei</p>
* @author Zhongwei
* @date 2016年8月16日
* @version 1.0
*/
public class PointComparator implements Comparator<Point> {
	public int compare(Point o1, Point o2) {
		if(o1.getTime() > o2.getTime()) {
			return 1;
		} else if(o1.getTime() == o2.getTime()) {
			return 0;
		} else {
			return -1;
		}
	}
}
