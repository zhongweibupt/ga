/**
 * 
 */
package tianchi.ga.controller;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import tianchi.ga.controller.node.Action;
import tianchi.ga.controller.node.Point;
import tianchi.ga.controller.node.Site;
import tianchi.ga.controller.route.Record;
import tianchi.ga.utils.DataMapper;
import tianchi.ga.utils.Utils;

/**
* <p>Title: Man.java</p>
* <p>Description: </p>
* <p>Copyright: Copyright (c) 2007</p>
* <p>Company: Zhongwei</p>
* @author Zhongwei
* @date 2016年8月21日
* @version 1.0
*/
public class Man {
	private String id;
	private List<Record> records;
	
	private List<Integer> space;
	private int dist;
	private int realDist;
	
	public Man(String id) {
		this.id = id;
		this.records = new LinkedList<Record>();
		this.space = new ArrayList<Integer>();
		
		this.space.add(Utils.START_TIME);
		this.space.add(Utils.START_TIME + Utils.MAX_DIST);
		
		this.dist = 0;
		this.realDist = 0;
	}
	
	public boolean insertRecord(Record record) {
		//TODO
		int startTime = record.getStartTime();
		int endTime = startTime + record.getDist();
		int dist = endTime - startTime;
		
		Site start = record.getStart();
		Site end = record.getEnd();
		
		for(int i = 0; i < space.size(); i += 2) {
			if(startTime != 0) {
				if(startTime >= space.get(i) &&
						endTime <= space.get(i + 1)) {
					
				}
			} else {
				if(space.get(i + 1) - space.get(i) >= dist) {
					
				}
			}	
		}
		
		return true;
	}
	
	public void update(List<Record> records) {
		this.records = new LinkedList<Record>(records);
		
		if(records.isEmpty()) {
			return;
		}
		
		this.dist = records.get(records.size() - 1).getStartTime() + 
				records.get(records.size() - 1).getDist();
	}
	
	public List<Record> getRecords() {
		return this.records;
	}
	
	public String getId() {
		return this.id;
	}
	
	public void writeActions() throws IOException {
		String file = "data/actions/" + this.id + ".csv";
		FileWriter fw = new FileWriter(file);
		StringBuffer info = new StringBuffer();
		
		Point pointLast = this.records.get(0).getPoints().get(this.records.get(0).getPoints().size() - 1);
		int timeLast = this.records.get(0).getLeaveTimes().get(this.records.get(0).getPoints().size() - 1);
		for(Record record : this.records) {
			String Courier_id = this.id;
			if(!record.getOrders().isEmpty()) {
				String Addr = record.getStart().getId();
				int Arrival_time = record.getStartTime();
				int Departure = record.getStartTime();
				for(Point spot : record.getPoints()) {
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
			
			int i = 0;
			for(Point spot : record.getPoints()) {
				String Addr = spot.getId();
				//TODO : 有问题
				int Arrival_time = 0;
				if(i == 0) {
					Arrival_time = record.getOrders().isEmpty() ? 
							timeLast + Utils.computeDist(pointLast, spot) : 
								record.getStartTime() + record.getArriveTimes().get(i);
				} else {
					Arrival_time = record.getArriveTimes().get(i) + (record.getOrders().isEmpty() ? 0 : record.getStartTime()); 
				}
				int Departure = record.getLeaveTimes().get(i) + (record.getOrders().isEmpty() ? 0 : record.getStartTime());
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
					+ (record.getOrders().isEmpty() ? 0 : record.getStartTime());
		}
		
		fw = new FileWriter(file);
		fw.write(info.toString());
		fw.flush();
		fw.close();
	}
	
	public int getRealDist() {
		return this.realDist;
	}
	
	public int getDist() {
		return this.dist;
	}
}
