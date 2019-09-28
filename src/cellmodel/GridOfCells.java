package cellmodel;
import processing.core.PApplet;

/* GridOfCells
 * An application displaying a traffic model based on cellular automata
 * The traffic flow is represented as a cross road and a traffic light
 * @author Roman Kotyubeev.
 * Date: June 10, 2019
 */

public class GridOfCells extends PApplet {

	//ca is for using CellularAutomaton methods
	private CellularAutomaton ca;
	
	//csvutil is for writing results in a csv file 
	private CSVutil csvutil = new CSVutil();

	//start point
	protected int totalCars = 50;
	private int seriesCount = 0;
	
	//parameters of the adaptive algorithm
	public float power = 0.8f;
	public  double treshold = 5;
	
	//how many experiments for a given amount of cars to measure overallTime and timeDelay
	private final int NUM_EXP = 10;
	
	//overallTime is number of iterations when there are no cars in the grid
	//timeDelay is number of iterations when a car don't move. It will count for every car
	public  double overallTime;
	public  double timeDelay;

	
	
	//in processing 3 needs to be in the main class. 
	//Another way is to add an argument "package.class" in the run configuration 
	public static void main(String[] args) {
        PApplet.main("cellmodel.GridOfCells");
    }
	
	//It needs to be here in new version of processing. 
	//The size method must be here not in the setup method 
	public void settings() {
		size(640, 640);
	}
	
	//setup method will be initialized just one time
	public void setup() {
		//frameRate is for speed of updating the grid
		frameRate(1);
		ca = new CellularAutomaton(totalCars, power, treshold, this);
	}

	//draw method will be always run until you don't stop by hand
	public void draw() {
		//When there is more than available cars on the road it needs to throw an exception
		//In catch I change the parameters of adaptive algorithm and make the new grid with 1 car
		try {
			if (!ca.stopped) {
				ca.nextIteration();
				ca.displayCells();
			} 
			else if (seriesCount < NUM_EXP) {
				seriesCount++;
				overallTime += ca.overallTime;
				timeDelay += ca.timeDelay;
				ca = new CellularAutomaton(totalCars, power, treshold, this);
			} 
			else {
				//display in console
				println();
				overallTime /= NUM_EXP;
				timeDelay /= NUM_EXP;

				println(" !!! TOTAL CARS = " + totalCars);
				println("mean=" + overallTime);
				println("timeDelay=" + timeDelay);
				
				//Write the data in a csv file
				//csvutil.csvForOverallTime(this);
				//csvutil.csvForTimeDelay(this);
				
				//increase amount of cars. Update the data
				totalCars++;
				overallTime = 0;
				timeDelay = 0;
				seriesCount = 0;
				
				//Make a new grid with more cars (increased by one)
				ca = new CellularAutomaton(totalCars, power, treshold, this);
			}
		} 
		
		//if amount of cars becomes more than available on the road, throw an exception
		//Here, make a new grid with new parameters
		catch (Exception e) {
			println();
			println("!!! NEW GENERATION !!!");
			totalCars = 0;
			seriesCount = 0;

			if (power < 2) {
				power += 0.5;
			} 
			else {
				treshold += 10;
				power = 0.5f;
			}

			println("t=" + treshold + ", p=" + power);
			ca = new CellularAutomaton(totalCars, power, treshold, this);
		}
	}
}
