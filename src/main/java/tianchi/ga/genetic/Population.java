/**
 * 
 */
package tianchi.ga.genetic;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import tianchi.ga.controller.node.Point;
import tianchi.ga.controller.route.Route;
import tianchi.ga.controller.route.Takeout;
import tianchi.ga.utils.Utils;

/**
* <p>Title: Population.java</p>
* <p>Description: </p>
* <p>Copyright: Copyright (c) 2007</p>
* <p>Company: Zhongwei</p>
* @author Zhongwei
* @date 2016年8月15日
* @version 1.0
*/
public class Population {
	public static final int sizeOfGroup = 8;
	
	private static final String METHOD_FIRST = "MPMX";//Modified Partially Matching Crossover, 脨猫脪陋碌梅脮没露脧碌茫
	private static final String METHOD_SECOND = "CX";//Cycle Crossover, 虏禄脨猫脪陋碌梅脮没露脧碌茫
	private static final String METHOD_THIRD = "MOX";//Modified Order Crossover, 脨猫脪陋碌梅脮没露脧碌茫
	
	protected int fitness;
	
	protected int size;
	private int maxMenNum;
	
	protected List<Chromosome> population;
	protected Chromosome winner;
	
	public Population() {
		this.winner = new Chromosome();
		this.fitness = Utils.MAX_VALUE;
		this.size = 0;
		this.maxMenNum = 0;
		this.population = new ArrayList<Chromosome>();
	}
	
	public void init(int size, int maxMenNum, List<Point> nodesList) {
		this.population.clear();
		this.fitness = Utils.MAX_VALUE;
		
		if(0 == (size%8)) {
			this.size = size;
			for(int i = 0; i < size; i++) {
				Chromosome chromosome = new Chromosome();
				chromosome.randomGenerate(maxMenNum, nodesList);
				
				this.population.add(chromosome);
				this.fitness += chromosome.getFitness();
			}
		} else {
			throw new IllegalArgumentException("Size must be divided by 8.");
		}
	}
	
	public void update(Population population, List<Chromosome> chromosomes) {
		this.maxMenNum = population.getMaxMenNum();
		this.size = chromosomes.size();
		this.population = chromosomes;
		
		for(Chromosome chromosome : this.population) {
			this.fitness += chromosome.getFitness();
		}
	}
	
	public Population reproduce(int level) throws IllegalArgumentException {
		Population offspringPopu = new Population();
		List<Chromosome> chromosomes = new ArrayList<Chromosome>();
		
		for(int i = 0; i < this.size; i += Population.sizeOfGroup) {
			Chromosome parent1 = select();
			Chromosome parent2 = select();
			
			while(parent2.equals(parent1) && parent2 == parent1) {
				parent2 = select();
			}

			List<Chromosome> offsprings1 = 
					Population.cross(parent1, parent2, METHOD_FIRST);
			
			Chromosome offspring1 = offsprings1.get(0);
			Chromosome offspring2 = offsprings1.get(1);

			offspring1.mutate(level);
			offspring2.mutate(level);

			chromosomes.add(offspring1);
			chromosomes.add(offspring2);
			
			List<Chromosome> offsprings2 = 
					Population.cross(parent1, parent2, METHOD_SECOND);
			
			Chromosome offspring3 = offsprings2.get(0);
			Chromosome offspring4 = offsprings2.get(1);

			offspring3.mutate(level);
			offspring4.mutate(level);
			chromosomes.add(offspring3);
			chromosomes.add(offspring4);
			
			List<Chromosome> offsprings3 = 
					Population.cross(parent1, parent2, METHOD_THIRD);
			
			Chromosome offspring5 = offsprings3.get(0);
			Chromosome offspring6 = offsprings3.get(1);
			
			offspring5.mutate(level);
			offspring6.mutate(level);
			chromosomes.add(offspring5);
			chromosomes.add(offspring6);
			
			Chromosome offspring7 = new Chromosome();
			Chromosome offspring8 = new Chromosome();
			
			offspring7.update(parent1.getRouteTotal(),  parent1.getBreakPoints());
			offspring8.update(parent2.getRouteTotal(),  parent2.getBreakPoints());
			
			offspring7.mutate(level);
			offspring8.mutate(level);
			chromosomes.add(offspring7);
			chromosomes.add(offspring8);
			
			//TODO
			/*
			System.out.println("==========After==========");
			for(Route route : parent1.getRoutes()) {
				System.out.println(route.getRoute());
			}
			System.out.println(parent1.getRouteTotal());
			System.out.println(parent1.getBreakPoints());
			*/
		}
		offspringPopu.update(this, chromosomes);
		return offspringPopu;
	}

	public int getMaxMenNum() {
		return this.maxMenNum;
	}
	
	public int getFitness() {
		return this.fitness;
	}
	
	public Chromosome getWinner() {
		this.winner = this.population.get(0);
		for(Chromosome c : this.population) {
			this.winner = compareFitness(c, this.winner);
		}
		return this.winner;
	}
	
	public static Chromosome compareFitness(Chromosome a, Chromosome b) {
		return a.getFitness() < b.getFitness() ? a : b;
	}
	
	public static List<Point> lcs(List<Point> a, List<Point> b) {//Longest Common Subsequence, LCS
		//TODO : 加入循环和倒序
		List<Point> result = new ArrayList<Point>();

		int[][] L = new int[a.size() + 1][b.size() + 1];
		for(int m = 0; m <= a.size(); m++) {
			for(int n = 0; n <= b.size(); n++) {
				L[m][n] = (int)0;
			}
		}
		
		for(int m = 1; m <= a.size(); m++) {
			for(int n = 1; n <= b.size(); n++) {
				if(a.get(m - 1) == b.get(n - 1))
					L[m][n] = L[m-1][n-1] + 1;
				else
					L[m][n] = Math.max(L[m][n-1], L[m-1][n]);
				
			}
		}
		
		int i = 0;
		int j = 0;
		for(i = a.size(), j = b.size(); i > 0 && j > 0;)
		{
			if(a.get(i - 1).getId().equals(b.get(j - 1).getId())) {
				result.add(0, a.get(i - 1));
				i--;
				j--;
				
			} else {
				if(L[i][j-1] > L[i-1][j])
					j--;
				else
					i--;
			}
		}
		
		return result;
	}

	protected Chromosome select() {
		Chromosome individual = new Chromosome();
		Random rand = new Random();
		
		for(int i = 0; i < Population.sizeOfGroup; i++) {
			int randNum = rand.nextInt(this.population.size());
			Chromosome c = this.population.get(randNum);
			individual = compareFitness(individual, c);
		}
		return individual;
	}
	
	public static List<Chromosome> cross(Chromosome pparent1, Chromosome pparent2, String method) {
		Chromosome offspring1 = new Chromosome();
		Chromosome offspring2 = new Chromosome();
		
		Chromosome parent1 = new Chromosome();
		Chromosome parent2 = new Chromosome();
		
		parent1.update(pparent1.getRouteTotal(), pparent1.getBreakPoints());
		parent2.update(pparent2.getRouteTotal(), pparent2.getBreakPoints());
		
		List<Chromosome> offsprings = new ArrayList<Chromosome>();
		
		if(method.equals("MPMX")) {//Modified Partially Matching Crossover
			//Init
			List<Point> routeTotal1 = new ArrayList<Point>();
			List<Integer> breakPointsList1 = new ArrayList<Integer>();
			
			List<Point> routeTotal2 = new ArrayList<Point>();
			List<Integer> breakPointsList2 = new ArrayList<Integer>();

			Random random = new Random();

			int randFront = random.nextInt(parent1.getRouteTotal().size());
			int randRear = random.nextInt(parent1.getRouteTotal().size());
			
			while(randFront == randRear || Math.abs(randFront - randRear) == parent1.getRouteTotal().size() - 1) {
				randRear = random.nextInt(parent1.getRouteTotal().size());
			}
			
			int tmp;
			tmp = Math.max(randFront, randRear);
			randRear = Math.max(randFront, randRear);
			randFront = tmp;
			
			List<Point> nodeIdSequence1 = new ArrayList<Point>(parent1.getRouteTotal().subList(randFront, randRear));
			List<Point> nodeIdSequence2 = new ArrayList<Point>(parent2.getRouteTotal().subList(randFront, randRear));
			
			List<Point> nodes = new ArrayList<Point>();

			//Generate offspring1
			nodes = new ArrayList<Point>(nodeIdSequence1);
			nodes.removeAll(nodeIdSequence2);

			breakPointsList1 = parent1.getBreakPoints();
			routeTotal1 = new ArrayList<Point>(parent1.getRouteTotal());

			for(int i = 0; i < parent1.getRouteTotal().size(); i++) {
				if(i >= randFront && i < randRear) {
					routeTotal1.add(nodeIdSequence1.get(i - randFront));
				} else {
					if(nodeIdSequence2.contains(parent1.getRouteTotal().get(i))) {
						int randNum = random.nextInt(nodes.size());
						routeTotal1.add(nodes.get(randNum));
						nodes.remove(randNum);
					}
				}
			}

			offspring1.update(routeTotal1, breakPointsList1);
					
			//Generate offspring2
			nodes = new ArrayList<Point>(nodeIdSequence2);
			nodes.removeAll(nodeIdSequence1);

			breakPointsList2 = parent2.getBreakPoints();
			routeTotal2 = new ArrayList<Point>(parent2.getRouteTotal());
			
			for(int i = 0; i < parent2.getRouteTotal().size(); i++) {
				if(i >= randFront && i < randRear) {
					routeTotal1.add(nodeIdSequence2.get(i - randFront));
				} else {
					if(nodeIdSequence1.contains(parent2.getRouteTotal().get(i))) {
						int randNum = random.nextInt(nodes.size());
						routeTotal2.add(nodes.get(randNum));
						nodes.remove(randNum);
					}
				}
			}

			offspring2.update(routeTotal2, breakPointsList2);

		} else if (method.equals("CX")) {//Cycle Crossover, 虏禄脨猫脪陋碌梅脮没露脧碌茫
			//Init
			List<Route> routes = new ArrayList<Route>();
			List<Point> routeTotal = new ArrayList<Point>();
			List<Integer> breakPointsList = new ArrayList<Integer>();
			
			List<Point> sequence = new ArrayList<Point>();
			List<Point> otherNodes = new ArrayList<Point>();
			
			Random random = new Random();
			
			//Generate offspring1
			for(Route route1 : parent1.getRoutes()) {
				int maxMatches = 0;
				for(Route route2 : parent2.getRoutes()) {
					List<Point> tmp = lcs(route1.getPoints(), route2.getPoints());
					if(tmp.size() >= maxMatches) {
						sequence = tmp;
						maxMatches = tmp.size();
					}
				}
				routeTotal.addAll(sequence);
				breakPointsList.add(routeTotal.size());

				Route route = new Route();
				route.update(sequence);
				routes.add(route);
			}
			
			otherNodes = new ArrayList<Point>(parent1.getRouteTotal());
			otherNodes.removeAll(routeTotal);

			for(int i = 0; i < routes.size(); i++) {
				Route route1 = parent1.getRoutes().get(i);
				Route route = routes.get(i);

				List<Point> nodes = new ArrayList<Point>(route.getPoints());
				for(; route1.getPoints().size() - nodes.size() > 0;) {
					int randNum = nodes.size() == 0 ? 0 : random.nextInt(nodes.size() + 1);
					nodes.add(randNum, otherNodes.get(otherNodes.size() - 1));
					otherNodes.remove(otherNodes.size() - 1);
				}
				route.update(nodes);
				routes.set(i, route);
			}
			
			offspring1.update(routes);

			//Generate offspring2
			routeTotal.clear();
			breakPointsList.clear();
			
			List<Route> routes2 = new ArrayList<Route>() ;
			
			for(Route route2 : parent2.getRoutes()) {
				int maxMatches = 0;
				for(Route route1 : parent1.getRoutes()) {
					List<Point> tmp = lcs(route1.getPoints(), route2.getPoints());
					if(tmp.size() >= maxMatches) {
						sequence = tmp;
						maxMatches = tmp.size();
					}
				}
				routeTotal.addAll(sequence);
				breakPointsList.add(routeTotal.size());

				Route route = new Route();
				route.update(sequence);
				routes2.add(route);
			}
			
			otherNodes = new ArrayList<Point>(parent2.getRouteTotal());
			otherNodes.removeAll(routeTotal);

			for(int i = 0; i < routes2.size(); i++) {
				Route route2 = parent2.getRoutes().get(i);
				Route route = routes2.get(i);

				List<Point> nodes = new ArrayList<Point>(route.getPoints());

				for(; route2.getPoints().size() - nodes.size() > 0;) {
					int randNum = nodes.size() == 0 ? 0 : random.nextInt(nodes.size() + 1);
					nodes.add(randNum, otherNodes.get(otherNodes.size() - 1));
					otherNodes.remove(otherNodes.size() - 1);
				}
				route.update(nodes);
				routes2.set(i, route);
			}
			
			offspring2.update(routes2);
			
		} else if (method.equals("MOX")) {//Modified Order Crossover
			//Init
			List<Point> routeTotal1 = new ArrayList<Point>();
			List<Integer> breakPointsList1 = new ArrayList<Integer>();
			
			List<Point> routeTotal2 = new ArrayList<Point>();
			List<Integer> breakPointsList2 = new ArrayList<Integer>();
			
			Random random = new Random();
			int randFront = random.nextInt(parent1.getRouteTotal().size() - 1);
			int randRear = random.nextInt(parent1.getRouteTotal().size() - 1);
			
			while(randFront == randRear) {
				randRear = random.nextInt(parent1.getRouteTotal().size() - 1);
			}
			
			int tmp;
			tmp = Math.max(randFront, randRear);
			randRear = Math.max(randFront, randRear);
			randFront = tmp;
			
			List<Point> nodeIdSequence1 = new ArrayList<Point>(parent1.getRouteTotal().subList(randFront, randRear));
			List<Point> nodeIdSequence2 = new ArrayList<Point>(parent2.getRouteTotal().subList(randFront, randRear));
			
			List<Point> otherNodes1 = new ArrayList<Point>(parent1.getRouteTotal());
			List<Point> otherNodes2 = new ArrayList<Point>(parent2.getRouteTotal());
			
			otherNodes1.removeAll(nodeIdSequence2);
			otherNodes2.removeAll(nodeIdSequence1);
			
			//Generate offspring1
			breakPointsList1 = parent1.getBreakPoints();
			for(int i = 0; i < parent1.getRouteTotal().size(); i++) {
				if(i >= randFront && i < randRear) {
					routeTotal1.add(nodeIdSequence1.get(i - randFront));
				} else {
					int randNum = random.nextInt(otherNodes1.size());
					routeTotal1.add(otherNodes1.get(randNum));
					otherNodes1.remove(randNum);
				}
			}
			
			offspring1.update(routeTotal1, breakPointsList1);
			
			//Generate offspring2
			breakPointsList2 = parent2.getBreakPoints();
			for(int i = 0; i < parent2.getRouteTotal().size(); i++) {
				if(i >= randFront && i < randRear) {
					routeTotal2.add(nodeIdSequence2.get(i - randFront));
				} else {
					int randNum = random.nextInt(otherNodes2.size());
					routeTotal2.add(otherNodes2.get(randNum));
					otherNodes2.remove(randNum);
				}
			}
			
			offspring2.update(routeTotal2, breakPointsList2);
		}
		
		offspring1.regulate();//根据MaxWeight修正路径，如果存在超过MaxWeight的路径则进行分裂
		offspring2.regulate();

		offsprings.add(offspring1);
		offsprings.add(offspring2);
		
		return offsprings;
	}

	public List<Chromosome> getPopulation() {
		return this.population;
	}
}
