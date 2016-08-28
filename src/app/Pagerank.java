package app;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import core.MaiterAPI;
import core.MaiterApplication;
import data.Bucket;
import data.Pair;


/*	
data formate： key;master;mirror mirror ;length;nei nei 
*/


public class Pagerank extends MaiterAPI<Integer,Double,Double,Integer> {
   public void readData(String line,Bucket<Integer,Double,Double,Integer> bk){
		String arrays[] = line.split(";");
//		System.out.println("line="+line);System.out.println("length="+arrays.length);
//		for(int i=0;i!=arrays.length;++i){
//			System.out.println(arrays[i]);
//		}
		bk.setKey(Integer.parseInt(arrays[0]));
		bk.setMaster(Integer.parseInt(arrays[1])+1);
		if(!arrays[2].isEmpty()){
			String s_mirrors[]=arrays[2].split(",");
			for(String mirror:s_mirrors){
				bk.getMirrors().add(Integer.parseInt(mirror));
			}
		}
		bk.setLength(Integer.parseInt(arrays[3]));
		if(arrays.length>=5){
			String s_links[]=arrays[4].split(",");
			for(String link:s_links){
				bk.getLinks().add(Integer.parseInt(link));
			}	
		}
	}
	
   public Double initDelta(Integer k){
		double delta=0.2;
		return delta;
	}
   public Double initValue(Integer k){
		double value = 0;
		return value;
	}
   public Double priority(Double value, Double delta){
		double pri= delta;
		return pri;
	}
	public Double default_v(){
		double defaultValue = 0;
		return defaultValue;
	}
	
	public Double accumulate(Double a, Double b){
		a+=b;
		return a;
	}

	public void g_func(Integer k,Double delta,Double value,ArrayList<Integer> links,int length,ArrayList<Pair<Integer,Double>> outlink){
		double outv = delta/length *0.8;
		Iterator<Integer> iter =links.iterator();
		while(iter.hasNext()){
			int id =iter.next();
			outlink.add(new Pair<Integer,Double>(id,outv));
		}
	} 
	public Double counteract(Double a, Double b){
		//System.out.println("a="+a+"  b="+b);
		return a-b;
	}
	public boolean isGreater(Double a,Double b){
		return a>b?true:false;
	}
	public Double abs(Double a,Double b){
		return Math.abs(a-b);
	}
	public Double getThreshold(){//set termination threshold
		Double threshold=(double) 1;
		return threshold;
	}
    public static void startMaiter(){
        //instance algorithm
    	MaiterApplication<Integer,Double,Double,Integer> application=new MaiterApplication<Integer,Double,Double,Integer>();
    	application.maiterRun();
    }
}






