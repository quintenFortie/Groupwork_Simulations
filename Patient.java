public class Patient {

    // variables for patient class
    int nr;
    int patientType; // 0 = none // 1 = elective // 2 = urgent
    int scanType;
                     // ELECTIVE  : 0 =  variable not applicable for this patient
                     // URGENT : 0 = brain // 1 = lumbar // 2 = cervical // 3 = abdomen // 4 = others
    int callWeek;
    int callDay;
    double callTime;
    int scanWeek;
    int scanDay;
    int slotNr ;
    double appTime ;
    double tardiness;
    boolean isNoShow;
    double scanTime;
    double durationScan;

    public Patient(int counter, int ptype, int stype, int w, int d, double cTime, double tard, boolean noShow, double duration_scan) {
        nr = counter;
        patientType = ptype;
        scanType = stype;
        callWeek = w;
        callDay = d;
        callTime = cTime;
        tardiness = tard;
        isNoShow = noShow;
        durationScan = duration_scan;

        //unplanned
        scanWeek = 999999999;
        scanDay = -1;
        slotNr = -1;
        appTime = -1;
        scanTime = -1.0;
    }

    //method to return arrivaltime of urgent patient or calltime of elective patient

    public int getCallWeek() {
        return callWeek;
    }


    public int getCallDay() {
        return callDay;
    }


    public double getCallTime() {
        return callTime;
    }

    public int getScanWeek() {
        return scanWeek;
    }

    public int getScanDay() {
        return scanDay;
    }

    public double getAppTime() {
        return appTime;
    }

    // method to return the appointment waiting time

    public double getAppWT(){
        double d = 0;
        if(slotNr != -1){
            d = (((scanWeek - callWeek)*7 + scanDay - callDay)*24 + appTime - callTime); // in hours
        }else{
            System.out.printf("CAN NOT CALCULATE APPOINTMENT WT OF PATIENT %d", nr);
            System.exit(1);
        }
        return d;
    }

    // method to return the scan waiting time
    public double getScanWT(){
        double d = 0;
        if(scanTime != 0){
            double wt = 0;
            if(patientType == 1){ // elective
                wt = scanTime - (appTime + tardiness);
            }else{ // urgent
                wt = scanTime - callTime;
            }
            d =  Math.max(0.0,wt);
        }else{
            System.out.printf("CAN NOT CALCULATE SCAN WT OF PATIENT %d", nr);  // in hours
            System.exit(1);
        }
        return d;
    }

}
