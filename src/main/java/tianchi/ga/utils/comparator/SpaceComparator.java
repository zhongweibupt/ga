/**
 * 
 */
package tianchi.ga.utils.comparator;

import java.util.Comparator;

import tianchi.ga.controller.Space;
import tianchi.ga.controller.route.Record;

/**
* <p>Title: SpaceComparator.java</p>
* <p>Description: </p>
* <p>Copyright: Copyright (c) 2007</p>
* <p>Company: Zhongwei</p>
* @author Zhongwei
* @date 2016年8月30日
* @version 1.0
*/
public class SpaceComparator
	implements Comparator<Space> {
		public int compare(Space o1, Space o2) {
			if(o1.getSpace() > o2.getSpace()) {
				return 1;
			} else if(o1.getSpace() < o2.getSpace()) {
				return -1;
			} else {
				return 0;
			}
		}
}
