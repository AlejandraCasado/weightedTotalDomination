package wtdp.improve;


import grafo.optilib.metaheuristics.Improvement;
import grafo.optilib.tools.RandomManager;
import wtdp.Main;
import wtdp.structure.WTDPSolution;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Predictive_Local_Search implements Improvement<WTDPSolution> {
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
            for (int nodeAdd : copyUnselected) {
                int ofNew = sol.calculateOFAdding(nodeAdd, nodeRemove);
                boolean improve = ofNew < of && isFeasibleTest(sol, nodeAdd, nodeRemove);
                if (improve) {
                    sol.removeNode(nodeRemove);
                    sol.addNode(nodeAdd);
                    sol.setOFValue(ofNew);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    private boolean isFeasibleTest(WTDPSolution sol, int addNode, int removeNode){
        sol.addNode(addNode);
        sol.removeNode(removeNode);
        boolean feasible=sol.isFeasible();
        sol.addNode(removeNode);
        sol.removeNode(addNode);
        return feasible;
    }

}
