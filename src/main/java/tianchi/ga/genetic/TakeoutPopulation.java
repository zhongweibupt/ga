/**
 * 
 */
package tianchi.ga.genetic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import tianchi.ga.controller.node.Point;
import tianchi.ga.controller.route.Route;
import tianchi.ga.controller.route.Takeout;
import tianchi.ga.utils.DataMapper;
import tianchi.ga.utils.Utils;
import tianchi.ga.utils.comparator.PointComparator;
import tianchi.ga.utils.comparator.TakeoutComparator;

/**
* <p>Title: TakeoutPopulation.java</p>
* <p>Description: </p>
* <p>Copyright: Copyright (c) 2007</p>
* <p>Company: Zhongwei</p>
* @author Zhongwei
* @date 2016年8月17日
* @version 1.0
*/
public class TakeoutPopulation extends Population {
	
	public TakeoutPopulation() {
		super();
	}
	
	public void init(int size, List<Takeout> takeoutsList) {
		this.population.clear();
		this.fitness = Utils.MAX_VALUE;
		
		if(0 == (size%8)) {
			this.size = size;
			for(int i = 0; i < size; i++) {
				TakeoutChromosome chromosome = new TakeoutChromosome();
				chromosome.randomGenerate(takeoutsList, Utils.INTERVALS);
				//TODO
				String info = chromosome.verify(takeoutsList);
				if(!info.equals("Success!")) {
					System.out.println("ERROR:" + info);
					System.exit(-1);
				}
				this.population.add(chromosome);
				this.fitness += chromosome.getFitness();
			}
		} else {
			throw new IllegalArgumentException("Size must be divided by 8.");
		}
	}
	
	public TakeoutPopulation reproduce(int level) throws IllegalArgumentException {
		TakeoutPopulation offspringPopu = new TakeoutPopulation();
		List<Chromosome> chromosomes = new ArrayList<Chromosome>();
		
		for(int i = 0; i < this.size; i += Population.sizeOfGroup) {			
			TakeoutChromosome parent1 = new TakeoutChromosome();
			parent1.update(select().getRoutes());
			TakeoutChromosome parent2 = new TakeoutChromosome();
			parent2.update(select().getRoutes());
			
			//TODO
			//while(parent2.equals(parent1) && parent2 == parent1) {
				//parent2 = select();
			//}

			List<TakeoutChromosome> offsprings1 = 
					cross((TakeoutChromosome)parent1, (TakeoutChromosome)parent2, 0);
			
			TakeoutChromosome offspring1 = offsprings1.get(0);
			TakeoutChromosome offspring2 = offsprings1.get(1);

			offspring1.mutate(level);
			offspring2.mutate(level);

			chromosomes.add(offspring1);
			chromosomes.add(offspring2);
			
			List<TakeoutChromosome> offsprings2 = 
					cross((TakeoutChromosome)parent1, (TakeoutChromosome)parent2, 1);
			
			TakeoutChromosome offspring3 = offsprings2.get(0);
			TakeoutChromosome offspring4 = offsprings2.get(1);

			offspring3.mutate(level);
			offspring4.mutate(level);
			chromosomes.add(offspring3);
			chromosomes.add(offspring4);
			
			List<TakeoutChromosome> offsprings3 = 
					cross((TakeoutChromosome)parent1, (TakeoutChromosome)parent2, 1);
			
			TakeoutChromosome offspring5 = offsprings3.get(0);
			TakeoutChromosome offspring6 = offsprings3.get(1);
			
			offspring5.mutate(level);
			offspring6.mutate(level);
			chromosomes.add(offspring5);
			chromosomes.add(offspring6);
			
			TakeoutChromosome offspring7 = new TakeoutChromosome();
			TakeoutChromosome offspring8 = new TakeoutChromosome();
			
			offspring7.update(parent1.getRoutes());
			offspring8.update(parent2.getRoutes());
			
			offspring7.mutate(level);
			offspring8.mutate(level);
			chromosomes.add(offspring7);
			chromosomes.add(offspring8);
		}
		
		offspringPopu.update(this, chromosomes);
		return offspringPopu;
	}
	
	public static List<TakeoutChromosome> cross(TakeoutChromosome pparent1, TakeoutChromosome pparent2, int method) {
		TakeoutChromosome offspring1 = new TakeoutChromosome();
		TakeoutChromosome offspring2 = new TakeoutChromosome();
		
		TakeoutChromosome parent1 = new TakeoutChromosome();
		TakeoutChromosome parent2 = new TakeoutChromosome();
		
		parent1.update(pparent1.getRoutes());
		parent2.update(pparent2.getRoutes());
		
		List<TakeoutChromosome> offsprings = new ArrayList<TakeoutChromosome>();
		
		//TODO
		//method = 0;
		
		if(0 == method) {//最大匹配，保留每个route的匹配组合以及序列，route的其余部分随机填充
			//Generate offspring1
			List<List<Takeout>> takeoutsListA = new ArrayList<List<Takeout>>();
			List<List<Point>> pointsListA = new ArrayList<List<Point>>();
			List<List<Point>> shopsListA = new ArrayList<List<Point>>();
			List<List<Point>> spotsListA = new ArrayList<List<Point>>();

			for(Route route : parent1.getRoutes()) {
				List<Takeout> takeouts = new LinkedList<Takeout>();
				List<Point> points = new LinkedList<Point>(route.getPoints());
				
				int maxMatch = 0;
				for(Route other : parent2.getRoutes()) {
					List<Takeout> tmp = new LinkedList<Takeout>(route.getTakeouts());
					
					tmp.retainAll(other.getTakeouts());
					
					if(maxMatch < tmp.size()) {
						maxMatch = tmp.size();
						takeouts = new LinkedList<Takeout>(tmp);
					}
				}
				if(takeouts.size() != 0) {
					takeoutsListA.add(takeouts);
					
					Map<String, Integer> takeoutMap = new HashMap<String, Integer>();
					for(Takeout t : takeouts) {
						takeoutMap.put(t.getId(), 1);
						//System.out.println("t:" + t.getId());
					}

					List<Point> tmp = new LinkedList<Point>();
					
					for(Point p : points) {
						if(!takeoutMap.containsKey(p.getOrderId())) {
							tmp.add(p);
						}
					}
					points.removeAll(tmp);
					
					List<Point> shops = new LinkedList<Point>();
					List<Point> spots = new LinkedList<Point>();
					for(Point point : points) {
						if(point.getType().equals(DataMapper.SHOP)) {
							shops.add(point);
						} else {
							spots.add(point);
						}
					}
					
					pointsListA.add(points);
					shopsListA.add(shops);
					spotsListA.add(spots);
				}
			}
			
			List<Takeout> takeoutOthers = new LinkedList<Takeout>(parent1.getTakeoutsTotal());
			for(List<Takeout> takeouts : takeoutsListA) {
				takeoutOthers.removeAll(takeouts);
			}
			
			List<Route> routesA = new ArrayList<Route>();
			for(int i = 0; i < pointsListA.size(); i++) {
				Route route = new Route();
				//TODO
				if(pointsListA.get(i).size() != takeoutsListA.get(i).size()*2) {
					System.out.println(route.getPoints().size());
					System.out.println("p1:" + pointsListA.get(i).size() + "," + takeoutsListA.get(i).size());
					System.exit(-1);
				}
				route.update(pointsListA.get(i), takeoutsListA.get(i), shopsListA.get(i), spotsListA.get(i));
				routesA.add(route);
			}
			offspring1 = randomInsert(routesA, takeoutOthers);
			
			//Generate offspring2
			List<List<Takeout>> takeoutsListB = new ArrayList<List<Takeout>>();
			List<List<Point>> pointsListB = new ArrayList<List<Point>>();
			List<List<Point>> shopsListB = new ArrayList<List<Point>>();
			List<List<Point>> spotsListB = new ArrayList<List<Point>>();

			for(Route route : parent2.getRoutes()) {
				List<Takeout> takeouts = new LinkedList<Takeout>();
				List<Point> points = new LinkedList<Point>(route.getPoints());
				
				int maxMatch = 0;
				for(Route other : parent1.getRoutes()) {
					List<Takeout> tmp = new LinkedList<Takeout>(route.getTakeouts());
					tmp.retainAll(other.getTakeouts());
					
					if(maxMatch < tmp.size()) {
						maxMatch = tmp.size();
						takeouts = new LinkedList<Takeout>(tmp);
					}
				}
				
				if(takeouts.size() != 0) {
					takeoutsListB.add(takeouts);
					
					Map<String, Integer> takeoutMap = new HashMap<String, Integer>();
					for(Takeout t : takeouts) {
						takeoutMap.put(t.getId(), 1);
					}

					List<Point> tmp = new LinkedList<Point>();
					for(Point p : points) {
						if(!takeoutMap.containsKey(p.getOrderId())) {
							tmp.add(p);
						}
					}
					points.removeAll(tmp);
					
					List<Point> shops = new LinkedList<Point>();
					List<Point> spots = new LinkedList<Point>();
					for(Point point : points) {
						if(point.getType().equals(DataMapper.SHOP)) {
							shops.add(point);
						} else {
							spots.add(point);
						}
					}
					
					pointsListB.add(points);
					shopsListB.add(shops);
					spotsListB.add(spots);
				}
			}
			
			takeoutOthers = new LinkedList<Takeout>(parent2.getTakeoutsTotal());
			for(List<Takeout> takeouts : takeoutsListB) {
				takeoutOthers.removeAll(takeouts);
			}
			
			List<Route> routesB = new ArrayList<Route>();
			for(int i = 0; i < pointsListB.size(); i++) {
				Route route = new Route();
				//TODO
				if(pointsListB.get(i).size() != takeoutsListB.get(i).size()*2) {
					System.out.println(route.getPoints().size());
					System.out.println("p1:" + pointsListB.get(i).size() + "," + takeoutsListB.get(i).size());
					System.exit(-1);
				}
				route.update(pointsListB.get(i), takeoutsListB.get(i), shopsListB.get(i), spotsListB.get(i));
				routesB.add(route);
			}
			offspring2 = randomInsert(routesB, takeoutOthers);
			
			//TODO
			String info = offspring1.verify(offspring1.getTakeoutsTotal());
			if(!info.equals("Success!")) {
				System.out.println("===Cross===" + "Method:" + method);
				System.out.println("ERROR:" + info);
				System.exit(-1);
			}
			info = offspring2.verify(offspring2.getTakeoutsTotal());
			if(!info.equals("Success!")) {
				System.out.println("===Cross===" + "Method:" + method);
				System.out.println("ERROR:" + info);
				System.exit(-1);
			}
			
			offsprings.add(offspring1);
			offsprings.add(offspring2);
			
		} else if(1 == method) {//部分交换,随机交换dist小于10000的route，其余重复随机填充，两个parent长度都不能为1
			//TODO
			
			List<Route> routesNew = new LinkedList<Route>();
			
			for(Route route : parent1.getRoutes()) {
				if(route.getDist() < Utils.MAX_DIST && route.getPoints().size() > 1) {
					routesNew.add(route); 
				} else {
					List<Takeout> takeouts = new LinkedList<Takeout>(route.getTakeouts());
					TakeoutChromosome c = new TakeoutChromosome();
					c.randomGenerate(takeouts, Utils.INTERVALS);	
					routesNew.addAll(c.getRoutes());
				}
			}
			offspring1.update(routesNew);
			
			List<Route> routesNew2 = new LinkedList<Route>();
			
			for(Route route : parent2.getRoutes()) {
				if(route.getDist() < Utils.MAX_DIST && route.getPoints().size() > 1) {
					routesNew2.add(route); 
				} else {
					List<Takeout> takeouts = new LinkedList<Takeout>(route.getTakeouts());
					TakeoutChromosome c = new TakeoutChromosome();
					c.randomGenerate(takeouts, Utils.INTERVALS);	
					routesNew2.addAll(c.getRoutes());
				}
			}
			offspring2.update(routesNew2);
			
			offsprings.add(offspring1);
			offsprings.add(offspring2);
			
		} else {//部分交换，随机交换dist大于10000的route部分point，如果新加入的point在可行route里，则不加入
			
		}
		return offsprings;
	}
	
	public static TakeoutChromosome randomInsert(List<Route> routes, List<Takeout> takeouts) {
		TakeoutChromosome chromosome = new TakeoutChromosome();
		
		List<Route> routesNew = new LinkedList<Route>(routes);
		
		Map<Integer, List<Takeout>> matches = new HashMap<Integer, List<Takeout>>();
		for(Takeout takeout : takeouts) {
			String orderId = takeout.getId();
			String shopId = takeout.getShop().getId();
			
			int bestMatch = -1;
			int i = 0;
			for(Route route : routes) {
				for(Takeout other : route.getTakeouts()) {
					if(//Math.abs(takeout.getPickupTime() - other.getPickupTime()) <= Utils.INTERVALS
							//&& 
							other.getShop().getId().equals(shopId)
							) {
						bestMatch = i;
						break;
					}
				}
				i++;
			}
			
			List<Takeout> tmp = new LinkedList<Takeout>();
			if(matches.containsKey(bestMatch)) {
				tmp = matches.get(bestMatch);
			}
			tmp.add(takeout);
			matches.put(bestMatch, tmp);
		}
		
		for(Map.Entry<Integer, List<Takeout>> entry : matches.entrySet()) {
			if(entry.getKey() != -1) {
				List<Takeout> takeoutsNew = new LinkedList<Takeout>(routes.get(entry.getKey()).getTakeouts());
				takeoutsNew.addAll(entry.getValue());
				Collections.sort(takeoutsNew, new TakeoutComparator());
				
				List<Point> pointsNew = new LinkedList<Point>(routes.get(entry.getKey()).getPoints());
				List<Point> shopsNew = new LinkedList<Point>(routes.get(entry.getKey()).getShops());
				List<Point> spotsNew = new LinkedList<Point>(routes.get(entry.getKey()).getSpots());
				
				List<Point> pointsOther = new LinkedList<Point>();
				List<Point> shopsOther = new LinkedList<Point>();
				List<Point> spotsOther = new LinkedList<Point>();
				
				for(Takeout t : entry.getValue()) {
					pointsOther.add(t.getShop());
					pointsOther.add(t.getSpot());
				}
				
				Collections.sort(pointsOther, new PointComparator());
				for(Point point : pointsOther) {
					if(point.getType().equals(DataMapper.SHOP)) {
						shopsOther.add(point);
					} else {
						spotsOther.add(point);
					}
				}
				
				pointsNew = TakeoutChromosome.merge(pointsNew, pointsOther);
				shopsNew = TakeoutChromosome.merge(shopsNew, shopsOther);
				spotsNew = TakeoutChromosome.merge(spotsNew, spotsOther);
				
				Route route = new Route();
				route.update(pointsNew, takeoutsNew, shopsNew, spotsNew);
				
				routesNew.set(entry.getKey(), route);
			}
		}
		
		if(matches.containsKey(-1)) {
			List<Takeout> takeoutOthers = matches.get(-1);
			TakeoutChromosome c = new TakeoutChromosome();
			c.randomGenerate(takeoutOthers, -1);	
			routesNew.addAll(c.getRoutes());
		}
		
		chromosome.update(routesNew);
		return chromosome;
	}
}
