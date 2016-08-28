package core;

import java.lang.reflect.Method;
import java.util.*;

//因为java中不支持泛型数组的定义，因此暂时所以就没有采用泛型 <int,double,double,Vector<Integer>>

import data.DataStructure;

public class MaiterApplication<K,V,D,E>{
 
	/**
	 * variable
    */
    static GlobalInformation global;
	/**
	 * method
    */
    public MaiterApplication(){
    }
    static void invokeStaticMethod(String className, String methodName) throws Exception {	
		Class ownerClass = Class.forName(className);
		Method[] methods=ownerClass.getDeclaredMethods();
		for(Method m:methods){
			if(m.getName().equals(methodName)){
				//System.out.println(className);
				m.invoke(null, null);
				break;
			}
		}
	}
	public void maiterRun(){//Maiter running
		try {
			//System.out.println("Maiter runing");
			MaiterAPI<K, V, D, E> api = (MaiterAPI<K,V,D,E>)Class.forName("app."+global.algorithm).newInstance();
			Helper<K, V, D, E> helper=new Helper<K, V, D, E>(global.workerNum,api);
			ArrayList<DataStructure<K,V,D,E>> data=new ArrayList<DataStructure<K,V,D,E>>();	
			ArrayList<Worker<K,V,D,E>> workers=new ArrayList<Worker<K,V,D,E>>();
			for(int i=0;i<global.workerNum;++i){
				data.add(new DataStructure<K,V,D,E>(global.workerNum,i+1,global.sampleLowerBound,api)); 
			}
			for(int i=0;i<global.workerNum;++i){
				workers.add(new Worker<K,V,D,E>(i+1,data,global,helper));
			}
			Master<K,V,D,E> master = new Master<K,V,D,E>(data,global,helper);
			//threads of workers and master start
			master.start();
			for(Worker<K,V,D,E> worker:workers){
				worker.start();
			}
		} catch (InstantiationException | IllegalAccessException| ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
    
	public static void main(String[] args) {
        //Maiter Entrance
		System.out.println("Welcome to use maiter ...");
		//read parameter
        global=new GlobalInformation();
		if(!global.readArgs(args)){
			System.out.println("error:read parameter");
			System.exit(1);
		}else{
			System.out.println("success:read parameter");
		}
		//running the application algorithm
		try {
			String className="app."+global.algorithm;
			String methodName="startMaiter";
			invokeStaticMethod(className,methodName);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
}
