/**
 * 
 */
package tianchi.ga.controller.route;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import tianchi.ga.controller.node.Node;
import tianchi.ga.controller.node.Point;
import tianchi.ga.controller.node.Site;
import tianchi.ga.utils.DataMapper;
import tianchi.ga.utils.Utils;

/**
* <p>Title: Record.java</p>
* <p>Description: </p>
* <p>Copyright: Copyright (c) 2007</p>
* <p>Company: Zhongwei</p>
* @author Zhongwei
* @date 2016年8月15日
* @version 1.0
*/
public class Record extends Route {
	private List<String> orders;
	
	private int realDist = 0;
	
	public Record() {
		super();
		this.orders = new ArrayList<String>();
		this.realDist = 0;
		this.takeouts = new ArrayList<Takeout>();
	}
	
	public List<String> getOrders() {
		return this.orders;
	}
	
	public void update(Site start, Site end, List<Point> points, int startTime) {
		this.orders.clear();
		
		for(Point point : points) {
			this.orders.add(point.getOrderId());
		}
		
		this.start = start;
		this.end = end;
		
		this.update(points);
		this.startTime = startTime;
		this.realDist = this.dist;
	}
	
	public void update(List<String> route, List<String> orders) {
		this.orders.clear();
		
		//this.start = start;
		//this.end = end;
		this.realDist = 0;
		
		List<Point> points = new ArrayList<Point>();
		List<Point> shops = new ArrayList<Point>();
		List<Point> spots = new ArrayList<Point>();
		List<Takeout> takeouts = new ArrayList<Takeout>();
		
		for(int i = 0; i < route.size(); i++) {
			Node node = Utils.getNode(route.get(i));
			Takeout takeout = Utils.getMap().getTakeouts().get(orders.get(i));
			takeouts.add(takeout);
			
			if(orders.get(i).charAt(0) == 'E') {
				if(node.getType().equals(DataMapper.SHOP)) {
					shops.add(takeout.getShop());
					points.add(takeout.getShop());
				} else {
					spots.add(takeout.getSpot());
					points.add(takeout.getSpot());
				}
			} else {
				Point pointNew = new Point(Utils.getSpot(route.get(i)), 
						orders.get(i), 
						Utils.MAX_DIST,
						Utils.getSpot(route.get(i)).getWeight());
				points.add(pointNew);
			}
		}
		
		takeouts = new ArrayList<Takeout>(new HashSet<Takeout>(takeouts));
		
		//this.startTime = startTime;
		
		this.length = points.size();
		this.weight = 0;
		for(Point point : points) {
			if(point.getOrderId().charAt(0) == 'F') {
				this.weight += point.getWeight();
			}
		}
		
		
		this.dist = 0;
		
		this.points = new ArrayList<Point>(points);
		this.route = new ArrayList<String>();
		
		this.takeouts = new LinkedList<Takeout>(takeouts);
		
		this.shops = new ArrayList<Point>(shops);
		this.spots = new ArrayList<Point>(spots);

		this.start = Utils.getNodeToSite(points.get(0).getId());
		this.end = Utils.getNodeToSite(points.get(this.length - 1).getId());
		this.startTime = points.get(0).getTime() - Utils.computeDist(start, points.get(0));
		
		this.arriveTimeList.clear();
		this.leaveTimeList.clear();
		this.spaceList.clear();
		
		for(int i = 0; i < points.size(); i++) {
			this.route.add(points.get(i).getId());
		}
		
		int currSpace = Utils.MAX_WEIGHT - this.weight;
		if(0 > currSpace) {
			this.dist = Utils.MAX_VALUE;//超重
			return;
		}
		int currTime = startTime;
		
		Node pre = (Node)this.start;
		for(int i = 0; i < points.size(); i++) {
			if(!points.get(i).getType().equals(DataMapper.SHOP)) {
				currTime += Utils.computeDist(pre, points.get(i));
				this.arriveTimeList.add(currTime);
				
				if(currTime > points.get(i).getTime()) {
					//System.out.println(currTime + "," + i + ",未及时送达");
					this.realDist += 4*(currTime - points.get(i).getTime());//未及时送达
					//return;
				}
				
				this.weight += points.get(i).getWeight();
				
				currSpace += points.get(i).getWeight();
				this.spaceList.add(currSpace);
				
				currTime += (int)Math.round(3*Math.sqrt(points.get(i).getWeight()) + 5);
				this.leaveTimeList.add(currTime);
			} else {
				currTime += Utils.computeDist(pre, points.get(i));
				this.arriveTimeList.add(currTime);
				
				if(currTime > points.get(i).getTime()) {
					this.dist = Utils.MAX_VALUE;//未及时取货
					return;
				}
				
				currSpace -= points.get(i).getWeight();
				
				if(0 > currSpace) {
					this.dist = Utils.MAX_VALUE;//超重
					return;
				}
				
				this.spaceList.add(currSpace);
				
				this.weight += points.get(i).getWeight();
				
				currTime = points.get(i).getTime();
				this.leaveTimeList.add(currTime);
			}
			pre = points.get(i);
		}
		
		this.dist = this.leaveTimeList.get(this.leaveTimeList.size() - 1) - this.startTime;
		this.dist += Utils.computeDist(pre, this.end);

		if(this.dist == 0) {
			this.dist = Utils.MAX_DIST;
		}
		
		this.realDist += this.dist;
		
		for(String order : orders) {
			if(order.charAt(0) == 'F') {
				this.orders.add(order);
			}
		}
	}
	
	public void updateStartTime(int time) {
		this.startTime = time;
	}
	
	public int getRealDist() {
		return this.realDist;
	}

	/**
	 * @param start
	 * @param end
	 * @param points
	 * @param orders
	 * @param takeouts
	 */
	public void update(Site start, Site end, int startTime, List<String> route, List<String> orders) {
		this.orders.clear();
		
		//this.start = start;
		//this.end = end;
		this.realDist = 0;
		
		List<Point> points = new ArrayList<Point>();
		List<Point> shops = new ArrayList<Point>();
		List<Point> spots = new ArrayList<Point>();
		List<Takeout> takeouts = new ArrayList<Takeout>();
		
		for(int i = 0; i < route.size(); i++) {
			Node node = Utils.getNode(route.get(i));
			
			if(orders.get(i).charAt(0) == 'E') {
				Takeout takeout = Utils.getMap().getTakeouts().get(orders.get(i));
				takeouts.add(takeout);
				
				if(node.getType().equals(DataMapper.SHOP)) {
					shops.add(takeout.getShop());
					points.add(takeout.getShop());
				} else {
					spots.add(takeout.getSpot());
					points.add(takeout.getSpot());
				}
			} else {
				Point pointNew = new Point(Utils.getSpot(route.get(i)), 
						orders.get(i), 
						Utils.MAX_DIST,
						Utils.getSpot(route.get(i)).getWeight());
				points.add(pointNew);
			}
		}
		//System.out.println(points.size());
		takeouts = new ArrayList<Takeout>(new HashSet<Takeout>(takeouts));
		
		//this.startTime = startTime;
		
		this.length = points.size();
		this.weight = 0;
		for(Point point : points) {
			if(point.getOrderId().charAt(0) == 'F') {
				this.weight += point.getWeight();
			}
		}
		
		
		this.dist = 0;
		
		this.points = new ArrayList<Point>(points);
		this.route = new ArrayList<String>();
		
		this.takeouts = new LinkedList<Takeout>(takeouts);
		
		this.shops = new ArrayList<Point>(shops);
		this.spots = new ArrayList<Point>(spots);

		this.start = start;
		this.end = end;
		this.startTime = startTime;
		
		this.arriveTimeList.clear();
		this.leaveTimeList.clear();
		this.spaceList.clear();
		
		for(int i = 0; i < points.size(); i++) {
			this.route.add(points.get(i).getId());
		}
		
		int currSpace = Utils.MAX_WEIGHT - this.weight;
		if(0 > currSpace) {
			this.realDist = Utils.MAX_VALUE;
			//System.out.println("超重");
			//return;
		}
		int currTime = startTime;
		
		Node pre = (Node)this.start;
		for(int i = 0; i < points.size(); i++) {
			if(!points.get(i).getType().equals(DataMapper.SHOP)) {
				currTime += Utils.computeDist(pre, points.get(i));
				this.arriveTimeList.add(currTime);
				
				if(currTime > points.get(i).getTime()) {
					//System.out.println(currTime + "," + i + ",未及时送达");
					//this.realDist += Utils.MAX_VALUE;//未及时送达
					//return;
				}
				
				this.weight += points.get(i).getWeight();
				
				currSpace += points.get(i).getWeight();
				this.spaceList.add(currSpace);
				
				currTime += (int)Math.round(3*Math.sqrt(points.get(i).getWeight()) + 5);
				this.leaveTimeList.add(currTime);
			} else {
				currTime += Utils.computeDist(pre, points.get(i));
				this.arriveTimeList.add(currTime);
				
				if(currTime > points.get(i).getTime()) {
					//System.out.println("未及时取货");
					//this.realDist += Utils.MAX_VALUE;//4*(currTime - points.get(i).getTime());//未及时取货
					//return;
				}
				
				currSpace -= points.get(i).getWeight();
				
				if(0 > currSpace) {
					//System.out.println("超重");
					this.realDist = Utils.MAX_VALUE;
					//this.dist = Utils.MAX_VALUE;//超重
					//return;
				}
				
				this.spaceList.add(currSpace);
				
				this.weight += points.get(i).getWeight();
				
				currTime = currTime > points.get(i).getTime() ? currTime : points.get(i).getTime();
				this.leaveTimeList.add(currTime);
			}
			pre = points.get(i);
		}

		this.dist = this.leaveTimeList.get(this.leaveTimeList.size() - 1) - this.startTime;
		this.dist += Utils.computeDist(pre, this.end);

		if(this.dist == 0) {
			this.dist = Utils.MAX_DIST;
		}
		
		//this.realDist += this.dist;
		
		for(String order : orders) {
			if(order.charAt(0) == 'F') {
				this.orders.add(order);
			}
		}
	}
}
