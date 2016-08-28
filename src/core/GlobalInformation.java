package core;

import java.util.ArrayList;


import util.TaskCheak;
import util.TaskStart;

//参数 --runner Pagerank --workers 4 ----termcheck_threshold 1000 --snapshot_interval 1 --graph_dir /home/data/input/part --result_dir /home/data/result/part- --portion 1 --sampleSize 10000
public class GlobalInformation {
	String algorithm;//name 0
	int workerNum;//1
	//double threshold;// 2
	String inputPath;//  3
	String outputPath;// 4
	double partion;//5
	int sampleLowerBound;// 6
	long snapshot;//ms 7
	private int flag;//to check the args
	
	public GlobalInformation(){
		flag=0;
		flag|=1<<2;//第二位暂时不用
		partion=1;flag|=1<<5;
		sampleLowerBound=10;flag|=1<<6 ;
	}
	boolean readArgs(String args[]) {
        //参数个数共7个，5个是必须的
		//System.out.println(args.length);
//		if(args.length<10){
//			System.out.println("number of args error!");
//			System.exit(1);
//		}
		//read args
		for(int i=0;i!=args.length;++i){
			switch(args[i]){
			case "--runner":
				algorithm=args[++i];flag|=1;break;
			case "--workers":
				workerNum=Integer.parseInt(args[++i]);flag|=1<<1;break;
//			case "----termcheck_threshold":
//				threshold=Double.parseDouble(args[++i]);flag|=1<<2;break;
			case "--graph_dir":
				inputPath=args[++i];flag|=1<<3;break;
			case "--result_dir":
				outputPath=args[++i];flag|=1<<4;break;
			case "--portion":
				partion=Double.parseDouble(args[++i]);flag|=1<<5;break;
			case "--sampleLowerBound":
				sampleLowerBound=Integer.parseInt(args[++i]);flag|=1<<6;break;
			case "--snapshot_interval":
				snapshot=Long.parseLong(args[++i]);flag|=1<<7;break;
			default:
				System.out.println(args[i]+"*"+args[i+1]);
				System.out.println("args prefix error");System.exit(1);
			}
		}
		return cheakArgs();
	}
	boolean cheakArgs(){
		if(flag==255)// 1111 1111
			return true;
		else
			return false;
	}
}
class IteratorFinished{//判断迭代计算是否终止
	private static boolean iteratorFinished=false;	
	synchronized static boolean readIteratorFinished(){
		return iteratorFinished;
	}
	synchronized static void writeIteratorFinished(boolean b){
		iteratorFinished=b;
	}
}
class GlobalVariable<K,V,D,E>{//全局变量类用于收集当前的结果 计算进度 
	private ArrayList<D> aggregation;
	private ArrayList<Boolean> isreached;
	int workerNum;
	D defaultValue;
	MaiterAPI<K,V,D,E> api;
    GlobalVariable(int Num,MaiterAPI<K,V,D,E> api){
    	workerNum=Num;
    	this.api=api;
    	defaultValue=api.default_v();
    	aggregation=new ArrayList<D>(); 
    	isreached=new ArrayList<Boolean>(); 
        for(int i=0;i!=workerNum;++i){
        	aggregation.add(defaultValue);
        	isreached.add(false);
        }
	} 
	synchronized void  updateAggregation(int workerId,D aggregationLocal) throws InterruptedException{
		aggregation.add(workerId-1, aggregationLocal);
		isreached.add(workerId-1,true);
	 }
	synchronized D readAggregation() throws InterruptedException{
			D aggregationGlobal=defaultValue;
		    int i;
			for(i=0;i<workerNum;++i){
				if(!isreached.get(i))return defaultValue;//只要有一个worker没有更新，就结束本次检测
			}
			for(i=0;i<workerNum;++i){
				aggregationGlobal=api.accumulate(aggregationGlobal,aggregation.get(i));
				aggregation.add(i, defaultValue);
				isreached.add(i,false);
			}
			return aggregationGlobal;
	 }
}
class Helper<K, V, D, E>{
	TaskStart tStart;
	TaskCheak tCheak;
	private D threshold;
	MaiterAPI<K, V, D, E> api;
	GlobalVariable<K,V,D,E> globalVariable;
	Helper(int workerNum,MaiterAPI<K, V, D, E> api){
		tStart= new TaskStart();
	    tCheak = new TaskCheak(workerNum);
	    this.api=api;
	    globalVariable=new GlobalVariable<K,V,D,E>(workerNum,api);	
	    threshold=api.getThreshold();
	}
	D getThreshold(){
		return threshold;
	}
}
