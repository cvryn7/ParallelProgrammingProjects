//******************************************************************************
//
// File: Coordinate.java
// This file is a part of project 2 of the course:Foundation of Parallel Computing,
// undertaken in Fall 2015 at Rochester Institute of Technology. 
//******************************************************************************
import java.io.IOException;

import edu.rit.io.InStream;
import edu.rit.io.OutStream;
import edu.rit.io.Streamable;


/**
 * This class stores points x, y and z forming one coordinate of a corner
 * of a tetrahedron.
 * 
 * @author Karan Bhagat
 * @version 15-Oct-2015
 */
class Coordinate implements Streamable{
	
	//For storing x,y,z coordinates of corner
	private double x;
	private double y;
	private double z;

	/**
	 * No parameter constructor
	 */
	Coordinate(){

	}

	/**
	 * Constructor for initializing x , y and z coordinates
	 */
	Coordinate(double x, double y, double z){
		this.x = x;
		this.y = y;
		this.z = z;
	}

	/**
	 * get x 
	 *
	 * @return x
	 */
	double getX(){
		return x;
	}

	/**
	 * get y 
	 *
	 * @return y
	 */
	double getY(){
		return y;
	}

	/**
	 * get z 
	 *
	 * @return z
	 */
	double getZ(){
		return z;
	}

	/**
	 * set x 
	 *
	 * @param 
	 * 			x coordinate of a corner
	 */
	void setX(double x){
		this.x = x;
	}


	/**
	 * set y 
	 *
	 * @param 
	 * 			y coordinate of a corner
	 */
	void setY(double y){
		this.y = y;
	}


	/**
	 * set z 
	 *
	 * @param 
	 * 			z coordinate of a corner
	 */
	void setZ(double z){
		this.z= z;
	}

	/**
	 * Reads the stream of data which is used for reading object of this
	 * class from tuple space
	 * 
	 * @param in
	 *            Input stream of data
	 */
	public void readIn(InStream in) throws IOException {
		x = in.readDouble();
		y = in.readDouble();
		z = in.readDouble();
	}
	
	/**
	 * writes the stream of data which used for transferring object of this
	 * class over tuple space.
	 * 
	 * @param out
	 *            output stream of data
	 */
	public void writeOut(OutStream out) throws IOException {
		out.writeDouble(x);
		out.writeDouble(y);
		out.writeDouble(z);
	}
}
