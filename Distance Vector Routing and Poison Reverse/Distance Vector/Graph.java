/*  Luqhasal
*   Computer Networks And Applications
*/

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Graph {

    public static final int INFINITY = Integer.MAX_VALUE / 2;
    private ArrayList<Node> nodes;
    private int numberOfNodes;
    private int numberOfEdges;
    private int numberOfModifiedEdges;
    private FileReader fr;
    private BufferedReader br;
    private String line;
    private String changedConfgFile;
    public static int MessageCount = 0;

    public Graph(String fileName, String chngCnfgFile) {
        changedConfgFile = chngCnfgFile;
        init(fileName);
        populateGraph();
        initializeNodeDistances();
        initLeastDistances();
        //for update file
        initConfigurationChangeFile(chngCnfgFile);
        populateChanges();
    }

    /**
     * ***************************************
     * init variables *************************************
     */
    private void init(String fileName) {
        try {
            //initialize reader
            fr = new FileReader(fileName);
            br = new BufferedReader(fr);
        } catch (Exception ex) {
            System.err.println(ex.toString());
        }
        nodes = new ArrayList<>();
    }

    public Node getNodeByName(String name) {
        for (Node node : nodes) {
            if (node.getName().equalsIgnoreCase(name)) {
                return node;
            }
        }
        return null;
    }

    /* ****************************************
     * Populate Graph *
     ******************************************/
    private void populateGraph() {
        readNumberOfNodes();
        createNodesOfGraph();
        readNumberOfEdges();
        addNodeEdges();
    }

    private void readNumberOfNodes() {
        try {
            line = br.readLine();
            numberOfNodes = Integer.parseInt(line);
        } catch (IOException | NumberFormatException e) {
            System.err.println("Invalid number of nodes");
            System.err.println(e.toString());
            System.exit(1);
        }
    }

    private void createNodesOfGraph() {
        try {
            for (int i = 0; i < numberOfNodes; i++) {
                line = br.readLine();
                String name = line.charAt(0) + "";
                nodes.add(new Node(name));
            }
        } catch (Exception ex) {
            System.err.println("Invalid node data");
            System.err.println(ex.toString());
            System.exit(1);
        }
    }

    private void readNumberOfEdges() {
        try {
            line = br.readLine();
            numberOfEdges = Integer.parseInt(line);
        } catch (IOException | NumberFormatException e) {
            System.err.println("Invalid number of nodes");
            System.err.println(e.toString());
            System.exit(1);
        }
    }

    private void addNodeEdges() {
        try {
            for (int i = 0; i < numberOfEdges; i++) {
                line = br.readLine();
                String[] edgeStrs = line.split(" ");
                //find node by name then get node's neighbours list, in list add new element
                getNodeByName(edgeStrs[0]).getNeighbourNodes().add(new NodeDistance(getNodeByName(edgeStrs[1]), Integer.parseInt(edgeStrs[2])));
                getNodeByName(edgeStrs[1]).getNeighbourNodes().add(new NodeDistance(getNodeByName(edgeStrs[0]), Integer.parseInt(edgeStrs[2])));
            }
        } catch (IOException | NumberFormatException ex) {
            System.err.println("Invalid graph edges record");
            System.err.println(ex.toString());
            System.exit(1);
        }
    }

    /**
     * **********Initialize distance for each node in Graph***********
     */
    private void initializeNodeDistances() {
        nodes.stream().forEach((Node node1) -> {
            nodes.stream().forEach((node2) -> {
                if (node1.getName().equalsIgnoreCase(node2.getName())) {
                    node1.getNodesDistanceList().add(new NodeDistance(node1, 0));

                } else {
                    NodeDistance n = node1.getNodeDistanceByNameFromList(node1.getNeighbourNodes(), node2.getName());
                    if (n == null) {
                        node1.getNodesDistanceList().add(new NodeDistance(node2, INFINITY));
                    } else {
                        node1.getNodesDistanceList().add(n);
                    }
                }
            });
        });
    }

    /**
     * **********Find least distances******************************************
     */
    public void initLeastDistances() {
        MessageCount = 0;
        ArrayList<Node> al;
        al = nodes;
        boolean isFirstIter = true;
        do {
            if(isFirstIter){
                al = nodes;
                isFirstIter = false;
            }
            else{
                al = new ArrayList<>(Node.newNodeList);
            }
            Node.newNodeList.clear();
            for (Node n1 : al) {
                for (Node n2 : al) {
                    n1.updateNodeDistanceList(n2.getNodesDistanceList(), n2.getName());
                }
            }
        } while (Node.newNodeList.size() > 0);
        System.out.println("\n\nDistance Table\n\t");
        for(Node n: nodes)
            System.out.print("\t"+n.getName());
        System.out.println("");
        for (Node n : nodes) {
            System.out.print(n.getName()+"\t");
            for (NodeDistance nd : n.getNodesDistanceList()) {
                System.out.print(nd.getDistance()+"\t");
            }
            System.out.println("");
        }
        System.out.println("Number of Messages is : "+MessageCount);
        MessageCount = 0;
        
        
        nodes.forEach((Node n)->{
           for(NodeDistance n1: n.getNodesDistanceList()){
               if(n.getName().equalsIgnoreCase(n1.getNode().getName())) continue;
               if(isBestNeighbourListContains(n,n1.getNode().getName())) continue;
               n.getBestNeighoursList().add(new BestNeighbour("Me", n1.getNode().getName(), n1.getDistance()));
           }
        });
        
        System.out.println("\n\n\n");
        nodes.forEach((n) -> {
            System.out.println("\nFor : "+n.getName()+"\n\n");
            n.getBestNeighoursList().forEach((bn) -> {
                System.out.println("For Destination : "+bn.getDestination()+", Best Neighbour Node is : "+bn.getNeighbour()+ ", with Destination Distance : "+bn.getDistance());
            });
        });
        //neighbour will update it's distance vector
    }
    
    boolean isBestNeighbourListContains(Node n, String destination){
        if (n.getBestNeighoursList().stream().anyMatch((bn) -> (bn.getDestination().equalsIgnoreCase(destination)))) {
            return true;
        }
        return false;
    }
    private void initConfigurationChangeFile(String fileName) {
        try {
            fr = new FileReader(fileName);
            br = new BufferedReader(fr);
        } catch (Exception ex) {
            System.err.println(ex.toString());
        }
    }
    
    private void populateChanges(){
        readNumberOfModifiedEdges();
        try {
            ArrayList<NodeDistance> nabList = new ArrayList<>();
            ArrayList<NodeDistance> distList = new ArrayList<>();
            NodeDistance nodeDistance;
            ArrayList<Node> updatedNodeList = new ArrayList<>();
            
                updatedNodeList.clear();
            for (int i = 0; i < numberOfModifiedEdges; i++) {
                nabList.clear();
                distList.clear();
                line = br.readLine();
                String[] edgeStrs = line.split(" ");
                //find node by name then get node's neighbours list, in list add new element
                //get neighbour/nodedistance list if found, update it otherwis add new neighbour
                nabList = getNodeByName(edgeStrs[0]).getNeighbourNodes();
                distList = getNodeByName(edgeStrs[0]).getNodesDistanceList();
                nodeDistance = getNodeDistanceByNameFromList(nabList,edgeStrs[0]);
                updatedNodeList.add(getNodeByName(edgeStrs[0]));
                if(nodeDistance == null){
                    getNodeByName(edgeStrs[0]).getNeighbourNodes().add(new NodeDistance(getNodeByName(edgeStrs[1]), Integer.parseInt(edgeStrs[2])));
                getNodeByName(edgeStrs[1]).getNeighbourNodes().add(new NodeDistance(getNodeByName(edgeStrs[0]), Integer.parseInt(edgeStrs[2])));
                }
                else{
                    getNodeByName(edgeStrs[0]).setDistanceOfNodeInList(nabList, edgeStrs[1], Integer.parseInt(edgeStrs[2]));
                }
                nodeDistance = getNodeDistanceByNameFromList(distList,edgeStrs[0]);
                if(nodeDistance == null){
                    getNodeByName(edgeStrs[0]).getNodesDistanceList().add(new NodeDistance(getNodeByName(edgeStrs[1]), Integer.parseInt(edgeStrs[2])));
                getNodeByName(edgeStrs[1]).getNodesDistanceList().add(new NodeDistance(getNodeByName(edgeStrs[0]), Integer.parseInt(edgeStrs[2])));
                }
                else{
                    getNodeByName(edgeStrs[0]).setDistanceOfNodeInList(distList, edgeStrs[1], Integer.parseInt(edgeStrs[2]));
                }
            }
                updateLeastDistances(updatedNodeList);
        System.out.println("\n\nSetting node distance after update");
        try{fr = new FileReader(changedConfgFile);
        br = new BufferedReader(fr);
        int n = Integer.parseInt(br.readLine());
            for (int i = 0; i < n; i++) {
                System.out.println(br.readLine());
                System.out.println("");
            }
        }
        catch(Exception e){
            System.out.println(e.toString());
        }
        for(Node n: nodes)
            System.out.print("\t"+n.getName());
        System.out.println("");
        for (Node n : nodes) {
            System.out.print(n.getName()+"\t");
            for (NodeDistance nd : n.getNodesDistanceList()) {
                System.out.print(nd.getDistance()+"\t");
            }
            System.out.println("");
        }
            
        } catch (IOException | NumberFormatException ex) {
            System.err.println("Invalid graph edges record");
            System.err.println(ex.toString());
            System.exit(1);
        }
        System.out.println("Number of Messages is : "+MessageCount);
        MessageCount = 0;
        
        
    }
    private void readNumberOfModifiedEdges() {
        try {
            line = br.readLine();
            numberOfModifiedEdges = Integer.parseInt(line);
        } catch (IOException | NumberFormatException e) {
            System.err.println("Invalid number of nodes");
            System.err.println(e.toString());
            System.exit(1);
        }
    }
    public void updateLeastDistances(ArrayList<Node> al) {

        ArrayList<Node> tempAl;
        tempAl = al;
        boolean isFirstIter = true;
        do {
            if(isFirstIter){
                tempAl = al;
                isFirstIter = false;
            }
            else{
                tempAl = new ArrayList<>(Node.newNodeList);
            }
            Node.newNodeList.clear();
            for (Node n1 : tempAl) {
                n1.getNeighbourNodes().forEach((n2) -> {
                    n1.updateNodeDistanceList(n2.getNode().getNodesDistanceList(), n2.getNode().getName());
                });
            }
        } while (Node.newNodeList.size() > 0);
    }
    
    public NodeDistance getNodeDistanceByNameFromList(ArrayList<NodeDistance> list, String nodeName){
        for(NodeDistance nd: list){
            if(nd.getNode().getName().equalsIgnoreCase(nodeName)){
                return nd;
            }
        }
        return null;
    }
    
    public int getDistanceOfNodeInList(ArrayList<NodeDistance> list,String nodeName){
        for(NodeDistance nd: list){
            if(nd.getNode().getName().equalsIgnoreCase(nodeName)){
                return nd.getDistance();
            }
        }
        return INFINITY;
    }
    public void setDistanceOfNodeInList(ArrayList<NodeDistance> list, String nodeName, int distance){
        for(NodeDistance nd: list){
            if(nd.getNode().getName().equalsIgnoreCase(nodeName)){
                nd.setDistance(distance);;
                break;
            }
        }
    }
    
}
