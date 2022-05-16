import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.System.exit;

public class Simulation {

    /* PARAMETERS GIVEN IN THE ASSIGNMENT */

    int D = 6;                          // amount of days in our schedule
    int amountOTSlotsPerDay =10;        // amount of overtime slots per day
    int S = 32 + amountOTSlotsPerDay;   // total amount of slots per day
    double slotLength = 15.0/60;        // each slot takes 15 minutes (0,25 hours)
    double lambdaElective = 28.345;       // number of elective arrivals per day: Poisson distributed lambda = 28,345

    
    double meanTardiness = 0.0;              // normal distribution of the tardiness: mu = 0 min.;
    double stdevTardiness = 2.5;             // normal distribution of the tardiness: sigma = 2.5 min.
    double probNoShow = 0.02;                // probability of not showing up
    double []lambdaUrgent = {2.5 , 1.25};
    double meanElectiveDuration = 15.0;      // service time elective patients: normal distr. mu = 15 min.
    double stdevElectiveDuration = 3.0;              // service time elective patients: normal distr. sigma = 3 min.
    double []meanUrgentDuration = { 15.0, 17.5 , 22.5, 30.0, 30.0 }; // ST urgent patients: normally distributed per type
    double []stdevUrgentDuration = {2.5, 1.0, 2.5, 1.0, 4.5};
    double []cumulativeProbUrgentType = {0.70, 0.80, 0.90, 0.95, 1.0};
    double weightUr = 1.0/9.0;              // weight assigned to the urgent patients
    double weightEl = 1.0/168.0;         // weight assigned to elective patients
    double k_sigma = 0.5;   // parameter used for the fourth benchmarking rule --> assumed to be 0.5 (given in the assignment)
    int replication=0;

    /* VARIABLES WE HAVE TO SET OURSELVES */
    int W = 156;                              // weeks to simulate
    int R = 20;                                 // number of replications
    static String inputFileName = "Strategy1/Strategy_1_14.txt"; // depended on the strategy
    boolean firstExperiment = false; 
    boolean complementary = false;
    int rule = 1;                            // the appointment scheduling rule to apply
    //1= FCFS //2 = Bailey-Welch//3 = Blocking//4 = Benchmarking

    // Initialize variables
    double avgElectiveAppWT = 0;
    double avgElectiveScanWT = 0;
    double avgUrgentScanWT = 0;
    double avgOT = 0;
    int numberOfUrgentPatientsPlanned = 0;
    int numberOfElectivePatientsPlanned = 0;

    // Initialize arrays //
    Slot[][] weekSchedule = new Slot [D][S];    // 2-dimensional array, filled in by reading in an inputfile

  

    double[] movingAvgElectiveAppWT = new double[W];
    double[] movingAvgElectiveScanWT = new double[W];
    double[] movingAvgUrgentScanWT = new double[W];
    double[] movingAvgOT = new double[W];


    List<Patient> patients = new ArrayList<>();


    Random random = new Random();

    //////////////////////////////////////////////////////////////////////////  methods ///////////////////////////////////////////////////////////////////////////
    public void runSimulations() throws IOException {
        double electiveAppWT = 0;
        double electiveScanWT = 0;
        double urgentScanWT = 0;
        double OT = 0;
        double OV = 0;
        setWeekSchedule();

        String fileName1 = "Output.txt";
        File file = new File(fileName1);
        // if file doesnt exists, then create it
        if (!file.exists()) {
            file.createNewFile(); // create the file
        }
        else
        {
            PrintWriter writer = new PrintWriter(file);
            writer.println("");
            writer.close();
        }
        FileWriter fileWriter = new FileWriter(file.getAbsoluteFile(),true); // APPENDS the text file with anything printed to the file during the rest of the procedure
        PrintWriter printWriter = new PrintWriter(fileWriter); // OPEN OUTPUT FILE
        printWriter.printf("elAppWT \t elScanWT \t urScanWT \t OT \n");
        printWriter.close();


        System.out.printf("r \t elAppWT \t elScanWT \t urScanWT \t OT \t OV \n");
        // run R replications
        for(int r = 0; r < R; r++){
            replication = r;
            resetSystem();                          // reset all variables related to 1 replication
            random.setSeed(r);                      // set seed value for random value generator
            runOneSimulation();                     // run 1 simulation / replication

            electiveAppWT += avgElectiveAppWT;
            electiveScanWT += avgElectiveScanWT;
            urgentScanWT += avgUrgentScanWT;
            OT += avgOT;
            OV += avgElectiveAppWT * weightEl + avgUrgentScanWT * weightUr;
            System.out.printf("%d \t %.2f \t %.2f \t %.2f \t %.2f \t %.2f \n", r, avgElectiveAppWT, avgElectiveScanWT, avgUrgentScanWT, avgOT, avgElectiveAppWT * weightEl + avgUrgentScanWT * weightUr);
        }
        electiveAppWT = electiveAppWT / R;
        electiveScanWT = electiveScanWT / R;
        urgentScanWT = urgentScanWT / R;
        OT = OT / R;
        OV = OV / R;
        double objectiveValue = electiveAppWT * weightEl + urgentScanWT * weightUr;
        System.out.printf("Avg.: \t %.2f \t %.2f \t %.2f \t %.2f \t %.2f \n", electiveAppWT, electiveScanWT, urgentScanWT, OT, objectiveValue);




    }

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
            for(int s = 0; s<32; s++)
            {
                int number = inputStream.nextInt();
                Slot newSlot = new Slot(0,0,number,number);
                weekSchedule[d][s] = newSlot;
            }
        }
        inputStream.close();

        // Set the type of the overtime slots (3=urgent in overtime)
        for(int d = 0; d < D; d++){
            for(int s = 32; s < S; s++){
                Slot newSlot = new Slot(0,0,2,3);
                weekSchedule[d][s] = newSlot;
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
                if(time == 12){ time = 13;} 
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

    public List<Patient> sortPatientsOnAppTime()
    {
        Comparator<Patient> compareByWeek = Comparator.comparing(Patient::getScanWeek);
        Comparator<Patient> compareByDay = Comparator.comparing(Patient::getScanDay);
        Comparator<Patient> compareByTime = Comparator.comparing(Patient::getAppTime);

        Comparator<Patient> compareByAppTime = compareByWeek.thenComparing(compareByDay).thenComparing(compareByTime);
        List<Patient> returnList = patients.stream().sorted(compareByAppTime).collect(Collectors.toList());
        return returnList;

    }

    public void runOneSimulation() throws IOException {
        if(firstExperiment)
        {generatePatients();}
        else
        {
            readPatientList();
        }
        // create patient arrival events (elective patients call, urgent patient arrive at the hospital)
        schedulePatients();         // schedule urgent and elective patients in slots based on their arrival events => detrmine the appointment wait time
        List <Patient> patientsList = sortPatientsOnAppTime();    // sort patients on their appointment time (unscheduled patients are grouped at the end of the list)

        // determine scan wait time per patient and overtime per day
        int prevWeek = 0; int prevDay = -1;
        int[] numberOfPatientsWeek = {0,0};
        int[] numberOfPatients = {0,0};
        double arrivalTime, wt;
        double prevScanEndTime = 0;
        boolean prevIsNoShow = false;
        // go over arrival events (i.e. the moment the patient arrives at the hospital)
        for(int i = 0; i< patientsList.size(); i++){
            Patient patient = patientsList.get(i);
            if(patient.scanWeek == 999999999){ // stop at the first unplanned patient
                break;
            }

            arrivalTime = patient.appTime + patient.tardiness;
            // SCAN WT
            if(!patient.isNoShow){ // als de patient opdaagt
                if(patient.scanWeek != prevWeek || patient.scanDay != prevDay){
                    patient.scanTime = arrivalTime;
                } else{
                    if(prevIsNoShow){
                        patient.scanTime =  Math.max(weekSchedule[patient.scanDay][patient.slotNr].startTime, Math.max(prevScanEndTime,arrivalTime)); // note we assume we wait at least 15minutes on a no-show patient to see whether he shows or is just late
                    }else{
                        patient.scanTime = Math.max(prevScanEndTime,arrivalTime);
                    }
                }
                wt = patient.getScanWT();
                if(patient.patientType == 1){
                    movingAvgElectiveScanWT[patient.scanWeek] += wt;
                }else{
                    movingAvgUrgentScanWT[patient.scanWeek] += wt;
                }
                numberOfPatientsWeek[patient.patientType - 1]++;
                if(patient.patientType == 1){
                    avgElectiveScanWT += wt;
                }else{
                    avgUrgentScanWT += wt;
                }
                numberOfPatients[patient.patientType - 1]++;
            }

            // OVERTIME
            if(prevDay > -1 && prevDay != patient.scanDay){
                if(prevDay == 3 || prevDay == 5){
                    movingAvgOT[prevWeek] += Math.max(0.0, prevScanEndTime - 13);
                }else{
                    movingAvgOT[prevWeek] += Math.max(0.0, prevScanEndTime - 17);
                }
                if(prevDay == 3 || prevDay == 5){
                    avgOT += Math.max(0.0, prevScanEndTime - 13);
                }else{
                    avgOT += Math.max(0.0, prevScanEndTime - 17);
                }
            }

            // update moving averages if week ends
            if(prevWeek != patient.scanWeek){
                movingAvgElectiveScanWT[prevWeek] = movingAvgElectiveScanWT[prevWeek] / numberOfPatientsWeek[0];
                movingAvgUrgentScanWT[prevWeek] = movingAvgUrgentScanWT[prevWeek] / numberOfPatientsWeek[1];
                movingAvgOT[prevWeek] = movingAvgOT[prevWeek] / D;
                numberOfPatientsWeek[0] = 0;
                numberOfPatientsWeek[1] = 0;
            }

            //set prev patient
            if(patient.isNoShow){
                //prevScanEndTime stays the same, it is the end time of the patient before the no-show patient
                prevIsNoShow = true;
                if(patient.scanWeek != prevWeek || patient.scanDay != prevDay){
                    prevScanEndTime = weekSchedule[patient.scanDay][patient.slotNr].startTime;
                }
            }else{
                prevScanEndTime = patient.scanTime + patient.durationScan;
                prevIsNoShow = false;
            }
            prevWeek = patient.scanWeek;
            prevDay = patient.scanDay;
        }
        // update moving averages of the last week
        movingAvgElectiveScanWT[W-1] = movingAvgElectiveScanWT[W-1] / numberOfPatientsWeek[0];
        movingAvgUrgentScanWT[W-1] = movingAvgUrgentScanWT[W-1] / numberOfPatientsWeek[1];
        movingAvgOT[W-1] = movingAvgOT[W-1] / D;

        // calculate objective values
        avgElectiveScanWT = avgElectiveScanWT / numberOfPatients[0];
        avgUrgentScanWT = avgUrgentScanWT / numberOfPatients[1];
        avgOT = avgOT / (D * W);

        // print moving avg
        String fileName1 =  "Output.txt";
        File file = new File(fileName1);
        // if file doesnt exists, then create it
        if (!file.exists()) {
            file.createNewFile(); // create the file
        }
        FileWriter fileWriter = new FileWriter(file.getAbsoluteFile(),true); // APPENDS the text file with anything printed to the file during the rest of the procedure
        PrintWriter printWriter = new PrintWriter(fileWriter); // OPEN OUTPUT FILE


        for(int w = 0; w < W; w++){
        printWriter.printf("%.2f \t %.2f \t %.2f \t %.2f \n", movingAvgElectiveAppWT[w], movingAvgElectiveScanWT[w], movingAvgUrgentScanWT[w], movingAvgOT[w]);
    }
    printWriter.close();
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
                    arrivalTimeNext = 8 + Helper.Exponential_distribution(lambdaElective, random, complementary) * (17 - 8);
                    while (arrivalTimeNext < 17) { // desk open from 8h until 17h
                        patientType = 1;                // elective
                        scanType = 0;                   // no scan type
                        callTime = arrivalTimeNext;     // set call time, i.e. arrival event time
                        tardiness = Helper.Normal_distribution(meanTardiness, stdevTardiness, random,complementary) / 60.0;       //this is in hours //in practice this is not known yet at time of call
                        int f = Helper.Bernouilli_distribution(probNoShow, random,complementary); // in practice this is not known yet at time of call
                        if (f == 0)
                            noShow = true;
                        else
                            noShow = false;
                        duration_scan = Helper.Normal_distribution(meanElectiveDuration, stdevElectiveDuration, random,complementary) / 60.0; // in practice this is not known yet at time of call
                        Patient patient = new Patient(counter, patientType, scanType, w, d, callTime, tardiness, noShow, duration_scan);
                        patients.add(patient); // patient is added to the patient list
                        counter++;
                        arrivalTimeNext = arrivalTimeNext + Helper.Exponential_distribution(lambdaElective, random,complementary) * (17 - 8); // arrival time of next patient (if < 17h)
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
                arrivalTimeNext = 8 + Helper.Exponential_distribution(lambda, random,complementary) * (endTime - 8);
                while (arrivalTimeNext < endTime) {   // desk open from 8h until 17h/12h
                    patientType = 2;                // urgent
                    scanType = getRandomScanType(); // set scan type
                    callTime = arrivalTimeNext;     // set arrival time, i.e. arrival event time
                    tardiness = 0;                  // urgent patients have an arrival time = arrival event time
                    noShow = false;                 // urgent patients are never no-show
                    duration_scan = Helper.Normal_distribution(meanUrgentDuration[scanType], stdevUrgentDuration[scanType], random,complementary) / 60.0; // in practice this is not known yet at time of arrival
                    Patient patient = new Patient(counter, patientType, scanType, w, d, callTime, tardiness, noShow, duration_scan);
                    patients.add(patient);
                    counter++;
                    arrivalTimeNext = arrivalTimeNext + Helper.Exponential_distribution(lambda, random,complementary) * (endTime - 8); // arrival time of next patient (if < 17h)
                }
            }
        }
    }

    public void readPatientList ()
    {
        Scanner inputStream = null;
        try{
            String complement;
            if(complementary)
            {
                complement = "1";
            }
            else
            {
                complement = "0";
            }
            inputStream = new Scanner(new File("PatientList"+replication+"_"+complement+".txt"));
        } catch (FileNotFoundException e) {
            System.out.println("Error opening the file " + inputFileName);
            exit(0);
        }
        //read first line
        inputStream.nextLine();
        while (inputStream.hasNext())
        {
            //reading of 1 line
            int counter = inputStream.nextInt();
            int patientType = inputStream.nextInt();
            int scanType = inputStream.nextInt();
            int w = inputStream.nextInt();
            int d = inputStream.nextInt();
            double callTime = Double.parseDouble(inputStream.next());
            double tardiness = Double.parseDouble(inputStream.next());
            boolean noShow = inputStream.nextBoolean();
            double duration_scan = Double.parseDouble(inputStream.next());
            Patient patient = new Patient(counter, patientType, scanType, w, d, callTime, tardiness, noShow, duration_scan);
            patients.add(patient);

        }

        inputStream.close();

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

    public List<Patient> sortPatients_arrivalTime (List<Patient> patientsList) throws IOException {

        Comparator<Patient> compareByWeek = Comparator.comparing(Patient::getCallWeek);
        Comparator<Patient> compareByDay = Comparator.comparing(Patient::getCallDay);
        Comparator<Patient> compareByTime = Comparator.comparing(Patient::getCallTime);

        Comparator<Patient> compareByArrivalTime = compareByWeek.thenComparing(compareByDay).thenComparing(compareByTime);
        List<Patient> returnList = patientsList.stream().sorted(compareByArrivalTime).collect(Collectors.toList());
        printPatientlistToFile(returnList);
        return returnList;

    }

    public void printPatientlistToFile (List<Patient> patientsList) throws IOException {
        // print moving avg
        String complement;
        if (complementary)
        {
             complement = "1";
        }
        else
        {
             complement = "0";
        }
        String fileName1 =  "PatientList"+replication+"_"+complement+".txt";
        File file = new File(fileName1);
        // if file doesnt exists, then create it
        if (!file.exists()) {
            file.createNewFile(); // create the file
        }
        else
        {
            PrintWriter writer = new PrintWriter(file);
            writer.print("");
            writer.close();
        }
        FileWriter fileWriter = new FileWriter(file.getAbsoluteFile(),true); // APPENDS the text file with anything printed to the file during the rest of the procedure
        PrintWriter printWriter = new PrintWriter(fileWriter); // OPEN OUTPUT FILE

        printWriter.println("Patient Number"+"\t"+"Patienttype"+"\t"+"Scantype"+"\t"+"Callweek"+"\t"+"Callday"+"\t"+"Calltime"+"\t"+"Tardiness"+"\t"+"IsNoShow"+"\t"+"Duration of scan");
        for(int p = 0; p < patientsList.size(); p++){
            printWriter.println(patientsList.get(p).nr + "\t" +  patientsList.get(p).patientType + "\t" + patientsList.get(p).scanType + "\t" + patientsList.get(p).callWeek + "\t" + patientsList.get(p).callDay + "\t" +patientsList.get(p).callTime + "\t" +patientsList.get(p).tardiness + "\t" + patientsList.get(p).isNoShow + "\t" +patientsList.get(p).durationScan);
        }
        printWriter.close();

    }


    public void schedulePatients() throws IOException {
        //sort arrival events (= patient list) on arrival time (call time for elective patients, arrival time for urgent)
        //hier moeten we nog gebruik maken van sortPatients_arrivalTime

        int [] week = new int[]{0,0}; // week of the next available slot {elective,urgent}
        int [] day = new int[]{0,0}; // day of the next available slot {elective,urgent}
        int [] slot = new int[]{0,0}; // slotNr of the next available slot {elective,urgent}

        //find first slot of each patient type (note, we assume each day (i.e. also day 0) has at least one slot of each patient type!)
        //elective
        int d = 0;
        boolean found = false;
        for(int s = 0; s < S && !found; s++){
            if(weekSchedule[d][s].patientType == 1){
                day[0] = d;
                slot[0] = s;
                found = true;
            }
        }
        //urgent
        found = false;
        for(int s = 0; s < S && !found; s++){
            if(weekSchedule[d][s].patientType == 2){
                day[1] = d;
                slot[1] = s;
                found = true;
            }
        }

        // go over SORTED patient list and assign slots
        int previousWeek = 0; int numberOfElective = 0; int numberOfElectivePerWeek = 0;   // keep track of week to know when to update moving average elective appointment waiting time
        double wt; int slotNr;
        List<Patient> patients_sorted = sortPatients_arrivalTime(patients);
        for(int count = 0; count < patients_sorted.size(); count++){
            //Patient *pat = &*patient;
            Patient patient = patients_sorted.get(count);
            //set index i dependant on patient type
            int i = patient.patientType - 1 ;

            // if still within the planning horizon:
            if(week[i] < W){

                // determine week where we start searching for a slot
                if(patient.callWeek > week[i]){
                    week[i] = patient.callWeek;
                    day[i] = 0;
                    slot[i] = getNextSlotNrFromTime(day[i], patient.patientType, 0);           // note we assume there is at least one slot of each patient type per day => this line will find first slot of this type
                }
                // determine day where we start searching for a slot if week[i] is equal to the callweek of the patient
                if(patient.callWeek == week[i] && patient.callDay > day[i]){
                    day[i] = patient.callDay;
                    slot[i] = getNextSlotNrFromTime(day[i], patient.patientType, 0);           // note we assume there is at least one slot of each patient type per day => this line will find first slot of this type
                }
                // determine slot
                if(patient.callWeek == week[i] && patient.callDay == day[i] && patient.callTime >= weekSchedule[day[i]][slot[i]].appTime){
                    // find last slot on day "day[i]"
                    found = false; slotNr = -1;
                    for(int s = S - 1; s >= 0 && !found; s--){
                        if(weekSchedule[day[i]][s].patientType == patient.patientType){
                            found = true;
                            slotNr = s;
                        }
                    }
                    // urgent patients have to be treated on the same day either in normal hours or in overtime (!! make sure there are enough overtime slots)
                    // for elective patients: check if the patient call time is before the last slot, i.e. if the patient can be planned on day "day[i]"
                    if(patient.patientType == 2 || patient.callTime < weekSchedule[day[i]][slotNr].appTime){
                        slot[i] = getNextSlotNrFromTime(day[i], patient.patientType, patient.callTime);   // find the first elective slot after the call time on day "day[i]"
                    }else{
                        // determine the next day
                        if(day[i] < D - 1){
                            day[i] = day[i] + 1;
                        }else{
                            day[i] = 0;
                            week[i] = week[i] + 1;
                        }
                        if(week[i] < W){   // find the first slot on the next day (if within the planning horizon)
                            slot[i] = getNextSlotNrFromTime(day[i], patient.patientType, 0);
                        }
                    }
                }

                // schedule the patient
                patient.scanWeek = week[i];
                patient.scanDay = day[i];
                patient.slotNr = slot[i];
                patient.appTime = weekSchedule[day[i]][slot[i]].appTime;

                // update moving average elective appointment waiting time
                if(patient.patientType == 1){
                    if(previousWeek < week[i]){
                        movingAvgElectiveAppWT[previousWeek] = movingAvgElectiveAppWT[previousWeek] / numberOfElectivePerWeek;
                        numberOfElectivePerWeek = 0;
                        previousWeek = week[i];
                    }
                    wt = patient.getAppWT();
                    movingAvgElectiveAppWT[week[i]] += wt;
                    numberOfElectivePerWeek++;
                    avgElectiveAppWT += wt;
                    numberOfElective++;
                }

                // set next slot of the current patient type
                found = false; int startD = day[i]; int startS = slot[i] + 1;
                for(int w = week[i]; w < W && !found; w++){
                    for(d = startD; d < D && !found; d++){
                        for(int s = startS; s < S && !found; s++){
                            if(weekSchedule[d][s].patientType == patient.patientType){
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
