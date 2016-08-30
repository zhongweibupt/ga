/**
 * 
 */
package tianchi.ga.utils.comparator;

import java.util.Comparator;

import tianchi.ga.controller.route.Record;

/**
* <p>Title: TakeoutRecordComparator.java</p>
* <p>Description: </p>
* <p>Copyright: Copyright (c) 2007</p>
* <p>Company: Zhongwei</p>
* @author Zhongwei
* @date 2016年8月23日
* @version 1.0
*/
public class TakeoutRecordComparator implements Comparator<Record> {
	public int compare(Record o1, Record o2) {
		if(o1.getStart().getId().compareTo(o2.getStart().getId()) > 0) {
			return 1;
		} else if(o1.getStart().getId().compareTo(o2.getStart().getId()) < 0) {
			return -1;
		} else {
			if(o1.getStartTime() > o2.getStartTime()) {
				return 1;
			} else if(o1.getStartTime() < o2.getStartTime()) {
				return -1;
			} else {
				if(o1.getDist() < o2.getDist()) {
					return 1;
				} else if(o1.getDist() > o2.getDist()) {
					return -1;
				} else {
					if(o1.getWeight() < o2.getWeight()) {
						return 1;
					} else if(o1.getWeight() == o2.getWeight()) {
						return 0;
					} else {
						return -1;
					}
				}
			}
		}
	}
}
