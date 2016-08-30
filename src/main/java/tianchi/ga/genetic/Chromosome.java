/**
 * 
 */
package tianchi.ga.genetic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import tianchi.ga.controller.node.Node;
import tianchi.ga.controller.node.Point;
import tianchi.ga.controller.route.Route;
import tianchi.ga.controller.route.Takeout;
import tianchi.ga.utils.Utils;

/**
* <p>Title: Chromosome.java</p>
* <p>Description: </p>
* <p>Copyright: Copyright (c) 2007</p>
* <p>Company: Zhongwei</p>
* @author Zhongwei
* @date 2016年8月15日
* @version 1.0
*/
public class Chromosome {
	private List<Point> routeTotal;								//不包含start/end
	private List<Integer> breakPointsList;							//不包含最后一个点
	
	protected List<Route> routes;
	protected int numOfMen;
	protected int sumOfDist;
	
	public Chromosome() {
		routeTotal = new ArrayList<Point>();
		breakPointsList = new ArrayList<Integer>();
		
		routes = new ArrayList<Route>();
		numOfMen = 0;
		sumOfDist = Utils.MAX_VALUE*100;
	}
	
	public void update(List<Point> routeTotal, List<Integer> breakPointsList) {
		this.routeTotal = routeTotal;
		this.breakPointsList = breakPointsList;
		
		this.routes.clear();
		this.sumOfDist = 0;
		
		int pre = 0;

		for(int breakPoint : this.breakPointsList) {
			Route route = new Route();
			List<Point> sequence = this.routeTotal.subList(pre, breakPoint);
			route.update(sequence);
			this.sumOfDist += route.getDist();
			this.routes.add(route);
			pre = breakPoint;
		}
		
		Route route = new Route();
		List<Point> sequence = this.routeTotal.subList(pre, this.routeTotal.size());
		
		if(sequence.size() == 0) {
			System.out.println(breakPointsList + "," + this.routeTotal.size());
		}
		route.update(sequence);
		this.sumOfDist += route.getDist();
		this.routes.add(route);

		this.numOfMen = this.routes.size();
	}
	
	public void update(List<Route> routes) {
		this.routes = new ArrayList<Route>();
		for(Route route : routes) {
			Route routeNew = new Route();
			routeNew.update(route.getPoints());
			
			this.routes.add(routeNew);
		}
		
		this.sumOfDist = 0;
		
		this.routeTotal = new ArrayList<Point>();
		this.breakPointsList = new ArrayList<Integer>();
		
		int rear = 0;
		for(Route route : routes) {
			this.routeTotal.addAll(route.getPoints());
			rear += route.getPoints().size();
			this.breakPointsList.add(rear);
			this.sumOfDist += route.getDist();
			
			//TODO
			if(this.sumOfDist < 0) {
				System.out.println(this.sumOfDist + "," + route.getStart().getId() + "," + route.getEnd().getId() + "," + route.getDist() + "," + route.getRoute());
				System.exit(-1);
			}
		}
		this.breakPointsList.remove(routes.size() - 1);
		if(this.sumOfDist == 0) {
			this.sumOfDist = Utils.MAX_VALUE;
		}
		this.numOfMen = this.routes.size();
	}
	
	public void primGenerate(int sizeOfBreakPoints, List<Point> pointsList) {
		//TODO : 用最小生成树生成染色体
	}
	
	public void randomGenerate(int sizeOfBreakPoints, List<Point> pointsList) {
		List<Route> routes = new ArrayList<Route>();
		List<Point> routeTotal = new ArrayList<Point>(pointsList);
		
		Collections.shuffle(routeTotal);

		for(Point point : routeTotal) {
			boolean success = false;
			for(int j = 0; j < routes.size(); j++) {
				Route route = routes.get(j);
				if(route.getWeight() + point.getWeight() <= Utils.MAX_WEIGHT) {
					List<Point> points = route.getPoints();
					points.add(point);
					route.update(points);
					routes.set(j, route);
					success = true;
					break;
				}
			}
			if(false == success) {
				Route route = new Route();
				List<Point> points = new ArrayList<Point>();
				points.add(point);
				route.update(points);
				routes.add(route);
			}
		}

		this.update(routes);
	}
	
	public void mutate(int level) {
		int method = gaussian(level);
		
		if(3 == method) {											//增加或减少一个快递员，增加通过分裂某个路径实现，减少通过合并两个路径实现
			List<Route> routes = this.routes;
			List<Integer> candidates = new ArrayList<Integer>();
			Random random = new Random();

			int action = random.nextInt(2);

			if(0 == action) {										//增加一个快递员，从长度大于1的路径中随机选取一个进行随机分裂
				int i = 0;
				for(Route route : routes) {
					if(1 < route.getLength()) {
						candidates.add(i);
					}
					i++;
				}

				if(0 == candidates.size()) {
					//Do nothing
				} else {
					int randIndex = random.nextInt(candidates.size());
					Route route = routes.get(candidates.get(randIndex));
					
					int randBreak = random.nextInt(route.getLength() - 1) + 1;

					Route a = new Route();
					Route b = new Route();

					a.update(route.getPoints().subList(0, randBreak));
					b.update(route.getPoints().subList(randBreak, route.getLength()));

					routes.remove(route);
					routes.add(a);
					routes.add(b);

					this.update(routes);
				}
			} else {												//减少一个快递员，合并两个路径，并使新路径满足负重不超过MaxWeight
				int randIndex = random.nextInt(routes.size());
				Route a = routes.get(randIndex);

				int i = 0;
				for(Route route : routes) {
					if(Utils.MAX_WEIGHT >= route.getWeight() + a.getWeight() && i != randIndex) {
						candidates.add(i);
					}
					i++;
				}

				if(0 == candidates.size()) {
					//Do nothing
				} else {
					int randIndexAnother = random.nextInt(candidates.size());
					Route b = routes.get(candidates.get(randIndexAnother));

					Route route = new Route();
					List<Point> sequence = new ArrayList<Point>(a.getPoints());
					sequence.addAll(b.getPoints());
					route.update(sequence);

					routes.remove(a);
					routes.remove(b);
					routes.add(route);

					this.update(routes);
				}
			}

		} else if(2 == method) {									//Inversion Mutation, IVM
			List<Route> routes = this.routes;
			
			Random random = new Random();
			int randIndex= random.nextInt(routes.size());

			Route route = routes.get(randIndex);
			int rand1 = random.nextInt(route.getPoints().size());
			int rand2 = random.nextInt(route.getPoints().size());
			
			while(rand1 == rand2) {
				if(1 == route.getPoints().size())
					break;
				rand2 = random.nextInt(route.getPoints().size());
			}

			int randFront = Math.min(rand1, rand2);
			int randRear = Math.max(rand1, rand2);

			route.update(reverse(route.getPoints(), randFront, randRear));
			
			routes.set(randIndex, route);
			
			this.update(routes);
		} else if(1 == method) {									//Modified Displacement Mutation, MDM
			Random random = new Random();
			List<Route> routes = new ArrayList<Route>();
			for(Route route : this.routes) {
				Route tmp = new Route();
				tmp.update(route.getPoints());
				routes.add(tmp);
			}
			
			int randNum1 = random.nextInt(routes.size());
			Route route = routes.get(randNum1);
			
			int n = 0;
			while(route.getPoints().size() == 1) {
				randNum1 = random.nextInt(routes.size());
				route = routes.get(randNum1);
				if(n > 20) {
					return;
				}
				n++;
			}
			
			int rand1 = random.nextInt(route.getPoints().size());
			int rand2 = random.nextInt(route.getPoints().size());
			
			while(Math.abs(rand2 - rand1) == route.getPoints().size() - 1) {
				rand2 = random.nextInt(route.getPoints().size());
			}
			
			int randFront = Math.min(rand1, rand2);
			int randRear = Math.max(rand1, rand2);
	
			List<Point> routeNew = new ArrayList<Point>(route.getPoints());
			List<Point> routePart = new ArrayList<Point>(route.getPoints().subList(randFront, randRear));

			routeNew.removeAll(routePart);
			route.update(routeNew);
			routes.set(randNum1, route);

			int randNum2 = random.nextInt(routes.size());
			
			Route routeOther = routes.get(randNum2);
			List<Point> routeNew2 = new ArrayList<Point>(routeOther.getPoints());
			int randIndex = random.nextInt(routeNew2.size());

			routeNew2.addAll(randIndex, routePart);
			routeOther.update(routeNew2);
			routes.set(randNum2, routeOther);
			
			if(Utils.MAX_WEIGHT < routeOther.getWeight()) {
				//Do nothing
			} else {
				this.update(routes);
			}
		} else if(0 == method) {
			//TODO
			//Do nothing
		}
		this.regulate();
	}
		
	public void regulate() {										//根据MaxWeight修正路径，如果存在超过MaxWeight的路径则进行分裂
		for(int i = 0; i < this.routes.size(); i++) {
			Route route = this.routes.get(i);
			if(Utils.MAX_WEIGHT < route.getWeight() || Utils.MAX_DIST < route.getDist()) {
				this.routes.remove(route);
				divide(route);
			}
		}
		this.update(routes);
	}
	
	private void divide(Route route) {
		Route a = new Route();
		Route b = new Route();

		List<Point> sequence1 = new ArrayList<Point>();
		List<Point> sequence2 = new ArrayList<Point>(route.getPoints());

		int weight = 0;
		int dist = 0;
		
		//Node pre = route.getStart();
		
		//TODO 可以用arriveTime
		int i = 0;
		for(Point point : route.getPoints()) {
			weight += point.getWeight();
			//dist += Utils.computeDist(pre, point)
				//	+ Utils.computeDist(point, route.getEnd())
					//+ (int)Math.round(3*Math.sqrt(point.getWeight()) + 5);
			dist = route.getLeaveTimes().get(i) + Utils.computeDist(point, route.getEnd());
			
			if(Utils.MAX_WEIGHT < weight || Utils.MAX_DIST < dist) {
				break;
			}
			sequence1.add(point);
			i++;
		}

		sequence2.removeAll(sequence1);
		//TODO
		//if(sequence2.size() == 0) {
			//System.out.println(route.getDist() + "," + dist + "," + sequence1.size() + "," + sequence2.size());
			//System.exit(-1);
		//}

		a.update(sequence1);
		b.update(sequence2);

		this.routes.add(a);

		if((Utils.MAX_WEIGHT < b.getWeight() || Utils.MAX_DIST < b.getDist())
			&& b.getLength() > 1) {
			divide(b);
		} else {
			this.routes.add(b);
		}

	}
	
	public static List<Point> reverse(List<Point> route, int randFront, int randRear) {
		List<Point> newRoute = new ArrayList<Point>(route);
		
		for(int i = randFront, j = randRear; i < j; i++, j--) {
			Point tmp = newRoute.get(i);
			newRoute.set(i, newRoute.get(j));
			newRoute.set(j, tmp);
		}
		
		return newRoute;
	}
	
	public static int gaussian(int level) {
		Random random = new Random();
		double result = random.nextGaussian() + level + 1;
		if(result < 0) {
			result = 0;
		} else if(result > 4) {
			result = 3;
		}
		return (int)result;
	}
	
	public int getMenNumber(){
		return this.numOfMen;
	}
	
	public int getFitness(){
		return this.sumOfDist;
	}
	
	public List<Route> getRoutes(){
		return this.routes;
	}
	
	public List<Point> getRouteTotal(){
		return this.routeTotal;
	}
	
	public List<Integer> getBreakPoints() {
		return this.breakPointsList;
	}
}
