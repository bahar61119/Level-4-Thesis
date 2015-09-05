# THESIS : Dissemination of Rich Media Contents in Disruption-tolerant Network
(Maruf Hasan Zaber, Mehrab Bin Morshed, Md. Habibullah Bin Ismail)

Dissemination of a media content in an opportunistic network is proportionally related to the capacity of connections. 
While the assumption, any connection has capacity to transfer data of any size holds true for analytical case, 
such assumption runs in vein for real scenario. In this poster, we argued to segment a large data content into chunks for progressive dissemination 
and designed a novel strategy for selecting which chunk to select for dissemination in a particular p2p communication.

#SYSTEM ARCHITECTURE
* Data is segmented into chunks depending on the capacity of the network.
* Each chunk corresponds to the relevant bit   of a bitstring of length equals to chunk number.
* Initially a set seeds  contains all the chunks.
* When two nodes meet they discover the set of deliverables by bitwise operations.
* Nodes send chunks to other nodes from deliverable set and vice versa.
* Exchange continues until both nodes have the union of chunks.
* Which Chunk to select for sending
	1.Random –  randomly selected from deliverable set. 
	2.Round Robin – sequentially selected from deliverable set.
	3.LDCF –least disseminated chunk in the network selected from deliverable set.
	4.Epidemic scheme.
* Using Opportunistic Network Environment simulator (ONE) four dissemination schemes were simulated.
* Helsinki City map was used for simulation. 
* Real-time movement trace of Helsinki City was used.

#RESULTS
* Simulation was aggregated over 4 different movement-trace and 600 simulation runs.
* RR first reaches to saturation compared to Random, LDCF and Epidemic scheme.
* Convergence speeds up by the spreading of infected  nodes.
* Saturation time is inversely proportional to chunk size. 1MB and 2MB chunk size yield same performance while 4MB and 10MB causes degradation of performance.
* Saturation curve resulted from the simulation is identical to convergence curve of α.

#IMPLEMENTATION

* This android application was built exploiting the seed selection and dissemination schemes.
* Underlying communication technology was Wi-Fi Direct.
* We implemented Round Robin in this application.



 






