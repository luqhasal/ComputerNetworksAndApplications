/*  Luqhasal
*   Computer Networks And Applications
*/

public class PoisonReverseDriver {
    public static final int INFINITY = Integer.MAX_VALUE/2;
    public static void main(String[] args) {
        System.out.println("Distance Vector (Using Poison Reverse)"
                + "\n\nAdding nodes\nAdding links");
       Graph g = new Graph(args[0],args[1]);
    }
}