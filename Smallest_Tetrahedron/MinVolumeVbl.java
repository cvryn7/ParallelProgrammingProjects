//******************************************************************************
//
// File:    MinVolumeVbl.java
// This file is a part of project 2 of the course:Foundation of Parallel Computing,
// under taken in Fall 2015 at Rochester Institute of Technology. 
//******************************************************************************
import java.io.IOException;

import edu.rit.io.InStream;
import edu.rit.io.OutStream;
import edu.rit.pj2.Tuple;
import edu.rit.pj2.Vbl;


/**
 * Class MinVolumeVbl is a reduction class which stores minimum volume of a
 * tetrahedron and also store indexes of the minimum volume tetrahedron. This 
 * class has method to find volume of tetradhedron as well.
 * 
 * @author Karan Bhagat
 * @version 15-Oct-2015
 */
class MinVolumeVbl extends Tuple implements Vbl{

	//Indexes of a tetrahedron with minimum volume.
	private int index1;
	private int index2;
	private int index3;
	private int index4;
	
	//volume of minimum volume tetrahedron
	private double volume;

	/**
	 * Constructor for initializing volume to Max value
	 */
	public MinVolumeVbl(){
		volume = Double.MAX_VALUE;
	}


	/**
	 * Make a clone of this reduction variable
	 *
	 * @return clone reduction variable.
	 */
	public Object clone(){
		MinVolumeVbl vbl = (MinVolumeVbl) super.clone();
		vbl.index1 = index1;
		vbl.index2 = index2;
		vbl.index3 = index3;
		vbl.index4 = index4;
		vbl.volume = volume;
		return vbl;
	}

	/**
	 * This method reduces provided reduction variable with this 
	 * variable object by check whose volume is lesser.
	 * 
	 * @param vbl
	 *            reference of another reduction variable.
	 */
	public void reduce(Vbl vbl){
		MinVolumeVbl newVbl = (MinVolumeVbl)vbl;
		
		//Check if parameter vbl is having lesser volume
		saveIfMin(newVbl.index1, newVbl.index2, newVbl.index3, newVbl.index4, newVbl.volume);
	}

	/**
	 * Sets the indexes and volume of this reduction variable to
	 * provided parameter reduction variable.
	 *
	 * @param vbl
	 *            reference of another reduction variable.
	 *
	 */
	public void set(Vbl vbl){
		MinVolumeVbl newVbl = (MinVolumeVbl) vbl;
		index1 = newVbl.index1;
		index2 = newVbl.index2;
		index3 = newVbl.index3;
		index4 = newVbl.index4;
		this.volume = newVbl.volume;
	}

	/**
	 * Checks and store if volume as parameter is less than volume
	 * store in this reduction variable
	 *
	 * @param i1
	 *            First index of a coordinate of tetrahedron.
	 * @param i2
	 *            Second index of a coordinate of tetrahedron.          
	 * @param i3
	 *            Third index of a coordinate of tetrahedron.
	 * @param i4
	 *            Fourth index of a coordinate of tetrahedron.
	 * @param volume
	 *            Volume of the tetrahedron define by given indexes.            	
	 */
	public void saveIfMin(int i1, int i2, int i3, int i4, double volume){
		if( this.volume > volume){
			this.volume = volume;
			index1 = i1;
			index2 = i2;
			index3 = i3;
			index4 = i4;
		}
	}

	/**
	 * For fetching index of a coordinate of minimum volume
	 * tetrahedron
	 *
	 * @param i
	 *            Index number which is request.
	 * @return index of the requested coordinate.
	 * 			            	
	 */
	int getIndexOf(int i){
		switch(i){
		case 1:
			return index1;
		case 2:
			return index2;
		case 3:
			return index3;
		case 4:
			return index4;
		default:
			return -1;
		}
	}
	
	/**
	 * Fetching volume of the minimum volume tetrahedron
	 *
	 * @return volume.
	 * 			            	
	 */
	public double getMinVolume(){
		return this.volume;
	}

	/**
	 * Calculates volume of tetrahedron define by parameter coordinates
	 *
	 * @param x1
	 *            x coordinate of first corner.
	 * @param y1
	 *            y coordinate of first corner.
	 * @param z1
	 *            z coordinate of first corner.            
	 * @param x2
	 *            x coordinate of second corner.
	 * @param y2
	 *            y coordinate of second corner.
	 * @param z2
	 *            z coordinate of second corner.
	 * @param x3
	 *            x coordinate of third corner.
	 * @param y3
	 *            y coordinate of third corner.
	 * @param z3
	 *            z coordinate of third corner.            
	 * @param x4
	 *            x coordinate of fourth corner.
	 * @param y4
	 *            y coordinate of fourth corner.
	 * @param z4
	 *            z coordinate of fourth corner.
	 *            
	 * @return volume.
	 * 			            	
	 */
	double findVolume(double x1, double y1, double z1,
			double x2, double y2, double z2,
			double x3, double y3, double z3,
			double x4, double y4, double z4){

		double volume;

		//formula for finding volume of tetrahedron
		volume = Math.abs((x1*( y2*(z3-z4)-y3*(z2-z4)+y4*(z2-z3))-
				x2*( y1*(z3-z4)-y3*(z1-z4)+y4*(z1-z3))+
				x3*( y1*(z2-z4)-y2*(z1-z4)+y4*(z1-z2))-
				x4*( y1*(z2-z3)-y2*(z1-z3)+y3*(z1-z2)))/6);
		return volume;
	}

	/**
	 * Reads the stream of data which is used for reading object of this
	 * class from tuple space
	 * 
	 * @param in
	 *            Input stream of data
	 */
	public void readIn(InStream in) throws IOException {
		index1 = in.readInt();
		index2 = in.readInt();
		index3 = in.readInt();
		index4 = in.readInt();
		volume = in.readDouble();
	}

	/**
	 * writes the stream of data which used for transferring object of this
	 * class over tuple space.
	 * 
	 * @param out
	 *            output stream of data
	 */
	public void writeOut(OutStream out) throws IOException {
		out.writeInt(index1);
		out.writeInt(index2);
		out.writeInt(index3);
		out.writeInt(index4);
		out.writeDouble(volume);
	}
}
