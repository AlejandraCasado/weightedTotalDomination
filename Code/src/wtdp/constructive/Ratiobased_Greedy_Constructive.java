package wtdp.constructive;

import grafo.optilib.metaheuristics.Constructive;
import wtdp.structure.WTDPInstance;
import wtdp.structure.WTDPSolution;

public class Ratiobased_Greedy_Constructive implements Constructive<WTDPInstance, WTDPSolution> {

    @Override
    public WTDPSolution constructSolution(WTDPInstance instance) {
        WTDPSolution sol=new WTDPSolution(instance);
        WTDPSolution bestSol=new WTDPSolution(instance);
        int bestSolOF=0x3f3f3f3f;
        int newOF=0;
        while (sol.checkStopSearching(bestSolOF)){
            sol.addBestNextNodeRatio();
            newOF=sol.objectiveFunction();
            if(bestSolOF>newOF && sol.isFeasible()){
                bestSolOF=newOF;
                bestSol.copy(sol);
            }
        }
        return bestSol;
    }

    public String toString(){
        return this.getClass().getSimpleName();
    }

}
