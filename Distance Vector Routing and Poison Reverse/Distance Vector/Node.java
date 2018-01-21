/*  Luqhasal
*   Computer Networks And Applications
*/

import java.util.ArrayList;

public class Node {

    public static final int INFINITY = Integer.MAX_VALUE / 2;
    private String name;
    private ArrayList<BestNeighbour> bestNeighboursList;     // best neighbours list contains neighbour which provides shortest distance to a destination node
    private ArrayList<NodeDistance> nodesDistanceList;      //to keep track of shortest path of all node
    private ArrayList<NodeDistance> neighbourNodesList;     //to keep list of neighbours with direct distance
    private ArrayList<ArrayList<NodeDistance>> neighbourNodeDistanceList; //to keep list of neighbours' neighbour distance list

    public Node(String name) {
        this.name = name;
        bestNeighboursList = new ArrayList<>();
        nodesDistanceList = new ArrayList<>();
        neighbourNodesList = new ArrayList<>();
        neighbourNodeDistanceList = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<BestNeighbour> getBestNeighoursList() {
        return bestNeighboursList;
    }

    public void setBestNeighoursList(ArrayList<BestNeighbour> bestNeighoursList) {
        this.bestNeighboursList = bestNeighoursList;
    }

    public ArrayList<NodeDistance> getNeighbourNodesList() {
        return neighbourNodesList;
    }

    public void setNeighbourNodesList(ArrayList<NodeDistance> neighbourNodesList) {
        this.neighbourNodesList = neighbourNodesList;
    }

    public ArrayList<NodeDistance> getNodesDistanceList() {
        return nodesDistanceList;
    }

    public void setNodesDistanceList(ArrayList<NodeDistance> nodesDistanceList) {
        this.nodesDistanceList = nodesDistanceList;
    }

    public ArrayList<NodeDistance> getNeighbourNodes() {
        return neighbourNodesList;
    }

    public void setNeighbourNodes(ArrayList<NodeDistance> neighbourNodes) {
        this.neighbourNodesList = neighbourNodes;
    }

    public ArrayList<ArrayList<NodeDistance>> getNeighbourNodeDistanceList() {
        return neighbourNodeDistanceList;
    }

    public void setNeighbourNodeDistanceList(ArrayList<ArrayList<NodeDistance>> neighbourNodeDistanceList) {
        this.neighbourNodeDistanceList = neighbourNodeDistanceList;
    }

    public Node getNodeByNameFromList(ArrayList<NodeDistance> list, String nodeName) {
        for (NodeDistance nd : list) {
            if (nd.getNode().getName().equalsIgnoreCase(nodeName)) {
                return nd.getNode();
            }
        }
        return null;
    }

    public NodeDistance getNodeDistanceByNameFromList(ArrayList<NodeDistance> list, String nodeName) {
        for (NodeDistance nd : list) {
            if (nd.getNode().getName().equalsIgnoreCase(nodeName)) {
                return nd;
            }
        }
        return null;
    }

    public int getDistanceOfNodeInList(ArrayList<NodeDistance> list, String nodeName) {
        for (NodeDistance nd : list) {
            if (nd.getNode().getName().equalsIgnoreCase(nodeName)) {
                return nd.getDistance();
            }
        }
        return INFINITY;
    }

    public void setDistanceOfNodeInList(ArrayList<NodeDistance> list, String nodeName, int distance) {
        for (NodeDistance nd : list) {
            if (nd.getNode().getName().equalsIgnoreCase(nodeName)) {
                nd.setDistance(distance);;
                break;
            }
        }
    }

    private void setBestNeighbour(String destinationNode, int distance) {

    }
    /**
     * *************************************************************************
     * it will return true if value in the nodeDistanceList changes. * it will
     * also update next hope for the given destination in * bestNeighbour list.
     * *
     **************************************************************************
     */
    public static ArrayList<Node> newNodeList = new ArrayList<>();

    public void updateNodeDistanceList(ArrayList<NodeDistance> newList, String callingNode) {
        int callerDistance = getDistanceOfNodeInList(neighbourNodesList, callingNode);
        for (NodeDistance nodeDistance : newList) {
            String _name = nodeDistance.getNode().getName();
            int newDistance = nodeDistance.getDistance();
            int currentDistance = getDistanceOfNodeInList(nodesDistanceList, _name);
            if (callerDistance + newDistance < currentDistance) {
                Graph.MessageCount++;
                if (!newNodeList.contains(this)) {
                    newNodeList.add(this);
                }
                BestNeighbour BN = this.getBestNeighbour(_name);
                if (BN == null) {
                    this.bestNeighboursList.add(new BestNeighbour(callingNode, _name, callerDistance + newDistance));
                } else {
                    if(callerDistance + newDistance < BN.getDistance())
                        BN.setNeighbour(callingNode);
                        BN.setDistance(newDistance+callerDistance);
                }
                //System.out.println((callerDistance + newDistance)+" calling node : "+callingNode+" nd "+_name+" this"+getName());
                setDistanceOfNodeInList(nodesDistanceList, _name, callerDistance + newDistance);
            }
        }
    }

    BestNeighbour getBestNeighbour(String Dest) {
        for (BestNeighbour bn : bestNeighboursList) {
            if (bn.getDestination().equalsIgnoreCase(Dest)) {
                return bn;
            }
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        Node n = (Node) o;
        return this.name.equalsIgnoreCase(n.getName());
    }

}
