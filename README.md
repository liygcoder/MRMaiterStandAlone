MRMaiterStandAlone
=================================== 
MRMaiterStandAlone简介
-----------------------------------
		MRMaiterStandAlone是基于DAIC计算模型，支持多副本顶点计算的图计算框架的一个单机版实现（具体实现机制请阅读PAGraph）。(分布式版本的MRMaiter是通过修改分布式图处理框架[Maiter](https://github.com/CodingCat/maiter/tree/master/Maiter)实现，在论文发表后将会对其开源。）
		MRMaiterStandAlone使用java语言编写，用多线程来模拟分布式集群，是对MRMaiter计算框架在运行机制上的一个模拟。


MRMaiterStandalone API
-----------------------------------
		用户编写算法需继承MaiterAPI抽象类，实现相关方法：
		readData：数据读取
		initDelta：初始化顶点delta值
		initValue:初始化顶点value值
		priority：指定顶点优先级
		default_v：设定默认值
		accumulate：定义累加计算
		g_func：定义顶点之间消息的计算公司
		counteract：累加计算的内运算
		isGreater：比较两个值的大小
		abs：计算两个值差值，返回这个差值的绝对值
		getThreshold：设定收敛阈值

MRMaiterStandalone RUNNING：
-----------------------------------
```
Parameters：
```
- `runner`:运行的算法的名字
- `worker`:worker线程的数量
- snapshot_interval：计算过程中,终止检测周期
- portion：在一次遍历本地顶点计算中，顶点被调度进行计算的比例
		--sampleLowerBound：在优先级计算中，设置采样的精度（默认10）
		--graph_dir：数据集位置
		--result_dir：计算结果位置
 
### RUNNING Example:
java -jar mr_maiter.jar --runner Pagerank --workers 4  --snapshot_interval 1 --graph_dir input/pagerank --result_dir result/pagerank --portion 1 --sampleLowerBound 10


# VGP
A software package for one-pass Vertex-cut balanced Graph Partitioning.

Based on the publication:

-F. Petroni, L. Querzoni, G. Iacoboni, K. Daudjee and S. Kamali: "Hdrf: Efficient stream-based partitioning for power-law graphs". CIKM, 2015.

If you use the application please cite the paper.

HDRF has been integrated in [GraphLab PowerGraph](https://github.com/dato-code/PowerGraph)!

###Usage:
```
VGP graphfile nparts [options]
```

Parameters:
- `graphfile`: the name of the file that stores the graph to be partitioned.
- `nparts`: the number of parts that the graph will be partitioned into. Maximum value 256.

Options:
- `-algorithm string`  ->  specifies the algorithm to be used (hdrf greedy hashing grid pds dbh). Default hdrf.
- `-lambda double`  ->  specifies the lambda parameter for hdrf. Default 1.
- `-threads integer`  ->  specifies the number of threads used by the application. Default all available processors.
- `-output string`  ->  specifies the prefix for the name of the files where the output will be stored (files: prefix.info, prefix.edges and prefix.vertices).


For a more in-depth discussion see the manual.

###Example

```
java -jar mr_maiter.jar example/sample_graph.txt 4 -algorithm hdrf -lambda 3 -threads 1 -output example/output  
```


