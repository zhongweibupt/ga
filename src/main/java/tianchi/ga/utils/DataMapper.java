/**
 * 
 */
package tianchi.ga.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import tianchi.ga.controller.node.Node;
import tianchi.ga.controller.node.Point;
import tianchi.ga.controller.node.Shop;
import tianchi.ga.controller.node.Site;
import tianchi.ga.controller.route.Record;
import tianchi.ga.controller.route.Takeout;

/**
* <p>Title: DataMapper.java</p>
* <p>Description: </p>
* <p>Copyright: Copyright (c) 2007</p>
* <p>Company: Zhongwei</p>
* @author Zhongwei
* @date 2016年8月15日
* @version 1.0
*/
public class DataMapper {
	public static final String SPOT = "Spot";
	public static final String SITE = "Site";
	public static final String SHOP = "Shop";
	
	private List<Node> nodes;
	private List<Node> spots;
	private List<Site> sites;
	private List<Shop> shops;
	private List<Takeout> takeouts;
	private List<String> menList;
	private List<Record> taskRecords;
	private List<Record> takeoutRecords;
	
	private Map<String, Site> siteMap;
	private Map<String, Node> spotMap;
	private Map<String, Shop> shopMap;
	
	private Map<String, Takeout> takeoutMap;

	private Map<Node, String> mapOfSpotToOrder;//<spotId, orderId>
	private Map<String, Site> mapOfSpotToSite;
	private Map<String, Site> mapOfNodeToSite;
	
	private Map<Integer, List<Node>> clusterMap;
	
	public DataMapper() {
		this.siteMap = new HashMap<String, Site>();
		this.spotMap = new HashMap<String, Node>();
		this.shopMap = new HashMap<String, Shop>();

		this.takeoutMap = new HashMap<String, Takeout>();

		this.mapOfSpotToOrder = new HashMap<Node, String>();
		this.mapOfSpotToSite = new HashMap<String, Site>();
		this.mapOfNodeToSite = new HashMap<String, Site>();

		this.nodes = new ArrayList<Node>();
		this.spots = new ArrayList<Node>();
		this.sites = new ArrayList<Site>();
		this.shops = new ArrayList<Shop>();
		this.takeouts = new ArrayList<Takeout>();
		this.menList = new ArrayList<String>();
		
		this.taskRecords = new ArrayList<Record>();
		this.takeoutRecords = new ArrayList<Record>();
	}
	
	public void updateMen(String str) throws FileNotFoundException {
		this.menList.clear();
		
		File file = new File(str);
		Scanner scanner;
		scanner = new Scanner(file);
		scanner.useDelimiter("[,\n\r]");
		
		int i = 0;
		while (scanner.hasNext()) {
			if(i == 0) {
				scanner.next();
				i++;
				continue;
			}
			String id = scanner.next();
			this.menList.add(id);
			i++;
	    }
	}
	
	public void updateTaskRecords(String str) throws FileNotFoundException {
		this.taskRecords.clear();
		
		File dir = new File(str); 
		File[] fileList = dir.listFiles();

		for(int j = 0; j < fileList.length; j++) {
			File file = fileList[j];
			String siteId = file.getName().split("-")[0];
			Site site = siteMap.get(siteId);
			
			Scanner scanner;
			scanner = new Scanner(file);
			Pattern pattern = Pattern.compile("\\[(.*)\\]");
			
			List<Record> tasksList = new ArrayList<Record>();
			while(scanner.hasNext()) {
				String line = scanner.next();

				Matcher match = pattern.matcher(line);
				match.find();
				String[] route = match.group(1).split(",");
				//TODO : 需要去掉所有空格
				
				List<String> nodeIdSequence = new ArrayList<String>();
				List<Node> nodesList = new ArrayList<Node>();
				nodesList.add(site);
				
				Record task = new Record();
				List<Point> points = new ArrayList<Point>();
				
				for(int i = 0; i < route.length; i++) {
					nodeIdSequence.add(route[i]);
					
					Node node = Utils.getNode(route[i]);
					String order = Utils.getOrderId(node);
					Point point = new Point(node, order, Utils.MAX_DIST, node.getWeight());
					
					points.add(point);
				}
				
				task.update(site, site, points, 0);
				tasksList.add(task);
			}
			this.taskRecords.addAll(tasksList);
			site.updateRecords(tasksList);
			siteMap.put(site.getId(), site);
		}
	}
	
	public void updateTakeoutRecords(String str) throws FileNotFoundException {
		this.takeoutRecords.clear();
		
		File dir = new File(str); 
		File[] fileList = dir.listFiles();
		
		for(int j = 0; j < fileList.length; j++) {
			File file = fileList[j];
			//String siteId = file.getName().split("-")[0];
			//Site site = siteMap.get(siteId);
			
			Scanner scanner;
			scanner = new Scanner(file);
			Pattern pattern = Pattern.compile("\\[(.*?)\\]");
			
			List<Record> recordsList = new ArrayList<Record>();
			int sum = 0;
			
			while(scanner.hasNext()) {
				String line = scanner.next();
				
				Matcher match = pattern.matcher(line);
				match.find();
				String[] route = match.group(1).split(",");
				match.find();
				String[] orders = match.group(1).split(",");
				//TODO : 需要去掉所有空格
				
				List<String> nodeIdSequence = new ArrayList<String>();
				List<String> orderIdSequence = new ArrayList<String>();
				//nodesList.add(site);
				
				Record record = new Record();
				
				for(int i = 0; i < route.length; i++) {
					nodeIdSequence.add(route[i]);
					orderIdSequence.add(orders[i]);
				}
				
				record.update(nodeIdSequence, orderIdSequence);
				recordsList.add(record);
				
				//TODO
				if(file.getName().equals("-1-76141-434.csv")) {
					if(record.getDist() == record.getRealDist()) {
						System.out.println(line);
						sum += record.getDist() == record.getRealDist() ? record.getDist() : 0;
						System.out.println("sum:" + sum + ",dist:" + record.getDist());
					}
						
				}
			}
			this.takeoutRecords.addAll(recordsList);
		}
		
		for(Record record : this.takeoutRecords) {
			String start = record.getStart().getId();
			Site site = this.siteMap.get(start);
			List<Record> recordList = site.getTakeoutRecords();
			recordList.add(record);
			site.updateTakeoutRecords(recordList);
			this.siteMap.put(site.getId(), site);
		}
	}
	
	public void updateSpots(String str) throws FileNotFoundException {
		this.spotMap.clear();
		
		File file = new File(str);
		Scanner scanner;
		scanner = new Scanner(file);
		scanner.useDelimiter("[,\n\r]");
		
		int i = 0;
		while (scanner.hasNext()) {
			String id = scanner.next();
			double lng = Double.valueOf(scanner.next()).doubleValue();
			double lat = Double.valueOf(scanner.next()).doubleValue();
			
			Node node = new Node(id, SPOT, lng, lat, 0);
			this.spotMap.put(id, node);
			this.nodes.add(node);
			this.spots.add(node);
			i++;
	    }
	}
	
	public void updateSites(String str) throws FileNotFoundException {
		this.siteMap.clear();
		
		File file = new File(str);
		Scanner scanner;
		scanner = new Scanner(file);
		scanner.useDelimiter("[,\n\r]");
		
		int i = 0;
		while (scanner.hasNext()) {
			String id = scanner.next();
			double lng = Double.valueOf(scanner.next()).doubleValue();
			double lat = Double.valueOf(scanner.next()).doubleValue();
			
			Site site = new Site(id, SITE, lng, lat, 0);
			this.siteMap.put(id, site);
			this.sites.add(site);
			this.nodes.add(site);
			i++;
	    }
	}
	
	public void updateShops(String str) throws FileNotFoundException {
		this.shopMap.clear();
		this.shops.clear();
		
		File file = new File(str);
		Scanner scanner;
		scanner = new Scanner(file);
		scanner.useDelimiter("[,\n\r]");
		
		int i = 0;
		while (scanner.hasNext()) {
			String id = scanner.next();
			double lng = Double.valueOf(scanner.next()).doubleValue();
			double lat = Double.valueOf(scanner.next()).doubleValue();
			
			Shop shop = new Shop(id, SHOP, lng, lat, 0);
			this.shopMap.put(id, shop);
			this.nodes.add(shop);
			this.shops.add(shop);
			i++;
	    }
	}
	
	public void updateOrders(String str) throws FileNotFoundException {
		File file = new File(str);
		Scanner scanner;
		scanner = new Scanner(file);
		scanner.useDelimiter("[,\n\r]");
		
		int i = 0;
		while (scanner.hasNext()) {
			String orderId = scanner.next();
			String spotId = scanner.next();
			this.mapOfSpotToOrder.put(this.spotMap.get(spotId), orderId);

			String siteId = scanner.next();
			int num = Integer.valueOf(scanner.next()).intValue();
			
			//TODO
			//System.out.println(spotId);
			Node node = this.spotMap.get(spotId);
			node.updateWeight(num);
			
			this.spotMap.put(spotId, node);
			
			Site site = this.siteMap.get(siteId);
			List<Node> nodes = site.getNodes();
			nodes.add(node);
			site.updateNodes(nodes);
			site.updateWeight();
			this.siteMap.put(siteId, site);

			this.mapOfSpotToSite.put(node.getId(), site);

			i++;
		}
		
		for(Map.Entry<String, Site> entry : this.siteMap.entrySet()) {
			String id = entry.getKey();
			Site site = entry.getValue();
			site.updateWeight();
			this.siteMap.put(id, site);
		}
	}
	
	public void updateTakeouts(String str) throws FileNotFoundException {
		File file = new File(str);
		Scanner scanner;
		scanner = new Scanner(file);
		scanner.useDelimiter("[,\n\r]");

		int i = 0;
		while (scanner.hasNext()) {
			String takeoutId = scanner.next();
			String spotId = scanner.next();
			String shopId = scanner.next();
			int pickTime = Utils.timeToMinute(scanner.next());
			int deliveryTime = Utils.timeToMinute(scanner.next());
			int num = Integer.valueOf(scanner.next()).intValue();
			
			Takeout takeout = new Takeout(takeoutId, this.shopMap.get(shopId), this.spotMap.get(spotId), pickTime, deliveryTime, num);	
			
			Shop shop = this.shopMap.get(shopId);
			List<Takeout> takeouts = shop.getTakeouts();
			takeouts.add(takeout);
			this.takeouts.add(takeout);
			this.takeoutMap.put(takeout.getId(), takeout);
			shop.updateTakeouts(takeouts);
			this.shopMap.put(shopId, shop);
			
			i++;
		}
	}
	
	public void updateRecords(String str) {
		//TODO
	}
	
	public void updateNodeToSite() {
		this.mapOfNodeToSite.clear();
		for(Node spot : this.spots) {
			this.mapOfNodeToSite.put(spot.getId(), getBestSite(spot));
		}
		
		for(Shop shop : this.shops) {
			this.mapOfNodeToSite.put(shop.getId(), getBestSite(shop));
		}
	}
	
	private Site getBestSite(Node node) {
		String best = new String();
		int minDist = Integer.MAX_VALUE;
		
		for(Site site : this.sites) {
			int tmp = Utils.computeDist(node, site);
			if(Utils.computeDist(node, site) < minDist) {
				minDist = tmp;
				best = site.getId();
			}
		}

		return this.siteMap.get(best);
	}
	
	public List<Record> getTaskRecords() {
		return this.taskRecords;
	}
	
	public List<Record> getTakeoutRecords() {
		return this.takeoutRecords;
	}
	
	public List<Node> getNodesList() {
		return this.nodes;
	}

	public List<Node> getSpotsList() {
		return this.spots;
	}
	
	public List<Site> getSitesList() {
		return this.sites;
	}
	
	public List<Takeout> getTakeoutsList() {
		return this.takeouts;
	}
	
	public List<String> getMenList() {
		return this.menList;
	}

	public Map<String, Site> getSites() {
		return this.siteMap;
	}

	public Map<String, Node> getSpots() {
		return this.spotMap;
	}

	public Map<String, Shop> getShops() {
		return this.shopMap;
	}
	
	public Map<String, Takeout> getTakeouts() {
		return this.takeoutMap;
	}

	public Map<Node, String> getMapOfSpotToOrder() {
		return this.mapOfSpotToOrder;
	}

	public Map<String, Site> getMapOfSpotToSite() {
		return this.mapOfSpotToSite;
	}
	
	public Map<String, Site> getMapOfNodeToSite() {
		return this.mapOfNodeToSite;
	}
}
