//******************************************************************************
//
// File:    MetricCenterGpu.java
// This file is a part of project 3 of the course:Foundation of Parallel Computing,
// under taken in Fall 2015 at Rochester Institute of Technology. 
//******************************************************************************

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.ByteBuffer;
import java.util.InputMismatchException;
import java.util.NoSuchElementException;
import java.util.Scanner;

import edu.rit.gpu.CacheConfig;
import edu.rit.gpu.Gpu;
import edu.rit.gpu.GpuStructArray;
import edu.rit.gpu.GpuStructVbl;
import edu.rit.gpu.Kernel;
import edu.rit.gpu.Module;
import edu.rit.gpu.Struct;
import edu.rit.pj2.Task;
import edu.rit.util.AList;

/**
 * Class MetricCenterGpu is GPU parallel program for finding metric center from a given
 * set of points. Metric center is the point with smallest radius such that circle with
 * that point as center covers enclose all other given points.
 * 
 * <P>
 * 
 * Usage: <TT>java pj2 MetricCenterGpu <I>fileName</I>
 * <BR><TT><I>fileName</I></TT> = input file containing point coordinates <BR>
 * <P>
 * 
 * @author Karan Bhagat
 * @version 6-Nov-2015
 */

public class MetricCenterGpu extends Task {

	//Structure for a point with x,y coordinates
	private static class Point extends Struct{
		public double x;
		public double y;

		//Constructor for intializing point with x,y.
		public Point(double x, double y){
			this.x = x;
			this.y = y;
		}

		//Returns no. of bytes required by C struct
		public static long sizeof(){
			return 16;
		}

		//For writing java object of this class as C struct
		public void toStruct(ByteBuffer buf){
			buf.putDouble(x);
			buf.putDouble(y);
		}

		//For reading C struct as java object
		public void fromStruct(ByteBuffer buf){
			x = buf.getDouble();
			y = buf.getDouble();
		}
	}

	//Structure for storing reduced metric center point's radius and index
	private static class ReducedRadius extends Struct{
		public double radius;
		public int index;

		//Constructor for intializing object with radius and index.
		public ReducedRadius(double radius, int index){
			this.radius = radius;
			this.index = index;
		}

		//For reading C struct as java object
		public void fromStruct(ByteBuffer buf) {
			radius = buf.getDouble();
			index = buf.getInt();
		}

		//For writing java object of this class as C struct
		public void toStruct(ByteBuffer buf) {
			buf.putDouble(radius);
			buf.putInt(index);
		}

		//Returns no. of bytes required by C struct
		public static long sizeof(){
			return 16;
		}
		
		/*
		 * Save the min radius along with index
		 * 
		 * @param a 
		 * 		ReducedRadius object to compare with
		 */
		public void saveMinReducedRadius(ReducedRadius a){
			if( a.radius < radius){
				radius = a.radius;
				index = a.index;
			}else if( a.radius == radius){
				if(a.index < index){
					index = a.index;					
				}
			}
		}
	}

	/**
	 * Kernel function interface for calculating 
	 * metric center in a thread in GPU
	 */
	private static interface MetricCenter extends Kernel{
		public void findMetricCenter( GpuStructArray<Point> points, int size, GpuStructArray<ReducedRadius> minOfMaxRadius);
	}

	/**
	 * Task main program
	 */
	public void main(String[] args) throws Exception {

		//no. of commandline arguments
		if( args.length != 1){
			exceptionCall(1);
		}

		//Declaring File object for fetching file input
		File dataFile = null;

		//For reading the file input
		Scanner sc = null;

		try{
			dataFile = new File(args[0]);
			sc = new Scanner(dataFile);
		}catch(NullPointerException e){
			exceptionCall(2);
		}catch(FileNotFoundException e){
			exceptionCall(2);
		}

		//List for storing points from file
		AList<Point> pointList = new AList<Point>();

		Point tempPoint = null;

		//reading from file 
		try{
			while(sc.hasNext()){
				tempPoint = new Point(sc.nextDouble(),sc.nextDouble());
				pointList.addLast(tempPoint);
			}
		}catch(InputMismatchException e){
			exceptionCall(4);
		}catch(NoSuchElementException e){
			exceptionCall(3);
		}

		if( pointList.size() < 2) exceptionCall(3);
		
		//Initialize GPU
		Gpu gpu = Gpu.gpu();
		gpu.ensureComputeCapability( 2, 0);

		int noOfMultiprocessor = gpu.getMultiprocessorCount();

		//Setting up GPU variables
		Module module = gpu.getModule("MetricCenterGpu.cubin");
		GpuStructArray<Point> points = gpu.getStructArray(Point.class, pointList.size());
		pointList.toArray(points.item);
		GpuStructArray<ReducedRadius> reducedR = gpu.getStructArray(ReducedRadius.class, noOfMultiprocessor);

		//Initialize reducedR array
		for(int i = 0; i < reducedR.length(); i++){
			reducedR.item[i] = new ReducedRadius(Double.MAX_VALUE,0);
		}

		//Setting up GPU kernel
		MetricCenter kernel = module.getKernel(MetricCenter.class);
		kernel.setBlockDim(1024);
		kernel.setGridDim(noOfMultiprocessor);
		kernel.setCacheConfig (CacheConfig.CU_FUNC_CACHE_PREFER_L1);

		//Copying from CPU to GPU
		points.hostToDev();
		reducedR.hostToDev();

		//Calling kernel function
		kernel.findMetricCenter(points, points.length(), reducedR);

		//Copying from GPU to CPU
		reducedR.devToHost();

		//Final reduction for finding point with metric center
		ReducedRadius finalRadius = new ReducedRadius(Double.MAX_VALUE,0);
		for(int i = 0; i < reducedR.length(); i++){
			finalRadius.saveMinReducedRadius(reducedR.item[i]);
		}

		int index = finalRadius.index;
		System.out.printf("%d (%.5g,%.5g)\n",index,points.item[index].x,points.item[index].y);
		System.out.printf("%.5g\n",finalRadius.radius);
	}


	/**
	 * Throws exception according to the input parameter
	 *
	 * @param e
	 *            Defines type message and exception to throw.
	 *
	 * @exception IllegalArgumentException
	 *                (unchecked exception) Thrown if command arguments are
	 *                incorrect.
	 */
	public static void exceptionCall(int e) {

		switch (e) {
		case 1:
			System.err.println("Program require commandline arguments");
			throw new IllegalArgumentException();
		case 2:
			System.err.println("No file found!");
			throw new IllegalArgumentException();
		case 3:
			System.err.println("Incomplete File");
			throw new IllegalArgumentException();
		case 4:
			System.err.println("Illegal Data in File");
			throw new IllegalArgumentException();
		}
	}

	/**
	 * For setting number of cores required by this task
	 */
	protected static int coresRequired()
	{
		return 1;
	}

	/**
	 * For setting number of GPUs required by this task
	 */
	protected static int gpusRequired()
	{
		return 1;
	}
}
