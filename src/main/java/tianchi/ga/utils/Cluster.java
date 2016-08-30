/**
 * 
 */
package tianchi.ga.utils;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import tianchi.ga.controller.node.Point;
import tianchi.ga.controller.route.Route;
import tianchi.ga.controller.route.Takeout;
import tianchi.ga.genetic.Chromosome;
import tianchi.ga.genetic.TakeoutChromosome;
import tianchi.ga.genetic.TakeoutPopulation;

/**
* <p>Title: Cluster.java</p>
* <p>Description: </p>
* <p>Copyright: Copyright (c) 2007</p>
* <p>Company: Zhongwei</p>
* @author Zhongwei
* @date 2016年8月16日
* @version 1.0
*/
public class Cluster {
	private double minShift = 0;
	private double bandwidth = 0;
	private int k = 0;
	
	private List<Point> points;
	private List<Integer> labels;
	
	private Map<Point, Integer> pointToLabel;
	private Map<Integer, List<Point>> labelToPoint;
	
	private List<List<Double>> centers;
	
	private Map<Integer, List<Takeout>> takeoutsLabeled;//-1表示有两个标签
	
	public Cluster() {
		this.minShift = 0.05;
		this.bandwidth = 0.1;
		this.k = 0;
		
		this.points = new LinkedList<Point>();
		this.labels = new LinkedList<Integer>();
		this.centers = new ArrayList<List<Double>>();
		this.pointToLabel = new HashMap<Point, Integer>();
		this.labelToPoint = new HashMap<Integer, List<Point>>();	
		this.takeoutsLabeled = new HashMap<Integer, List<Takeout>>();
	}
	
	public void map(List<Takeout> takeouts) {
		Map<String, Point> pointMap = new HashMap<String, Point>();
		
		for(Takeout takeout : takeouts) {
			if(!pointMap.containsKey(takeout.getShop())) {
				pointMap.put(takeout.getShop().getId(), takeout.getShop());
				this.points.add(takeout.getShop());
			}
			
			if(!pointMap.containsKey(takeout.getSpot())) {
				pointMap.put(takeout.getSpot().getId(), takeout.getSpot());
				this.points.add(takeout.getSpot());
			}
		}
		
	}
	
	public void setMinShift(double minShift) {
		this.minShift = minShift;
	}
	
	public void setBandwidth(double bandwidth) {
		this.bandwidth = bandwidth;
	}
	
	public void meanShift() throws IOException {
		//meanShift聚类
		List<Point> labeled = new LinkedList<Point>();
		List<Point> unlabeled = new LinkedList<Point>();
		
		Map<Integer, List<Point>> pointsList = new HashMap<Integer, List<Point>>();
		
		unlabeled = new LinkedList<Point>(this.points);
		
		Random random = new Random();
		
		while(!unlabeled.isEmpty()) {
			int randIndex = random.nextInt(unlabeled.size());
			List<Point> points = new ArrayList<Point>();
			
			Point centerPoint = unlabeled.get(randIndex);
			
			List<Double> center = new ArrayList<Double>();
			center.add(centerPoint.getLat());
			center.add(centerPoint.getLng());
			
			List<Double> shift = new ArrayList<Double>();
			shift.add(Double.MAX_VALUE);
			shift.add(Double.MAX_VALUE);

			while(Math.sqrt(Math.pow(shift.get(0), 2) + 
					Math.pow(shift.get(1), 2)) > this.minShift) {
				List<Point> interPoints = search(center, this.points, this.bandwidth);
				points.addAll(interPoints);
				unlabeled.removeAll(interPoints);
				
				shift = computeShift(center, interPoints);
				center.set(0, center.get(0) + shift.get(0));
				center.set(1, center.get(1) + shift.get(1));
			}
			
			List<Point> interPoints = search(center, this.points, this.bandwidth);
			unlabeled.removeAll(interPoints);
			points.addAll(interPoints);
			
			int i = 0;
			for(i = 0; i < this.centers.size(); i++) {
				List<Double> c = this.centers.get(i);
				if(Math.sqrt(
						Math.pow(c.get(0)-center.get(0), 2) + 
								Math.pow(c.get(1)-center.get(1), 2)) <= this.minShift) {
					List<Point> tmp = pointsList.get(i);
					tmp.addAll(points);
					pointsList.put(i, tmp);
					break;
				}
			}
			
			if(i == this.centers.size()) {
				pointsList.put(i, points);
				this.centers.add(center);
				this.labels.add(i);
			}
		}
		
		this.k = this.labels.size();

		for(Point point : this.points) {
			int label = 0;
			int maxCount = 0;
			
			for(int i = 0; i < this.k; i++) {
				List<Point> list = pointsList.get(i);
				
				int count = 0;
				for(Point otherPoint : list) {
					if(otherPoint.getId().equals(point.getId())) {
						count++;
					}
				}

				if(count > maxCount) {
					maxCount = count;
					label = i;
				}
			}
			
			this.pointToLabel.put(point, label);
			
			
			List<Point> tmp = new ArrayList<Point>();
			if(this.labelToPoint.containsKey(label)) {
				tmp = this.labelToPoint.get(label);
			}
			
			tmp.add(point);
			this.labelToPoint.put(label, tmp);	
		}
		
		//TODO
		for(int label : labels) {
			List<Point> tmp = this.labelToPoint.get(label);
			
			String file = "data//clusters//" + label + ".csv";
			FileWriter fw = new FileWriter(file);
			StringBuffer info = new StringBuffer();
			
			for(Point t : tmp) {
				info.append(t.getId() + "," + t.getLng() + "," + t.getLat() + "\r\n");
			}
			
			fw.write(info.toString());
			fw.flush();
			fw.close();
		}		
	}
	
	public void labelTakeouts() {
		List<Takeout> others = new ArrayList<Takeout>();
		for(Takeout t : Utils.getMap().getTakeoutsList()) {
			List<Takeout> takeouts = new ArrayList<Takeout>();
			if(this.pointToLabel.get(t.getShop()) == 
					this.pointToLabel.get(t.getSpot())) {
				if(this.takeoutsLabeled.containsKey(this.pointToLabel.get(t.getShop()))) {
					takeouts = this.takeoutsLabeled.get(this.pointToLabel.get(t.getShop()));
				}
				takeouts.add(t);
				this.takeoutsLabeled.put(this.pointToLabel.get(t.getShop()), takeouts);
			} else {
				others.add(t);
			}
		}
		
		this.takeoutsLabeled.put(-1, others);
	}
	
	private List<Point> search(List<Double> center, List<Point> points, double bandwidth) {
		List<Point> result = new LinkedList<Point>();
		
		for(Point a : points) {
			if(bandwidth >= Math.sqrt(Math.pow(center.get(0)-a.getLat(), 2) + 
					Math.pow(center.get(1)-a.getLng(), 2))) {
				result.add(a);
			}
		}
		return result;
	}
	
	private List<Double> computeShift(List<Double> center, List<Point> interPoints) {
		List<Double> shift = new ArrayList<Double>();
		shift.add(0.0);
		shift.add(0.0);
		
		for(Point inter : interPoints) {
			double lat = inter.getLat() - center.get(0);
			double lng = inter.getLng() - center.get(1);
			
			shift.set(0, shift.get(0) + lat);
			shift.set(1, shift.get(0) + lng);
		}
		
		return shift;
	}
	
	public Map<Integer, List<Takeout>> getTakeouts() {
		return this.takeoutsLabeled;
	}
	
	public List<List<Double>> getCenters() {
		return this.centers;
	}
	
	public List<Integer> getLabels() {
		return this.labels;
	}
	
	/*
	public static void main(String[] args) throws IOException {
		//TODO
		Utils utils = new Utils();
		
		Cluster cluster = new Cluster();
		
		cluster.map(Utils.getMap().getTakeoutsList());
		
		cluster.meanShift();
		cluster.labelTakeouts();
		
		System.out.println(cluster.getTakeouts().get(-1).size());
		
		int sum = 0;
		for(Map.Entry<Integer, List<Takeout>> entry :cluster.getTakeouts().entrySet()) {
			List<Takeout> takeouts = entry.getValue();
			//TODO
			System.out.println(takeouts.size() + "====" + (entry.getKey()));
			
			TakeoutPopulation population = new TakeoutPopulation();
			population.init(8, takeouts);
			
			int sumFitness = population.getFitness();
			TakeoutChromosome winner = (TakeoutChromosome)population.getWinner();
			TakeoutChromosome realWinner = (TakeoutChromosome)population.getWinner();
			int bestFitness = winner.getFitness();
			int realFitness = bestFitness;

			int count = 1;

			while(count <= 3000) {
				population = population.reproduce(2);
	
				sumFitness = population.getFitness();
				winner = (TakeoutChromosome)population.getWinner();
				System.out.println(winner.verify(takeouts));
				
				bestFitness = winner.getFitness();
	
				if(bestFitness <= realFitness) {
					realFitness = bestFitness;
					realWinner = winner;
				}
	
				count++;
	
				//TODO
				System.out.println(entry.getKey() + "," + count + "," + bestFitness + "," + winner.getRoutes().size());
				//for(Route route : this.winner.getRoutes()) {
					//System.out.println(route.getWeightSum() + ":" + route.getDist() + ":" +route.getRoute());
				//}
				//System.out.println(this.bestFitness + ":" + this.winner.getRouteTotal());
				//System.out.println(this.winner.getBreakPoints());
			}
	
			
			String file = "data/takeoutpopulations/" + entry.getKey() + ".csv";
			FileWriter fw = new FileWriter(file);
			StringBuffer info = new StringBuffer();
	
			for(Chromosome c : population.getPopulation()) {
				for(Point p : c.getRouteTotal()) {
					info.append(p.getId() + ",");
				}
				info.append(":" + c.getBreakPoints() + "\r\n");
			}
			fw.write(info.toString());
			fw.flush();
			fw.close();	
			
			info = new StringBuffer();
			int i = 0;
			for(Route route : realWinner.getRoutes()) {
				i++;
				List<String> orders = new ArrayList<String>();
				List<String> points = new ArrayList<String>();
				
				for(Point point : route.getPoints()) {
					orders.add(point.getOrderId());
					points.add(point.getId());
				}
				
				info.append(i + ":" + route.getWeight() + "-" + route.getDist() + "-" + 
				route.getStartTime() + "-" +
				route.getStart().getId() + "-" + 
				route.getEnd().getId() + "-" +
				points + orders + "\r\n");
			}

			file = "data/takeoutwinners/" + entry.getKey() + "-" + (int)realFitness + "-" + realWinner.getRoutes().size() + ".csv";

			fw = new FileWriter(file);
			fw.write(info.toString());
			fw.flush();
			fw.close();
			
		}
	}*/
}
