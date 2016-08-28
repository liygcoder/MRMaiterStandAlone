package util;
//check the task whether has finished 
public class TaskCheak {
    boolean cheakTab[][];//
    int workerNum;
    int kernel;
    public TaskCheak(int num){
    	workerNum=num;
		cheakTab =new boolean[3][workerNum];
		for(int i=0;i!=3;++i){
			for(int j=0;j!=workerNum;++j){
				cheakTab[i][j]=false;
			}
		}
	}
	  public synchronized void  synchronous(int kernel_,int id) throws InterruptedException{
		  kernel=kernel_-1;
		  if(id==0){//master
			boolean allFinish=false;
			while(!allFinish){
				wait();//active master,when any worker finish the task
				allFinish=true;
				for(int i=0;i!=workerNum;++i){
					allFinish=allFinish&&cheakTab[kernel][i];
				}
			}
		}
		else{//worker
			cheakTab[kernel][id-1]=true;
			notifyAll();
		}
	 }
}
