//******************************************************************************
//
// File:    DnaCensus.java
// This file is a part of project 4 of the course:Foundation of Parallel Computing,
// under taken in Fall 2015 at Rochester Institute of Technology. 
//******************************************************************************

import java.io.File;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.rit.pj2.vbl.LongVbl;
import edu.rit.pjmr.Combiner;
import edu.rit.pjmr.Customizer;
import edu.rit.pjmr.Mapper;
import edu.rit.pjmr.PjmrJob;
import edu.rit.pjmr.Reducer;
import edu.rit.pjmr.TextDirectorySource;
import edu.rit.pjmr.TextId;
/**
 * Class DnaCensus is cluster Map Reduce program for finding number of occurrences of a
 * given pattern in Dna sequences provided in FASTA files on different nodes of a cluster.
 * <P>
 * 
 * Usage: <TT>java pj2 DnaCensus <I>pattern</I> <I>directory</I> <I>nodes</I></TT>
 * <TT><I>pattern</I><TT> = pattern to find in the Dna sequences
 * <BR><TT><I>directory</I></TT> = directory on nodes where file is kept <BR>
 * <BR><TT><I>nodes</I></TT> = This is space separated name so backend nodes.
 * <P>
 *
 * @author Karan Bhagat
 * @version 20-Nov-2015
 */
public class DnaCensus extends PjmrJob<TextId,String,String,LongVbl> {

	/**
	 * main program of PJMR job class
	 *
	 * @param  args  Command line arguments.
	 */
	public void main(String[] args) throws Exception {

		//Checking number of arguments
		if( args.length < 3 ){
			exceptionCall(1);
		}

		String pattern = args[0];
		
		String dir = args[1];

		//Retrieving node names
		String[] nodes = new String[args.length - 2];
		for(int i = 2; i < args.length; i++){
			nodes[i-2] = args[i];
		}

		//Configuring mapper tasks
		int NT = 1;
		for(String node : nodes){
			mapperTask(node).source(new TextDirectorySource(dir)).mapper(NT,DnaMapper.class, pattern);
		}
		
		//Configuring reducer tasks
		reducerTask().customizer(DnaCustomizer.class).reducer(DnaReducer.class);
		
		startJob();
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
			System.err.println("Very few arguments");
			throw new IllegalArgumentException();
		}
	}

	/**
	 * Mapper Class
	 * This class contains map method which parse each line for 
	 * pattern and add that pattern to combiner for reduction.
	 * @author Karan Bhagat
	 */
	private static class DnaMapper extends Mapper<TextId,String,String,LongVbl>{
		String pattern; //pattern to search for
		public static final LongVbl countOne = new LongVbl.Sum(1L);
		
		//Variable for handling pattern matching
		StringBuilder sb;  
		Pattern pat;
		Matcher m;
		int lastMatchIndex;
		String seqName;
		
		//initialize variables
		public void start(String[] args, Combiner<String,LongVbl> combiner){
			pattern = args[0];
			pat = Pattern.compile(pattern);
		}

		//map process one line of data file at time. 
		//This finds pattern from the stringbuilder and
		//that to combiner for reduction.
		public void map(TextId id, String data, Combiner<String, LongVbl> combiner) {

			//if this is start of new sequence
			if(data.charAt(0) == '>'){
				seqName = data.substring(1,data.indexOf(' '));
				sb = new StringBuilder();
				lastMatchIndex = -1;
			}
			//line containing actual sequence
			else if( data.length() > 0){
				sb.append(data);
				m = pat.matcher(sb.toString());
				m.region(lastMatchIndex+1, sb.length());
				while( m.find()){
					//pattern matched and added to combiner against the key seqName
					combiner.add(seqName, countOne);
					if( m.start() + 1 < sb.length()){
						lastMatchIndex = m.start();
						m.region(lastMatchIndex+1, sb.length());
					}		
				}
			}
			
		}

	}

	/**
	 * Customizer for Reducer task
	 * @author Karan Bhagat
	 *
	 */
	private static class DnaCustomizer extends Customizer<String, LongVbl>{
		//Sorts in decreasing order of values and if values are same
		//then keys in lexicographical order.
		public boolean comesBefore(String key1, LongVbl val1, String key2, LongVbl val2){
			if(val1.item > val2.item){
				return true;
			}else if( val1.item < val2.item){
				return false;
			}else{
				return key1.compareTo(key2) < 0;
			}
		}
	}

	/**
	 * Reducer Class, this just takes key value pair from
	 * combiner and then prints it to the console
	 * 
	 * @author Karan Bhagat 
	 *
	 */
	private static class DnaReducer extends Reducer<String, LongVbl>{
		
		//Prints the sequence Name(key) and the number of occurrences of given
		//pattern (value) to the console
		public void reduce(String key, LongVbl val){
			System.out.printf("%s\t%d\n",key,val.item);
			System.out.flush();
		}
	}

}
