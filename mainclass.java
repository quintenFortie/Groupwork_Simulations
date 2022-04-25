import java.io.IOException;

public class mainclass {

    public static void main(String[] args) throws IOException {
        Simulation sim = new Simulation();
        sim.runOneSimulation();
        sim.runSimulations();
    }
}
