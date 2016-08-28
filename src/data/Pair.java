package data;

import java.util.ArrayList;
import java.util.HashMap;

public class Pair<K, D> {// k-v 
	private K key;
	private D delta;
    //method
	public Pair(K k,D d){
	   key=k;
	   delta=d;
	}
	public synchronized D getDelta() {
		return delta;
	}

	public synchronized void setDelta(D delta) {
		this.delta = delta;
	}

	public K getKey() {
		return key;
	}
}
