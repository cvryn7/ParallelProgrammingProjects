

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.InputMismatchException;
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


class MinVolumeVbl extends Tuple implements Vbl{

	private Tetrahedron minTetra;
	private double volume;

	public MinVolumeVbl(){
		minTetra = new Tetrahedron();
		volume = Double.MAX_VALUE;
	}

	public Object clone(){
		MinVolumeVbl vbl = (MinVolumeVbl) super.clone();
		vbl.minTetra = (Tetrahedron)minTetra.clone();
		vbl.volume = volume;
		return vbl;
	}

	public void reduce(Vbl vbl){
		MinVolumeVbl newVbl = (MinVolumeVbl)vbl;
		saveMin(newVbl.minTetra,newVbl.volume);
	}

	public void set(Vbl vbl){
		MinVolumeVbl newVbl = (MinVolumeVbl) vbl;
		this.minTetra.replaceAllCorners( newVbl.minTetra );
		this.volume = newVbl.volume;
	}

	public void saveMin(Tetrahedron t, double volume){
		if( this.volume > volume){
			this.volume = volume;
			this.minTetra.replaceAllCorners(t);
		}
	}

	public Tetrahedron getTetra(){
		return this.minTetra;
	}

	public double getMinVolume(){
		return this.volume;
	}

	public void readIn(InStream in) throws IOException {
		minTetra = (Tetrahedron)in.readObject();
		volume = in.readDouble();
	}

	public void writeOut(OutStream out) throws IOException {
		out.writeObject(minTetra);
		out.writeDouble(volume);
	}
}


class Tetrahedron implements Cloneable, Streamable{
	private Coordinate[] corners;
	private final int numberOfCorners = 4;
	int arrayIndexes;

	Tetrahedron(){
		corners = new Coordinate[numberOfCorners];
		arrayIndexes = 0;
		intializeCorners();
	}

	private void intializeCorners(){
		for(int i = 0; i < numberOfCorners; i++){
			corners[i] = new Coordinate();
		}
	}

	public Object clone(){
		try{
			Tetrahedron t = (Tetrahedron) super.clone();
			t.corners = this.corners == null?null:(Coordinate[])this.corners.clone();
			t.arrayIndexes = arrayIndexes;
			return t;
		}catch( CloneNotSupportedException e){
			throw new RuntimeException("cloning in Tetraheadron failed",e);
		}
	}

	void addCorner(Coordinate c){
		if( arrayIndexes < numberOfCorners){
			corners[arrayIndexes] = c;
			arrayIndexes++;
		}
	}

	Coordinate getCorner(int index){
		return corners[index];
	}

	void replaceAllCorners(Tetrahedron t){
		for(int i = 0; i < numberOfCorners; i++){
			corners[i] = t.getCorner(i);
		}
	}

	void clear(){
		arrayIndexes = 0;
	}

	double findVolume(){
		double x1,y1,z1;
		double x2,y2,z2;
		double x3,y3,z3;
		double x4,y4,z4;
		double volume;
		x1 = corners[0].getAtIndex(0);
		y1 = corners[0].getAtIndex(1);
		z1 = corners[0].getAtIndex(2);
		x2 = corners[1].getAtIndex(0);
		y2 = corners[1].getAtIndex(1);
		z2 = corners[1].getAtIndex(2);
		x3 = corners[2].getAtIndex(0);
		y3 = corners[2].getAtIndex(1);
		z3 = corners[2].getAtIndex(2);
		x4 = corners[3].getAtIndex(0);
		y4 = corners[3].getAtIndex(1);
		z4 = corners[3].getAtIndex(2);


		volume = Math.abs((x1*( y2*(z3-z4)-y3*(z2-z4)+y4*(z2-z3))-
				x2*( y1*(z3-z4)-y3*(z1-z4)+y4*(z1-z3))+
				x3*( y1*(z2-z4)-y2*(z1-z4)+y4*(z1-z2))-
				x4*( y1*(z2-z3)-y2*(z1-z3)+y3*(z1-z2)))/6);
		return volume;
	}

	Coordinate getCornerAtIndex(int index){
		return corners[index];
	}
	public void readIn(InStream in) throws IOException {
		corners = (Coordinate[])in.readObjectArray();
		arrayIndexes = in.readInt();
	}

	public void writeOut(OutStream out) throws IOException {
		out.writeObjectArray(corners);
		out.writeInt(arrayIndexes);

	}
}

class Coordinate implements Cloneable, Streamable{
	private double[] array = new double[3];
	private int objectIndex;
	Coordinate(){

	}

	Coordinate(int objectIndex){
		this.objectIndex = objectIndex;
	}

	int getObjectIndex(){
		return objectIndex;
	}

	double getAtIndex(int index){
		return array[index];
	}

	void setAtIndex(double item, int index){
		array[index] = item;
	}

	public Object clone(){
		try{
			Coordinate d2DArray = (Coordinate) super.clone();
			d2DArray.array = this.array == null? null: (double[]) this.array.clone();
			d2DArray.objectIndex = objectIndex;
			return d2DArray;
		}catch(CloneNotSupportedException e){
			throw new RuntimeException("Cloning in Coordinate failed",e);
		}
	}

	public void readIn(InStream in) throws IOException {
		array = in.readDoubleArray();
		objectIndex = in.readInt();
	}

	public void writeOut(OutStream out) throws IOException {
		out.writeDoubleArray(array);
		out.writeInt(objectIndex);
	}
}


public class TetraClu extends Job {


	public void main(String[] args) throws Exception {
		if( args.length != 1) exceptionCall(1);
		File dataFile = null;
		Scanner sc = null;
		try{
			dataFile = new File(args[0]);
			sc = new Scanner(dataFile);
		}catch(NullPointerException e){
			exceptionCall(2);
		}catch(FileNotFoundException e){
			exceptionCall(2);
		}
		Coordinate[] coordinates = new Coordinate[1000];

		int arraySize = 0;
		int j = 0;
		coordinates[arraySize] = new Coordinate(arraySize);
		try{
			while(sc.hasNext()){
				coordinates[arraySize].setAtIndex(sc.nextDouble(),j);
				j++;
				if( j > 2){
					arraySize++;
					j = 0;
					coordinates[arraySize] = new Coordinate(arraySize);
				}
			}
		}catch(InputMismatchException e){
			exceptionCall(4);
		}

		if( ( arraySize * j )%3 != 0){
			exceptionCall(3);
		}

		sc.close();

		putTuple(new ObjectArrayTuple<Coordinate>(coordinates));

		int K = workers();
		if( K == DEFAULT_WORKERS) K = 1;
		masterSchedule(proportional);
		masterChunk(10);
		masterFor(0, arraySize-1, TetraWorkerTask.class).args(""+arraySize);
		rule().atFinish().task(ReduceTask.class).args().runInJobProcess();
	}

	public static void exceptionCall(int e) {

		switch (e) {
		case 1:
			System.err.println("Program require commandline arguments");
			throw new IllegalArgumentException();
		case 2:
			System.err.println("No file found!");
			throw new NumberFormatException();
		case 3:
			System.err.println("Incomplete File");
			throw new IllegalArgumentException();
		case 4:
			System.err.println("Illegal Data in File");
			throw new IllegalArgumentException();
		}
	}

	private static class TetraWorkerTask extends Task{

		public void main(String[] args) throws Exception {
			if( args.length != 1){};

			MinVolumeVbl minVolume = new MinVolumeVbl();

			int size = Integer.parseInt(args[0]);
			//int k = Integer.parseInt(args[1]);
			Coordinate[] coordinates = readTuple(new ObjectArrayTuple<Coordinate>()).item;
			//	System.out.println( coordinates[0].getAtIndex(0)+ " "+coordinates[0].getAtIndex(1));


			workerFor().exec(new Loop(){

				MinVolumeVbl thrdMinVolume;
				double newVolume;
				Tetrahedron thrdTetra;
				public void start(){
					thrdMinVolume = threadLocal(minVolume);
					thrdTetra = new Tetrahedron();
				}

				public void run(int i) throws Exception {

					for(int j = i+1; j < size-2; j++){
						for( int m = j+1; m < size-1; m++){
							for( int n = m+1; n < size; n++){
								thrdTetra.clear();
								thrdTetra.addCorner(coordinates[i]);
								thrdTetra.addCorner(coordinates[j]);
								thrdTetra.addCorner(coordinates[m]);
								thrdTetra.addCorner(coordinates[n]);
								newVolume = thrdTetra.findVolume();
								thrdMinVolume.saveMin(thrdTetra, newVolume);
							}
						}
					}	
				}
			});

			//int index;
		//	double x,y,z;
		//	double volume;
			//System.out.println("******Worker "+taskRank()+"******");
		
			//volume = minVolume.getMinVolume();
			//System.out.println(volume);

			putTuple(minVolume);
		}

	}

	private static class ReduceTask extends Task{

		public void main(String[] arg0) throws Exception {
			MinVolumeVbl finalVolumeVbl = new MinVolumeVbl();
			MinVolumeVbl template = new MinVolumeVbl();
			MinVolumeVbl taskVbl;
			while( ( taskVbl = tryToTakeTuple(template)) != null){
				finalVolumeVbl.reduce(taskVbl);
			}
			int index;
			double x,y,z;
			double volume;
			for( int i = 0; i < 4; i++){
				index = finalVolumeVbl.getTetra().getCorner(i).getObjectIndex();
				x = finalVolumeVbl.getTetra().getCorner(i).getAtIndex(0);
				y = finalVolumeVbl.getTetra().getCorner(i).getAtIndex(1);
				z = finalVolumeVbl.getTetra().getCorner(i).getAtIndex(2);
				System.out.printf("%d (%.5g,%.5g,%.5g)\n",index,x,y,z);
			}
			volume = finalVolumeVbl.getMinVolume();
			System.out.printf("%.5g\n",volume);
		}

	}

}
