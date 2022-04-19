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
    int scanWeek = -1;
    int scanDay = -1;
    int slotNr = -1;
    double appTime = -1;
    double tardiness;
    boolean isNoShow;
    double scanTime = -1;
    double durationScan;

    public Patient(int counter, int ptype, int stype, int w, int d, double cTime, double tard, boolean noShow, double duration_scan) {
        nr = counter;
        patientType = ptype;
        scanType = stype;
        scanWeek = w;
        scanDay = d;
        callTime = cTime;
        tardiness = tard;
        isNoShow = noShow;
        durationScan = duration_scan;
    }

    // method to return the appointment waiting time

    static void getAppWT(){

    }

    // method to return the scan waiting time
    static void getScanWT(){

    }

}
