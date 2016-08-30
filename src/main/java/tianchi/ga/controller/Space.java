/**
 * 
 */
package tianchi.ga.controller;

import java.util.List;

import tianchi.ga.controller.node.Node;
import tianchi.ga.controller.node.Point;
import tianchi.ga.controller.route.Record;
import tianchi.ga.utils.Utils;

/**
* <p>Title: Space.java</p>
* <p>Description: </p>
* <p>Copyright: Copyright (c) 2007</p>
* <p>Company: Zhongwei</p>
* @author Zhongwei
* @date 2016年8月30日
* @version 1.0
*/
public class Space {
	private int frontTime;
	private int rearTime;
	
	private Node front;
	private Node rear;
	private int space;
	
	public Space(int frontTime, int rearTime, Node front, Node rear) {
		this.frontTime = frontTime;
		this.rearTime = rearTime;
		this.front = front;
		this.rear = rear;
		this.space = rearTime - frontTime;
	}
	
	public Node getFront() {
		return this.front;
	}
	
	public Node getRear() {
		return this.rear;
	}
	
	public int getFrontTime() {
		return this.frontTime;
	}
	
	public int getRearTime() {
		return this.rearTime;
	}
	
	public int getSpace() {
		return this.space;
	}
	
	public boolean insert(Record record) {
		if(frontTime + (front != null ? Utils.computeDist(record.getStart(), front) : 0)
				+ record.getDist()
				+ Utils.computeDist(record.getPoints().get(record.getPoints().size() - 1), rear) <= rearTime) {
			frontTime += (front != null ? Utils.computeDist(record.getStart(), front) : 0) + record.getDist();
			front = (Node)record.getPoints().get(record.getPoints().size() - 1);
			
			this.space = rearTime - frontTime;
			return true;
		}
		return false;
	}
}
