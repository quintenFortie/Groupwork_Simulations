import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.System.exit;

public class Simulation {

    /* PARAMETERS GIVEN IN THE ASSIGNMENT */
    static String inputFileName = "Strategy_1.txt"; // afhankelijk van de strategy!!
    int D = 6;                          // amount of days in our schedule
    int amountOTSlotsPerDay =10;        // amount of overtime slots per day
    int S = 32 + amountOTSlotsPerDay;   // total amount of slots per day
    double slotLength = 15.0/60;        // each slot takes 15 minutes (0,25 hours)
    double lambdaElective = 28.345;       // number of elective arrivals per day: Poisson distributed lambda = 28,345

    // Deze heb ik er zelf bijgezet -- kijken jullie na?? //
    double meanTardiness = 0.0;              // normal distribution of the tardiness: mu = 0 min.;
    double stdevTardiness = 2.5;             // normal distribution of the tardiness: sigma = 2.5 min.
    double probNoShow = 0.02;                // probability of not showing up
    double lambdaUrgent[]  = {2.5 , 1.25};
    double meanElectiveDuration = 15.0;      // service time elective patients: normal distr. mu = 15 min.
    double stdevElectiveDuration = 3.0;              // service time elective patients: normal distr. sigma = 3 min.
    double meanUrgentDuration[] = { 15.0, 17.5 , 22.5, 30.0, 30.0 }; // ST urgent patients: normally distributed per type
    double stdevUrgentDuration[] = {2.5, 1.0, 2.5, 1.0, 4.5};
    double changes_frequency_scanType[] = {0.70, 0.10, 0.10, 0.05, 0.05};    // frequency per scan type;
    double cumulativeProbUrgentType[] = {0.70, 0.80, 0.90, 0.95, 1.0};
    double weightUr = 1.0/9.0;              // weight assigned to the urgent patients
    double weightEl = 1 - weightUr;         // weight assigned to elective patients
    double k_sigma = 0.5;                   // parameter used for the fourth benchmarking rule --> assumed to be 0.5 (given in the assignment)

    /* VARIABLES WE HAVE TO SET OURSELVES */
    int W = 10;                              // weeks to simulate
    int R = 1;                              // number of replications
    int rule = 1;                           // the appointment scheduling rule to apply

    // Initialize variables
    double avgElectiveAppWT = 0;
    double avgElectiveScanWT = 0;
    double avgUrgentScanWT = 0;
    double avgOT = 0;
    int numberOfUrgentPatientsPlanned = 0;
    int numberOfElectivePatientsPlanned = 0;

    // Initialize arrays //
    Slot weekSchedule[][] = new Slot [D][S];    // 2-dimensional array, filled in by reading in an inputfile

    // in C++ - file was dit het volgende:
    // klopt het wat wij doen???
    // weekSchedule = new Slot*[D];
    // for(d = 0; d < D; d++){
    //    weekSchedule[d] = new Slot[S];
    // }

    double movingAvgElectiveAppWT[] = new double[W];
    double movingAvgElectiveScanWT[] = new double[W];
    double movingAvgUrgentScanWT[] = new double[W];
    double movingAvgOT[] = new double[W];


    List<Patient> patients = new ArrayList<Patient>();
    List<Patient> patient = new ArrayList<Patient>(); //iterator?
    double movingAvgElectiveAppWT;

    Random random = new Random();

    //////////////////////////////////////////////////////////////////////////  methods ///////////////////////////////////////////////////////////////////////////

    public void runSimulations(){
        double electiveAppWT = 0;
        double electiveScanWT = 0;
        double urgentScanWT = 0;
        double OT = 0;
        double OV = 0;
        setWeekSchedule();
        System.out.printf("r \t elAppWT \t elScanWT \t urScanWT \t OT \t OV \n");
        // run R replications
        for(int r = 0; r < R; r++){
            resetSystem();                          // reset all variables related to 1 replication
            random.setSeed(r);                      // set seed value for random value generator
            runOneSimulation();                     // run 1 simulation / replication
            electiveAppWT += avgElectiveAppWT;
            electiveScanWT += avgElectiveScanWT;
            urgentScanWT += avgUrgentScanWT;
            OT += avgOT;
            OV += avgElectiveAppWT / weightEl + avgUrgentScanWT / weightUr;
            System.out.printf("%d \t %.2f \t %.2f \t %.2f \t %.2f \t %.2f \n", r, avgElectiveAppWT, avgElectiveScanWT, avgUrgentScanWT, avgOT, avgElectiveAppWT / weightEl + avgUrgentScanWT / weightUr);
        }
        electiveAppWT = electiveAppWT / R;
        electiveScanWT = electiveScanWT / R;
        urgentScanWT = urgentScanWT / R;
        OT = OT / R;
        OV = OV / R;
        double objectiveValue = electiveAppWT / weightEl + urgentScanWT / weightUr;
        System.out.printf("Avg.: \t %.2f \t %.2f \t %.2f \t %.2f \t %.2f \n", electiveAppWT, electiveScanWT, urgentScanWT, OT, objectiveValue);



        // print results
        // FILE *file = fopen("/Users/tinemeersman/Documents/project SMA 2022 student code /output.txt", "a"); // TODO: use your own directory
        // TODO: print the output you need to a .txt file
        // fclose(file);


    }
    /*
    more variables required here
     */



    public void setWeekSchedule(){
        // Read and set the slot types (0=none, 1=elective, 2=urgent within normal working hours)
        Scanner inputStream = null;
        try{
            inputStream = new Scanner(new File(inputFileName));
        } catch (FileNotFoundException e) {
            System.out.println("Error opening the file " + inputFileName);
            exit(0);
        }
        for (int d = 0; d<D; d++){
            for(int s = 0; s<32; s++){
                weekSchedule[d][s].slotType = inputStream.nextInt();
                weekSchedule[d][s].patientType = inputStream.nextInt();
            }
        }
        inputStream.close();

        // Set the type of the overtime slots (3=urgent in overtime)
        for(int d = 0; d < D; d++){
            for(int s = 32; s < S; s++){
                weekSchedule[d][s].slotType = 3;
                weekSchedule[d][s].patientType = 2;
            }
        }

        // set start and appointment time
        double time;
        for(int d = 0; d < D; d++){
            time = 8; // start time slot schedule
            for(int s = 0; s < S; s++){
                weekSchedule[d][s].startTime = time;        // start time slot

                // appointment time slot
                if(weekSchedule[d][s].slotType != 1){    // all slot types not elective : appointment time = slot start time
                    weekSchedule[d][s].appTime = time;
                }else{                                   // elective slots: appointment time is set according to rule !
                    if(rule == 1){ // FIFO
                        weekSchedule[d][s].appTime = time;
                    }else if(rule == 2){
                        // Bailey-Welch rule
                        if ((s==0)||(s==1)){
                            weekSchedule[d][s].appTime = 8;
                        }
                        else{
                            weekSchedule[d][s].appTime = time - slotLength;
                        }
                    }else if(rule == 3){
                        // Blocking rule
                        if ((s%2) == 0){
                            weekSchedule[d][s].appTime = time;
                        }
                        else{
                            weekSchedule[d][s].appTime = time - slotLength;
                        }
                    }else if(rule == 4){
                        // Benchmark rule
                        weekSchedule[d][s].appTime = time - k_sigma * stdevElectiveDuration;
                    }
                }

                //update time variable
                time += slotLength;
                if(time == 12){ time = 13;} // skip to the end of the luchbreak
            }
        }
    }

    public void resetSystem(){
        patients.clear();                           // clearing out patientslist
        avgElectiveAppWT = 0;
        avgElectiveScanWT = 0;
        avgUrgentScanWT = 0;
        avgOT = 0;
        numberOfElectivePatientsPlanned = 0;
        numberOfUrgentPatientsPlanned = 0;

        for(int w = 0; w < W; w++){
            movingAvgElectiveAppWT[w] = 0;
            movingAvgElectiveScanWT[w] = 0;
            movingAvgUrgentScanWT[w] = 0;
            movingAvgOT[w] = 0;
        }

    }

    public void runOneSimulation(){
        generatePatients();         // create patient arrival events (elective patients call, urgent patient arrive at the hospital)
        schedulePatients();         // schedule urgent and elective patients in slots based on their arrival events => detrmine the appointment wait time
        sortPatientsOnAppTime();   // sort patients on their appointment time (unscheduled patients are grouped at the end of the list)
    }

    public int getRandomScanType(){
        double r = random.nextDouble();
        int type = -1;
        for(int i = 0; i < 5 && type == -1; i++){
            if(r < cumulativeProbUrgentType[i]){ type = i; }
        }
        return type;
    }

    public void generatePatients() {
        double arrivalTimeNext;
        int counter = 0; // total number of patients so far
        int patientType, scanType, endTime;
        double callTime, tardiness, duration_scan, lambda;
        boolean noShow;
        for (int w = 0; w < W; w++) {
            for (int d = 0; d < D; d++) { // not on Sunday
                // generate ELECTIVE patients for this day
                if (d < D - 1) {  // not on Saturday either then they can't call (elective)
                    arrivalTimeNext = 8 + Helper.Exponential_distribution(lambdaElective, random) * (17 - 8); // moet er nog in komen
                    while (arrivalTimeNext < 17) { // desk open from 8h until 17h
                        patientType = 1;                // elective
                        scanType = 0;                   // no scan type
                        callTime = arrivalTimeNext;     // set call time, i.e. arrival event time
                        tardiness = Helper.Normal_distribution(meanTardiness, stdevTardiness, random) / 60.0;       //this is in hours //in practice this is not known yet at time of call
                        int f = Helper.Bernouilli_distribution(probNoShow, random);                                // in practice this is not known yet at time of call
                        if (f == 0)
                            noShow = true;
                        else
                            noShow = false;
                        duration_scan = Helper.Normal_distribution(meanElectiveDuration, stdevElectiveDuration, random) / 60.0; // in practice this is not known yet at time of call
                        Patient patient = new Patient(counter, patientType, scanType, w, d, callTime, tardiness, noShow, duration_scan);
                        patients.add(patient); // patient is added to the patient list
                        counter++;
                        arrivalTimeNext = arrivalTimeNext + Helper.Exponential_distribution(lambdaElective, random) * (17 - 8); // arrival time of next patient (if < 17h)
                    }
                }

                // generate URGENT patients for this day
                if (d == 3 || d == 5) {
                    lambda = lambdaUrgent[1]; // on Wed and Sat, only half a day!
                    endTime = 12;
                } else {
                    lambda = lambdaUrgent[0];
                    endTime = 17;
                }
                arrivalTimeNext = 8 + Helper.Exponential_distribution(lambda, random) * (endTime - 8);
                while (arrivalTimeNext < endTime) {   // desk open from 8h until 17h/12h
                    patientType = 2;                // urgent
                    scanType = getRandomScanType(); // set scan type
                    callTime = arrivalTimeNext;     // set arrival time, i.e. arrival event time
                    tardiness = 0;                  // urgent patients have an arrival time = arrival event time
                    noShow = false;                 // urgent patients are never no-show
                    duration_scan = Helper.Normal_distribution(meanUrgentDuration[scanType], stdevUrgentDuration[scanType], random) / 60.0; // in practice this is not known yet at time of arrival
                    Patient patient = new Patient(counter, patientType, scanType, w, d, callTime, tardiness, noShow, duration_scan);
                    patients.add(patient);
                    counter++;
                    arrivalTimeNext = arrivalTimeNext + Helper.Exponential_distribution(lambda, random) * (endTime - 8); // arrival time of next patient (if < 17h)
                }
            }
        }
    }

    public int getNextSlotNrFromTime (int day, int patientType,double time)
    {
            boolean found = false;
            int slotNr = -1;
            for(int s = 0; !found && s < S; s++){
                if(weekSchedule[day][s].appTime > time && patientType == weekSchedule[day][s].patientType){
                    found = true;
                    slotNr = s;
                }
            }
            if(!found){
                System.out.printf("NO SLOT EXISTS DURING TIME %.2f \n", time);
                exit(0);
            }
            return slotNr;
    }

    public List<Patient> sortPatients_arrivalTime (List<Patient> patientsList)
    {

        Comparator<Patient> compareByWeek = Comparator.comparing(Patient::getCallWeek);
        Comparator<Patient> compareByDay = Comparator.comparing(Patient::getCallDay);
        Comparator<Patient> compareByTime = Comparator.comparing(Patient::getCallTime);

        Comparator<Patient> compareByArrivalTime = compareByWeek.thenComparing(compareByDay).thenComparing(compareByTime);
        List<Patient> returnList = patientsList.stream().sorted(compareByArrivalTime).collect(Collectors.toList());
        return returnList;

    }


    public int schedulePatients(int day, int patientType, double time){
        //sort arrival events (= patient list) on arrival time (call time for elective patients, arrival time for urgent)
        //hier moeten we nog gebruik maken van sortPatients_arrivalTime

        int week[2] = {0,0}; // week of the next available slot {elective,urgent}
        int day[2] = {0,0}; // day of the next available slot {elective,urgent}
        int slot[2] = {0,0}; // slotNr of the next available slot {elective,urgent}

        //find first slot of each patient type (note, we assume each day (i.e. also day 0) has at least one slot of each patient type!)
        //elective
        d = 0;
        boolean found = false;
        for(s = 0; s < S && !found; s++){
            if(weekSchedule[d][s].patientType == 1){
                day[0] = d;
                slot[0] = s;
                found = true;
            }
        }
        //urgent
        found = false;
        for(s = 0; s < S && !found; s++){
            if(weekSchedule[d][s].patientType == 2){
                day[1] = d;
                slot[1] = s;
                found = true;
            }
        }

        // go over SORTED patient list and assign slots
        int previousWeek = 0; int numberOfElective = 0; int numberOfElectivePerWeek = 0;   // keep track of week to know when to update moving average elective appointment waiting time
        double wt; int slotNr;
        for(patient = patients.begin(); patient != patients.end(); patient++){
            //Patient *pat = &*patient;

            //set index i dependant on patient type
            int i = patient->patientType - 1;

            // if still within the planning horizon:
            if(week[i] < W){

                // determine week where we start searching for a slot
                if(patient->callWeek > week[i]){
                    week[i] = patient->callWeek;
                    day[i] = 0;
                    slot[i] = getNextSlotNrFromTime(day[i], patient->patientType, 0);           // note we assume there is at least one slot of each patient type per day => this line will find first slot of this type
                }
                // determine day where we start searching for a slot
                if(patient->callWeek == week[i] && patient->callDay > day[i]){
                    day[i] = patient->callDay;
                    slot[i] = getNextSlotNrFromTime(day[i], patient->patientType, 0);           // note we assume there is at least one slot of each patient type per day => this line will find first slot of this type
                }
                // determine slot
                if(patient->callWeek == week[i] && patient->callDay == day[i] && patient->callTime >= weekSchedule[day[i]][slot[i]].appTime){
                    // find last slot on day "day[i]"
                    found = false; slotNr = -1;
                    for(s = S - 1; s >= 0 && !found; s--){
                        if(weekSchedule[day[i]][s].patientType == patient->patientType){
                            found = true;
                            slotNr = s;
                        }
                    }
                    // urgent patients have to be treated on the same day either in normal hours or in overtime (!! make sure there are enough overtime slots)
                    // for elective patients: check if the patient call time is before the last slot, i.e. if the patient can be planned on day "day[i]"
                    if(patient->patientType == 2 || patient->callTime < weekSchedule[day[i]][slotNr].appTime){
                        slot[i] = getNextSlotNrFromTime(day[i], patient->patientType, patient->callTime);   // find the first elective slot after the call time on day "day[i]"
                    }else{
                        // determine the next day
                        if(day[i] < D - 1){
                            day[i] = day[i] + 1;
                        }else{
                            day[i] = 0;
                            week[i] = week[i] + 1;
                        }
                        if(week[i] < W){   // find the first slot on the next day (if within the planning horizon)
                            slot[i] = getNextSlotNrFromTime(day[i], patient->patientType, 0);
                        }
                    }
                }

                // schedule the patient
                patient->scanWeek = week[i];
                patient->scanDay = day[i];
                patient->slotNr = slot[i];
                patient->appTime = weekSchedule[day[i]][slot[i]].appTime;

                // update moving average elective appointment waiting time
                if(patient->patientType == 1){
                    if(previousWeek < week[i]){
                        movingAvgElectiveAppWT[previousWeek] = movingAvgElectiveAppWT[previousWeek] / numberOfElectivePerWeek;
                        numberOfElectivePerWeek = 0;
                        previousWeek = week[i];
                    }
                    wt = patient->getAppWT();
                    movingAvgElectiveAppWT[week[i]] += wt;
                    numberOfElectivePerWeek++;
                    avgElectiveAppWT += wt;
                    numberOfElective++;
                }

                // set next slot of the current patient type
                found = false; int startD = day[i]; int startS = slot[i] + 1;
                for(w = week[i]; w < W && !found; w++){
                    for(d = startD; d < D && !found; d++){
                        for(s = startS; s < S && !found; s++){
                            if(weekSchedule[d][s].patientType == patient->patientType){
                                week[i] = w;
                                day[i] = d;
                                slot[i] = s;
                                found = true;
                            }
                        }
                        startS = 0;
                    }
                    startD = 0;
                }
                if(!found) week[i] = W;
            }
        }

        // update moving average elective appointment waiting time in last week
        movingAvgElectiveAppWT[W-1] = movingAvgElectiveAppWT[W-1] / numberOfElectivePerWeek;

        // calculate objective value
        avgElectiveAppWT = avgElectiveAppWT / numberOfElective;
    }




}
