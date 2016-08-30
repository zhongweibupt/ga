/**
 * 
 */
package tianchi.ga.controller.node;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import tianchi.ga.controller.Man;
import tianchi.ga.controller.route.Record;
import tianchi.ga.controller.route.Route;

/**
* <p>Title: Site.java</p>
* <p>Description: </p>
* <p>Copyright: Copyright (c) 2007</p>
* <p>Company: Zhongwei</p>
* @author Zhongwei
* @date 2016年8月15日
* @version 1.0
*/
public class Site extends Node {
	private List<Node> nodesList;
	private List<Record> routesList;
	private List<Record> takeoutRoutes;
	
	private List<Man> men;
	private List<Man> takeoutMen;
	
	public Site(String id, String type, double lng, double lat, int weight) {
		super(id, type, lng, lat, weight);
		this.nodesList = new ArrayList<Node>();
		this.routesList = new ArrayList<Record>();
		this.takeoutRoutes = new ArrayList<Record>();
		this.men = new LinkedList<Man>();
		this.takeoutMen = new LinkedList<Man>();
	}

	public void updateNodes(List<Node> nodes) {
		this.nodesList = new ArrayList<Node>(nodes);
	}
	
	public void updateWeight() {
		int weight = 0;
		for(Node node : this.nodesList) {
			weight += node.getWeight();
		}
		this.weight = weight;
	}

	public void updateRecords(List<Record> routes) {
		this.routesList = new ArrayList<Record>(routes);
	}
	
	public void updateTakeoutRecords(List<Record> routes) {
		this.takeoutRoutes = new ArrayList<Record>(routes);
	}
	
	public void updateMen(List<Man> men) {
		this.men = new LinkedList<Man>(men);
	}
	
	public void updateTakeoutMen(List<Man> men) {
		this.takeoutMen = new LinkedList<Man>(men);
	}
	
	public List<Node> getNodes() {
		return this.nodesList;
	}

	public List<Record> getRecords() {
		return this.routesList;
	}
	
	public List<Record> getTakeoutRecords() {
		return this.takeoutRoutes;
	}
	
	public List<Man> getMen() {
		return this.men;
	}
	
	public List<Man> getTakeoutMen() {
		return this.takeoutMen;
	}
}
