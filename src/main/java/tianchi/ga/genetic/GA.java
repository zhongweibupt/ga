/**
 * 
 */
package tianchi.ga.genetic;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import tianchi.ga.controller.node.Node;
import tianchi.ga.controller.node.Point;
import tianchi.ga.controller.node.Site;
import tianchi.ga.controller.route.Route;
import tianchi.ga.utils.Utils;

/**
* <p>Title: GA.java</p>
* <p>Description: </p>
* <p>Copyright: Copyright (c) 2007</p>
* <p>Company: Zhongwei</p>
* @author Zhongwei
* @date 2016年8月15日
* @version 1.0
*/
public class GA {
	private List<Point> points;
	private int maxMenNum;
	
	private Chromosome winner;
	private Population population;
	
	private int bestFitness;
	private int sumFitness;
	private double threshold;

	private int realFitness;
	private Chromosome realWinner;
	
	private int level;

	private static final int iter = 100;
	
	public GA() {
		this.points = new ArrayList<Point>();
		this.maxMenNum = 0;
		
		this.winner = new Chromosome();
		this.population = new Population();
		
		this.bestFitness = Utils.MAX_VALUE;
		this.sumFitness = Utils.MAX_VALUE;
		
		this.realFitness = Utils.MAX_VALUE;
		this.realWinner = new Chromosome();

		this.threshold = 3;
		this.level = 0;
	}
	
	public void run(int level, int size, int maxMenNum, Site site) throws IOException {
		this.maxMenNum = maxMenNum;

		List<Point> points = new ArrayList<Point>();
		for(Node node : site.getNodes()) {
			Point point = new Point(node, Utils.getOrderId(node), Utils.MAX_VALUE, node.getWeight());
			points.add(point);
		}
		
		this.population.init(size, this.maxMenNum, points);
		
		this.sumFitness = this.population.getFitness();
		this.winner = this.population.getWinner();
		this.bestFitness = this.winner.getFitness();
		this.realFitness = this.bestFitness;

		int count = 1;

		while(count <= points.size()*iter ) {
			this.population = this.population.reproduce(level);

			this.sumFitness = this.population.getFitness();
			this.winner = this.population.getWinner();
			this.bestFitness = this.winner.getFitness();

			if(this.bestFitness <= this.realFitness) {
				this.realFitness = this.bestFitness;
				this.realWinner = this.winner;
			}

			count++;

			//TODO
			System.out.println(count + "," + site.getId() + ":" + this.bestFitness);
			//for(Route route : this.winner.getRoutes()) {
				//System.out.println(route.getWeightSum() + ":" + route.getDist() + ":" +route.getRoute());
			//}
			//System.out.println(this.bestFitness + ":" + this.winner.getRouteTotal());
			//System.out.println(this.winner.getBreakPoints());
		}


		String file = "data/populations/" + site.getId() + ".csv";
		FileWriter fw = new FileWriter(file);
		StringBuffer info = new StringBuffer();

		for(Chromosome c : this.population.getPopulation()) {
			for(Point p : c.getRouteTotal()) {
				info.append(p.getId() + ",");
			}
			info.append(":" + c.getBreakPoints() + "\r\n");
		}
		fw.write(info.toString());
		fw.flush();
		fw.close();	
	}
	
	
/*
	public static void main(String[] args) throws IOException {
		//TODO
		int level = 2;
		int size = 256;
		int men = 100;

		GA ga = new GA();
		List<Node> nodesList = new ArrayList<Node>();
		Utils utils = new Utils();

		double result = 0;
		int sumMen = 0;

		//for(Map.Entry<String, Site> entry: utils.getMap().getSites().entrySet())
		{
			//Site start = entry.getValue();
			
			Site start = utils.getMap().getSites().get("A073");
			
			StringBuffer info = new StringBuffer();
			
			ga.run(level, size, men, start);

			int i = 0;
			for(Route route : ga.realWinner.getRoutes()) {
				i++;
				info.append(i + ":" + route.getWeight() + "-" + route.getDist() + "-" + route.getRoute() + "\r\n");
			}

			String file = "data/winners/" + start.getId() + "-" + (int)ga.realFitness + "-" + ga.realWinner.getRoutes().size() + ".csv";

			FileWriter fw = new FileWriter(file);
			fw.write(info.toString());
			fw.flush();
			fw.close();

			result += ga.realFitness;
			sumMen += ga.realWinner.getRoutes().size();
		}

		String file = "data/winners/" + "Result" + ".csv";

		FileWriter fw = new FileWriter(file);
		StringBuffer info = new StringBuffer();
		info.append("Result:" + result + "    " + "Men:" + sumMen + "Level:" + level + "    " + "Size:" + size + "    " + "MaxMen:" + men + "\r\n");
	
		fw.write(ga.toString());
		fw.flush();
		fw.close();
	}*/


}
