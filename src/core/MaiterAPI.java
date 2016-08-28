package core;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import data.Bucket;
import data.Pair;



//import DataStructure;
public abstract class MaiterAPI<K,V,D,E> {
    //algorithm whitch runs on Maiter must implement MaiterAPI 
   
	//read data
	public abstract void readData(String line,Bucket<K,V,D,E> bk);//return key
	public abstract D initDelta(K k);// return delta
	public abstract V initValue(K k);// return value
	public abstract D priority(V value, D delta);// return priority
	public abstract D default_v();// return default_v
	//operate data
	public abstract D accumulate(D a, D b);//.e.g,return a=a+b
    //V process_delta_v( K k, V delta,V value, D data){return delta;}//for simrank
	public abstract void g_func(K k,D delta,V value,ArrayList<E> links,int length,ArrayList<Pair<K,D>> outlink);
	public abstract D counteract(D a, D b);
	//abstract void g_func(D delta,D offset,ArrayList<Integer> mirrors,ArrayList<Pair<Integer,D>> outMirrors);//replace
	public abstract boolean isGreater(D d, D value);
	public abstract D abs(D d, D value);
	public abstract D getThreshold();
}


