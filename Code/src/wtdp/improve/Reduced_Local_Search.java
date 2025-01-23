package wtdp.improve;

import grafo.optilib.metaheuristics.Improvement;
import grafo.optilib.tools.RandomManager;
import wtdp.structure.WTDPSolution;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Reduced_Local_Search implements Improvement<WTDPSolution> {

    @Override
    public void improve(WTDPSolution solution) {
        boolean improve = true;
        while(improve){
            improve=checkImprove(solution);
        }
    }

    private boolean checkImprove(WTDPSolution sol){
        List<Integer> copySelected=new ArrayList<>(sol.getSolution());
        List<Integer> copyUnselected=new ArrayList<>(sol.getUnselected());
        Collections.shuffle(copyUnselected,RandomManager.getRandom());
        Collections.shuffle(copySelected, RandomManager.getRandom());
        int of=sol.objectiveFunction();
        for (int nodeRemove : copySelected) {
            sol.removeNode(nodeRemove);
            for (int nodeAdd : copyUnselected) {
                if(sol.partialIsFeasibleAdd(nodeRemove,nodeAdd)){
                    sol.addNode(nodeAdd);
                    int ofNew = sol.objectiveFunction();
                    if (ofNew < of) {
                        return true;
                    }
                    sol.removeNode(nodeAdd);
                }
            }
            sol.addNode(nodeRemove);
        }
        return false;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

}
