package cellmodel;
/*
 * A class defines two coordinates.
 * It will help to use as velocities, directions, etc
 */

public class Vector2D {
	// first is y-coordinate
	// second is x-coordinate
	int first;
	int second;

	Vector2D() {
		first = 0;
		second = 0;
	}

	Vector2D(int first, int second) {
		this.first = first;
		this.second = second;
	}

	public int getFirst() {
		return first;
	}

	public int getSecond() {
		return second;
	}

	public void setSecond(int second) {
		this.second = second;
	}

	public void setFirst(int first) {
		this.first = first;
	}

}
