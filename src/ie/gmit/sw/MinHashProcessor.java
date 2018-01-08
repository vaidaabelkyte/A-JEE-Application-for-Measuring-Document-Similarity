package ie.gmit.sw;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import com.db4o.Db4o;
import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;

public class MinHashProcessor {

	private static final int K = 100;
	private Set<Integer> hashes;
	private Set<String> words;
	private List<Integer> shingles;

	public MinHashProcessor(Set<String> words) {
		if (words != null) { 
			initHashes();
			this.words = words;
			shingles = new ArrayList<Integer>();
		}
	}

	@SuppressWarnings({ "deprecation", "rawtypes" })
	private void initHashes() {

		ObjectContainer db = null;
		try {
			db = Db4o.openFile("hashes2.data");	
			
			ObjectSet hashesLoad = db.queryByExample(new MinHashProcessor(null));
	        if (hashesLoad.hasNext()) {
	        	MinHashProcessor loadedHashProc = (MinHashProcessor) hashesLoad.next();
	        	hashes = loadedHashProc.getHashes();
	        } else {
	        	// Create the set of hash integers as random numbers
	    		hashes = new TreeSet<Integer>();
	    		Random r = new Random();
	    		for (int i = 0; i < K; i++) {
	    			// Create k random integers
	    			hashes.add(r.nextInt());
	    		}
	        	// persist hashes in DB
	    		db.store(this);
	    		db.commit();
	        }
		} finally {
			db.close();
		}
		
	}

	public Set<Integer> getHashes() {
		return hashes;
	}

	public List<Integer> getShingles() {
		//XOR the integer word values with the hashes 
		for (Integer hash : hashes) {    
			int minXor = Integer.MAX_VALUE;    
			for (String word : words) { 
				int xorHash = word.hashCode() ^ hash; //Bitwise XOR the string hashCode with the hash       
				if (xorHash < minXor) 
					minXor = xorHash; 
			} 
			shingles.add(minXor); //Only store the shingle with the minimum hash for each hash function 
		}  
		
		return shingles;
	}
	
	public static double computeSimilarity(List<Integer> shingles1, List<Integer> shingles2) {
		int countEquals = 0;
		for (int i = 0; i < K; i++) {
            if (shingles1.get(i).equals(shingles2.get(i))) {
            	countEquals++;
            }
		}
        return (double) countEquals / (double) K;
	}

}
