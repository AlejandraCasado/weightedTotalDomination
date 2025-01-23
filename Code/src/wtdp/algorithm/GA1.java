package wtdp.algorithm;

import grafo.optilib.metaheuristics.Algorithm;
import grafo.optilib.results.Result;
import grafo.optilib.structure.Solution;
import grafo.optilib.tools.RandomManager;
import grafo.optilib.tools.Timer;
import wtdp.structure.WTDPInstance;
import wtdp.structure.WTDPSolution;

import java.util.*;

//import java.util.*;

public class GA1 implements Algorithm<WTDPInstance, WTDPSolution> {

    private int initialPopulationSize;
    private int populationSize;
    private int cutoff;
    private int ml;
    private int mu;
    private int nIterations;
    private WTDPSolution best;

    public GA1(int initialPopulationSize, int populationSize, int cutoff, int ml, int mu, int nIterations) {
        this.initialPopulationSize = initialPopulationSize;
        this.populationSize = populationSize;
        this.cutoff = cutoff;
        this.ml = ml;
        this.mu = mu;
        this.nIterations = nIterations;
    }

    @Override
    public Result execute(WTDPInstance instance) {
        Timer.initTimer();
        System.out.println(instance.getName());
        Result r = new Result(instance.getName());
        List<WTDPSolution> population = new ArrayList<>();
        float time=0.0f;
        for (int i = 0; i < initialPopulationSize && time<1800; i++) {
            WTDPSolution sol = new WTDPSolution(instance);
            grasp(instance, cutoff, sol);
            if (!isInPopulation(sol, population)) {
                population.add(sol);
            }
            time=Timer.getTime()/1000f;
        }
        population.sort(Comparator.comparingInt(WTDPSolution::objectiveFunction));
        for (int i = 0; i < nIterations; i++) {
            for (int j = 0; j < populationSize && time<1800; j++) {
                WTDPSolution d1 = population.get(j);
                for (int k = j+1; k < populationSize && time<1800; k++) {
                    WTDPSolution d2 = population.get(k);
                    WTDPSolution newD = new WTDPSolution(d1);
                    for(int node:d2.getSolution()){
                        newD.addNode(node);
                    }
                    grasp(instance, cutoff, newD);
                    mutation(instance, newD, ml, mu);
                    localSearch(instance, newD);
                    if (!isInPopulation(newD, population)) {
                        population.add(newD);
                    }
                    time=Timer.getTime()/1000f;
                }
            }
            population.sort(Comparator.comparingInt(WTDPSolution::objectiveFunction));
        }
        System.out.println(population.get(0).objectiveFunction()+"\t"+(Timer.getTime()/1000f)+"\t"+population.get(0).isFeasible());
        r.add("OF", population.get(0).objectiveFunction());
        r.add("Time", Timer.getTime()/1000f);
        return r;
    }

    private boolean isTotalDominatingSet(WTDPSolution sol, int i) {
        WTDPSolution newSol = new WTDPSolution(sol);
        newSol.removeNode(i);
        return newSol.isFeasible();
    }

    private int minCostToSelected(WTDPInstance instance, Set<Integer> dh, int i, int exclude) {
        int minWij = 0x3f3f3f3f;
        for (int j : dh) {
            if (j == exclude) continue;
            int wij = instance.getEdgeWeight(i,j);
            minWij = Math.min(wij, minWij);
        }
        return minWij;
    }

    private void grasp(WTDPInstance instance, int cutoff, WTDPSolution initialSolution) {
        int n = instance.getNumNodes();
        boolean createEmpty = initialSolution.getSolution().isEmpty();
        int[] score = new int[n];
        for (int i = 0; i < n; i++) {
            if (createEmpty) {
                initialSolution.addNode(i);
            }
            score[i] = -0x3f3f3f3f;
        }
        boolean improvingMoveExists = false;
        do {
            improvingMoveExists = false;
            int vertexToRemove = -1;
            int bestScore = 0;
            for (int i : initialSolution.getSolution()) {
                if (!isTotalDominatingSet(initialSolution, i)) {
                    continue;
                }
                if (score[i] == -0x3f3f3f3f) {
                    score[i] = instance.getNodeWeight(i);
                    for (int j : instance.getAdjacent(i)) {
                        if (initialSolution.getSolution().contains(j)) {
                            score[i] += instance.getEdgeWeight(i,j);
                        }
                    }
                    int wStar = minCostToSelected(instance, initialSolution.getSolution(), i, -1);
                    score[i] = score[i] - wStar;
                    for (int j : instance.getAdjacent(i)) {
                        if (!initialSolution.getSolution().contains(j)) {
                            int wjStar = minCostToSelected(instance, initialSolution.getSolution(), j, i);
                            score[i] = score[i] - wjStar;
                        }
                    }
                }
                if (score[i] > bestScore && RandomManager.getRandom().nextInt(100) >= cutoff) {
                    bestScore = score[i];
                    vertexToRemove = i;
                    improvingMoveExists = true;
                }
            }
            if (improvingMoveExists) {
                initialSolution.removeNode(vertexToRemove);
                for (int j : instance.getAdjacent(vertexToRemove)) {
                    for (int jPrime : instance.getAdjacent(j)) {
                        score[jPrime] = -0x3f3f3f3f;
                    }
                }
            }
        } while (improvingMoveExists);
    }

    private void localSearch(WTDPInstance instance, WTDPSolution sol) {
        boolean improvingMoveExists = false;
        int n = instance.getNumNodes();
        do {
            improvingMoveExists = false;
            for (int i = 0; i < n; i++) {
                if (!sol.getSolution().contains(i)) {
                    int ofPrev = sol.objectiveFunction();
                    sol.addNode(i);
                    int of = sol.objectiveFunction();
                    if (of < ofPrev && sol.isFeasible()) {
                        improvingMoveExists = true;
                        break;
                    }
                    sol.removeNode(i);
                }
            }
            if (!improvingMoveExists) {
                for (int i = 0; i < n; i++) {
                    if (sol.getSolution().contains(i)) {
                        int ofPrev = sol.objectiveFunction();
                        sol.removeNode(i);
                        int of = sol.objectiveFunction();
                        if (of < ofPrev && sol.isFeasible()) {
                            improvingMoveExists = true;
                            break;
                        }
                        sol.addNode(i);
                    }
                }
            }
        } while (improvingMoveExists);
    }

    private boolean isInPopulation(WTDPSolution sol, List<WTDPSolution> population) {
        for (WTDPSolution solPopulation : population) {
            if (solPopulation.objectiveFunction() == sol.objectiveFunction() || solPopulation.getSolution().equals(sol.getSolution())) {
                return true;
            }
        }
        return false;
    }

    private void mutation(WTDPInstance instance, WTDPSolution newD, int ml, int mu) {
        int remove = ml + RandomManager.getRandom().nextInt(mu-ml+1);
        int n = newD.getSolution().size();
        int nodeToRem = -1;
        List<Integer> selectedCopy=new ArrayList<>(newD.getSolution());
        for (int i = 0; i < remove; i++) {
            int rnd=RandomManager.getRandom().nextInt(selectedCopy.size());
            nodeToRem=selectedCopy.get(rnd);
            selectedCopy.remove(rnd);
            newD.removeNode(nodeToRem);
        }
        if(!newD.isFeasible()){
            int nNodes = newD.getUnselected().size();
            List<int[]> nodes = new ArrayList<>(nNodes);
            for (int j:newD.getUnselected()) {
                nodes.add(new int[]{j, instance.getAdjacent(j).size()});
            }
            nodes.sort((o1, o2) -> -Integer.compare(o1[1], o2[1]));
            int next = 0;
            while (!newD.isFeasible()) {
                newD.addNode(nodes.get(next)[0]);
                next++;
            }
        }
    }

    @Override
    public WTDPSolution getBestSolution() {
        return best;
    }

    @Override
    public String toString() {
        return "GA_Previo{" +
                "initialPopulationSize=" + initialPopulationSize +
                ",populationSize=" + populationSize +
                ",cutoff=" + cutoff +
                ",ml=" + ml +
                ",mu=" + mu +
                ",nIterations=" + nIterations +
                '}';
    }
}
