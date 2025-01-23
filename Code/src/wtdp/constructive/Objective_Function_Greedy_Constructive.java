
package wtdp.constructive;

import grafo.optilib.metaheuristics.Constructive;
import wtdp.structure.WTDPInstance;
import wtdp.structure.WTDPSolution;

public class Objective_Function_Greedy_Constructive implements Constructive<WTDPInstance, WTDPSolution> {

    @Override
    public WTDPSolution constructSolution(WTDPInstance instance) {
        WTDPSolution sol=new WTDPSolution(instance);
        WTDPSolution bestSol=new WTDPSolution(instance);
        int bestSolOF=0x3F3F3F3F;
        int newOF=0;
        while (sol.checkStopSearching(bestSolOF)){
            sol.addBestNextNodeOF();
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
