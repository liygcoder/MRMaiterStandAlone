package data;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/*
 * DeltaTable
 */
public class DeltaTable<K, D> extends Table implements PTypeTable<K, D> {

	// variable
	private HashMap<K, Pair<K, D>> deltaTable;
	SequenceIterator sequIterator;
	D defaultValue;
	// method
	DeltaTable(D defaultvalue) {//constructor
		deltaTable=new HashMap<K,Pair<K, D>>();
		sequIterator = new SequenceIterator(defaultvalue);
		this.defaultValue=defaultvalue;
//		if(this.defaultValue==null){System.out.println("failed....................");}
//		else System.out.println("success...................."+this.defaultValue);
	}
    public Pair<K, D> get(K key){
    	if(!deltaTable.containsKey(key)){
    		deltaTable.put(key,new Pair<K,D>(key,defaultValue));
    	}
    	return deltaTable.get(key);
    }
	public void put(K key, Pair<K, D> pair) {
		deltaTable.put(key, pair);
	}
	public void clear() {
		deltaTable.clear();
	}

	public SequenceIterator getSequenceIterator() {
		return sequIterator;
	}
	//iterator
	class SequenceIterator extends PBaseIterator<K,D> {
		Iterator iter;
		D defaultValue;
		//method
		SequenceIterator(D defaultvalue) {
			iter = deltaTable.entrySet().iterator();
			defaultValue =defaultvalue;
		}
        
		public boolean hasNext() {
			return iter.hasNext();
		}

		public Pair<K,D> next() {
			boolean isHasNext = false;
			while (iter.hasNext()) {
				Map.Entry entry = (Map.Entry) iter.next();
				Pair<K,D> pair = (Pair<K,D>) entry.getValue();
				if (!pair.getDelta().equals(0)) {// filter the vertex whose delta=0
					return pair;
				}
			}
			return null;
		}
	}
}
