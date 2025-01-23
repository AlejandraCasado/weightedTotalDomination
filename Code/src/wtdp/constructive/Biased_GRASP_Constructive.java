package wtdp.constructive;

import grafo.optilib.metaheuristics.Constructive;
import grafo.optilib.tools.RandomManager;
import wtdp.Main;
import wtdp.structure.WTDPInstance;
import wtdp.structure.WTDPSolution;

import java.util.*;
import java.util.List;

public class Biased_GRASP_Constructive implements Constructive<WTDPInstance, WTDPSolution> {

    private class Candidate {
        private int node;
        private double probability;
        private double greedyValue;
        public Candidate (int node) {
            this.node=node;
        }
        public int getNode () {
            return node;
        }
        public double getProbability () {
            return probability;
        }
        public double getGreedyValue () {
            return greedyValue;
        }
        public void setGreedyValue (double greedyValue) {
            this.greedyValue = greedyValue;
        }
        public void setProbability (double probability) {
            this.probability = probability;
        }
    }
    class SortByProbability implements Comparator<Candidate>
    {
        @Override
        public int compare(Candidate c1, Candidate c2) {
            if (c1.getProbability() > c2.getProbability()) return -1;
            else if (c1.getProbability() < c2.getProbability()) return 1;
            return 0;
        }
    }

    private List<Candidate> cl;
    private int[] numberAppearances;
    private double alpha;
    private double amountProbabilities;

    public void setNumberAppearances(int [] numberAppearances){
        this.numberAppearances=numberAppearances;
    }

    private void updateNumberAppearances(WTDPSolution sol){
        for (int node:sol.getSolution()) {
            numberAppearances[node]-=1;
        }
    }

    @Override
    public WTDPSolution constructSolution (WTDPInstance instance) {
        WTDPSolution sol=new WTDPSolution(instance);
        WTDPSolution bestSol=new WTDPSolution(instance);
        int bestSolOF=0x3f3f3f3f;
        int newOF;
        alpha=Main.alpha;
        if(alpha==-1){
            alpha=RandomManager.getRandom().nextFloat(0.99f)+0.01f;
        }
        initializeCandidateList(instance);
        int firstNode=RandomManager.getRandom().nextInt(instance.getNumNodes());
        addNode(firstNode,sol,firstNode);
        while (sol.checkStopSearching(bestSolOF)){
            calculateProbability(sol);
            addNodeFromCLLinear(sol);
            newOF=sol.objectiveFunction();
            if(bestSolOF>newOF && sol.isFeasible()){
                bestSolOF=newOF;
                bestSol.copy(sol);
            }
        }
        updateNumberAppearances(bestSol);

        return bestSol;
    }

    private void addNode(int node,WTDPSolution sol, int id){
        sol.addNode(node);
        cl.remove(id);
    }

    private void initializeCandidateList (WTDPInstance instance) {
        cl=new ArrayList<>(instance.getNumNodes());
        for(int i=0;i<instance.getNumNodes();i++){
            cl.add(new Candidate(i));
        }
    }

    private void calculateProbability (WTDPSolution sol) {
        amountProbabilities=0;
        double max=0;
        double min=0x3F3F3F3F;
        for(Candidate candidate:cl){
            double greedyValue;
            greedyValue=sol.calculateNodeRatio(candidate.getNode());
            candidate.setGreedyValue(greedyValue);
            if(min>greedyValue){
                min=greedyValue;
            }
            if(max<greedyValue){
                max=greedyValue;
            }
        }
        for(Candidate candidate:cl){
            double prob = (max==min) ? candidate.getGreedyValue() : (candidate.getGreedyValue()-min)/(max-min);
            amountProbabilities+=prob;
            candidate.setProbability(prob);
        }
    }

    private void addNodeFromCLLinear (WTDPSolution sol) {
        if(sol.isDifferentRatio() || !sol.isFeasible()){
            double rnd=RandomManager.getRandom().nextDouble();
            double cont=0;
            cl.sort(new Biased_GRASP_Constructive.SortByProbability());
            for(int i=0; i<cl.size();i++){
                Candidate candidate=cl.get(i);
                cont+=candidate.getProbability()/amountProbabilities;
                if(cont>rnd){
                    addNode(candidate.getNode(),sol,i);
                    break;
                }
            }
        }else{
            int rnd=RandomManager.getRandom().nextInt(cl.size());
            addNode(cl.get(rnd).getNode(),sol,rnd);
        }
    }

    public String toString () {
        String str=Main.alpha==-1?"rnd":(""+Main.alpha);
        return this.getClass().getSimpleName()+"_"+str+"_";
    }
}