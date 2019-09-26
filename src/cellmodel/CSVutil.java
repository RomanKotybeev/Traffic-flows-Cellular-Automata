package cellmodel;

import java.io.*;

//A class for writing the adaptive parameters (treshold and power) in a csv file
//There are two methods.

public class CSVutil {

	private FileWriter fw;
	
	private String path = "C:\\Users\\Roman\\Documents\\Processing\\MyFiles\\WithGrid\\NewData\\";

	//OT for overallTime
	private String OTfile = path + "OTtest15(pow:0.8).csv";

	//TD for timeDelay
	private String TDfile = path + "TDtest15(pow:0.8).csv";

	//notice, we have to calculate the maximum amount of cars  
	private int MAX_CARS = 253;

	private final String COMMA = ",";
	private final String NEW_LINE_SEPARATOR = "\n";

	
	//A method writes average time for each amount of cars and the given parameters
	public void csvForOverallTime(GridOfCells gridOfCells) {
		try {
			fw = new FileWriter(OTfile, true);
			if (gridOfCells.totalCars == 1) {
				String title = "p=" + gridOfCells.power + ";k=" + gridOfCells.treshold;
				fw.append(title);
				fw.append(COMMA);
			}
			
			fw.append(String.valueOf(gridOfCells.overallTime));
			fw.append(COMMA);
			if (gridOfCells.totalCars == MAX_CARS) {
				fw.append(NEW_LINE_SEPARATOR);
			}
			fw.flush();
			fw.close();
		} 
		catch (Exception e) {
			System.out.print(e.getMessage());
		}
	}
	
	//A similiar method to write a time delay for each amount of cars and the given parameters
	public void csvForTimeDelay(GridOfCells gridOfCells) {
		try {
			fw = new FileWriter(TDfile, true);
			if (gridOfCells.totalCars == 1) {
				String title = "p=" + gridOfCells.power + ";k=" + gridOfCells.treshold;
				fw.append(title);
				fw.append(COMMA);
			}
			fw.append(String.valueOf(gridOfCells.timeDelay));
			fw.append(COMMA);
			if (gridOfCells.totalCars == MAX_CARS) {
				fw.append(NEW_LINE_SEPARATOR);
			}
			fw.flush();
			fw.close();
		} 
		catch (Exception e) {
			System.out.print(e.getMessage());
		}
	}
	

}