
//******************************************************************************
//
// File:    OutputVbl.java
// This file is a part of project 1 of the course:Foundation of Parallel Computing,
// under taken in Fall 2015 at Rochester Institute of Technology. 
//******************************************************************************
import java.math.BigInteger;

import edu.rit.pj2.Vbl;

/**
 * Class OutputVbl is a reduction class which stores maximum first prime found
 * in a Big Integer for given range of Big integers while checking the given
 * range of numbers for Goldbach conjeture.
 * 
 * @author Karan Bhagat
 * @version 20-Sep-2015
 */
class OutputVbl implements Vbl {

	private BigInteger maxPrime;// maximum prime found
	private BigInteger testNum;// number the maxPrime belongs to

	/**
	 * Constructor for initializing private variable maxPrime, testNum to 0
	 */
	public OutputVbl() {
		maxPrime = new BigInteger("0");
		testNum = new BigInteger("0");
	}

	/**
	 * Make a clone of this reduction variable
	 *
	 * @return clone reduction variable.
	 */
	public Object clone() {
		try {
			OutputVbl vbl = (OutputVbl) super.clone();
			vbl.maxPrime = new BigInteger(this.maxPrime.toByteArray());
			vbl.testNum = new BigInteger(this.testNum.toByteArray());
			return vbl;
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException("Cloning Exception", e);
		}
	}

	/**
	 * This method stores the maximum prime out of this object's maxPrime or
	 * Parameter's maxPrime. This is used for reduction after parallel loop
	 *
	 * @param vbl
	 *            reference of another reduction variable.
	 */
	public void reduce(Vbl vbl) {
		OutputVbl tempOutVbl = (OutputVbl) vbl;
		if (this.maxPrime.compareTo(tempOutVbl.maxPrime) < 0) {
			this.maxPrime = new BigInteger(tempOutVbl.maxPrime.toByteArray());
			this.testNum = new BigInteger(tempOutVbl.testNum.toByteArray());
		} else if (this.maxPrime.compareTo(tempOutVbl.maxPrime) == 0) {
			if (this.testNum.compareTo(tempOutVbl.testNum) < 0) {
				this.maxPrime = new BigInteger(tempOutVbl.maxPrime.toByteArray());
				this.testNum = new BigInteger(tempOutVbl.testNum.toByteArray());
			}
		}

	}

	/**
	 * Sets the value private members to parameter's private members if
	 * parameter's private members are greater.
	 *
	 * @param vbl
	 *            reference of another reduction variable.
	 *
	 */
	public void set(Vbl vbl) {
		OutputVbl tempOutVbl = (OutputVbl) vbl;
		setMaxPrimeAndTestNum(tempOutVbl.maxPrime, tempOutVbl.testNum);
	}

	/**
	 * Get the value of maxPrime
	 *
	 * @return maxPrime
	 */
	public BigInteger getMaxPrime() {
		return maxPrime;
	}

	/**
	 * Check and saves the value of maxPrime and testNum
	 *
	 * @param maxPrime
	 *            new candidate maxPrime.
	 * @param testNum
	 *            new candidate testNum.
	 * 
	 * @see #setMaxPrimeAndTestNum(BigInteger,BigInteger)
	 */
	public void setMaxPrimeAndTestNum(BigInteger maxPrime, BigInteger testNum) {
		if (this.maxPrime.compareTo(maxPrime) <= 0) {
			this.maxPrime = new BigInteger(maxPrime.toByteArray());
			setTestNum(testNum);
		}
	}

	/**
	 * Get the value of testNum
	 *
	 * @return testNum
	 */
	public BigInteger getTestNum() {
		return testNum;
	}

	/**
	 * Sets the value of testNum
	 *
	 * @param testNum
	 *            new BigInteger from with new maxPrime is saved
	 */
	private void setTestNum(BigInteger testNum) {
		this.testNum = new BigInteger(testNum.toByteArray());
	}
}