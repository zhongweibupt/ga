/**
 * 
 */
package tianchi.ga.controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import tianchi.ga.controller.node.Action;
import tianchi.ga.controller.node.Node;
import tianchi.ga.controller.node.Point;
import tianchi.ga.controller.node.Site;
import tianchi.ga.controller.route.Record;
import tianchi.ga.controller.route.Takeout;
import tianchi.ga.utils.DataMapper;
import tianchi.ga.utils.Utils;
import tianchi.ga.utils.comparator.SpaceComparator;
import tianchi.ga.utils.comparator.TakeoutRecordComparator;
import tianchi.ga.utils.comparator.TaskRecordComparator;

/**
* <p>Title: Controller.java</p>
* <p>Description: </p>
* <p>Copyright: Copyright (c) 2007</p>
* <p>Company: Zhongwei</p>
* @author Zhongwei
* @date 2016年8月15日
* @version 1.0
*/
public class Controller {
	Map<String, Record> taskRecords;
	Map<String, Man> menMap;
	List<Man> men;
	
	List<Record> tasks;
	List<Record> takeouts;
	
	List<Space> spaces;
	
	public Controller() {
		taskRecords = new HashMap<String, Record>();
		menMap = new HashMap<String, Man>();
		men = new ArrayList<Man>();
		
		for(String id : Utils.getMen()) {
			Man man = new Man(id);
			man.update(new ArrayList<Record>());
			menMap.put(id, man);
			men.add(man);
		}
		
		tasks = new LinkedList<Record>();
		takeouts = new LinkedList<Record>();
		
		spaces = new LinkedList<Space>();
	}
	
	public void updateTaskRecords(String str) throws FileNotFoundException {
		Utils.updateTaskRecords(str);
	}
	
	public void updateTakeoutRecords(String str) throws FileNotFoundException {
		Utils.updateTakeoutRecords(str);
		//System.out.println(Utils.getMap().getTakeouts().size());
		
		List<Record> records = new LinkedList<Record>(Utils.getMap().getTakeoutRecords());
		int size = 0;
		for(Record record : records) {
			size += record.getPoints().size();
		}
		//System.out.println(size);
		
	}
	
	public boolean verify() {
		//TODO
		int c = 0;
		for(Site site : Utils.getMap().getSitesList()) {
			for(Record record : site.getRecords()) {
				if(!record.getStart().getId().equals(site.getId())) {
					System.out.println("ERROR 1");
				}
			}
			
			for(Record record : site.getTakeoutRecords()) {
				if(!record.getStart().getId().equals(site.getId())) {
					System.out.println("ERROR 2");
				}
			}
			
			for(Man man : site.getMen()) {
				if(man.getRecords().size() == 0) {
					System.out.println("ERROR 5");
				}
				for (Record record : man.getRecords()) {
					if(!record.getStart().getId().equals(site.getId())) {
						System.out.println("ERROR 3");
					}
				}
			}
			
			
			for(Man man : site.getTakeoutMen()) {
				if(man.getRecords().size() == 0) {
					System.out.println("ERROR 6");
				}
				for (Record record : man.getRecords()) {
					c += record.getTakeouts().size();
					if(!record.getStart().getId().equals(site.getId())) {
						System.out.println("ERROR 4");
					}
				}
			}
			
		}
		
		if(c <= 3417) {
			//System.out.println(c);
		}
		
		c = 0;
		int sum = 0;
		int num = 0;
		
		for(Man man : this.men) {
			for(Record record : man.getRecords()) {
				c += record.getTakeouts().size();
			}
			
			sum += man.getDist();
			num += man.getRecords().size();
		}
		
		System.out.println("Takeout:" + c);
		System.out.println(sum);
		System.out.println(num);
		
		return true;
	}
	
	public void preDeal() throws IOException {
		int sumPre = 0;
		for(Record record : Utils.getMap().getTaskRecords()) {
			sumPre += record.getRealDist();
		}
		
		for(Record record : Utils.getMap().getTakeoutRecords()) {
			sumPre += record.getRealDist();
		}
		
		List<Record> taskRecords = new LinkedList<Record>();
		List<Record> takeoutRecords = new LinkedList<Record>(Utils.getMap().getTakeoutRecords());
		
		List<String> allPart = new ArrayList<String>();
		for(Record takeoutRecord : takeoutRecords) {
			move(takeoutRecord, allPart);
		}
		
		allPart = new ArrayList<String>(new HashSet<String>(allPart));
		System.out.println("ALL:" + allPart.size());
		
		for(Record taskRecord : Utils.getMap().getTaskRecords()) {
			List<Point> points = new LinkedList<Point>();
			for(Point point : taskRecord.getPoints()) {
				if(!allPart.contains(point.getId())) {
					points.add(point);
				}
			}
			
			Record record = new Record();
			if(!points.isEmpty()) {
				record.update(taskRecord.getStart(),
						taskRecord.getEnd(), 
						points, 
						0);
				taskRecords.add(record);
			}
		}
		
		int sum = 0;
		for(Record record : takeoutRecords) {
			sum += record.getRealDist();
		}
		
		for(Record record : taskRecords) {
			sum += record.getRealDist();
		}
		
		System.out.println(sum);
		System.out.println(sumPre);
		
		
		dealTakeouts(takeoutRecords);
		dealTasks(taskRecords);
		
		writeActions();
		
	}
	
	private void move(Record record, List<String> part) {
		String siteId = record.getStart().getId();
		
		List<String> tmp = new ArrayList<String>();
		List<Point> points = new ArrayList<Point>();
		
		List<String> route = new ArrayList<String>();
		List<String> orders = new ArrayList<String>();
		
		for(Point point : record.getPoints()) {
			route.add(point.getId());
			orders.add(point.getOrderId());
			
			if(point.getType().equals(DataMapper.SPOT)) {
				if(Utils.getSpotToSite(point.getId()).getId().equals(siteId)) {
					if(!part.contains(point.getId()) && !tmp.contains(point.getId())) {
						tmp.add(point.getId());

						route.add(point.getId());
						orders.add(Utils.getMap().getMapOfSpotToOrder()
								.get(Utils.getNode(point.getId())));
					}
				}
			}
		}
		
		Record recordNew = new Record();
		recordNew.update(route, orders);
		if(recordNew.getDist() < Utils.MAX_DIST && 
				recordNew.getStartTime() + recordNew.getDist() < Utils.MAX_DIST) {
			record.update(route, orders);
			part.addAll(tmp);
		}
	}
	
	public void dealTakeouts(List<Record> takeoutRecords) {
		List<Record> records = new LinkedList<Record>(takeoutRecords);
		Collections.sort(records, new TakeoutRecordComparator());
		//Collections.shuffle(records);
		
		int index = 0;
		List<Record> menRecords = new LinkedList<Record>();
		int sum = 0;
		while(true) {
			boolean success = false;
			for(Record record : records) {
				if(add(menRecords, record)) {
					sum += record.getRealDist();
					records.remove(record);
					success = true;
					break;
				}
			}
			
			if(!success) {
				if(index >= 1000) {
					System.out.println(index + ": index > 1000");
					//System.exit(-1);
				}
				Man man = men.get(index);
				//Man man = new Man("id");
				man.update(new ArrayList<Record>(menRecords));
				menRecords = new LinkedList<Record>();
				
				if(records.isEmpty()) {
					break;
				}
				
				index++;
			}
		}
		
		System.out.println(index);
		System.out.println(sum);
		
		int c = 0;
		for(Man man : men) {
			c += man.getRecords().size();
		}
		System.out.println(c + "," + Utils.getMap().getTakeoutRecords().size());
	}
	
	public void dealTasks(List<Record> taskRecords) {
		List<Record> records = new LinkedList<Record>(taskRecords);
		Collections.sort(records, new TaskRecordComparator());
		//Collections.shuffle(records);
		
		int index = 0;
		List<Record> menRecords = new LinkedList<Record>();
		menRecords = men.get(index).getRecords();
		
		int sum = 0;
		while(!records.isEmpty()) {
			boolean success = false;
			for(Record record : records) {
				if(insert(menRecords, record)
						//merge(menRecords, record)
						) {
					sum += record.getRealDist();
					records.remove(record);
					success = true;
					break;
				}
			}
			
			if(!success) {
				if(index >= 1000) {
					System.out.println(index + ": index > 1000");
					//System.exit(-1);
				}
				Man man = men.get(index);
				man.update(new ArrayList<Record>(menRecords));
				men.set(index, man);
				
				index++;
				menRecords = men.get(index).getRecords();
				//System.out.println("Size:" + records.size() + "," + index);
			}
		}
		
		System.out.println(index);
		System.out.println(sum);
	}
	
	private boolean isPassed(Record record) {
		int i = 0;
		for(Point point : record.getPoints()) {
			//System.out.println("=========================Start" + record.getStartTime() + "," +record.getLeaveTimes().get(i));
			if(record.getLeaveTimes().get(i) > Utils.MAX_DIST 
					&& point.getOrderId().charAt(0) == 'F') {
				//System.out.println("=========================" + record.getLeaveTimes().get(i));
				return false;
			}
			
			if(record.getSpaceList().get(i) < 0) {
				return false;
			}
			
			if(record.getRealDist() >= Utils.MAX_VALUE) {
				return false;
			}
			i++;
		}
		
		return true;
	}
	
	private boolean merge(List<Record> records, Record record) {
		if(records.isEmpty()) {
			record.updateStartTime(0);
			records.add(0, record);
			return true;
		}
		
		if(!records.get(0).getTakeouts().isEmpty() && records.get(0).getOrders().isEmpty()) {
			if(record.getDist() <= records.get(0).getStartTime() - 
			//record.getEnd().getId().equals(records.get(0).getStart().getId())
				Utils.computeDist(record.getEnd(), records.get(0).getStart())) {
				record.updateStartTime(0);
				records.add(0, record);
				return true;
			} else {
				//TODO
				int k = 0;
				
				Record recordNew = new Record();
				recordNew.update(record.getStart(), record.getEnd(), record.getPoints(), 0);
				
				while(recordNew.getStartTime() + recordNew.getDist() > records.get(k).getStartTime()
						// - record.getEnd().getId().equals(records.get(0).getStart().getId())
						// - Utils.computeDist(recordNew.getEnd(), records.get(k).getStart())
						+ Utils.computeDist(record.getEnd(), record.getPoints().get(record.getPoints().size() - 1))
						- Utils.computeDist(record.getPoints().get(record.getPoints().size() - 1), records.get(k).getPoints().get(0))
						) {
					recordNew = merge(recordNew, records.get(k));
					k++;
					if(k >= records.size()) {
						break;
					}
				}
				
				if(isPassed(recordNew)) {					
					records.removeAll(new LinkedList<Record>(records.subList(0, k)));
					records.add(0, recordNew);
					return true;
				} else {
					return false;
				}
			}
		}
		
		for(int i = 0; i < records.size() - 1; i++) {
			Record pre = records.get(i);
			Record next = records.get(i + 1);
			
			if(!next.getTakeouts().isEmpty()
				&& next.getOrders().isEmpty()) {
				
				int time = pre.getStartTime() + pre.getDist() + 
							Utils.computeDist(pre.getPoints().get(pre.getPoints().size() - 1), record.getStart()) +
							Utils.computeDist(record.getPoints().get(record.getPoints().size() - 1), next.getPoints().get(0)) - 
							Utils.computeDist(pre.getEnd(), pre.getPoints().get(pre.getPoints().size() - 1)) -
							Utils.computeDist(record.getEnd(), record.getPoints().get(record.getPoints().size() - 1));
					
				if(next.getOrders().isEmpty() && 
						record.getDist() <= next.getStartTime() - time
						){
					record.updateStartTime(pre.getStartTime() + pre.getDist() - Utils.computeDist(pre.getEnd(), pre.getPoints().get(pre.getPoints().size() - 1)) 
							+ Utils.computeDist(pre.getPoints().get(pre.getPoints().size() - 1), record.getStart()));
					records.add(i + 1, record);
					return true;
				} else {
					
					int k = i + 1;
					
					Record recordNew = new Record();
					recordNew.update(record.getStart(), record.getEnd(), record.getPoints(), pre.getStartTime() + pre.getDist() - Utils.computeDist(pre.getEnd(), pre.getPoints().get(pre.getPoints().size() - 1)) 
							+ Utils.computeDist(pre.getPoints().get(pre.getPoints().size() - 1), record.getStart()));
					
					while(recordNew.getStartTime() + recordNew.getDist() > records.get(k).getStartTime()
							// - record.getEnd().getId().equals(records.get(0).getStart().getId())
							// - Utils.computeDist(recordNew.getEnd(), records.get(k).getStart())
							+ Utils.computeDist(record.getEnd(), record.getPoints().get(record.getPoints().size() - 1))
							- Utils.computeDist(record.getPoints().get(record.getPoints().size() - 1), records.get(k).getPoints().get(0))
							) {
						
						recordNew = merge(recordNew, records.get(k));
						k++;
						if(k >= records.size()) {
							break;
						}
					}
					
					if(isPassed(recordNew)) {					
						records.removeAll(new LinkedList<Record>(records.subList(i + 1, k)));
						records.add(i + 1, recordNew);
						return true;
					} else {
						return false;
					}
					
				}
			}	
		}
		
		int time = (records.get(records.size() - 1).getStartTime() + records.get(records.size() - 1).getDist())
				//record.getEnd().getId().equals(records.get(0).getStart().getId())
				- Utils.computeDist(records.get(records.size() - 1).getEnd(), records.get(records.size() - 1).getPoints().get(records.get(records.size() - 1).getPoints().size() - 1))
				+ Utils.computeDist(record.getStart(), records.get(records.size() - 1).getPoints().get(records.get(records.size() - 1).getPoints().size() - 1));
		if(time + record.getDist() <= Utils.MAX_DIST) {
				if(records.get(records.size() - 1).getTakeouts().size() == 0) {
					record.updateStartTime(time);
					records.add(records.size(), record);
					return true;
				}
			}
		
		return false;
	}
	
	private boolean insert(List<Record> records, Record record) {
		if(records.isEmpty()) {
			record.updateStartTime(0);
			records.add(0, record);
			return true;
		}
		
		//TODO有问题
		if(!records.get(0).getTakeouts().isEmpty()) {
			if(record.getDist() <= records.get(0).getStartTime()
			// - record.getEnd().getId().equals(records.get(0).getStart().getId())
				 - Utils.computeDist(record.getEnd(), records.get(0).getStart())
				//+ Utils.computeDist(record.getEnd(), record.getPoints().get(record.getPoints().size() - 1))
				//- Utils.computeDist(record.getPoints().get(record.getPoints().size() - 1), records.get(0).getPoints().get(0))
					) {
				record.updateStartTime(0);
				records.add(0, record);
				return true;
			} else {
				//TODO
				
			}
		}
		
		for(int i = 0; i < records.size() - 1; i++) {
			Record pre = records.get(i);
			Record next = records.get(i + 1);
			
			if(!next.getTakeouts().isEmpty()) {
				if(next.getOrders().isEmpty()) {
					int time = pre.getStartTime() + pre.getDist() + 
							Utils.computeDist(pre.getPoints().get(pre.getPoints().size() - 1), record.getStart()) +
							Utils.computeDist(record.getPoints().get(record.getPoints().size() - 1), next.getPoints().get(0)) - 
							Utils.computeDist(pre.getEnd(), pre.getPoints().get(pre.getPoints().size() - 1)) -
							Utils.computeDist(record.getEnd(), record.getPoints().get(record.getPoints().size() - 1));
					
					if(next.getOrders().isEmpty() && 
							record.getDist() <= next.getStartTime() - time
							){
						record.updateStartTime(pre.getStartTime() + pre.getDist() - Utils.computeDist(pre.getEnd(), pre.getPoints().get(pre.getPoints().size() - 1)) 
								+ Utils.computeDist(pre.getPoints().get(pre.getPoints().size() - 1), record.getStart()));
						records.add(i + 1, record);
						return true;
					}
				} else {
					int time = pre.getStartTime() + pre.getDist() + 
							Utils.computeDist(pre.getPoints().get(pre.getPoints().size() - 1), record.getStart()) +
							Utils.computeDist(record.getPoints().get(record.getPoints().size() - 1), next.getStart()) - 
							Utils.computeDist(pre.getEnd(), pre.getPoints().get(pre.getPoints().size() - 1)) -
							Utils.computeDist(record.getEnd(), record.getPoints().get(record.getPoints().size() - 1));
					
					if(record.getDist() <= next.getStartTime() - time) {
						record.updateStartTime(pre.getStartTime() + pre.getDist() - Utils.computeDist(pre.getEnd(), pre.getPoints().get(pre.getPoints().size() - 1)) 
								+ Utils.computeDist(pre.getPoints().get(pre.getPoints().size() - 1), record.getStart()));
						
						records.add(i + 1, record);
						return true;
					}
				}
			}
			
					
		}
		
		int time = (records.get(records.size() - 1).getStartTime() + records.get(records.size() - 1).getDist())
				//record.getEnd().getId().equals(records.get(0).getStart().getId())
				- Utils.computeDist(records.get(records.size() - 1).getEnd(), records.get(records.size() - 1).getPoints().get(records.get(records.size() - 1).getPoints().size() - 1))
				+ Utils.computeDist(record.getStart(), records.get(records.size() - 1).getPoints().get(records.get(records.size() - 1).getPoints().size() - 1));
		if(time + record.getDist() <= Utils.MAX_DIST) {
				if(records.get(records.size() - 1).getTakeouts().size() == 0) {
					record.updateStartTime(time);
					records.add(records.size(), record);
					return true;
				}
			}
		
		return false;
	}
	
	private boolean add(List<Record> records, Record record) {
		if(records.isEmpty()) {
			records.add(record);
			return true;
		}
		
		if(!records.get(records.size() - 1).getEnd().getId().equals(
				record.getStart().getId())) {
			//return false;
		}
		
		if(!record.getTakeouts().isEmpty()) {
			if(record.getOrders().isEmpty()) {
				if(record.getPoints().get(0).getTime() - records.get(records.size() - 1).getLeaveTimes().get(records.get(records.size() - 1).getLeaveTimes().size() - 1) >=
						Utils.computeDist(records.get(records.size() - 1).getPoints().get(records.get(records.size() - 1).getPoints().size() - 1), record.getPoints().get(0))
						//>= records.get(records.size() - 1).getStartTime() +
						//records.get(records.size() - 1).getDist()
						// + Utils.computeDist(record.getPoints().get(record.getPoints().size() - 1), records.get(records.size() - 1).getPoints().get(0))
						//+ Utils.computeDist(record.getStart(), records.get(records.size() - 1).getEnd()) 
						//&& record.getStartTime() + record.getDist() <= Utils.MAX_DIST
						) {
					
					records.add(record);
					return true;
				}
			} else {
				if(record.getStartTime() - records.get(records.size() - 1).getLeaveTimes().get(records.get(records.size() - 1).getLeaveTimes().size() - 1) >=
						Utils.computeDist(records.get(records.size() - 1).getPoints().get(records.get(records.size() - 1).getPoints().size() - 1), record.getStart())
						//>= records.get(records.size() - 1).getStartTime() +
						//records.get(records.size() - 1).getDist()
						// + Utils.computeDist(record.getPoints().get(record.getPoints().size() - 1), records.get(records.size() - 1).getPoints().get(0))
						//+ Utils.computeDist(record.getStart(), records.get(records.size() - 1).getEnd()) 
						&& record.getStartTime() + record.getDist() <= Utils.MAX_DIST) {
					records.add(record);
					return true;
				}
			}
		} else {
			//TODO
		}
		
		
		return false;
	}
	
	public List<Man> getMen() {
		return this.men;
	}
	
	public void writeActions() throws IOException {
		String file = "data/actions/" + new Date().getTime() + ".csv";
		FileWriter fw = new FileWriter(file);
		StringBuffer info = new StringBuffer();
		
		int sum = 0;
		
		for(Man man : this.men) {
			if(man.getRecords().size() == 0) {
				continue;
			}
			Point pointLast = man.getRecords().get(0).getPoints().get(man.getRecords().get(0).getPoints().size() - 1);
			int timeLast = 0;
			for(Record record : man.getRecords()) {
				String Courier_id = man.getId();
				if(!record.getOrders().isEmpty()) {
					String Addr = record.getStart().getId();
					int Arrival_time = record.getTakeouts().isEmpty() ? record.getStartTime()
							: timeLast + Utils.computeDist(pointLast, record.getStart());
					if(timeLast == 0) {
						Arrival_time = 0;
					}
					int Departure = Arrival_time;
					
					for(Point spot : record.getPoints()) {
						if(spot.getOrderId().charAt(0) == 'F' && spot.getType().equals(DataMapper.SPOT)) {
							int Amount = spot.getWeight();
							String Order_id = spot.getOrderId();
							
							Action action = new Action(
									Courier_id,
									Addr,
									Arrival_time,
									Departure,
									Amount,
									Order_id);
							info.append(action.getAction() + "\r\n");
						}
					}
				}
				
				int i = 0;
				for(Point spot : record.getPoints()) {
					String Addr = spot.getId();
					//TODO : 有问题
					int Arrival_time = 0;
					if(i == 0) {
						Arrival_time = !record.getTakeouts().isEmpty() ? 
								(record.getOrders().isEmpty() ? timeLast + Utils.computeDist(pointLast, spot) : record.getArriveTimes().get(i)) 
								: record.getStartTime() + record.getArriveTimes().get(i);
						if(!record.getOrders().isEmpty() && !record.getTakeouts().isEmpty()) { 
							Arrival_time = record.getStartTime() + Utils.computeDist(record.getStart(), record.getPoints().get(0));//record.getArriveTimes().get(i);
									//timeLast + Utils.computeDist(pointLast, record.getStart()) +
									//Utils.computeDist(record.getPoints().get(0), record.getStart());
						}
						if(timeLast == 0 && record.getOrders().isEmpty()) {
							Arrival_time = record.getStartTime();
						}
					} else {
						Arrival_time = record.getArriveTimes().get(i) + (!record.getTakeouts().isEmpty() ? 0 : record.getStartTime()); 
					}
					int Departure = record.getLeaveTimes().get(i) + (!record.getTakeouts().isEmpty() ? 0 : record.getStartTime());
					int Amount = spot.getType().equals(DataMapper.SHOP) ? spot.getWeight() : 0 - spot.getWeight();
					String Order_id = spot.getOrderId();
					
					Action action = new Action(
							Courier_id,
							Addr,
							Arrival_time,
							Departure,
							Amount,
							Order_id);
					info.append(action.getAction() + "\r\n");
					
					i++;
				}
				pointLast = record.getPoints().get(record.getPoints().size() - 1);
				timeLast = record.getLeaveTimes().get(record.getLeaveTimes().size() - 1)
						+ (!record.getTakeouts().isEmpty() ? 0 : record.getStartTime());
				
			}
			
			
		}
		
		fw = new FileWriter(file);
		fw.write(info.toString());
		fw.flush();
		fw.close();
	}
	
	public void dealTasksOfSites() {
		int maxDist = Utils.MAX_DIST;
		
		int index = 0;
		
		int i = 0;
		int sum = 0;
		for(Site site : Utils.getMap().getSitesList()) {
			List<Record> tasks = new ArrayList<Record>(site.getRecords());
			//Collections.sort(tasks, new TaskRecordComparator());
			Collections.shuffle(tasks);
			
			List<Man> men = new LinkedList<Man>();
			List<Record> menRecords = new LinkedList<Record>();
			
			int distOfMan = 0;
			while(true) {
				boolean success = false;
				for(Record record : tasks) {
					if(distOfMan + record.getDist() <= maxDist) {
						tasks.remove(record);
						
						menRecords.add(record);
						distOfMan += record.getDist();
						
						success = true;
						break;
					}
				}
				
				if(!success) {
					if(index >= 1000) {
						System.out.println(index + ": index > 1000");
						System.exit(-1);
					}
					//Man man = this.men.get(index);
					if(!menRecords.isEmpty()) {
						Man man = new Man("id");
						man.update(new ArrayList<Record>(menRecords));
						sum += man.getDist();
						
						men.add(man);
					}
					
					menRecords = new LinkedList<Record>();
					distOfMan = 0;
					
					if(tasks.isEmpty()) {
						break;
					}
					
					index++;
				}
			}
			
			site.updateMen(men);
			//System.out.println(site.getId() + ":" + men.size());
		}
		System.out.println(sum);
		System.out.println(index);
	}
	
	public void dealTakeoutsOfSites() {
		int maxDist = Utils.MAX_DIST;
		
		int index = 0;
		
		int sum = 0;
		int i = 0;
		for(Site site : Utils.getMap().getSitesList()) {
			List<Record> records = new ArrayList<Record>(site.getTakeoutRecords());
			//Collections.sort(records, new TakeoutRecordComparator());
			Collections.shuffle(records);
			
			List<Man> men = new LinkedList<Man>();
			List<Record> menRecords = new LinkedList<Record>();
			
			int distOfMan = 0;
			while(true) {
				boolean success = false;
				for(Record record : records) {
					if(add(menRecords,record)) {
						records.remove(record);
						distOfMan += record.getDist();
						
						success = true;
						break;
					}
				}
				
				if(!success) {
					if(index >= 1000) {
						System.out.println(index + ": index > 1000");
						System.exit(-1);
					}
					//Man man = this.men.get(index);
					if(!menRecords.isEmpty()) {
						Man man = new Man("id");
						man.update(new ArrayList<Record>(menRecords));
						
						sum += man.getDist();
						
						men.add(man);
					}
					
					menRecords = new LinkedList<Record>();
					distOfMan = 0;
					
					if(records.isEmpty()) {
						break;
					}
					
					index++;
				}
			}
			
			site.updateTakeoutMen(men);
			//System.out.println(site.getId() + ":" + sum);
		}
		
		System.out.println(sum);
		System.out.println(index);
	}
	
	public void deal(List<Record> takeoutRecords, List<Record> taskRecords) {
		List<Man> taskManOther = new LinkedList<Man>();
		List<Man> takeoutManOther = new LinkedList<Man>();
		
		List<Record> records = new LinkedList<Record>(takeoutRecords);
		Collections.sort(records, new TakeoutRecordComparator());
		
		int index = 0;
		List<Record> menRecords = new LinkedList<Record>();
		int sum = 0;
		while(true) {
			boolean success = false;
			for(Record record : records) {
				if(add(menRecords, record)) {
					sum += record.getRealDist();
					records.remove(record);
					success = true;
					break;
				}
			}
			
			if(!success) {
				if(index >= 1000) {
					System.out.println(index + ": index > 1000");
					//System.exit(-1);
				}
				//Man man = men.get(index);
				Man man = new Man("id");
				man.update(new ArrayList<Record>(menRecords));
				
				takeoutManOther.add(man);
				
				menRecords = new LinkedList<Record>();
				
				if(records.isEmpty()) {
					break;
				}
				
				index++;
			}
		}
		
		System.out.println(index);
		System.out.println(sum);
		
		int c = 0;
		for(Man man : takeoutManOther) {
			c += man.getRecords().size();
		}
		
		List<Record> tasks = new LinkedList<Record>(taskRecords);
		//Collections.sort(tasks, new TaskRecordComparator());
		Collections.shuffle(tasks);
		
		menRecords = new LinkedList<Record>();
		
		int distOfMan = 0;
		index = 0;
		while(true) {
			boolean success = false;
			for(Record record : tasks) {
				if(distOfMan + record.getDist() <= Utils.MAX_DIST) {
					tasks.remove(record);
					
					menRecords.add(record);
					distOfMan += record.getDist();
					
					success = true;
					break;
				}
			}
			
			if(!success) {
				if(index >= 1000) {
					System.out.println(index + ": index > 1000");
					System.exit(-1);
				}
				//Man man = this.men.get(index);
				if(!menRecords.isEmpty()) {
					Man man = new Man("id");
					man.update(new ArrayList<Record>(menRecords));
					sum += man.getDist();
					
					taskManOther.add(man);
				}
				
				menRecords = new LinkedList<Record>();
				distOfMan = 0;
				
				if(tasks.isEmpty()) {
					break;
				}
				
				index++;
			}
		}
		
		System.out.println("??" + taskManOther.size() + "," + takeoutManOther.size());
		
		List<Man> men = new LinkedList<Man>();
		
		while(!taskManOther.isEmpty() && !takeoutManOther.isEmpty()) {
			men.addAll(merge(taskManOther, takeoutManOther));
			System.out.println("??" + men.size() + "," + taskManOther.size() + "," + takeoutManOther.size());
		}
		
		if(!takeoutManOther.isEmpty()) {
			System.out.println("??" + takeoutManOther.size());
			System.exit(-1);
		}
		
		if(!taskManOther.isEmpty()) {
			for(Man man : taskManOther) {
				int time = 0;
				for(Record r : man.getRecords()) {
					r.updateStartTime(time);
					time += r.getDist();
				}
				man.update(man.getRecords());
			}
		}
		
		men.addAll(taskManOther.isEmpty() ? takeoutManOther : taskManOther);
		
		int dist = 0;
		int i = 0;
		for(Man man : men) {
			
			dist += man.getDist();
			
			Man manNew = this.men.get(i);
			manNew.update(man.getRecords());
			System.out.println(i++ + "," + man.getDist());
		}
		System.out.println(dist);
	}
	
	
	public void dealMan() {
		List<Man> taskManOther = new LinkedList<Man>();
		List<Man> takeoutManOther = new LinkedList<Man>();
		
		List<Man> men = new LinkedList<Man>();
		int c = 0;
		for(Site site : Utils.getMap().getSitesList()) {
			List<Man> a = new LinkedList<Man>(site.getMen());
			List<Man> b = new LinkedList<Man>(site.getTakeoutMen());
			
			c += b.size();
			System.out.println("=================" + site.getId());
			men.addAll(merge(a, b));
			System.out.println("ab" + men.size() + "," + a.size() + "," + c + "," + takeoutManOther.size());
			
			taskManOther.addAll(a);
			takeoutManOther.addAll(b);
		}
		
		
		System.out.println("==========================");
		while(!taskManOther.isEmpty() && !takeoutManOther.isEmpty()) {
			men.addAll(merge(taskManOther, takeoutManOther));
			System.out.println("??" + men.size() + "," + taskManOther.size() + "," + takeoutManOther.size());
		}
		
		if(!takeoutManOther.isEmpty()) {
			System.out.println("??" + takeoutManOther.size());
			System.exit(-1);
		}
		
		if(!taskManOther.isEmpty()) {
			for(Man man : taskManOther) {
				int time = 0;
				for(Record r : man.getRecords()) {
					r.updateStartTime(time);
					time += r.getDist();
				}
				man.update(man.getRecords());
			}
		}
		
		men.addAll(taskManOther.isEmpty() ? takeoutManOther : taskManOther);
		
		//TODO
		//System.out.println(men.size());
		
		int index = 0;
		for(Man man : men) {
			
			//System.out.println(this.men.get(index).getId() + ":" + man.getDist() + "," + man.getRecords().size() + "," + man.getRecords().get(man.getRecords().size() - 1).getStartTime());
			
//			if(man.getDist() != 0) {
				Man manNew = this.men.get(index);
				manNew.update(new ArrayList<Record>(man.getRecords()));
				index++;
//			}
		}
	}
	
	public List<Man> merge(List<Man> a, List<Man> b) {
		List<Man> men = new LinkedList<Man>();
		int size = b.size();
		
		//TODO : 二分图？ a,b同时只能剩余一个，others加到a里
		List<Record> others = new LinkedList<Record>();
		
		int cost = 0;
		
		while(!a.isEmpty() && !b.isEmpty()) {
			Man man = new Man("id");
			Man m1 = a.get(0);
			Man m2 = b.get(0);
			
			man = add(m1, m2, others);
			//System.out.println("Man:" + man.getDist() + "  Cost:" + cost);
			
			
			men.add(man);
			a.remove(0);
			b.remove(0);
		}
		
		if(others.isEmpty()) {
			return men;
		}
		
		List<Record> tasks = new ArrayList<Record>(others);
		//Collections.sort(tasks, new TaskRecordComparator());
		Collections.shuffle(tasks);
		
		List<Record> menRecords = new LinkedList<Record>();
		
		int distOfMan = 0;
		while(true) {
			boolean success = false;
			for(Record record : tasks) {
				if(distOfMan + record.getDist() <= Utils.MAX_DIST) {
					tasks.remove(record);
					
					menRecords.add(record);
					distOfMan += record.getDist();
					
					success = true;
					break;
				}
			}
			
			if(!success) {
				Man man = new Man("id");
				man.update(new ArrayList<Record>(menRecords));
				
				a.add(man);
				
				menRecords = new LinkedList<Record>();
				distOfMan = 0;
				
				if(tasks.isEmpty()) {
					break;
				}
			}
		}
		
		if(men.size() > size) {
			System.out.println(">>>>");
			System.exit(-1);
		}
		return men;
	}
	
	private Man add(Man m1, Man m2, List<Record> others) {
		int cost = 0;
		int dist1= 0, dist2 = 0, distOther = 0;
		
		Man man = new Man("id");
		
		List<Record> records1 = new LinkedList<Record>(m1.getRecords());
		List<Record> records2 = new LinkedList<Record>(m2.getRecords());
		
		List<Record> recordsNew = new LinkedList<Record>();
		List<Record> recordOthers = new LinkedList<Record>();
		
		for(Record r : records1) {
			dist1 += r.getDist();
		}
		dist2 = m2.getDist();
		
		man = new Man("id");
		
		int i = 0, j = 0;
		int currTime = 0;
		Node currNode = (Node)records1.get(0).getStart();
		
		for(; j < records2.size();) {
			if(i == records1.size()) {
				recordsNew.addAll(records2.subList(j, records2.size()));
				break;
			}
			
			if(currTime + records1.get(i).getDist()
					//+ Utils.computeDist(currNode, records1.get(i).getStart())
					//- Utils.computeDist(records1.get(i).getPoints().get(records1.get(i).getPoints().size() - 1), records1.get(i).getEnd())
				> records2.get(j).getArriveTimes().get(0)
					- Utils.computeDist(records1.get(i).getPoints().get(records1.get(i).getPoints().size() - 1), records2.get(j).getPoints().get(0))
					) {
				
				int tmpTime = currTime + records1.get(i).getDist()
						+ Utils.computeDist(currNode, records1.get(i).getStart())
						- Utils.computeDist(records1.get(i).getPoints().get(records1.get(i).getPoints().size() - 1), records1.get(i).getEnd());
				
				List<Record> tmp = new LinkedList<Record>();
				
				Record record = new Record();
				record.update(
						records1.get(i).getStart(), 
						records1.get(i).getEnd(), 
						records1.get(i).getPoints(),
						currTime + Utils.computeDist(currNode, records1.get(i).getStart()));
				
				int k = j;
				while(tmpTime > records2.get(k).getArriveTimes().get(0)
						+ Utils.computeDist(currNode, records1.get(i).getStart())
						- Utils.computeDist(records1.get(i).getPoints().get(records1.get(i).getPoints().size() - 1), records1.get(i).getEnd())
						- Utils.computeDist(records1.get(i).getPoints().get(records1.get(i).getPoints().size() - 1), records2.get(k).getPoints().get(0))
						) {
					//tmp.add(records2.get(k));
					//System.out.println("K:" + i + "," + k);
					record = merge(records2.get(k), record);
					
					tmpTime = currTime + record.getDist()
							+ Utils.computeDist(currNode, record.getStart())
							- Utils.computeDist(record.getPoints().get(record.getPoints().size() - 1), record.getEnd());
					k++;
					
					if(k >= records2.size()) {
						break;
					}
				}
				
				if(tmpTime <= Utils.MAX_DIST) {
					i++;
					j = k;
					currTime = tmpTime;
					currNode = (Node)record.getPoints().get(record.getPoints().size() - 1);;
					
					recordsNew.add(record);
				} else {
					recordsNew.addAll(records2.subList(j, records2.size()));
					j = records2.size();
					break;
				}
				
			} else {
				Record recordNew = new Record();
				recordNew.update(
						records1.get(i).getStart(), 
						records1.get(i).getEnd(), 
						records1.get(i).getPoints(),
						currTime + Utils.computeDist(currNode, records1.get(i).getStart()));
				
				currTime += recordNew.getDist() + Utils.computeDist(currNode, recordNew.getStart())
				- Utils.computeDist(recordNew.getPoints().get(recordNew.getPoints().size() - 1), recordNew.getEnd());
				
				currNode = (Node)recordNew.getPoints().get(recordNew.getPoints().size() - 1);
				
				if(currTime <= Utils.MAX_DIST) {
					recordsNew.add(recordNew);
					i++;
				} else {
					recordsNew.addAll(records2.subList(j, records2.size()));
					j = records2.size();
					break;
				}
			}
		}
		recordOthers.addAll(records1.subList(i, records1.size()));
		
		man.update(recordsNew);
		
		//System.out.println(currTime + "============" + man.getDist());
		
		for(Record r : recordOthers) {
			distOther += r.getDist();
		}
		
		others.addAll(recordOthers);
		
		cost = dist1 + dist2 - man.getDist() - distOther;
		System.out.println(cost);
		return man;
	}
	
	private Record merge(Record record, Record a) {
		Record result = new Record();
		
		List<Point> points = new LinkedList<Point>(record.getPoints());
		List<String> orders = new LinkedList<String>();
		List<String> route = new LinkedList<String>();
		
		List<Integer> arriveTimes = new LinkedList<Integer>(record.getArriveTimes());
		List<Integer> leaveTimes = new LinkedList<Integer>(record.getLeaveTimes());
		
		//arriveTimes.add(0, record.getStartTime());
		//leaveTimes.add(0, record.getStartTime());
		
		int i = 0;
		for(int time : leaveTimes) {
			int tmp = record.getTakeouts().isEmpty() ? time + record.getStartTime() : time;
			
			//System.out.println("i:" + i + "," + tmp + "," + (a.getArriveTimes().get(0)
				//	- Utils.computeDist(points.get(i), a.getPoints().get(0))));
			
			if(tmp > a.getArriveTimes().get(0)
					- Utils.computeDist(points.get(i), a.getPoints().get(0))
					) {
				break;
			}
			i++;
		}

		//System.out.println("i:" + i);
		points.addAll(i, a.getPoints());
		
		
		for(Point p : points) {
			route.add(p.getId());
			orders.add(p.getOrderId());
		}
		
		//TODO
		/*
		System.out.println("StartTime:" + record.getStartTime());
		System.out.println("Start:" + record.getStart().getId());
		System.out.println("Route:" + route);
		System.out.println("Orders:" + orders);
		*/
		
		result.update(record.getStart(), record.getEnd(), record.getStartTime(), route, orders);
		//System.out.println("leaves:" + result.getLeaveTimes());
		if(result.getTakeouts().size() != a.getTakeouts().size() + record.getTakeouts().size()) {
			System.out.println(result.getTakeouts().size() + "," + a.getTakeouts().size());
			System.exit(-1);
		}
		return result;
		
		
	}
	
	private void fill(List<Record> taskRecords) {
		for(Man man : this.men) {
			if(man.getRecords().isEmpty()) {				
				continue;
			}
			
			int i = 0;
			int preTime = 0;
			
			Point pre = null;
			
			for(i = 0; i < man.getRecords().size(); i++) {
				Space space = new Space(preTime, man.getRecords().get(i).getLeaveTimes().get(0),
						pre, man.getRecords().get(i).getPoints().get(0));
				this.spaces.add(space);
				
				int len = man.getRecords().get(i).getPoints().size() - 1;
				preTime = man.getRecords().get(i).getLeaveTimes().get(len);
				pre = man.getRecords().get(i).getPoints().get(len);
			}
		}
		
		Collections.sort(spaces, new SpaceComparator());
		
		List<Record> records = new LinkedList<Record>(taskRecords);
		Collections.sort(records, new TaskRecordComparator());
		//Collections.shuffle(records);	
		
		List<Record> recordOthers = new LinkedList<Record>();
		int dist = 0;
		
		while(!records.isEmpty()) {
			boolean success = false;
			Record record = records.get(0);
			for(Space space : this.spaces) {
				if(space.insert(record)
						//merge(menRecords, record)
					) {
					success = true;
					break;
				}
			}
			
			if(!success) {
				recordOthers.add(record);
				System.out.println("Dist:" + record.getDist());
				dist += record.getDist();
			}
			
			records.remove(record);
			Collections.sort(spaces, new SpaceComparator());
		}
		
		int sum = 0;
		for(Space space : this.spaces) {
			if(space.getSpace() > 30)
				sum += space.getSpace();
			System.out.println(space.getSpace());
		}
		
		System.out.println(sum);
		
		System.out.println(recordOthers.size() + "," + dist);
		
		
	}
	
	public static void main(String[] args) throws IOException {
		Utils utils = new Utils();
		Controller controller = new Controller();
		System.out.println("=============");
		
		controller.updateTaskRecords("data/winners");
		controller.updateTakeoutRecords("data/takeoutwinners");
		
		//controller.dealTakeouts();
		//controller.dealTasks();
		
		//controller.preDeal();
		
		controller.dealTakeouts(new LinkedList<Record>(Utils.getMap().getTakeoutRecords()));
		controller.fill(new LinkedList<Record>(Utils.getMap().getTaskRecords()));
		//controller.dealTasks(new LinkedList<Record>(Utils.getMap().getTaskRecords()));
	
		//controller.writeActions();
		
		//controller.dealTasksOfSites();
		//controller.dealTakeoutsOfSites();
		
		
		//controller.deal(new LinkedList<Record>(Utils.getMap().getTakeoutRecords()),
			//	new LinkedList<Record>(Utils.getMap().getTaskRecords()));
		
		//System.exit(-1);
		//controller.verify();
		
		int sum = 0;
		int i = 0;
		
		int num = 0;
		
		int c1 = 0;
		int c2 = 0;
		for(Man man : controller.getMen()) {
			//System.out.println(i + ":" + man.getDist()
			//+ "," + man.getRecords().get(man.getRecords().size() - 1).getStartTime()
			//+ "," + man.getRecords().get(man.getRecords().size() - 1).getDist()
			//);
			sum += man.getDist();
			num += man.getRecords().size();
			i++;
			
			for(Record r : man.getRecords()) {
				c1 += r.getTakeouts().size();
				c2 += r.getOrders().size();
			}
		}
		
		System.out.println(c1 + "," + c2);
		System.out.println(sum);
		System.out.println(num);
		//controller.writeActions();
		
		System.out.println(Utils.computeDist(Utils.getNode("B5551"), Utils.getNode("A006")));
		

	}
}
