import java.util.ArrayList;
import java.util.List;

public class Simulation {
    String inputFileName;
    int D = 6;
    int amountOTSlotsPerDay =10;
    int S = 32 + amountOTSlotsPerDay;
    double slotLength = 15/60;
    double lambaElective =28.345;

    /*
    more variables required here
     */

    double weightUr =1/9;
    int W; //weeks to simulate
    int R; //number of replications
    int rule; //the appointment scheduling rule to apply


    // Initialize variables
    double avgElectiveAppWT = 0;
    double avgElectiveScanWT = 0;
    double avgUrgentScanWT = 0;
    double avgOT = 0;
    int numberOfUrgentPatientsPlanned = 0;
    int numberOfElectivePatientsPlanned = 0;
    // Initialize arrays
    //moet nog gebeuren

    List<Patient> patients = new ArrayList<Patient>();
    List<Patient> patient = new ArrayList<Patient>(); //iterator?
    double movingAvgElectiveAppWT;



    //////////////////////////////////////////////////////////////////////////  methods ///////////////////////////////////////////////////////////////////////////
    public static void runSimulations(){

    }

    /*
    more variables required here
     */

    public static void setWeekSchedule(){

    }

    public static void resetSystem(){

    }

    public static void runOneSimulation(){

    }

    public static int getRandomScanType(){
        return Integer.parseInt(null);
    }

    public static void generatePatients(){

    }

    public static int getNextSlotNrFromTime(int day, int patientType, double time){
        return Integer.parseInt(null);
    }

    public static void schedulePatient(){

    }

    public static void sortPatientsOnAppTime(){

    }



}
