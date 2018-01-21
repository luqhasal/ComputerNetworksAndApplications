/*  Luqhasal
*   Computer Networks And Applications
*/

public class NodeDistance {
    public static final int INFINITY = Integer.MAX_VALUE/2;
    private Node node;
    private int distance;

    public NodeDistance(Node node, int distance) {
        this.node = node;
        this.distance = distance;
    }

    public NodeDistance() {
        this.setNode(null);
        this.setDistance(-1);
    }

    public Node getNode() {
        return node;
    }

    public final void setNode(Node node) {
        this.node = node;
    }

    public int getDistance() {
        return distance;
    }

    public final void setDistance(int distance) {
        this.distance = distance;
    }
    
}
