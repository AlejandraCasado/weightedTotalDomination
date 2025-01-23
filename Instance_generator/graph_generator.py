import networkx as nx
import random

nVals = [200, 500, 1000]
pVals = [0.2, 0.5, 0.8]
weights = (((1,50), (1,10)), ((1,25), (1,25)), ((1,10), (1,50)))
nGraphs = 5

for n in nVals:
    for p in pVals:
        for cw in weights:
            c = cw[0]
            w = cw[1]
            for i in range(nGraphs):
                fileName = "graphs/CSGM_"+str(n)+"_"+str(p)+"_"+str(c[1])+"_"+str(w[1])+"_"+str(i)+".txt"
                print("GENERATING "+fileName, end= " ... ")
                with open(fileName, "w") as f:
                    g = nx.fast_gnp_random_graph(n, p)
                    while not nx.is_connected(g):
                        g = nx.fast_gnp_random_graph(n, p)
                    f.write(str(n)+" "+str(len(g.edges))+" "+str(c[1])+" "+str(w[1])+"\n")
                    for v in range(n):
                        rndW = random.randint(w[0],w[1])
                        f.write(str(v)+" "+str(rndW)+"\n")
                    idxEdge = 0
                    for e in g.edges:
                        rndC = random.randint(c[0], c[1])
                        f.write(str(idxEdge)+" "+str(e[0])+" "+str(e[1])+" "+str(rndC)+"\n")
                        idxEdge += 1
                print("DONE")



