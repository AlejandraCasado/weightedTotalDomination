package wtdp.improve;


import grafo.optilib.metaheuristics.Improvement;
import grafo.optilib.structure.Solution;
import grafo.optilib.tools.RandomManager;
import jdk.swing.interop.SwingInterOpUtils;
import wtdp.Main;
import wtdp.structure.WTDPInstance;
import wtdp.structure.WTDPSolution;

import java.util.*;

public class Exhaustive_Local_Search implements Improvement<WTDPSolution> {
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
                sol.addNode(nodeAdd);
                int ofNew = sol.objectiveFunction();
                if (ofNew < of && sol.isFeasible()) {
                    return true;
                }
                sol.removeNode(nodeAdd);
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
