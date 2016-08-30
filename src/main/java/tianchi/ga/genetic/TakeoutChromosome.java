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
* <p>Title: TakeoutChromosome.java</p>
* <p>Description: TakeoutChromosome继承Chromosome类，对应一个center的解。</p>
* <p>Copyright: Copyright (c) 2007</p>
* <p>Company: Zhongwei</p>
* @author Zhongwei
* @date 2016年8月16日
* @version 1.0
*/
public class TakeoutChromosome extends Chromosome {
	private List<Takeout> takeoutsTotal;
	
	private List<List<Takeout>> takeoutsList;
	private List<List<Point>> shopsList;
	private List<List<Point>> spotsList;
	
	public TakeoutChromosome() {
		super();
		this.takeoutsTotal = new LinkedList<Takeout>();
		
		this.takeoutsList = new ArrayList<List<Takeout>>();
		this.shopsList = new ArrayList<List<Point>>();
		this.spotsList = new ArrayList<List<Point>>();
		this.sumOfDist = Utils.MAX_VALUE;
	}
	
	@Override
	public void update(List<Route> routes) {
		this.takeoutsTotal.clear();
		this.takeoutsList.clear();
		this.shopsList.clear();
		this.spotsList.clear();
		this.sumOfDist = Utils.MAX_VALUE;
		
		this.routes = new ArrayList<Route>();
		for(Route route : routes) {
			Route routeNew = new Route();
			routeNew.update(route.getPoints(), route.getTakeouts(), route.getShops(), route.getSpots());
			//TODO
			if(routeNew.getPoints().size() != routeNew.getTakeouts().size()*2) {
				System.out.println("=============================" + routeNew.getTakeouts().size()
						+ "," + routeNew.getPoints().size());
			}
			
			this.takeoutsTotal.addAll(route.getTakeouts());
			this.takeoutsList.add(new ArrayList<Takeout>(route.getTakeouts()));
			this.shopsList.add(new ArrayList<Point>(route.getShops()));
			this.spotsList.add(new ArrayList<Point>(route.getSpots()));
			
			this.routes.add(routeNew);
		}
		
		int sumOfDist = 0;

		int rear = 0;
		for(Route route : routes) {
			rear += route.getPoints().size();
			sumOfDist += route.getDist();
			
			if(sumOfDist < 0) {
				System.out.println(sumOfDist + "," + route.getStart().getId() + "," + route.getEnd().getId() + "," + route.getDist() + "," + route.getRoute());
				System.exit(-1);
			}
		}
		
		this.sumOfDist = sumOfDist >= Utils.MAX_VALUE ? Utils.MAX_VALUE : sumOfDist;
		this.numOfMen = this.routes.size();
		
		//TODO
		//System.out.println(this.getTakeoutsTotal().size());
	}
	
	public void randomGenerate(List<Takeout> takeoutsTotal, int randTime) {
		Random random = new Random();
		//int randTime = random.nextInt(Utils.INTERVALS);
		
		Collections.sort(takeoutsTotal, new TakeoutComparator());
		
		int startTime = takeoutsTotal.get(0).getPickupTime();
		String shopPre = takeoutsTotal.get(0).getShop().getId(); 
		List<Takeout> takeouts = new LinkedList<Takeout>();
		
		takeouts = new LinkedList<Takeout>();
		takeouts.add(takeoutsTotal.get(0));
		
		for(int i = 1; i < takeoutsTotal.size(); i++) {
			Takeout takeout = takeoutsTotal.get(i);
			if(startTime + randTime >= takeout.getPickupTime() && takeout.getShop().getId().equals(shopPre)) {
				takeouts.add(takeout);
			} else {
			//{
				//takeouts = new LinkedList<Takeout>();
				//takeouts.add(takeout);
				this.takeoutsList.add(new LinkedList<Takeout>(takeouts));
				startTime = takeout.getPickupTime();
				shopPre = takeout.getShop().getId(); 
				takeouts = new LinkedList<Takeout>();
				takeouts.add(takeout);
			}
		}
		this.takeoutsList.add(new LinkedList<Takeout>(takeouts));
		
		List<Route> routes = new ArrayList<Route>();
		for(List<Takeout> tmp : takeoutsList) {
			List<Point> shops = new ArrayList<Point>();
			List<Point> spots = new ArrayList<Point>();
			List<Point> points = new ArrayList<Point>();
			
			for(Takeout t : tmp) {
				shops.add(t.getShop());
				spots.add(t.getSpot());
			}
			
			//points = new ArrayList<Point>(shops);
			//points.addAll(spots);
			//Collections.sort(points, new PointComparator());
			Collections.sort(shops, new PointComparator());	
			Collections.shuffle(spots);
			
			points = new ArrayList<Point>();
			points = merge(shops, spots);
			
			Route route = new Route();
			route.update(points, tmp, shops, spots);
			
			routes.add(route);
		}
		
		this.update(routes);
		
		//TODO
		if(takeoutsTotal.size() != this.takeoutsTotal.size()) {
			System.out.println("===1===");
			System.exit(-1);
		}
	}
	
	@Override
	public void mutate(int level) {
		int method = gaussian(level);
		
		int takeoutsTotalPre = this.takeoutsTotal.size();
		
		if(3 == method) {
			List<Route> routes = this.routes;
			List<Integer> candidates = new ArrayList<Integer>();
			Random random = new Random();

			int action = random.nextInt(10);
			if(0 == action) {//增加一个快递员
				int i = 0;
				for(Route route : routes) {
					if(2 < route.getLength()) {
						candidates.add(i);
					}
					i++;
				}
				
				if(0 == candidates.size()) {
					//Do nothing
				} else {
					int randIndex = random.nextInt(candidates.size());
					Route route = routes.get(candidates.get(randIndex));
					
					int randBreak = random.nextInt(route.getTakeouts().size() - 1) + 1;
					
					List<Takeout> takeoutsA = new ArrayList<Takeout>(
							route.getTakeouts().subList(0, randBreak));
					List<Takeout> takeoutsB = new ArrayList<Takeout>(
							route.getTakeouts().subList(randBreak, route.getTakeouts().size()));
					
					List<Point> shopsA = new ArrayList<Point>();
					List<Point> shopsB = new ArrayList<Point>();
					
					List<Point> spotsA = new ArrayList<Point>();
					List<Point> spotsB = new ArrayList<Point>();
					
					List<Point> pointsA = new ArrayList<Point>();
					List<Point> pointsB = new ArrayList<Point>();
					
					List<String> listA = new LinkedList<String>();
					for(Takeout tmp : takeoutsA) {
						listA.add(tmp.getId());
					}
					
					for(Point point : route.getPoints()) {
						if(listA.contains(point.getOrderId())) {
							pointsA.add(point);
							if(point.getType().equals(DataMapper.SHOP)) {
								shopsA.add(point);
							} else {
								spotsA.add(point);
							}
						} else {
							pointsB.add(point);
							if(point.getType().equals(DataMapper.SHOP)) {
								shopsB.add(point);
							} else {
								spotsB.add(point);
							}
						}
					}
					
					//TODO
					if(pointsA.size() + pointsB.size() != route.getPoints().size()) {
						System.out.println("??????");
						System.exit(-1);
					}
					
					Route a = new Route();
					Route b = new Route();
					
					//TODO
					if(pointsA.size() != takeoutsA.size()*2) {
						System.out.println(route.getPoints().size());
						System.out.println("a:" + pointsA.size() + "," + takeoutsA.size());
						for(Point p : pointsA) {
							System.out.println(p.getId() + "," + p.getOrderId());
						}
						System.out.println("=============");
						for(Point p : route.getPoints()) {
							System.out.println(p.getId() + "," + p.getOrderId());
						}
						System.exit(-1);
					}
					if(pointsB.size() != takeoutsB.size()*2) {
						System.out.println(route.getPoints().size());
						System.out.println("b:" + pointsB.size() + "," + takeoutsB.size());
						System.exit(-1);
					}
					
					a.update(pointsA, takeoutsA, shopsA, spotsA);
					b.update(pointsB, takeoutsB, shopsB, spotsB);
					
					routes.remove(route);
					routes.add(a);
					routes.add(b);

					this.update(routes);
				}
			} else {
				int randIndex = random.nextInt(routes.size());
				Route a = routes.get(randIndex);

				int i = 0;
				for(Route route : routes) {
					if(i != randIndex) {
						candidates.add(i);
					}
					i++;
				}

				if(0 == candidates.size()) {
					//Do nothing
				} else {
					int randIndexAnother = random.nextInt(candidates.size());
					Route b = routes.get(candidates.get(randIndexAnother));
					
					List<Takeout> takeouts = new ArrayList<Takeout>(a.getTakeouts());
					takeouts.addAll(b.getTakeouts());
					Collections.sort(takeouts, new TakeoutComparator());
					
					List<Point> points = merge(a.getPoints(), b.getPoints());
					
					List<Point> shops = new ArrayList<Point>();
					List<Point> spots = new ArrayList<Point>();
					
					for(Point point : points) {
						if(point.getType().equals(DataMapper.SHOP)) {
							shops.add(point);
						} else {
							spots.add(point);
						}
					}
					
					Route route = new Route();
					
					//TODO
					if(points.size() != takeouts.size()*2) {
						System.out.println(route.getPoints().size());
						System.out.println("c:" + points.size() + "," + takeouts.size());
						System.exit(-1);
					}
					
					route.update(points, takeouts, shops, spots);
					
					routes.remove(a);
					routes.remove(b);
					routes.add(route);

					this.update(routes);
				}
			}
			
		} else if(2 == method) {//替换,有问题
			Random random = new Random();
			List<Route> routes = new ArrayList<Route>();
			for(Route route : this.routes) {
				Route tmp = new Route();
				tmp.update(route.getPoints(), route.getTakeouts(), route.getShops(), route.getSpots());
				routes.add(tmp);
			}
			
			int randNum1 = random.nextInt(routes.size());
			Route route = routes.get(randNum1);
			
			int n = 0;
			while(route.getTakeouts().size() == 1) {
				randNum1 = random.nextInt(routes.size());
				route = routes.get(randNum1);
				if(n > 20) {
					return;
				}
				n++;
			}
			
			int rand1 = random.nextInt(route.getTakeouts().size());
			int rand2 = random.nextInt(route.getTakeouts().size());
			
			while(Math.abs(rand2 - rand1) == route.getTakeouts().size() - 1) {
				rand2 = random.nextInt(route.getTakeouts().size());
			}
			
			int randFront = Math.min(rand1, rand2);
			int randRear = Math.max(rand1, rand2);
	
			List<Takeout> takeoutsNew = new ArrayList<Takeout>(route.getTakeouts());
			List<Takeout> takeoutsPart = new ArrayList<Takeout>(route.getTakeouts().subList(randFront, randRear));

			takeoutsNew.removeAll(takeoutsPart);
			
			List<Point> pointsNew = new ArrayList<Point>(route.getPoints());
			List<Point> shopsNew = new ArrayList<Point>(route.getShops());
			List<Point> spotsNew = new ArrayList<Point>(route.getSpots());
			
			List<Point> pointsPart = new ArrayList<Point>();
			List<Point> shopsPart = new ArrayList<Point>();
			List<Point> spotsPart = new ArrayList<Point>();
			
			Map<String, Integer> takeoutMap = new HashMap<String, Integer>();
			for(Takeout t : takeoutsPart) {
				takeoutMap.put(t.getId(), 1);
			}
			
			for(Point point : route.getPoints()) {
				if(takeoutMap.containsKey(point.getOrderId())) {
					pointsPart.add(point);
					
					if(point.getType().equals(DataMapper.SHOP)) {
						shopsPart.add(point);
					} else {
						spotsPart.add(point);
					}
				}
			}
			
			pointsNew.removeAll(pointsPart);
			shopsNew.removeAll(shopsPart);
			spotsNew.removeAll(spotsPart);
			
			int randNum2 = random.nextInt(routes.size());
			n = 0;
			while(randNum2 == randNum1) {
				randNum2 = random.nextInt(routes.size());
				if(n > 20) {
					return;
				}
				n++;
			}
			
			Route routeOther = routes.get(randNum2);
			List<Point> pointsNew2 = new ArrayList<Point>(routeOther.getPoints());
			List<Point> shopsNew2 = new ArrayList<Point>(routeOther.getShops());
			List<Point> spotsNew2 = new ArrayList<Point>(routeOther.getSpots());

			pointsNew2 = merge(pointsNew2, pointsPart);
			shopsNew2 = merge(shopsNew2, shopsPart);
			spotsNew2 = merge(spotsNew2, spotsPart);
			
			List<Takeout> takeoutsNew2 = new ArrayList<Takeout>(routeOther.getTakeouts());
			takeoutsNew2.addAll(takeoutsPart);
			
			Collections.sort(takeoutsNew2, new TakeoutComparator());
			
			//TODO
			if(pointsNew2.size() != takeoutsNew2.size()*2) {
				System.out.println(route.getPoints().size());
				System.out.println("d:" + pointsNew2.size() + "," + takeoutsNew2.size());
				System.exit(-1);
			}
			if(pointsNew.size() != takeoutsNew.size()*2) {
				System.out.println(route.getPoints().size());
				System.out.println("e:" + pointsNew.size() + "," + takeoutsNew.size());
				System.exit(-1);
			}
			
			int a = routes.get(randNum2).getPoints().size() +
					routes.get(randNum1).getPoints().size();
			
			routeOther.update(pointsNew2, takeoutsNew2, shopsNew2, spotsNew2);
			routes.set(randNum2, routeOther);
			
			route.update(new ArrayList<Point>(pointsNew), 
					new ArrayList<Takeout>(takeoutsNew), 
					new ArrayList<Point>(shopsNew), 
					new ArrayList<Point>(spotsNew));
			routes.set(randNum1, route);
			
			int b = routes.get(randNum2).getPoints().size() +
					routes.get(randNum1).getPoints().size();
			
			if(a != b) {
				System.out.println(takeoutsPart.size() + "," + 
						takeoutsNew.size() + "," +
						takeoutsNew2.size() + "," +
						route.getTakeouts().size() + "," +
						routeOther.getTakeouts().size() + "," +
						routes.get(randNum2).getTakeouts().size() + "," + routes.get(randNum1).getTakeouts().size());
				System.out.println(a + "," + b);
				System.exit(-1);
			}
			
			if(Utils.MAX_WEIGHT < routeOther.getWeight()) {
				//Do nothing
			} else {
				this.update(routes);
			}
			
		} else if(1 == method) {//改变插入位置，改变顺序		
			Random random = new Random();
			List<Route> routes = new ArrayList<Route>();
			for(Route route : this.routes) {
				Route tmp = new Route();
				tmp.update(route.getPoints(), route.getTakeouts(), route.getShops(), route.getSpots());
				routes.add(tmp);
			}
			
			int randNum1 = random.nextInt(routes.size());
			Route route = routes.get(randNum1);
			
			int n = 0;
			while(route.getTakeouts().size() == 1) {
				randNum1 = random.nextInt(routes.size());
				route = routes.get(randNum1);
				if(n > 20) {
					return;
				}
				n++;
			}
			
			List<Point> spots = new ArrayList<Point>(route.getSpots());
			
			int rand1 = random.nextInt(route.getTakeouts().size());
			int rand2 = random.nextInt(route.getTakeouts().size());
			
			while(Math.abs(rand2 - rand1) == route.getTakeouts().size() - 1) {
				rand2 = random.nextInt(route.getTakeouts().size());
			}
			
			int randFront = Math.min(rand1, rand2);
			int randRear = Math.max(rand1, rand2);
			
			if(random.nextBoolean()) {
				spots = reverse(new ArrayList<Point>(route.getSpots()), randFront, randRear);
			}
			
			if(random.nextBoolean()) {
				List<Point> tmp = new ArrayList<Point>(spots.subList(randFront, randRear));
				spots.removeAll(tmp);
				int randIndex = random.nextInt(spots.size());
				spots.addAll(randIndex, tmp);
			}
			
			List<Point> points = new ArrayList<Point>(route.getPoints());
			
			int j = 0;
			for(int i = 0; i < points.size(); i++) {
				if(points.get(i).getType().equals(DataMapper.SPOT)) {
					points.set(i, spots.get(j++));
				}
			}
			
			if(random.nextBoolean()) {
				Point randShop = route.getTakeouts().get(random.nextInt(route.getTakeouts().size() - 1)).getShop();
				
				int pre = 0;
				int next = 0;
				int set = 0;
				int tmp = 0;
				
				int i = 0;
				for(Point point : route.getPoints()) {
					if(point.getOrderId().equals(randShop.getOrderId())) {
						if(point.getType().equals(DataMapper.SHOP)) {
							set = i;
							pre = tmp;
						} else {
							next = i;
						}
					}
					
					if(point.getType().equals(DataMapper.SHOP)) {
						tmp = i;
					}
					i++;
				}
				
				points.remove(set);
				next -= 1;
				
				int setNew = 0;
				if(next - pre > 0) {
					setNew = random.nextInt(next - pre) + pre + 1;
				}
				
				points.add(setNew, randShop);
			}
			
			List<Point> spotsNew = new ArrayList<Point>();
			List<Point> shopsNew = new ArrayList<Point>();
			
			for(Point point : points) {
				if(point.getType().equals(DataMapper.SHOP)) {
					shopsNew.add(point);
				} else {
					spotsNew.add(point);
				}
			}
			
			Route routeNew = new Route();
			routeNew.update(points, route.getTakeouts(), shopsNew, spotsNew);
			routes.set(randNum1, routeNew);
			this.update(routes);
		} else {
			List<Route> routesNew = new LinkedList<Route>();
			
			for(Route route : this.routes) {
				if(route.getDist() < Utils.MAX_DIST) {
					routesNew.add(route); 
				} else {
					List<Takeout> takeouts = route.getTakeouts();
					TakeoutChromosome c = new TakeoutChromosome();
					c.randomGenerate(takeouts, -1);	
					routesNew.addAll(c.getRoutes());
				}
			}
			
			this.update(routesNew);
		}
		
		//TODO
		String info = this.verify(this.takeoutsTotal);
		if(!info.equals("Success!")) {
			System.out.println("===Mutate===" + "Method:" + method);
			System.out.println("ERROR:" + info);
			//System.exit(-1);
		}
		
		if(takeoutsTotalPre != this.takeoutsTotal.size()) {
			System.out.println("===2===" + "Method:" + method);
			System.exit(-1);
		}
	}
	
	public static List<Point> merge(List<Point> a, List<Point> b) {
		List<Point> c = new ArrayList<Point>();
		
		int i = 0, j = 0;
		for(;i < a.size() && j < b.size();) {
			if(a.get(i).getTime() < b.get(j).getTime()) {
				c.add(a.get(i));
				i++;
			} else {
				c.add(b.get(j));
				j++;
			}
		}
		
		if(i != a.size()) {
			c.addAll(a.subList(i, a.size()));
		}
		
		if(j != b.size()) {
			c.addAll(b.subList(j, b.size()));
		}
		
		//TODO
		if(c.size() != a.size() + b.size()) {
			System.out.println("========================");
			System.exit(-1);
		}
		return c;
	}
	
	public List<Takeout> getTakeoutsTotal() {
		return this.takeoutsTotal;
	}
	
	public String verify(List<Takeout> takeouts) {
		if(takeouts.size() != takeoutsTotal.size()) {
			return new String("ERROR 1: takeouts.size() != takeoutsTotal.size()");
		}
		
		Map<String, Integer> takeoutsMap = new HashMap<String, Integer>();
		
		for(Takeout t : takeouts) {
			takeoutsMap.put(t.getId(), 0);
		}
		
		for(Takeout t : this.takeoutsTotal) {
			if(!takeoutsMap.containsKey(t.getId())) {
				return new String("ERROR 2: !takeoutsMap.containsKey(t.getId())");
			}
			takeoutsMap.put(t.getId(), 1);
		}
		
		for(Map.Entry<String, Integer> entry : takeoutsMap.entrySet()) {
			if(entry.getValue() != 1) {
				return new String("ERROR 3: entry.getValue() != 1");
			}
		}
		
		for(Route route : this.routes) {
			if(route.getShops().size() != route.getSpots().size()) {
				return new String("ERROR 4: route.getShops().size() != route.getSpots().size()");
			}
			
			if(route.getPoints().size() != route.getTakeouts().size()*2) {
				return new String("ERROR 5: route.getPoints().size() != route.getTakeouts().size()*2");
			}
			
			if(route.getTakeouts().size() != route.getShops().size()) {
				System.out.println(route.getPoints().size() + "," + 
						route.getShops().size() + "," + 
						route.getSpots().size() + "," + 
						route.getTakeouts().size());
				return new String("ERROR 6: route.getTakeouts().size() != route.getShops().size()");
			}
			
			Map<String, Integer> takeoutMap =  new HashMap<String, Integer>();
			
			for(Takeout t : route.getTakeouts()) {
				takeoutMap.put(t.getId(), 0);
			}
			
			for(Point p : route.getPoints()) {
				if(!takeoutMap.containsKey(p.getOrderId())) {
					return new String("ERROR 10: !takeoutMap.containsKey(p.getId())");
				}
				takeoutMap.put(p.getOrderId(), 1);
			}
			
			for(Map.Entry<String, Integer> entry : takeoutMap.entrySet()) {
				if(entry.getValue() != 1) {
					return new String("ERROR 7: entry.getValue() != 1");
				}
			}
			
			for(Point p : route.getShops()) {
				if(!takeoutMap.containsKey(p.getOrderId())) {
					return new String("ERROR 11: !takeoutMap.containsKey(p.getId())");
				}
				takeoutMap.put(p.getOrderId(), 2);
			}
			
			for(Map.Entry<String, Integer> entry : takeoutMap.entrySet()) {
				if(entry.getValue() != 2) {
					return new String("ERROR 8: entry.getValue() != 2");
				}
			}
			
			for(Point p : route.getSpots()) {
				if(!takeoutMap.containsKey(p.getOrderId())) {
					return new String("ERROR 12: !takeoutMap.containsKey(p.getId())");
				}
				takeoutMap.put(p.getOrderId(), 3);
			}
			
			for(Map.Entry<String, Integer> entry : takeoutMap.entrySet()) {
				if(entry.getValue() != 3) {
					return new String("ERROR 9: entry.getValue() != 3");
				}
			}
		}
		
		return new String("Success!");
	}
}
