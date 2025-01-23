package wtdp;

import grafo.optilib.metaheuristics.Algorithm;
import grafo.optilib.results.Experiment;
import wtdp.algorithm.*;
import wtdp.constructive.*;
import wtdp.improve.*;
import wtdp.structure.WTDPFactory;
import wtdp.structure.WTDPInstance;
import wtdp.structure.WTDPSolution;

import java.nio.file.Path;
import java.nio.file.Paths;


public class Main {

    public static int kMax=40;
    public static double alpha=0.1;
    public static boolean intensifiedShake=false;

    public static void main(String[] args){
        Algorithm<WTDPInstance, WTDPSolution> [] algorithms=new Algorithm[]{
                new BiasedGRASPAlgorithm_VNS(new Biased_GRASP_Constructive(),new Reduced_Local_Search(),100),
        };
        Path currentPath = Paths.get("").toAbsolutePath();
        Path parentPath = currentPath.getParent();
        String instanceFolder=parentPath+"/Instances/MA";
        WTDPFactory factory=new WTDPFactory();
        Experiment<WTDPInstance, WTDPFactory, WTDPSolution> experiment=new Experiment<>(algorithms,factory);
        experiment.launch(instanceFolder, new String[]{""});
    }
}
