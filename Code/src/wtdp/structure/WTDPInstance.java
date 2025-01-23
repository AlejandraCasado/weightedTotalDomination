package wtdp.structure;

import grafo.optilib.structure.Instance;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

public class WTDPInstance implements Instance {

    private int numNodes;
    private int numEdges;
    private int [] nodeWeight;
    private String name;
    private ArrayList<Integer> [] adjacencyList;
    private int [][] adjacencyMatrix;

    public WTDPInstance(String path){
        String separator = Pattern.quote(File.separator);
        String str[]=path.split(separator);
        int pathLength=str.length;
        name=str[pathLength-1].split(".wtdp")[0];
        readInstance(path);
    }

    @Override
    public void readInstance(String path) {
        try (BufferedReader br = new BufferedReader(new FileReader(path))){
            String line;
            String[] lineContent;

            line= br.readLine();
            lineContent = line.split("\\s");

            numNodes = Integer.parseInt(lineContent[0]);
            numEdges = Integer.parseInt(lineContent[1]);

            nodeWeight=new int[numNodes];
            adjacencyList=new ArrayList[numNodes];
            adjacencyMatrix=new int[numNodes][numNodes];

            for (int i=0; i<numNodes; i++){
                line= br.readLine();
                lineContent = line.split("\\s");
                nodeWeight[Integer.parseInt(lineContent[0])]=Integer.parseInt(lineContent[1]);
                adjacencyList[i]=new ArrayList<>(numNodes);
                for(int j=i+1;j<numNodes;j++){
                    adjacencyMatrix[i][j]=0;
                    adjacencyMatrix[j][i]=0;
                }
            }

            for (int i=0; i< numEdges;i++){
                line= br.readLine();
                lineContent = line.split("\\s");
                int node1 = Integer.parseInt(lineContent[1]);
                int node2 = Integer.parseInt(lineContent[2]);
                int weight = Integer.parseInt(lineContent[3]);

                adjacencyList[node1].add(node2);
                adjacencyList[node2].add(node1);
                adjacencyMatrix[node1][node2]=weight;
                adjacencyMatrix[node2][node1]=weight;
            }
        } catch (FileNotFoundException e){
            System.out.println(("File not found " + path));
        } catch (IOException e){
            System.out.println("Error reading line");
        }
    }

    public int getEdgeWeight(int node1, int node2){
        return adjacencyMatrix[node1][node2];
    }

    public int getNodeWeight(int node1){
        return nodeWeight[node1];
    }

    public List<Integer> getAdjacent(int node){
        return adjacencyList[node];
    }

    public boolean areAdjacent(int node1, int node2){
        return adjacencyMatrix[node1][node2]!=0;
    }

    public int getNumNodes() {
        return numNodes;
    }

    public String getName() {
        return name;
    }
}
