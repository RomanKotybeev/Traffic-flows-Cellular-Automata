package cellmodel;

import java.util.ArrayList;
import java.util.Random;

import processing.core.PApplet;
import processing.core.PGraphics;

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


	/* @totalCars is a maximum cars in the grid
	 * @power is one of the parameters of the adaptive algorithm
	 * @treshold is one of the parameters of the adaptive algorithm
	 * @p : PApplet. is necessary for defining width and height of the screen available in PApplet
	 */
	CellularAutomaton(int totalCars, float power, double treshold, PApplet p) {
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
	public void displayCells(PApplet pg) {

		for (int y = 0; y < cells.length; y++) {
			for (int x = 0; x < cells[0].length; x++) {
				if (cells[y][x] == CellType.WALL) {
					pg.fill(125);
				} 
				else if (cells[y][x] == CellType.ROAD) {
					pg.fill(255);
				} 
				else if (cells[y][x] == CellType.TRAFFIC_LIGHT) {
					switch (trafficLightState) {
					case RED:
						pg.fill(255, 0, 0);
						break;
					case GREEN:
						pg.fill(0, 255, 0);
						break;
					case GREEN_TO_RED:
					case RED_TO_GREEN:
						pg.fill(255, 255, 0);
						break;
					}
				} 
				else if (cells[y][x] == CellType.CAR) {
					pg.fill(0, 0, 255);
				} 
				else {
					throw new java.lang.Error("cell[y][x] == ?");
				}
				pg.rect(x * SCALE, y * SCALE, SCALE, SCALE);
			}
		}
	}

	// ¬ €чейках с машиной передвигаем эту машину на speedX/speedY
	public void nextIteration(PApplet p) {
		speedUp();
		speedDown();

		CellType[][] nextCells = initNextCells(p);
		Vector2D[][] nextVelocities = initNextVelocities(p);

		for (int y = 0; y < cells.length; y++) {
			for (int x = 0; x < cells[0].length; x++) {
				if (cells[y][x] == CellType.CAR) {
					int speedY = velocities[y][x].getFirst();
					int speedX = velocities[y][x].getSecond();

					if (speedX == 0 && speedY == 0) {
						timeDelay++;
					}

					if (y + speedY < cells.length && x + speedX < cells[0].length && y + speedY >= 0
							&& x + speedX >= 0) {
						nextCells[y + speedY][x + speedX] = CellType.CAR;
						nextVelocities[y + speedY][x + speedX] = velocities[y][x];
					}
				}
			}
		}

		cells = nextCells;
		velocities = nextVelocities;

		numberOfIterations++;
		checkTrafficLight();
		checkTime();
	}

	public CellType[][] initNextCells(PApplet p) {
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

	public Vector2D[][] initNextVelocities(PApplet p) {
		Vector2D[][] nextVelocities = new Vector2D[p.height / SCALE][p.width / SCALE];
		for (int y = 0; y < cells.length; y++) {
			for (int x = 0; x < cells[0].length; x++) {
				nextVelocities[y][x] = new Vector2D();
			}
		}
		return nextVelocities;
	}

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

	private void adaptiveAlg() {
		double fh = 0;
		double fv = 0;

		int H = cells.length; // высота дороги по Y
		int L = cells[0].length; // длина дороги по X

		for (int x = 0; x < L / 2 - 1; x++) {
			if (cells[H / 2 - 1][x] == CellType.CAR) {
				int distance = (L / 2 - 1) - x;
				fh += Math.pow((float) 1 / distance, power);
			}
		}
		for (int x = L / 2 + 1; x < L; x++) {
			if (cells[H / 2][x] == CellType.CAR) {
				int distance = x - L / 2;
				fh += Math.pow((float) 1 / distance, power);
			}
		}
		// Vertical
		for (int y = 0; y < H / 2 - 1; y++) {
			if (cells[y][L / 2] == CellType.CAR) {
				int distance = (H / 2 - 1) - y;
				fv += Math.pow((float) 1 / distance, power);
			}
		}
		for (int y = H / 2 + 1; y < H; y++) {
			if (cells[y][L / 2 - 1] == CellType.CAR) {
				int distance = y - H / 2;
				fv += Math.pow((float) 1 / distance, power);
			}
		}
		// println("fh = " + fh + ", fv = " + fv);

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

	public void checkTrafficLight() {
		//classicAlg();
		adaptiveAlg();
	}

	public void switchTrafficLight() {
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

	/*
	 * ”скорение машины: ѕробегаемс€ по всему массиву €чеек. ѕровер€етс€ знак
	 * направлени€(directions) по y и по х. ≈сли скорость машины меньше VMAX, то к
	 * скорости добавл€етс€ 1, в случах отрицательного направлени€ сравниваетс€ с
	 * -VMAX, если больше этого значени€, то к скорости доавбл€етс€ -1
	 */
	public void speedUp() {
		for (int y = 0; y < cells.length; y++) {
			for (int x = 0; x < cells[0].length; x++) {

				// int dirY = Integer.signum(velocities[y][x].getFirst());
				// int dirX = Integer.signum(velocities[y][x].getSecond());
				int dirY = directions[y][x].getFirst() * movePermissions[y][x].getFirst();
				int dirX = directions[y][x].getSecond() * movePermissions[y][x].getSecond();
				int absVelocityY = Math.abs(velocities[y][x].getFirst());
				int absVelocityX = Math.abs(velocities[y][x].getSecond());

				if (dirY > 0 && absVelocityY < VMAX) {
					velocities[y][x].setFirst(velocities[y][x].getFirst() + 1);
				} else if (dirY < 0 && absVelocityY < VMAX) {
					velocities[y][x].setFirst(velocities[y][x].getFirst() - 1);
				}

				if (dirX > 0 && absVelocityX < VMAX) {
					velocities[y][x].setSecond(velocities[y][x].getSecond() + 1);
				} else if (dirX < 0 && absVelocityX < VMAX) {
					velocities[y][x].setSecond(velocities[y][x].getSecond() - 1);
				}
			}
		}
	}

	/*
	 * јналогично со speedUp пробегаемс€ по массиву с €чейками ѕровер€етс€ знак
	 * направлени€(directions) и провер€етс€ больше ли скорость, чем дистанци€ до
	 * следующей машины, вызыва€ метод distanceToClosestCar(y,x) (он написан ниже).
	 * ≈сли скорость больше, чем дистанци€ до ближайшей машины, то скорость
	 * приравниваетс€ к этой дистанции. ¬ случа€х отрицательного направлени€
	 * скорость сравниваетс€ с -distanceToClosestCar(y,x), если больше чем это
	 * значение, то скорость приравниваетс€ к -distanceToClosestCar(y,x)
	 * 
	 * ”словием cells[y][x] == CellType.CAR можно пренебречь
	 */
	public void speedDown() {
		for (int y = 0; y < cells.length; y++) {
			for (int x = 0; x < cells[0].length; x++) {
				if (cells[y][x] == CellType.CAR) {
					if (directions[y][x].getFirst() > 0
							&& velocities[y][x].getFirst() > distanceToClosestObstacle(y, x)) {
						velocities[y][x].setFirst(distanceToClosestObstacle(y, x));
					} else if (directions[y][x].getFirst() < 0
							&& velocities[y][x].getFirst() < (-distanceToClosestObstacle(y, x))) {
						velocities[y][x].setFirst(-distanceToClosestObstacle(y, x));
					} else if (directions[y][x].getSecond() > 0
							&& velocities[y][x].getSecond() > distanceToClosestObstacle(y, x)) {
						velocities[y][x].setSecond(distanceToClosestObstacle(y, x));
					} else if (directions[y][x].getSecond() < 0
							&& velocities[y][x].getSecond() < (-distanceToClosestObstacle(y, x))) {
						velocities[y][x].setSecond(-distanceToClosestObstacle(y, x));
					}
				}
			}
		}
	}

	/*
	 * Ќахождение преп€тствий впереди себ€. ѕод преп€тстви€ми понимаетс€ машина или
	 * неразрешимость туда ехать (movePermission) ¬ыход за пределы массива тоже
	 * значит преп€тствие. „тобы смотреть, что впереди, добавл€ем скорость каждый
	 * раз по 1 (или -1) в зависимости от знака. ¬озвращает рассто€ние до
	 * преп€тстви€
	 * 
	 * @carY - машина по Y
	 * @carX - машина по ’ отнсоительно carX and carY находим преп€тствие
	 */
	private int distanceToClosestObstacle(int carY, int carX) {
		int distance = -1;
		int sgnVelocityY = Integer.signum(velocities[carY][carX].getFirst());
		int sgnVelocityX = Integer.signum(velocities[carY][carX].getSecond());

		int y = carY;
		int x = carX;
		boolean obstacleFound = false;
		while (!obstacleFound) {
			y += sgnVelocityY;
			x += sgnVelocityX;
			distance++;

			if (y < 0 || y >= cells.length || x < 0 || x >= cells[0].length) {
				distance = Integer.MAX_VALUE;
				obstacleFound = true;
				break;
			}

			if (sgnVelocityY != 0 && movePermissions[carY][carX].getFirst() != 0
					&& movePermissions[y][x].getFirst() == 0
					|| sgnVelocityX != 0 && movePermissions[carY][carX].getSecond() != 0
							&& movePermissions[y][x].getSecond() == 0) {

				obstacleFound = true;
			}

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

	public void checkTime() {
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
