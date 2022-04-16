import java.util.ArrayList;
import java.util.List;

public class Simulation {

    // 1st group of variables: variables given in the assignment itself
    String inputFileName;
    int D = 6;
    int amountOTSlotsPerDay =10;
    int S = 32 + amountOTSlotsPerDay;
    double slotLength = 15/60;
    double lambaElective =28.345;
    double weightUr =1/9;

    /*
    more variables required here
     */

    //2nd group: variables you have to set yourself

    int W; //weeks to simulate
    int R; //number of replications
    int rule; //the appointment scheduling rule you are testing
    int[][] weekschedule; //= new int[][];    // 2dimensional array of slot objects indicating the week schedule you want to test
                                              // in order to fill in the weekschedule we use an inputfile which contains 0,1 or 2
                                              // which indicates the slots.


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

    // to start the whole simulation process called by the main, within we have to set the weekschedule, next reset system with simulation, runonesimulation
    public static void runSimulations(){

    }

    /*
    more variables required here
     */

    public static void setWeekSchedule(){

    }

    public static void resetSystem(){

    }
//runOneSimulation includes 3 steps
    // 1 generate patients
    // 2 schedule patient appointment waiting time
    // 3 patients arrivals at the hospital scan waiting time and the overtime

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
