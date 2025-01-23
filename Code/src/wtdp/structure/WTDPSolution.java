package wtdp.structure;

import grafo.optilib.structure.Solution;
import wtdp.Main;

import java.util.*;

public class WTDPSolution implements Solution {

    private WTDPInstance instance;
    private Set<Integer> solution;
    private Set<Integer> unselected;
    private int vertexSelectionWeight;
    private int internalEdgeWeight;
    private Set<Integer>[] dominatedBy;
    private boolean flagCalculateOF =false;
    private boolean isFeasible=false;
    private int lastOFValue =0;
    private boolean flagCalculateFeasibility=false;
    private boolean differentRatio=true;

    public WTDPSolution(WTDPInstance instance){
        this.instance=instance;
        vertexSelectionWeight =0;
        dominatedBy=new HashSet[instance.getNumNodes()];
        unselected=new HashSet<>(instance.getNumNodes());
        solution=new HashSet<>();
        for(int i=0; i<instance.getNumNodes();i++){
            dominatedBy[i]=new HashSet<Integer>();
            unselected.add(i);
        }
    }

    public WTDPSolution(WTDPSolution sol) {
        copy(sol);
    }

    public void addBestNextNodeRatio(){
        float maxValue=-1;
        int bestNode=-1;
        for(int unselectedNode:unselected){
            float nodeValue=calculateNodeRatio(unselectedNode);
            if(maxValue<nodeValue){
                maxValue=nodeValue;
                bestNode=unselectedNode;
            }
        }
        addNode(bestNode);
    }

    public float calculateNodeRatio(int unselectedNode){
        int internalEdgeWeight=calculateInternalEdgeWeight(unselectedNode);
        int dominatedWeight=0;
        int lowestExternalWeight=0x3F3F3F3F;
        for (int neighbor: instance.getAdjacent(unselectedNode)) {
            if(dominatedBy[neighbor].isEmpty()){
                dominatedWeight+= instance.getNodeWeight(neighbor);
            }
            if(solution.contains(neighbor) && lowestExternalWeight>instance.getEdgeWeight(unselectedNode,neighbor)){
                lowestExternalWeight= instance.getEdgeWeight(unselectedNode,neighbor);
            }
        }
        if(!isFeasible() || !differentRatio){
            return dominatedWeight/((instance.getNodeWeight(unselectedNode)+internalEdgeWeight)*1.0f);
        }else{
            return (instance.getNodeWeight(unselectedNode)+lowestExternalWeight)/(internalEdgeWeight*1.0f);
        }
    }

    public int calculateNodeOF(int node){
        int internalEdgeWeightAux=calculateInternalEdgeWeight(node);
        return instance.getNodeWeight(node)+internalEdgeWeightAux; //+externalEdgeWeight;
    }

    public void addBestNextNodeOF(){
        int minValue=0x3F3F3F;
        int bestNode=-1;
        for(int node:unselected){
            int value=calculateNodeOF(node);
            if(minValue>value){
                minValue=value;
                bestNode=node;
            }
        }
        addNode(bestNode);
    }

    public boolean checkStopSearching(int bestOF){
        return !unselected.isEmpty() && bestOF>(internalEdgeWeight+vertexSelectionWeight);
    }

    public int calculateInternalEdgeWeight(int node){
        int internalEdgeWeightAux=0;
        for(int selected:solution){
            internalEdgeWeightAux+=instance.getEdgeWeight(selected,node);
        }
        return internalEdgeWeightAux;
    }

    public void addNode(int node){
        flagCalculateOF =true;
        flagCalculateFeasibility=true;
        solution.add(node);
        unselected.remove(node);
        for (int neighbor: instance.getAdjacent(node)) {
            dominatedBy[neighbor].add(node);
        }
        vertexSelectionWeight +=instance.getNodeWeight(node);
        internalEdgeWeight+=calculateInternalEdgeWeight(node);
    }

    public void removeNode(int node){
        flagCalculateOF =true;
        flagCalculateFeasibility=true;
        solution.remove(node);
        unselected.add(node);
        for (int neighbor: instance.getAdjacent(node)) {
            dominatedBy[neighbor].remove(node);
        }
        vertexSelectionWeight -=instance.getNodeWeight(node);
        internalEdgeWeight-=calculateInternalEdgeWeight(node);
    }

    public int objectiveFunction(){
        int externalEdgeWeight=0;
        if(flagCalculateOF){
            for(int unselected:unselected){
                externalEdgeWeight+=calculateMinExternalWeight(unselected);
            }
            if(externalEdgeWeight<0x3f3f3f3f){
                flagCalculateOF =false;
                lastOFValue=vertexSelectionWeight +externalEdgeWeight+ internalEdgeWeight;
            }
        }
        return lastOFValue;
    }

    public int calculateMinExternalWeight(int unselected){
        int min=0x3f3f3f3f;
        for(int node:solution){
            if(instance.areAdjacent(unselected,node) && min>instance.getEdgeWeight(unselected,node)){
                min=instance.getEdgeWeight(unselected,node);
            }
        }
        return min;
    }

    public boolean isFeasible(){
        if(flagCalculateFeasibility){
            for(int i=0;i<instance.getNumNodes();i++){
                if(dominatedBy[i].isEmpty()){
                    isFeasible=false;
                    return false;
                }
            }
            isFeasible=true;
            flagCalculateFeasibility=false;
        }
        return isFeasible;
    }

    public boolean partialIsFeasibleAdd(int nodeRemove, int nodeAdd){
        for(int i:instance.getAdjacent(nodeRemove)){
            if(!instance.areAdjacent(i,nodeAdd) && dominatedBy[i].isEmpty()){
                return false;
            }
        }
        flagCalculateFeasibility=false;
        return true;
    }

    public int calculateOFAdding(int addNode, int removeNode){
        int externalEdgeWeight=0;
        int min=0x3f3f3f;
        for(int i=0;i<instance.getNumNodes();i++){
            if(instance.areAdjacent(removeNode,i) && min>instance.getEdgeWeight(removeNode,i) && (solution.contains(i) || i==addNode) && i!=removeNode){
                min=instance.getEdgeWeight(removeNode,i);
            }
        }
        externalEdgeWeight+=min;
        for(int unselected:unselected){
            if(unselected!=addNode){
                min=0x3f3f3f;
                for(int i=0;i<instance.getNumNodes();i++){
                    if(instance.areAdjacent(unselected,i) && min>instance.getEdgeWeight(unselected,i) && (solution.contains(i) || i==addNode) && i!=removeNode){
                        min=instance.getEdgeWeight(unselected,i);
                    }
                }
                externalEdgeWeight+=min;
            }
        }
        int vertexWeight= vertexSelectionWeight+instance.getNodeWeight(addNode)-instance.getNodeWeight(removeNode);
        int internalEdgeWeightAux=0;
        for(int selected:solution){
            if(selected!=removeNode){
                internalEdgeWeightAux+=instance.getEdgeWeight(selected,addNode);
            }
        }
        int internalWeight= internalEdgeWeight+internalEdgeWeightAux-calculateInternalEdgeWeight(removeNode);
        return vertexWeight +externalEdgeWeight+ internalWeight;
    }

    public void copy(WTDPSolution sol){
        this.instance=sol.getInstance();
        this.solution=new HashSet<>(sol.getSolution());
        this.unselected=new HashSet<>(sol.getUnselected());
        this.vertexSelectionWeight=sol.getVertexSelectionWeight();
        this.internalEdgeWeight=sol.getInternalEdgeWeight();
        this.dominatedBy=new HashSet[this.instance.getNumNodes()];
        for(int i=0; i<this.instance.getNumNodes();i++){
            this.dominatedBy[i]=new HashSet<>(sol.getDominatedBy()[i]);
        }
        this.lastOFValue=sol.getLastOFValue();
        this.flagCalculateOF =sol.getFlagCalculateOF();
        this.flagCalculateFeasibility =sol.getFlagCalculateFeasibility();
        this.isFeasible=sol.isFeasible();
        this.differentRatio=sol.differentRatio;
    }

    public int getVertexSelectionWeight() {
        return vertexSelectionWeight;
    }

    public int getInternalEdgeWeight() {
        return internalEdgeWeight;
    }

    public Set<Integer> getSolution() {
        return solution;
    }

    public Set<Integer> getUnselected() {
        return unselected;
    }

    public WTDPInstance getInstance() {
        return instance;
    }

    public Set<Integer>[] getDominatedBy() {
        return dominatedBy;
    }

    public boolean getFlagCalculateOF() {
        return flagCalculateOF;
    }

    public boolean getFlagCalculateFeasibility() {
        return flagCalculateFeasibility;
    }

    public int getLastOFValue() {
        return lastOFValue;
    }
    public void setOFValue(int of) {
        lastOFValue=of;
    }

    public boolean isDifferentRatio() {
        return differentRatio;
    }
}
