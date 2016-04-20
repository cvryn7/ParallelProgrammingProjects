//******************************************************************************
//
// File:    TetraClu.java
// This file is a part of project 2 of the course:Foundation of Parallel Computing,
// under taken in Fall 2015 at Rochester Institute of Technology. 
//******************************************************************************


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.NoSuchElementException;
import java.util.Scanner;

import edu.rit.io.InStream;
import edu.rit.io.OutStream;
import edu.rit.io.Streamable;
import edu.rit.pj2.Job;
import edu.rit.pj2.Loop;
import edu.rit.pj2.ObjectLoop;
import edu.rit.pj2.Task;
import edu.rit.pj2.Tuple;
import edu.rit.pj2.Vbl;
import edu.rit.pj2.WorkQueue;
import edu.rit.pj2.tuple.ObjectArrayTuple;
import edu.rit.util.AList;


/**
 * Class TetraClu is cluster parallel program for finding tetrahedron with minimum
 * volume from a very large set of tetradehrons.
 * 
 * <P>
 * 
 * Usage: <TT>java pj2 [workers=<I>K</I>] TetraClu <I>fileName</I>
 * <TT><I>K</I><TT> = Number of workers
 * <BR><TT><I>fileName</I></TT> = input file containing coordinates <BR>
 * <P>
 * The program uses class MinVolumeVbl for final reduction of minimum volume, for finding
 * find volume and also for storing indexes of coordinates of minimum vol3ume tetrahedron.
 *
 * @author Karan Bhagat
 * @version 15-Oct-2015
 */
public class TetraClu extends Job {

	//Array of type Coordinate for storing points in
	//input file
	static Coordinate[] coordinates;
	
	//Job Main program
	public void main(String[] args) throws Exception {
		
		//Checking of invalid number of command line arguments
		if( args.length != 1) exceptionCall(1);
		
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
		
		//Dynamic list for storing coordinates from file
		AList<Coordinate> coordinateList = new AList<Coordinate>();
		
		//Fetching coordinates from input file and storing in array
		int arraySize = 0;
		int j = 0;
		try{
			while(sc.hasNext()){
				coordinateList.add(arraySize, new Coordinate(sc.nextDouble(),sc.nextDouble(),sc.nextDouble()));
				arraySize++;
			}
		}catch(InputMismatchException e){
			exceptionCall(4);
		}catch(NoSuchElementException e){
			exceptionCall(3);
		}

		
		if( arraySize < 4) exceptionCall(3);
		
		sc.close();

		coordinates = new Coordinate[arraySize];
		
		//transferring coordinates for dynamic list to static array. 
		coordinateList.toArray(coordinates);
		
		//Putting coordinates array in tuple space for worker tasks.
		putTuple(new ObjectArrayTuple<Coordinate>(coordinates));

		//Getting the number of worker tasks
		int K = workers();
		if( K == DEFAULT_WORKERS) K = 1;
		
		//Initializing masterFor loop with TetraWorkerTask. 
		masterSchedule(proportional);
		masterChunk(10);
		masterFor(0, arraySize-4, TetraWorkerTask.class).args(""+arraySize);
		
		//Declaring finish rule
		rule().atFinish().task(ReduceTask.class).args().runInJobProcess();
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
	 * Class TetraWorkerTask Compute minimum volume tetrahedron where first
	 * coordinate of the tetrahedron is provided by masterFor and workerTask
	 * traverse on coordinate array for other three coordinates to find volume
	 * of a tetrahedron. This class also reduces volumes to find minimum volume
	 * and then put the minimum volume found and its corresponding tetrahedron 
	 * indexes into tuple space.
	 * 
	 * @author Karan Bhagat
	 * @version 15-Oct-2015
	 */
	private static class TetraWorkerTask extends Task{

		//Task main program
		public void main(String[] args) throws Exception {
			if( args.length != 1){};

			//Reduction variable for in-worker final reduction 
			MinVolumeVbl minVolume = new MinVolumeVbl();

			//Size of the coordinate array
			int size = Integer.parseInt(args[0]);
			
			//fetching coordinate array from tuple space
			Coordinate[] coordinates = readTuple(new ObjectArrayTuple<Coordinate>()).item;

			//Loop on all the indexes provided by masterFor
			//in parallel.
			workerFor().schedule(proportional).exec(new Loop(){
				
				//Per thread reduction variable
				MinVolumeVbl thrdMinVolume;
				
				//For storing new volume calculated
				double newVolume;

				//Initializing Per thread variables
				public void start(){
					thrdMinVolume = threadLocal(minVolume);
				}

				public void run(int i) throws Exception {

					//for given one coordinate 'i' find other three coordinates
					//and calculate volume of new tetrahedron.
					for(int j = i+1; j < size-2; j++){
						for( int m = j+1; m < size-1; m++){
							for( int n = m+1; n < size; n++){
								//finding volume of tetrahedron defined by i,j,m and n.
								newVolume = thrdMinVolume.findVolume(coordinates[i].getX(),coordinates[i].getY(),coordinates[i].getZ(),
										coordinates[j].getX(),coordinates[j].getY(),coordinates[j].getZ(),
										coordinates[m].getX(),coordinates[m].getY(),coordinates[m].getZ(),
										coordinates[n].getX(),coordinates[n].getY(),coordinates[n].getZ());
								//checking and storing if volume is minimum.
								thrdMinVolume.saveIfMin(i,j,m,n, newVolume);
							}
						}
					}	
				}
			});

			//putting reduction variable in tuple space
			//for reduce task.
			putTuple(minVolume);
		}

	}

	/**
	 * ReduceTask class takes final minimum volume tetrahedron found
	 * by each worker task and then reduce it to find the final tetrahedron
	 * with minimum volume.
	 *  
	 * @author Karan Bhagat
	 * @version 15-Oct-2015
	 */
	private static class ReduceTask extends Task{

		//ReduceTask main program
		public void main(String[] arg0) throws Exception {
			
			//Reduction variable for storing final min volume and tetrahedron indexes
			MinVolumeVbl finalVolumeVbl = new MinVolumeVbl();
			
			//declaring template reduction variable for fetching tuple
			MinVolumeVbl template = new MinVolumeVbl();
			
			MinVolumeVbl taskVbl;
			
			//Fetching each tuple from worker task one by one and reducing
			//it to the finalVolumeVbl
			while( ( taskVbl = tryToTakeTuple(template)) != null){
				finalVolumeVbl.reduce(taskVbl);
			}
			int index;
			double x,y,z;
			double volume;
			
			//Printing the final results
			for( int i = 0; i < 4; i++){
				index = finalVolumeVbl.getIndexOf(i+1);
				x = coordinates[index].getX();
				y = coordinates[index].getY();
				z = coordinates[index].getZ();
				System.out.printf("%d (%.5g,%.5g,%.5g)\n",index,x,y,z);
			}
			volume = finalVolumeVbl.getMinVolume();
			System.out.printf("%.5g\n",volume);
		}

	}

}
