package core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Vector;



import util.TaskCheak;
import util.TaskStart;

import data.DataStructure;

public class Worker<K,V,D,E> extends Thread{
    int workerId;
    ArrayList<DataStructure<K,V,D,E>> data;
	String inputPath;
    String outputPath;
    TaskStart tStart;
    TaskCheak tCheak;
    MaiterKernel<K,V,D,E> kernel;
    int workerNum;
    GlobalVariable<K,V,D,E> globalVariable;
    double partion;
    double checkPeriod;
	public Worker(int workerId,ArrayList<DataStructure<K,V,D,E>> data,GlobalInformation global,Helper<K, V, D, E> helper){
		this.workerId = workerId;
		this.data = data;
		this.tStart = helper.tStart;
		this.tCheak = helper.tCheak;
		kernel = new MaiterKernel<K,V,D,E>(helper.api,data,workerId);
		inputPath = global.inputPath;
		outputPath = global.outputPath;
		checkPeriod = global.snapshot;
		workerNum = global.workerNum;
	    globalVariable=helper.globalVariable;
		this.partion=global.partion;
	}
	
	public void run(){
		//**To check whether kernel1 start
		try {
			tStart.synchronous(1,workerId);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("worker"+workerId+":I am reading the data");
		if(!readDate()){System.out.println("read data failed!");System.exit(0);}
		System.out.println("worker"+workerId+":I have read the data");
		//*kernel1 finish roport
		try {
			tCheak.synchronous(1,workerId);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//showBuckets(); //for show the inputdata
		
		//**To check whether kernel2 start
		try {
			tStart.synchronous(2,workerId);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("worker"+workerId+":I am computing");
		try {//start iterator computing,and periodically check at the same time.If the value of WriteStart is true,kernel2 end.
            runLoop();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}

		//**kernel3 start
		System.out.println("worker"+workerId+":I am writing the result");
		if(!writeResult()){System.out.println("error:write result");System.exit(0);}
		System.out.println("I am worker"+workerId+":I have written the data");
		//**kernel3 finish report
		try {
			tCheak.synchronous(3,workerId);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("worker"+workerId+" stopped！");
	}

	boolean readDate(){
		inputPath = inputPath +"/part"+ (workerId-1);
		return (kernel.readDataKernel(inputPath));//call kernel.read
	}	
	void showBuckets(){
		kernel.showBuckets();//show data for test
	}

	void runLoop() throws InterruptedException{
		long lastCheckTime=0;
		long currCheckTime = System.currentTimeMillis(); 
		int step=0;
		while(true){step++;
		//for(step=1;step<=100;++step){
		    //System.out.println("worker"+workerId+":The "+step+" time iterator computing");
		    kernel.runLoop(partion);	
			//showBuckets();System.out.println();
			currCheckTime = System.currentTimeMillis(); 
			if(Math.abs(currCheckTime-lastCheckTime)>=checkPeriod){
				if(IteratorFinished.readIteratorFinished()){break;}   //Terminate kernel2 and start kernel3
				estimate_prog();
				lastCheckTime = currCheckTime;
			}
		}
	}
	void estimate_prog() throws InterruptedException{
		//Collect the local progress
		D aggregationLocal = kernel.estimate_prog();//get the sum of all the vertics`value
		globalVariable.updateAggregation(workerId, aggregationLocal);//update the local sum to the master
		//System.out.println("worker_"+workerId+": aggregationLocal="+aggregationLocal);
	}
    boolean writeResult() {
		// kernel 3:write result
    	outputPath = outputPath +"/part_"+ (workerId-1);
		return (kernel.writeResultKernel(outputPath));
	}
	
}
