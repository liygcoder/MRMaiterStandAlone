package core;

import java.util.ArrayList;
import java.util.Scanner;



import util.TaskCheak;
import util.TaskStart;

import data.DataStructure;

public class Master<K,V,D,E> extends Thread {
    TaskStart tStart;
    TaskCheak tCheak;
	long checkPeriod;//检测周期
	D threshold;
	MaiterKernel<K,V,D,E> kernel;
	GlobalVariable<K,V,D,E> globalVariable;
	MaiterAPI<K,V,D,E> api;
	Master(ArrayList<DataStructure<K,V,D,E>> data,GlobalInformation global,Helper<K, V, D, E> helper){
		tStart = helper.tStart;
		tCheak = helper.tCheak;
		checkPeriod = global.snapshot;//millisecond
		threshold = helper.getThreshold();
	    globalVariable=helper.globalVariable;
	    api=helper.api;
	    kernel =new MaiterKernel<K,V,D,E>(api,data,1);
	}
	
	public void run(){//thread entrance
		//System.out.println("Maiter starts to run...");
	 	long startTime = System.currentTimeMillis();//start_time
		long kernelStartTime= startTime;//kernel1 start_time
		/*
		 * kernel1:read input_data
		 */
	 	//notice workers kernel1 start
	 	System.out.println("\nkernel1: starts to read intputdata");
	 	try {
	 		tStart.synchronous(1,0);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		try {//To check whether kernel1 finished
			tCheak.synchronous(1,0);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		long kernelEndTime = System.currentTimeMillis();
		long kernelTime=(kernelEndTime-kernelStartTime);
		System.out.println("kernel1:time="+kernelTime+"ms");//second
		
		
		/*
		 * kernel2:iterator computing
		*/
	    kernelStartTime = kernelEndTime;
	    //notice workers kernel2 start
	 	System.out.println("\nkernel2: starts to Iterative compute");
	 	try {
	 		tStart.synchronous(2,0);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	 	//To check whether kernel2 finished
	 	terminate();
		kernelEndTime = System.currentTimeMillis();
		kernelTime=(kernelEndTime-kernelStartTime);
		System.out.println("kernel2:time="+kernelTime+"ms");
	    System.out.println("\nkernel3: starts to write result");
	    
		/*
	 	 * kernel3:write resutlt
	 	 */
		kernelStartTime = kernelEndTime;
		//To check whether kernel3 finished
		try {
			tCheak.synchronous(1,0);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
//		try {
//			sleep(100);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	 	kernelEndTime = System.currentTimeMillis();
		kernelTime=(kernelEndTime-kernelStartTime);
		System.out.println("kernel3:time="+kernelTime+"ms");
		System.out.println("All time="+(kernelEndTime-startTime+"ms"));
	 	//Algorithm teminate
	 	System.out.println("success");
	 	System.out.println("Maiter stopped！");
	}
	
    void terminate() {
    	boolean termination = false;
	    while(!termination){	
	      try {//master checkPeriod 
	    	  sleep(checkPeriod);
	    	  D aggregationGlobal = globalVariable.readAggregation();
	    	  if(aggregationGlobal.equals(api.default_v()))continue;
	    	  //As long as there is a workernot received, will continue to wait
	    	  //System.out.println("111aggregationGlobal="+aggregationGlobal);
	          termination = kernel.terminate(threshold,aggregationGlobal);
		   } catch (InterruptedException e) {
			  e.printStackTrace();
		   }
		}
	    IteratorFinished.writeIteratorFinished(true);
	}
}
