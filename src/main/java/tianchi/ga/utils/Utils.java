/**
 * 
 */
package tianchi.ga.utils;

import java.io.FileNotFoundException;
import java.util.List;

import tianchi.ga.controller.node.Node;
import tianchi.ga.controller.node.Shop;
import tianchi.ga.controller.node.Site;

/**
* <p>Title: Utils.java</p>
* <p>Description: </p>
* <p>Copyright: Copyright (c) 2007</p>
* <p>Company: Zhongwei</p>
* @author Zhongwei
* @date 2016年8月15日
* @version 1.0
*/
public class Utils {
	public static final int START_TIME = 8*60;
	public static final int MAX_DIST = 12*60;
	public static final int MAX_WEIGHT = 140;
	public static final int MAX_VALUE = 1000000;
	public static final int INTERVALS = 10;
	
	private final static int R = 6378137;
	private final static double speed = 15000/60;
	
	private static DataMapper map = new DataMapper();
	
	public Utils() {
		try {
			map = new DataMapper();
			map.updateSites("data/new_1.csv");
			map.updateSpots("data/new_2.csv");
			map.updateShops("data/new_3.csv");
			map.updateOrders("data/new_4.csv");
			map.updateTakeouts("data/new_5.csv");
			map.updateMen("data/new_6.csv");
			map.updateNodeToSite();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public static DataMapper getMap() {
		return map;
	}
	
	public static String getOrderId(Node spot) {
		return map.getMapOfSpotToOrder().get(spot);
	}
	
	public static Site getNodeToSite(String nodeId) {
		//TODO
		return map.getMapOfNodeToSite().get(nodeId);
	}
	
	public static Site getSpotToSite(String spotId) {
		return map.getMapOfSpotToSite().get(spotId);
	}
	
	public static Site getSite(String siteId) {
		return map.getSites().get(siteId);
	}

	public static Node getSpot(String spotId) {
		return map.getSpots().get(spotId);
	}

	public static Shop getShop(String shopId) {
		return map.getShops().get(shopId);
	}
	
	public static Node getNode(String nodeId) {
		if(nodeId.charAt(0) == 'A') {
			return getSite(nodeId);
		} else if(nodeId.charAt(0) == 'B') {
			return getSpot(nodeId);
		} else if(nodeId.charAt(0) == 'S') {
			return getShop(nodeId);
		}
		return null;
	}
	
	public static List<String> getMen() {
		return map.getMenList();
	}
	
	public static int timeToMinute(String time) {
		int hour = Integer.valueOf(time.substring(0, 2)).intValue();
		int minute = Integer.valueOf(time.substring(3, 5)).intValue();

		return hour*60 + minute - START_TIME;
	}

	public static String minuteToTime(int time) {
		int timeReal = time + START_TIME;
		int hour = timeReal/60;
		int minute = timeReal%60;

		String result = new String();
		result = hour + ":" + minute;
		return  result;
	}
	
	public static int computeDist(Node src, Node dst) {
		double dist = 0;

		double deltaLat = (src.getLat() - dst.getLat())/2;
		double deltaLng = (src.getLng() - dst.getLng())/2;
		
		double sum = Math.pow(Math.sin(Math.PI*deltaLat/180), 2) +
					Math.pow(Math.sin(Math.PI*deltaLng/180), 2) * Math.cos(Math.PI*src.getLat()/180) * Math.cos(Math.PI*dst.getLat()/180);
		double S = 2 * R * Math.asin(Math.sqrt(sum));
		
		dist = Math.round(S / speed);
		
		return (int)dist;
	}
	
	public static int computeDist(List<Double> src, List<Double> dst) {
		double dist = 0;

		double deltaLat = (src.get(0) - dst.get(0))/2;
		double deltaLng = (src.get(1) - dst.get(1))/2;
		
		double sum = Math.pow(Math.sin(Math.PI*deltaLat/180), 2) +
					Math.pow(Math.sin(Math.PI*deltaLng/180), 2) * Math.cos(Math.PI*src.get(0)/180) * Math.cos(Math.PI*dst.get(0)/180);
		double S = 2 * R * Math.asin(Math.sqrt(sum));
		
		dist = Math.round(S / speed);
		return (int)dist;
	}
	
	public static void updateTaskRecords(String str) throws FileNotFoundException {
		map.updateTaskRecords(str);
	}
	
	public static void updateTakeoutRecords(String str) throws FileNotFoundException {
		map.updateTakeoutRecords(str);
	}
}
