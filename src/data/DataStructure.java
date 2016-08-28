package data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

import core.MaiterAPI;


abstract class Table {}

interface TypeTable<K, V, D, E> {
	abstract void put(K key, Bucket<K, V, D, E> bk);
	abstract Bucket<K, V, D, E> get(K key);
}

interface PTypeTable<K, D> {
	abstract void put(K key, Pair<K, D> p);
	abstract Pair<K, D> get(K key);
	abstract void clear();
}

// data in every worker
public class DataStructure<K,V,D,E> {
	// variable
	private Table tables[];//loacl data(stateTable) and sendMessage(deltaTabel) Storage unit
	private ArrayList<DeltaTable<K,D>> receiveTable;// receiveMessage Storage unit
	private int workerId;// workrId:1-workerNum
	private int sampleLowerBound;
	private int workerNum;
	private MaiterAPI<K,V,D,E> api;
	
	// method
	public DataStructure(int workernum, int workerid, int samplelowerbound,MaiterAPI<K,V,D,E> api) {
		workerId = workerid;
		workerNum = workernum;
		sampleLowerBound=samplelowerbound;
		this.api=api;
		tables = new Table[workerNum];
		for (int i = 0; i != workerNum; ++i) {
			if (i == workerId - 1) {
				tables[i] = new StateTable<K,V,D,E>(samplelowerbound,api);
			} else {
				tables[i] = new DeltaTable<K,D>(api.default_v());
			}
		}
		receiveTable= new ArrayList<DeltaTable<K,D>>();
		for (int i = 0; i != workerNum; ++i) {
			receiveTable.add(new DeltaTable<K,D>(api.default_v()));
		}
	}
	
	public DeltaTable<K,D> getReceiveTable(int sender) {
		return receiveTable.get(sender);
	}
	public StateTable<K,V,D,E> getStateTable() {
		return (StateTable<K,V,D,E>) tables[workerId - 1];
	}

	public DeltaTable<K,D> getDeltaTable(int index) {
		return (DeltaTable<K,D>) tables[index];
	}

	public int getWorkerId() {
		return workerId;
	}

	public int getSampleSize() {
		return sampleLowerBound;
	}

	public int getWorkerNum() {
		return workerNum;
	}
}
