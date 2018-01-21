/*  Luqhasal
*   Computer Networks And Applications
*/

public class BestNeighbour {
    private String neighbour;
    private String destination;
    private int distance;

    public BestNeighbour(String neighbour, String destination, int distance) {
        this.neighbour = neighbour;
        this.destination = destination;
        this.distance = distance;
    }
    
    public String getNeighbour() {
        return neighbour;
    }

    public void setNeighbour(String neighbour) {
        this.neighbour = neighbour;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }
    
}
