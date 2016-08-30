/**
 * 
 */
package tianchi.ga.controller.route;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import tianchi.ga.controller.node.Node;
import tianchi.ga.controller.node.Point;
import tianchi.ga.controller.node.Site;
import tianchi.ga.utils.DataMapper;
import tianchi.ga.utils.Utils;

/**
* <p>Title: Route.java</p>
* <p>Description: </p>
* <p>Copyright: Copyright (c) 2007</p>
* <p>Company: Zhongwei</p>
* @author Zhongwei
* @date 2016年8月15日
* @version 1.0
*/
public class Route {
	protected Site start;
	protected Site end;
	
	protected int startTime;
	protected int length;
	protected int weight;
	protected int dist;
	
	protected List<Point> points;
	protected List<String> route;
	
	protected List<Integer> arriveTimeList;
	protected List<Integer> leaveTimeList;
	protected List<Integer> spaceList;				//space长度比其他List大1

	protected List<Takeout> takeouts;
	protected List<Point> shops;
	protected List<Point> spots;
	
	public Route() {
		this.start = null;
		this.end = null;
		
		this.dist = Utils.MAX_VALUE;
		this.weight = 0;
		this.length = 0;
		this.startTime = 0;
		
		this.points = new ArrayList<Point>();
		this.route = new ArrayList<String>();
		
		this.arriveTimeList = new ArrayList<Integer>();
		this.leaveTimeList = new ArrayList<Integer>();
		this.spaceList = new ArrayList<Integer>();
	}
	
	public void update(List<Point> points) {
		this.length = points.size();
		this.weight = 0;
		this.dist = 0;
		
		this.points = new ArrayList<Point>(points);
		this.route = new ArrayList<String>();
		
		if(points.get(0).getOrderId().charAt(0) == 'E') {
			this.start = Utils.getNodeToSite(points.get(0).getId());
			this.end = Utils.getNodeToSite(points.get(this.length - 1).getId());
			this.startTime = points.get(0).getTime() - Utils.computeDist(start, points.get(0));
		} else {
			this.start = Utils.getSpotToSite(points.get(0).getId());
			this.end = Utils.getSpotToSite(points.get(this.length - 1).getId());
			this.startTime = 0;
		}
		
		this.arriveTimeList.clear();
		this.leaveTimeList.clear();
		this.spaceList.clear();
		
		int currSpace = Utils.MAX_WEIGHT;
		int currTime = startTime;
		
		Node pre = (Node)this.start;
		for(int i = 0; i < points.size(); i++) {
			if(!points.get(i).getType().equals(DataMapper.SHOP)) {
				currTime += Utils.computeDist(pre, points.get(i));
				this.arriveTimeList.add(currTime);
				
				if(currTime > points.get(i).getTime()) {
					System.out.println(currTime + "," + i + ",未及时送达");
					this.dist = Utils.MAX_VALUE;//未及时送达
					return;
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
		
		for(int i = 0; i < points.size(); i++) {
			this.route.add(points.get(i).getId());
		}
	}
	
	public void update(List<Point> points, List<Takeout> takeouts, List<Point> shops, List<Point> spots) {
		this.length = points.size();
		this.weight = 0;
		this.dist = 0;
		
		this.points = new ArrayList<Point>(points);
		this.route = new ArrayList<String>();
		
		this.takeouts = new LinkedList<Takeout>(takeouts);
		
		this.shops = new ArrayList<Point>(shops);
		this.spots = new ArrayList<Point>(spots);
		
		if(shops.size() != spots.size()) {
			System.out.println("Shop != Spot :" + points.size() + "," + shops.size() + "," + spots.size() + "," + takeouts.size());
			System.exit(-1);
		}
		
		if(points.get(0).getOrderId().charAt(0) == 'E') {
			this.start = Utils.getNodeToSite(points.get(0).getId());
			this.end = Utils.getNodeToSite(points.get(this.length - 1).getId());
			this.startTime = points.get(0).getTime() - Utils.computeDist(start, points.get(0));
		} else {
			this.start = Utils.getSpotToSite(points.get(0).getId());
			this.end = Utils.getSpotToSite(points.get(this.length - 1).getId());
			this.startTime = 0;
		}
		
		this.arriveTimeList.clear();
		this.leaveTimeList.clear();
		this.spaceList.clear();
		
		for(int i = 0; i < points.size(); i++) {
			this.route.add(points.get(i).getId());
		}
		
		int currSpace = Utils.MAX_WEIGHT;
		int currTime = startTime;
		
		Node pre = (Node)this.start;
		for(int i = 0; i < points.size(); i++) {
			if(!points.get(i).getType().equals(DataMapper.SHOP)) {
				currTime += Utils.computeDist(pre, points.get(i));
				this.arriveTimeList.add(currTime);
				
				if(currTime > points.get(i).getTime()) {
					//System.out.println(currTime + "," + i + ",未及时送达");
					//Takeout tmp = Utils.getMap().getTakeouts().get(points.get(i).getOrderId());
					//if(Utils.computeDist(tmp.getShop(), tmp.getSpot()) <= tmp.getDeliveryTime() - tmp.getPickupTime()) {
						this.dist = Utils.MAX_VALUE;//未及时送达
						return;
					//}
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
		
		//TODO
		if(this.points.size() != this.takeouts.size()*2) {
			System.out.println(points.size() + "," + takeouts.size());
			System.exit(-1);
		}
		
	}
	
	public int getDist() {
		return this.dist;
	}
	
	public int getLength() {
		return this.length;
	}
	
	public int getWeight() {
		return this.weight;
	}
	
	public Site getStart() {
		return this.start;
	}
	
	public Site getEnd() {
		return this.end;
	}
	
	public int getStartTime() {
		return this.startTime;
	}
	
	public List<Integer> getSpaceList() {
		return this.spaceList;
	}
	
	public List<Integer> getArriveTimes() {
		return this.arriveTimeList;
	}

	public List<Integer> getLeaveTimes() {
		return this.leaveTimeList;
	}
	
	public List<Point> getPoints() {
		return this.points;
	}
	
	public List<String> getRoute() {
		return this.route;
	}
	
	public List<Takeout> getTakeouts() {
		return this.takeouts;
	}
	
	public List<Point> getShops() {
		return this.shops;
	}
	
	public List<Point> getSpots() {
		return this.spots;
	}
}
