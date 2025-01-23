package wtdp.algorithm;

import grafo.optilib.metaheuristics.Algorithm;
import grafo.optilib.metaheuristics.Improvement;
import grafo.optilib.results.Result;
import grafo.optilib.tools.RandomManager;
import grafo.optilib.tools.Timer;
import wtdp.Main;
import wtdp.constructive.Biased_GRASP_Constructive;
import wtdp.structure.WTDPInstance;
import wtdp.structure.WTDPSolution;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BiasedGRASPAlgorithm_VNS implements Algorithm<WTDPInstance, WTDPSolution> {

    private Biased_GRASP_Constructive constructive;
    private Improvement<WTDPSolution> improvement=null;
    private int numSolutions;
    private WTDPSolution bestSol;
    private int [] numberOfAppearances;
    private final int kStepPercentage=10;
    private int kMaxPercentage;
    private int k;
    private int kMax;
    private int kStep;
    private int numVnsIterations=60;

    public BiasedGRASPAlgorithm_VNS(Biased_GRASP_Constructive constructive, Improvement<WTDPSolution> improvement, int numSolutions){
        this.constructive=constructive;
        this.improvement=improvement;
        this.numSolutions=numSolutions;
    }

    @Override
    public Result execute(WTDPInstance instance) {
        kMaxPercentage= Main.kMax;
        RandomManager.setSeed(2304);
        numberOfAppearances=new int[instance.getNumNodes()];
        for (int i=0;i<instance.getNumNodes();i++){
            numberOfAppearances[i]=numSolutions;
        }
        Result result=new Result(instance.getName());
        int bestOF=0x3f3f3f;
        Timer.initTimer();
        float time=0.0f;

        for(int i=0;i<numSolutions && time<1800;i++){
            constructive.setNumberAppearances(numberOfAppearances);
            WTDPSolution sol=constructive.constructSolution(instance);
            if(improvement!=null) {
                improvement.improve(sol);
            }
            if(sol.objectiveFunction()<bestOF){
                bestOF=sol.objectiveFunction();
                bestSol=sol;
            }
            time=Timer.getTime()/1000f;
        }
        WTDPSolution auxSol=new WTDPSolution(bestSol);
        for(int i=0; i<numVnsIterations && time<1800;i++){
            vnsProcedure(auxSol);
            if(auxSol.objectiveFunction()< bestOF){
                bestOF=auxSol.objectiveFunction();
                bestSol.copy(auxSol);
            }
            time=Timer.getTime()/1000f;
        }
        result.add("OF",bestOF);
        result.add("Time",Timer.getTime()/1000f);
        System.out.print("Instance "+instance.getName());
        System.out.println(" value: "+bestOF);
        return result;
    }

    private void vnsProcedure(WTDPSolution sol){
        k=1;
        kStep=(int)Math.ceil((kStepPercentage/100.0)*sol.getSolution().size());
        kMax=(int)Math.ceil((kMaxPercentage/100.0)*sol.getSolution().size());
        WTDPSolution localSol = new WTDPSolution(sol);
        while (k<=kMax){
            shake(localSol);
            improvement.improve(localSol);
            neighborhoodChange(sol, localSol);
        }
    }

    private void shake(WTDPSolution localSol){
        WTDPSolution bestLocalSol=new WTDPSolution(localSol);
        int bestLocalOF=0x3F3F3F3F;
        List<Integer> selectedCopy=new ArrayList<>(localSol.getSolution());
        Collections.shuffle(selectedCopy,RandomManager.getRandom());
        for(int i=0;i<k;i++){
            localSol.removeNode(selectedCopy.get(i));
        }
        List<Integer> unselectedCopy=new ArrayList<>(localSol.getUnselected());
        if(!Main.intensifiedShake){
            Collections.shuffle(unselectedCopy,RandomManager.getRandom());
        }
        int i=0;
        while(localSol.checkStopSearching(bestLocalOF)){
            if(!Main.intensifiedShake){
                localSol.addNode(unselectedCopy.get(i));
            }else{
                localSol.addBestNextNodeRatio();
            }
            int of=localSol.objectiveFunction();
            if(of<bestLocalOF && localSol.isFeasible()){
                bestLocalSol.copy(localSol);
                bestLocalOF=of;
            }
            i++;
        }
        localSol.copy(bestLocalSol);
    }

    private void neighborhoodChange(WTDPSolution sol,WTDPSolution localSol){
        if(sol.objectiveFunction()>localSol.objectiveFunction() && localSol.isFeasible()){
            sol.copy(localSol);
            k=1;
        }else{
            k+=kStep;
            localSol.copy(sol);
        }
    }

    @Override
    public WTDPSolution getBestSolution() {
        return bestSol;
    }

    public String toString(){
        return this.getClass().getSimpleName()+"("+constructive.toString()+(improvement!=null?improvement.toString():"")+")_Kmax"+Main.kMax;
    }
}
