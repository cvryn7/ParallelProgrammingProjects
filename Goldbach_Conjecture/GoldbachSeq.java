
//******************************************************************************
//
// File:    GoldbachSeq.java
// This file is a part of project 1 of the course:Foundation of Parallel Computing,
// under taken in Fall 2015 at Rochester Institute of Technology. 
//******************************************************************************

import edu.rit.pj2.Task;
import java.math.BigInteger;

/**
 * Class GoldbachSeq is a sequential program that checks every number in a given
 * range for Goldbach conjecture.
 * <P>
 * Usage: <TT>java pj2 GoldbachSeq <I>lb</I> <I>ub</I></TT> <BR>
 * <TT><I>lb</I></TT> = Lower bound number <BR>
 * <TT><I>ub</I></TT> = Upper bound number
 * <P>
 *
 * @author Karan Bhagat
 * @version 20-Sep-2015
 */
public class GoldbachSeq extends Task {

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

		// Variable for storing output result
		BigInteger outputNum = new BigInteger("0");
		BigInteger maxPrime = new BigInteger("0");

		BigInteger half;// Store half of current Big Integer
		BigInteger nextPrime;// For storing next probable prime
		BigInteger remainNum;// Number left after subtracting

		// iterate for range of given Big Integers
		for (; lowerB.compareTo(upperB) <= 0; lowerB = lowerB.add(two)) {

			// Finds minimum primes in current Big Integer
			// which satisfy Goldbach conjecture

			nextPrime = new BigInteger("2");
			half = lowerB.divide(two);

			// Check current Big Integer till its half
			while (nextPrime.compareTo(half) <= 0) {
				remainNum = lowerB.subtract(nextPrime);
				if (remainNum.isProbablePrime(100)) {
					if (nextPrime.compareTo(maxPrime) >= 0) {
						maxPrime = nextPrime;
						outputNum = lowerB;
					}
					break;
				}
				nextPrime = nextPrime.nextProbablePrime();
			}
		}

		// Getting other prime half of result output number
		remainNum = outputNum.subtract(maxPrime);

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

	/**
	 * This method tell number of cores to use
	 * 
	 * @return integer specifying number cores
	 */
	protected static int coresRequired() {
		return 1;
	}

}
