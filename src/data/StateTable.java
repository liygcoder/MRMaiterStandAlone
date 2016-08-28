package data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import core.MaiterAPI;


/*
 * StateTable
 */
public class StateTable<K, V, D, E> extends Table implements TypeTable<K, V, D, E> {
	// variable
	private HashMap<K, Bucket<K, V, D, E>> dataTable;
	private SequenceIterator sequIterator;
	private ScheduledIterator scheIterator;
	private EntirePassIterator entiIterator;
	private int sampleLowerBound;//partion=sampleLowerBound/sampleSize    sampleLowerBound:default=10
	private MaiterAPI<K,V,D,E> api;
	private double partion;
	// method
	StateTable(int samplelowerbound,MaiterAPI<K,V,D,E> api) {
		dataTable = new HashMap<K, Bucket<K, V, D, E>>();
		sampleLowerBound=samplelowerbound;
		this.api=api;
	}  
    
	public void put(K key, Bucket<K, V, D, E> bk) {
		dataTable.put(key, bk);
//		System.out.println("key="+key+" getkey="+bk.getKey());
//		System.out.println("current:");
//		Iterator iter = dataTable.entrySet().iterator();
//		while(iter.hasNext()){
//			Map.Entry entry = (Map.Entry) iter.next();
//			System.out.println(entry.getValue());
//		}
	}
    public Bucket<K, V, D, E> get(K key){
    	return dataTable.get(key);
    }
	public BaseIterator<K, V, D, E> getSequenceIterator() {
		sequIterator = new SequenceIterator();
		return sequIterator;
	}

	public BaseIterator<K, V, D, E> getScheduledIterator(double partion) {
		scheIterator = new ScheduledIterator();
		return scheIterator;
	}
	public BaseIterator<K, V, D, E> getEntirePassIterator() {
		entiIterator =new EntirePassIterator();
		return entiIterator;
	}

	class SequenceIterator extends BaseIterator<K, V, D, E> {
		Iterator iter;
		SequenceIterator() {
			iter = dataTable.entrySet().iterator();
		}
		public boolean hasNext() {
			return iter.hasNext();
		}
		public Bucket<K, V, D, E> next() {
			boolean isHasNext = false;
			while (iter.hasNext()) {
				Map.Entry entry = (Map.Entry) iter.next();
				Bucket<K, V, D, E> bk = (Bucket<K, V, D, E>) entry.getValue();
				if (!bk.getDelta().equals(0)) {// filter the vertex whose delta=defaultvalue
					return bk;
				}
			}
			return null;
		}
	}

	class EntirePassIterator extends BaseIterator<K, V, D, E> {
		Iterator iter;
		EntirePassIterator() {
			iter = dataTable.entrySet().iterator();
		}
		public boolean hasNext() {
			return iter.hasNext();
		}
		public Bucket<K, V, D, E> next() {
			Map.Entry entry = (Map.Entry) iter.next();
			Bucket<K, V, D, E>  bk = (Bucket<K, V, D, E> ) entry.getValue();
			return bk;
		}
	}
	
	class ScheduledIterator extends BaseIterator<K, V, D, E> {
		double partion;
		//int sampleLowerBound;
		Iterator iter;
		ArrayList<D> sample;
       
		ScheduledIterator() {
			iter = dataTable.entrySet().iterator();
			sample = new ArrayList<D>();
		}

		public boolean hasNext() {
			return iter.hasNext();
		}

		public Bucket next() {
			boolean isHasNext = false;
			while (iter.hasNext()) {
				Map.Entry entry = (Map.Entry) iter.next();
				Bucket bk = (Bucket) entry.getValue();
				if (!bk.getDelta().equals(0)) {// filter the vertex whose delta=defaultvalue
					return bk;
				}
			}
			return null;
		}
		D getThreshhold(){//get the threshold of priority
			int sampleSize=(int) (sampleLowerBound/partion);
			int inter=dataTable.size()/sampleSize;
		    BaseIterator<K, V, D, E> iter=(BaseIterator<K, V, D, E>) getEntirePassIterator();
		    Bucket<K, V, D, E> bk;
		    int index=0;
		    while(iter.hasNext()){
				bk=iter.next();
				if((++index%inter)==0){
					sample.add(bk.getDelta());
				}
			}
		    qksort(sample,0,sample.size()-1);// sort the sample
			D thresh = sample.get(sampleLowerBound);// get the threshold
			return thresh;
		}
		// 
		 void qksort(ArrayList<D> data,int s,int t){
			if(s<t){
				int k=qkpass(data,s,t);
			    qksort(data,s,k-1);
			    qksort(data,k+1,t);
			}
		}
		 int qkpass(ArrayList<D> data,int s,int t){
			int i=s,j=t;
			D value=data.get(i);
			while(i<j){
				while(i<j){
					if(api.isGreater(value,data.get(j))){
						data.add(i, data.get(j));
						++i;
						break;
					}
					--j;
				}
				while(i<j){
					if(api.isGreater(data.get(i), value)){
						data.add(j, data.get(i));
						--j;
						break;
					}
					++i;
				}
			}
			data.add(i, value);
			return i;
		}
		
	}
}


