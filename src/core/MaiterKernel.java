package core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.Scanner;
import java.util.Vector;


import data.BaseIterator;
import data.Bucket;
import data.DataStructure;
import data.DeltaTable;
import data.PBaseIterator;
import data.Pair;
import data.StateTable;

//implement the core operations of Maiter
public class MaiterKernel<K, V, D, E> {
	/*
	 * Variable
	 */
	int workerNum;
	int workerId;
	MaiterAPI<K, V, D, E> api;
	ArrayList<DataStructure<K,V,D,E>> globalData;
	DataStructure<K, V, D, E> data;
	D aggregation_last;// Record the progress of the test result last time
	StateTable<K, V, D, E> table;
	/*
	 * Method
	 */
	public MaiterKernel(MaiterAPI<K, V, D, E> api,ArrayList<DataStructure<K,V,D,E>> globalData,int workerId) {
		this.globalData=globalData;
		data=globalData.get(workerId-1);
		this.api = api;
		workerNum = data.getWorkerNum();
		this.workerId = workerId;
		table = data.getStateTable();
		aggregation_last=api.default_v();
	}

	boolean readDataKernel(String inputPath) {
		try {
			FileReader in = new FileReader(inputPath);
			BufferedReader reader = new BufferedReader(in);
			String line;
			while ((line = reader.readLine()) != null) {
				Bucket<K, V, D, E> bk = new Bucket<K, V, D, E>();
				api.readData(line, bk);
				if (bk.getMaster() == workerId) {// only Initial value for master vertex
					bk.setDelta(api.initDelta(bk.getKey()));
				} else { // // only Initial defaultvalue for mirrors vertex
					bk.setDelta(api.default_v());
				}
				bk.setValue(api.initValue(bk.getKey()));
				bk.setPriority(api.priority(bk.getValue(), bk.getDelta()));
				table.put(bk.getKey(),bk);
				//System.out.println("line="+line);
			}
			reader.close();
			in.close();
		} catch (IOException e) {
			System.out.println(e);
			return false;
		}
		return true;
	}

	/*
	 * kernel2 iterate computing
	 */
	void runLoop(double partion) {
		sendMessage();
		receiveMessage();
		D defaultV = api.default_v();
		K key;D delta;V value;
		BaseIterator<K, V, D, E> iter;
		if (partion < 1) {
			iter = data.getStateTable().getScheduledIterator(partion);
		} else {//
			iter = data.getStateTable().getSequenceIterator();
		}
		Bucket<K, V, D, E> bk;
		ArrayList<Pair<K, D>> outLink = new ArrayList<Pair<K, D>>();
		ArrayList<Pair<Integer, D>> outMirrors = new ArrayList<Pair<Integer, D>>();
		while (iter.hasNext()) {
			bk = iter.next();
			if (bk == null)
				break;
			outLink.clear();
			outMirrors.clear();
			key=bk.getKey();value=bk.getValue();delta=bk.getDelta();
			if (bk.getMaster() == workerId) {// 只对master顶点执行累积更新value
				//1更新value
				value = (V) api.accumulate((D)value, delta);
				bk.setValue(value);
				//2 由于优化了和缓存了master向mirror发送消息，所以master在此时要向其mirrors发送消息
				Iterator<Integer> iterM = bk.getMirrors().iterator();
				while (iterM.hasNext()) {
					int id = iterM.next();
					D off=api.default_v();
					if(bk.getOffset().containsKey(key)){
						off=bk.getOffset().get(key);
					}
					D deltaM=api.counteract(delta,off);
				    outMirrors.add(new Pair<Integer, D>(id, deltaM));
					accumlateDeltaIn(outMirrors,key);
				}
				bk.getOffset().clear();//清空Offset
			}
			//3计算 delta传递给本地的邻居顶点的值
			api.g_func(key,delta, bk.getValue(),bk.getLinks(), bk.getLength(), outLink);

			//4更新delta、priority
			bk.setDelta(defaultV);// 更新delta
			delta=bk.getDelta();
			bk.setPriority(api.priority(value, delta));// 更新priority

			//5 对2、3中计算结果进行传递 应用
			accumlateDelta(outLink,true,-1);
		}
	}
	void accumlateDeltaIn(ArrayList<Pair<Integer, D>> outMirrors,K key) {//5. 顶点内部副本顶点的消息同步 累积
		Iterator<Pair<Integer,D>> iter = outMirrors.iterator();
		Pair<Integer,D> pair;
		Pair<K,D> pair2;
		D delta;
		while (iter.hasNext()) {
			pair = iter.next();
			DeltaTable<K,D> deltaTable=(DeltaTable<K,D>)data.getDeltaTable(pair.getKey());//pair.getKey()is the worker_id
			pair2=deltaTable.get(key);
			delta=api.accumulate(pair.getDelta(),pair2.getDelta());
			pair.setDelta(delta);
		}	
	}
	void accumlateDelta(ArrayList<Pair<K, D>> outLink,boolean isLocalMessage,int workerid) {//5. 顶点之间的消息传递 累积
		// TODO 将当前顶点的值传递出去，首先更新本地的buckets_。
		// 然后如果是master，则向mirrors发送消息；如果不是master顶点，则向其对应的master顶点发送消息
		Pair<K,D> pair;
		Bucket<K,V,D,E> bk;
		Iterator<Pair<K,D>> iter = outLink.iterator();
		while (iter.hasNext()) {
			pair = iter.next();
			K key=pair.getKey();
			// 第一步：更新本地的delta
			bk=table.get(key);//获取目的顶点
			D delta=api.accumulate(pair.getDelta(),bk.getDelta());
			bk.setDelta(delta);//更新delta
			// 第二步，如果是mirror顶点的更新
			if(workerId==bk.getMaster()){
				if(!isLocalMessage){//是master顶点且是远程消息,则更新offset
					HashMap<Integer, D> offset=bk.getOffset();
					if(offset.containsKey(workerid)){
						offset.put(workerid,api.default_v());
					}
					delta=api.accumulate(pair.getDelta(),offset.get(workerid));
					offset.put(workerid, delta);
				}
			}else{
				if(isLocalMessage){//mirror顶点且是本地消息 则将消息发送给master顶点
					DeltaTable<K,D> deltaTable=(DeltaTable<K,D>)data.getDeltaTable(bk.getMaster()-1);
					pair=deltaTable.get(bk.getKey());				
					delta=api.accumulate(pair.getDelta(),bk.getDelta());
					pair.setDelta(delta);
				}
			}
		}
	}
	private void receiveMessage() {//pair->bucket
		// TODO 不同线程会有冲突 远程写和本地读 消息队列
		StateTable<K,V,D,E> stateTable=data.getStateTable();
		for(int i=0;i!=workerNum;++i){
			if(i==workerId-1)break;
			int sender=i;
			DeltaTable<K,D> message=data.getReceiveTable(sender);
			PBaseIterator<K,D> iter=message.getSequenceIterator();
			Pair<K,D> pair;
			Bucket<K,V,D,E> bk;
			while(iter.hasNext()){
				pair=iter.next();
				K key=pair.getKey();
				D delta=pair.getDelta();
				bk=stateTable.get(key);//需添加错误检查 
				delta=api.accumulate(delta, bk.getDelta());
				bk.setDelta(delta);
			}
			message.clear();
		}
	}
	private void sendMessage() {//pair->pair
		// TODO 将消息队列更新到对应的worker的接收消息的队列中 没有冲突
		for(int i=0;i!=workerNum;++i){
			if(i==workerId-1)break;
			DeltaTable<K,D> sender=data.getDeltaTable(i);
			DeltaTable<K,D> receiver=globalData.get(i).getReceiveTable(workerId-1);
			PBaseIterator<K,D> iter=sender.getSequenceIterator();
			Pair<K,D> spair;
			Pair<K,D> rpair;
			while(iter.hasNext()){
				spair=iter.next();
				K key=spair.getKey();
				D delta=spair.getDelta();
				rpair=receiver.get(key);
				if(rpair==null){//不存在，则创建
					Pair<K,D> newp=new Pair<K,D>(key,api.default_v());
					receiver.put(key, newp);
				}
			    delta=api.accumulate(delta, rpair.getDelta());
			    rpair.setDelta(delta);
			}
			sender.clear();
		}
	}
	
	/**
	 * terminate check
	 */
	// worker收集本地各个顶点的value值 读本地数据
	D estimate_prog() {
		Bucket<K,V,D,E> bk;
		// 数据的收集
		BaseIterator<K,V,D,E> iter = data.getStateTable().getEntirePassIterator();
		D aggregation_curr = api.default_v();
		while (iter.hasNext()) {
			bk = iter.next();
			if (bk.getMaster() == workerId){aggregation_curr = api.accumulate(aggregation_curr, (D)bk.getValue());}// 只把master顶点的value进行收集		
		}
		return aggregation_curr;
	}

	// master进行终止判断 读消息数据
	boolean terminate(D threshold, D aggregation_curr) {
		boolean finished = false;
		D change=api.abs(aggregation_curr,aggregation_last);
		if (api.isGreater(threshold, change) ) {
			finished = true;
		}
		System.out.println("Termination check at"+System.currentTimeMillis()+"\ncurr:" + aggregation_curr + "  last:"
				+ aggregation_last + " 差值:"
				+ change);
		aggregation_last = aggregation_curr;
		return finished;
	}

	/*
	 * kernel3 write the result to the disk
	 */
	public boolean writeResultKernel(String outputPath) {
		// TODO Auto-generated method stub
		try {
			//System.out.println(outputPath);
			FileWriter out = new FileWriter(outputPath);
			BufferedWriter writer = new BufferedWriter(out);
			BaseIterator<K,V,D,E> iter = data.getStateTable().getEntirePassIterator();
			Bucket<K,V,D,E> bk;
			String line;
			while (iter.hasNext()) {
				bk = iter.next();
				if(bk.getMaster()==workerId){
					line = bk.getKey() + "  " + bk.getValue()+"\n";
					writer.write(line);
				}
			}
			writer.close();
			out.close();
		} catch (IOException e) {
			System.out.println(e);
			return false;
		}
		return true;
	}

	/*
	 * for test
	 */
	// for test show the data of the buckets_
	void showBuckets() {
		BaseIterator<K, V, D, E> iter = data.getStateTable().getEntirePassIterator();
		while (iter.hasNext()) {
			Bucket buck = iter.next();
			System.out.println("workerId=" + workerId + "; key=" + buck.getKey()
					+ "; delta=" + buck.getDelta() + "; value=" + buck.getValue()
					+ "; pri=" + buck.getPriority() + "; links=" + buck.getLinks().size() 
					+ "; length=" + buck.getLength()
					+ "; master=" + buck.getMaster()
					+ "; mirrors=" +buck.getMirrors().size());
		}
	}
}
