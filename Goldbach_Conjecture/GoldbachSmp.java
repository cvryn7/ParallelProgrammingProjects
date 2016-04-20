
//******************************************************************************
//
// File:    GoldbachSmp.java
// This file is a part of project 1 of the course:Foundation of Parallel Computing,
// under taken in Fall 2015 at Rochester Institute of Technology. 
//******************************************************************************

import edu.rit.pj2.LongLoop;
import edu.rit.pj2.ObjectLoop;
import edu.rit.pj2.Task;
import edu.rit.pj2.Vbl;
import edu.rit.pj2.WorkQueue;
import java.math.BigInteger;

/**
 * Class GoldbachSmp is a multicore parallel program that checks every number in
 * a given range for Goldbach conjecture.
 * <P>
 * Usage: <TT>java pj2 cores=<I>k</I> GoldbachSmp <I>lb</I> <I>ub</I></TT> <BR>
 * <TT><I>k</I><TT> = Number of cores
 * <BR><TT><I>lb</I></TT> = Lower bound number <BR>
 * <TT><I>ub</I></TT> = Upper bound number
 * <P>
 * The program uses class OutputVbl for final reduction of prime numbers and
 * also for storage of maximum prime in one thread run.
 *
 * @author Karan Bhagat
 * @version 20-Sep-2015
 */
public class GoldbachSmp extends Task {

	// Main program
	public void main(String[] args) throws Exception {

		// Checking number of command line arguments
		if (args.length != 2)
			exceptionCall(1);

		// For storing command line arguments lower bound and upper bound
		BigInteger lowerB = null;
		BigInteger tempUpperB = null;

		// checking number format of command line arguments
		try {
			lowerB = new BigInteger(args[0]);
			tempUpperB = new BigInteger(args[1]);
		} catch (NumberFormatException e) {
			exceptionCall(2);
		}

		// Transferring temp variable to permanent upperB variable
		BigInteger upperB = new BigInteger(tempUpperB.toByteArray());

		// BigInteger two for Big Integer operations with number two
		BigInteger two = new BigInteger("2");

		// Checking correctness of command line arguments
		if (upperB.subtract(lowerB).intValue() < 0) {
			exceptionCall(3);
		} else if (lowerB.compareTo(two) <= 0) {
			exceptionCall(4);
		} else if ((lowerB.mod(two).intValue() != 0) || (upperB.mod(two).intValue() != 0)) {
			exceptionCall(5);
		}

		// Reduction variable
		OutputVbl outVbl = new OutputVbl();

		// WorkerQueue for storing BigInteger generated in parallel for
		WorkQueue<BigInteger> bigIntQueue = new WorkQueue<BigInteger>();

		// number of threads to be utilized
		int numOfThreads = threads();

		// Increment in BigInteger after every run() method call
		int leap = 2 * numOfThreads;

		// Add as same number of BigIntegers to WorkQueue as there are
		// number of threads to be utilized
		for (int i = 0; i < numOfThreads && (lowerB.compareTo(upperB) <= 0); i++) {
			bigIntQueue.add(lowerB);
			lowerB = lowerB.add(two);
		}

		// ObjectParallelFor with ObjectLoop
		// Loop until WorkQueue is not empty
		parallelFor(bigIntQueue).exec(new ObjectLoop<BigInteger>() {

			OutputVbl perThreadOutVbl;// Per thread reduction variable
			BigInteger half;// Store half of current Big Integer
			BigInteger bigLeap;// Number of Big Integer to skip to next Big Int
			BigInteger nextNum;// Number got after adding bigLeap
			BigInteger nextPrime;// For storing next probable prime
			BigInteger remainNum;// Number left after subtracting

			// Initialize perThread reduction variable and bigLeap
			public void start() {
				perThreadOutVbl = threadLocal(outVbl);
				bigLeap = new BigInteger(new Integer(2 * threads()).toString());
			}

			// Finds minimum primes in current Big Integer
			// which satisfy Goldbach conjecture
			public void run(BigInteger currentBig) throws Exception {
				nextPrime = new BigInteger("2");
				half = currentBig.divide(two);
				while (nextPrime.compareTo(half) <= 0) {
					remainNum = currentBig.subtract(nextPrime);
					if (remainNum.isProbablePrime(100)) {
						perThreadOutVbl.setMaxPrimeAndTestNum(nextPrime, currentBig);
						break;
					}
					nextPrime = nextPrime.nextProbablePrime();
				}
				nextNum = currentBig.add(bigLeap);
				if (nextNum.compareTo(upperB) <= 0)
					bigIntQueue.add(currentBig.add(bigLeap));
			}

		});

		// Retrieve final output results
		BigInteger outputNum = outVbl.getTestNum();
		BigInteger maxPrime = outVbl.getMaxPrime();
		BigInteger remainNum = outputNum.subtract(maxPrime);

		System.out.println(outputNum + " = " + maxPrime + " + " + remainNum);
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
	 * @exception NumberFormatEception
	 *                (checked exception) Thrown if <TT>lb</TT> or <TT>ub</TT>
	 *                are not of Big Integer format
	 */
	public static void exceptionCall(int e) {

		switch (e) {
		case 1:
			System.err.println("Input to programm should be " + " a lower bound and a upper bound");
			throw new IllegalArgumentException();
		case 2:
			System.err.println("Illegal input numbers");
			throw new NumberFormatException();
		case 3:
			System.err.println("lower bound is greater than upperbound");
			throw new IllegalArgumentException();
		case 4:
			System.err.println("lower bound should be greater than 2");
			throw new IllegalArgumentException();
		case 5:
			System.err.println("Both numbers should be even numbers");
			throw new IllegalArgumentException();
		}
	}
}
