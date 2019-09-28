package cellmodel;

import java.util.ArrayList;
import java.util.Random;
import processing.core.PApplet;


/* CellularAutomaton
 * This project is for building a traffic flow model based on cellular automata.
 * 
 * nextIteration(), speedUp(), speedDown(), distanceToClosestObstacle(int, int) are methods
 * implementing the Nagel-Schrekenberg model 
 * 
 * There are the classic algorithm and adaptive algorithm (made by me) of controlling the traffic light.
 * The classic algorithm switches the traffic light states after given time (number of iterations)
 * The adaptive algorithm decides when to switch the traffic light state based on the weights in the adaptiveAlg() method
 * You can choose what algorithm to use by commenting one of them in checkTrafficLight() method
 * 
 * @author Roman K.
 * Date: June 10, 2019
 */

public class CellularAutomaton {

	//SCALE is a length of a car
	//VMAX is maximum speed of a car
	//TIME_ORANGE_TRAFFIC_LIGHT is an orange time interval  
	//TIME_RED_OR_GREEN_TRAFFIC_LIGHT is a red and a green time interval (for classic algorithm). 
	private final int SCALE = 10;
	private final int VMAX = 4;
	private final int TIME_ORANGE_TRAFFIC_LIGHT = 2;
	//This variable is only for the classic algorithm
	private final int TIME_RED_OR_GREEN_TRAFFIC_LIGHT = 5;

	//Define a grid by cell types (enum) with properties as directions, velocities, movePermissions.
	//directions, velocities, movePermissions are necessary to cars
	private CellType[][] cells;
	private Vector2D[][] directions;
	private Vector2D[][] velocities;
	private Vector2D[][] movePermissions;

	//start state of the traffic light is RED (enum) 
	private TrafficLightStates trafficLightState = TrafficLightStates.RED;
	
	//every single update of the grid is one iteration. This variable counts iterations
	private int numberOfIterations = 0;

	//overallTime is number of iterations when there are no cars in the grid
	//timeDelay is number of iterations when a car don't move. It will count for every car
	protected int overallTime;
	protected int timeDelay;

	//the parameters of the adaptive algorithm
	private float power;
	private double treshold;

	//stopped is need for declaring the new grid
	protected boolean stopped = false;
	
	//max cars in the grid
	private int totalCars;

	//to use processing library tools
	private PApplet p;

	/* @totalCars is a maximum cars in the grid
	 * @power is one of the parameters of the adaptive algorithm
	 * @treshold is one of the parameters of the adaptive algorithm
	 * @p : PApplet is necessary for defining width and height of the screen available in PApplet
	 */
	CellularAutomaton(int totalCars, float power, double treshold, PApplet p) {
		this.p = p;
		
		int height = p.height;
		int width = p.width;
		
		cells = new CellType[height / SCALE][width / SCALE];
		velocities = new Vector2D[height / SCALE][width / SCALE];
		directions = new Vector2D[height / SCALE][width / SCALE];
		movePermissions = new Vector2D[height / SCALE][width / SCALE];
		this.totalCars = totalCars;
		this.power = power;
		this.treshold = treshold;
		timeDelay = 0;
		overallTime = 0;
		filling();
	}


	//A method contains methods for filling the grid
	public void filling() {
		fillByWall();
		fillByRoadHorizontal();
		fillByRoadVertical();
		fillByTrafficLight();
		fillByCars();
	}
	
	//fill the grid by the WALL(enum)
	public void fillByWall() {
		for (int y = 0; y < cells.length; y++) {
			for (int x = 0; x < cells[0].length; x++) {
				cells[y][x] = CellType.WALL;
				directions[y][x] = new Vector2D();
				velocities[y][x] = new Vector2D();
				movePermissions[y][x] = new Vector2D();
			}
		}
	}

	// define an array by road in the horizontal way
	public void fillByRoadHorizontal() {
		for (int y = cells.length / 2 - 1; y < cells.length / 2 + 1; y++) {
			for (int x = 0; x < cells[0].length; x++) {
				cells[y][x] = CellType.ROAD;

				if (y < cells.length / 2) {
					directions[y][x].setSecond(1);
				} else {
					directions[y][x].setSecond(-1);
				}
				movePermissions[y][x] = new Vector2D(0, 1);
			}
		}
	}
	
	// define an array by road in the horizontal way
	public void fillByRoadVertical() {
		for (int y = 0; y < cells.length; y++) {
			for (int x = cells[0].length / 2 - 1; x < cells[0].length / 2 + 1; x++) {
				cells[y][x] = CellType.ROAD;

				if (x < cells.length / 2) {
					directions[y][x].setFirst(-1);
				} else {
					directions[y][x].setFirst(1);
				}
				movePermissions[y][x] = new Vector2D(1, 0);
			}
		}
	}

	//fill the traffic light near the crossing
	public void fillByTrafficLight() {
		for (int y = cells.length / 2 - 3; y < cells.length / 2 - 1; y++) {
			for (int x = cells[0].length / 2 - 3; x < cells[0].length / 2 - 1; x++) {
				cells[y][x] = CellType.TRAFFIC_LIGHT;
			}
		}
	}
	
	//Add maximum cars (total cars) to the grid at random places. 
	public void fillByCars() {
		int carsPlaced = 0;
		Random rand = new Random();
		
		//To implement this I used a list of Vector2D(class), where a cell is the ROAD. 
		ArrayList<Vector2D> roadCells = new ArrayList<Vector2D>();
		for (int y = 0; y < cells.length; y++) {
			for (int x = 0; x < cells[0].length; x++) {
				if (cells[y][x] == CellType.ROAD) {
					roadCells.add(new Vector2D(y, x));
				}
			}
		}
		
		//Randomly get an index (from 0 to size of the list). A car will be with this index
		//To not add a car in the same place remove an object(Vector2D) from a list
		//The sign of velocity is defined by multiplication by direction
		while (carsPlaced < totalCars) {
			int index = rand.nextInt(roadCells.size());
			int y = roadCells.get(index).getFirst();
			int x = roadCells.get(index).getSecond();
			roadCells.remove(index);

			cells[y][x] = CellType.CAR;

			// (rand.nextInt(3) + 1) method gives random number from 0 to VMAX
			int speed = (rand.nextInt(VMAX) + 1);
			velocities[y][x].setFirst(speed * directions[y][x].getFirst());
			velocities[y][x].setSecond(speed * directions[y][x].getSecond());

			carsPlaced++;
		}
	}
	
	//Draw cells with processing library
	public void displayCells() {

		for (int y = 0; y < cells.length; y++) {
			for (int x = 0; x < cells[0].length; x++) {
				if (cells[y][x] == CellType.WALL) {
					p.fill(125);
				} 
				else if (cells[y][x] == CellType.ROAD) {
					p.fill(255);
				} 
				else if (cells[y][x] == CellType.TRAFFIC_LIGHT) {
					switch (trafficLightState) {
					case RED:
						p.fill(255, 0, 0);
						break;
					case GREEN:
						p.fill(0, 255, 0);
						break;
					case GREEN_TO_RED:
					case RED_TO_GREEN:
						p.fill(255, 255, 0);
						break;
					}
				} 
				else if (cells[y][x] == CellType.CAR) {
					p.fill(0, 0, 255);
				} 
				else {
					throw new java.lang.Error("cell[y][x] == ?");
				}
				p.rect(x * SCALE, y * SCALE, SCALE, SCALE);
			}
		}
	}

	// This method and all methods which this method calls use Nagel-Schrekenberg model
	// This method creates an updated grid of cells. Updating is based on rules of NS-model
	public void nextIteration() {
		
		//A car speeds down if there is another car in front of it or the traffic light forbids to move further  
		//A car speeds up if there is no obstacle in front of it
		speedUp();
		speedDown();

		//Initialization a grid of cells without any car to not save previous states
		CellType[][] nextCells = initNextCells();
		Vector2D[][] nextVelocities = initNextVelocities();

		//change positions of cars according to their velocities
		//write new positions and new velocities of cars to the updated grid of cells  
		for (int y = 0; y < cells.length; y++) {
			for (int x = 0; x < cells[0].length; x++) {
				if (cells[y][x] == CellType.CAR) {
					int speedY = velocities[y][x].getFirst();
					int speedX = velocities[y][x].getSecond();

					//for scientific purpose let's count every car which doesn't move
					if (speedX == 0 && speedY == 0) {
						timeDelay++;
					}
					
					// dont't forget about bounds when car move beyond the screen
					if ( (y + speedY) < cells.length && (x + speedX) < cells[0].length 
							&& (y + speedY) >= 0 && (x + speedX) >= 0 ) {
						//write new positions and new velocities of cars to the updated grid of cells
						nextCells[y + speedY][x + speedX] = CellType.CAR;
						nextVelocities[y + speedY][x + speedX] = velocities[y][x];
					}
				}
			}
		}
		//this updated grid becomes the present grid
		cells = nextCells;
		velocities = nextVelocities;

		//Iteration is each update of the grid of cells. We count it
		numberOfIterations++;
		
		//adaptive algorithm or classic algorithm for controlling the traffic light
		chooseTrafficLightMode();
		
		//check if there is no cars
		checkCarsAndTime();
	}

	//Initialization a new grid without any car
	private CellType[][] initNextCells() {
		CellType[][] nextCells = new CellType[p.height / SCALE][p.width / SCALE];
		for (int y = 0; y < cells.length; y++) {
			for (int x = 0; x < cells[0].length; x++) {
				switch (cells[y][x]) {
					case CAR:
						nextCells[y][x] = CellType.ROAD;
						break;
					default:
						nextCells[y][x] = cells[y][x];
				}
			}
		}
		return nextCells;
	}
	
	//Initialization a new velocities with zero values
	private Vector2D[][] initNextVelocities() {
		Vector2D[][] nextVelocities = new Vector2D[p.height / SCALE][p.width / SCALE];
		for (int y = 0; y < cells.length; y++) {
			for (int x = 0; x < cells[0].length; x++) {
				nextVelocities[y][x] = new Vector2D();
			}
		}
		return nextVelocities;
	}

	//Classic algorithm switch the traffic light state after the definite time interval
	private void classicAlg() {
		switch (trafficLightState) {
		case RED:
		case GREEN:
			if (numberOfIterations >= TIME_RED_OR_GREEN_TRAFFIC_LIGHT) {
				switchTrafficLight();
				numberOfIterations = 0;
			}
			break;

		case RED_TO_GREEN:
		case GREEN_TO_RED:
			if (numberOfIterations >= TIME_ORANGE_TRAFFIC_LIGHT) {
				switchTrafficLight();
				numberOfIterations = 0;
			}
			break;
		}
	}

	// adaptive algorithm changes the traffic light state according to the weights fh and fv
	private void adaptiveAlg() {
		//the weights
		double fh = 0;
		double fv = 0;

		//height and width
		int H = cells.length; 
		int L = cells[0].length; 

		//Horizontal
		//from left to the center
		for (int x = 0; x < L / 2 - 1; x++) {
			if (cells[H / 2 - 1][x] == CellType.CAR) {
				int distance = (L / 2 - 1) - x;
				//here we calculate the weight along the horizontal line
				fh += Math.pow((float) 1 / distance, power);
			}
		}
		//from tight to the center
		for (int x = L / 2 + 1; x < L; x++) {
			if (cells[H / 2][x] == CellType.CAR) {
				int distance = x - L / 2;
				fh += Math.pow((float) 1 / distance, power);
			}
		}
		// Vertical
		//from top to the center
		for (int y = 0; y < H / 2 - 1; y++) {
			if (cells[y][L / 2] == CellType.CAR) {
				int distance = (H / 2 - 1) - y;
				fv += Math.pow((float) 1 / distance, power);
			}
		}
		//from bottom to the center
		for (int y = H / 2 + 1; y < H; y++) {
			if (cells[y][L / 2 - 1] == CellType.CAR) {
				int distance = y - H / 2;
				//here we calculate the weight along the vertical line
				fv += Math.pow((float) 1 / distance, power);
			}
		}
	
		//change the traffic light state according to relations between fh and fv 
		switch (trafficLightState) {
		case RED:
			if (fh / fv > treshold) {
				switchTrafficLight();
				numberOfIterations = 0;
			}
			break;
		case GREEN:
			if (fv / fh > treshold) {
				switchTrafficLight();
				numberOfIterations = 0;
			}
			break;

		case RED_TO_GREEN:
		case GREEN_TO_RED:
			if (numberOfIterations >= TIME_ORANGE_TRAFFIC_LIGHT) {
				switchTrafficLight();
				numberOfIterations = 0;
			}
			break;
		}
	}

	//choose what kind of algorithm of the traffic light control you want to try
	public void chooseTrafficLightMode() {
		//classicAlg();
		adaptiveAlg();
	}

	//Change move permissions with changing the traffic light state 
	private void switchTrafficLight() {
		int yPermission = 0;
		int xPermission = 0;

		switch (trafficLightState) {
		case RED:
			trafficLightState = TrafficLightStates.RED_TO_GREEN;
			break;
		case RED_TO_GREEN:
			trafficLightState = TrafficLightStates.GREEN;
			xPermission = 1;
			break;
		case GREEN:
			trafficLightState = TrafficLightStates.GREEN_TO_RED;
			break;
		case GREEN_TO_RED:
			trafficLightState = TrafficLightStates.RED;
			yPermission = 1;
			break;
		}

		for (int y = cells.length / 2 - 1; y < cells.length / 2 + 1; y++) {
			for (int x = cells[0].length / 2 - 1; x < cells[0].length / 2 + 1; x++) {
				movePermissions[y][x] = new Vector2D(yPermission, xPermission);
			}
		}
	}

	// if the velocit of a car is less VMAX, the car speeds up
	private void speedUp() {
		for (int y = 0; y < cells.length; y++) {
			for (int x = 0; x < cells[0].length; x++) {

				//multiplication by movePermissions allows to move through the center
				int dirY = directions[y][x].getFirst() * movePermissions[y][x].getFirst();
				int dirX = directions[y][x].getSecond() * movePermissions[y][x].getSecond();
				
				//to not make it difficult, let's get the absolute velocity
				int absVelocityY = Math.abs(velocities[y][x].getFirst());
				int absVelocityX = Math.abs(velocities[y][x].getSecond());

				//along Y-axis (vertical)
				if (dirY > 0 && absVelocityY < VMAX) {
					velocities[y][x].setFirst(velocities[y][x].getFirst() + 1);
				} 
				else if (dirY < 0 && absVelocityY < VMAX) {
					velocities[y][x].setFirst(velocities[y][x].getFirst() - 1);
				}
				else {
					throw new java.lang.Error("What the value of dirY?");
				}
				
				//along X-axis (horizontal)
				if (dirX > 0 && absVelocityX < VMAX) {
					velocities[y][x].setSecond(velocities[y][x].getSecond() + 1);
				} 
				else if (dirX < 0 && absVelocityX < VMAX) {
					velocities[y][x].setSecond(velocities[y][x].getSecond() - 1);
				}
				else {
					throw new java.lang.Error("What the value of dirX?");
				}
			}
		}
	}

	//if there is an obstacle in front of a car, the car speeds down
	private void speedDown() {
		for (int y = 0; y < cells.length; y++) {
			for (int x = 0; x < cells[0].length; x++) {
				if (cells[y][x] == CellType.CAR) {
					
					if (directions[y][x].getFirst() > 0
							&& velocities[y][x].getFirst() > distanceToClosestObstacle(y, x)) {
						
						velocities[y][x].setFirst(distanceToClosestObstacle(y, x));
					} 
					else if (directions[y][x].getFirst() < 0
							&& velocities[y][x].getFirst() < (-distanceToClosestObstacle(y, x))) {
						
						velocities[y][x].setFirst(-distanceToClosestObstacle(y, x));
					} 
					else if (directions[y][x].getSecond() > 0
							&& velocities[y][x].getSecond() > distanceToClosestObstacle(y, x)) {
						
						velocities[y][x].setSecond(distanceToClosestObstacle(y, x));
					} 
					else if (directions[y][x].getSecond() < 0
							&& velocities[y][x].getSecond() < (-distanceToClosestObstacle(y, x))) {
						
						velocities[y][x].setSecond(-distanceToClosestObstacle(y, x));
					}
					else {
						throw new java.lang.Error("speedDown?");
					}
				}
			}
		}
	}
	
	
	/*
	 * Finding the distance to an obstacle in front of a car. 
	 * An obstacle can be another car, a wall, the bounds of the screen, prohibition to move further (movePermision == 0).
	 * @carY and @carX are the coordinate of the car
	 * 
	 */
	private int distanceToClosestObstacle(int carY, int carX) {
		//anyway we enter the loop, that's why -1
		int distance = -1;
		
		//the sign of velocity
		//if the value is positive, it returns 1,
		//negative, it returns -1
		//0, it returns 0
		int sgnVelocityY = Integer.signum(velocities[carY][carX].getFirst());
		int sgnVelocityX = Integer.signum(velocities[carY][carX].getSecond());

		//coordinates which will be used to find the closest obstacle
		int y = carY;
		int x = carX;
		
		//a flag
		boolean obstacleFound = false;
		while (!obstacleFound) {
			y += sgnVelocityY;
			x += sgnVelocityX;
			distance++;

			//the bounds
			if (y < 0 || y >= cells.length || x < 0 || x >= cells[0].length) {
				distance = Integer.MAX_VALUE;
				obstacleFound = true;
				break;
			}

			//the center is prohibited to cross
			if (sgnVelocityY != 0 && movePermissions[carY][carX].getFirst() != 0
					&& movePermissions[y][x].getFirst() == 0
					|| sgnVelocityX != 0 && movePermissions[carY][carX].getSecond() != 0
							&& movePermissions[y][x].getSecond() == 0) {

				obstacleFound = true;
			}

			//ROAD is not an obstacle, but WALL and CAR are obstacles
			switch (cells[y][x]) {
				case ROAD:
					break;
				default:
					obstacleFound = true;
					break;
			}
		}

		return distance;
	}

	//if there in no cars on the grid, stopped = true.
	//It helps to initialize a new grid with a different numbers of cars
	public void checkCarsAndTime() {
		//number of iterations required to get rid of cars on the grid
		overallTime++;

		boolean thereAreCars = false;
		for (int y = 0; y < cells.length; y++) {
			for (int x = 0; x < cells[0].length; x++) {
				if (cells[y][x] == CellType.CAR) {
					thereAreCars = true;
					break;
				}
			}

			if (thereAreCars) {
				break;
			}
		}

		if (!thereAreCars) {
			stopped = true;
		}
	}

}
