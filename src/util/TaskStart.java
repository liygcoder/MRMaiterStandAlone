package util;
//check the task whether start
public class TaskStart{
	 private boolean start[] = {false,false,false};
	 int kernel;
	 public synchronized void  synchronous(int kernel_,int id) throws InterruptedException{
		 kernel=kernel_-1;
		 if(0==id){
		    start[kernel]=true;
			notifyAll();
		}
		else{
			if(!start[kernel]){//wait the command of task from master
				wait();
			}
		}
	}
}
