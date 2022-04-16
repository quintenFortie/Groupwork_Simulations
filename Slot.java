// an object class that contains all info about a slot such independent of the appointment scheduling rule

public class Slot {

    double startTime;       // may be different from apptime if fifo is not used
    double appTime;         // appointment time a patient receives
    int patientType;        // 0 = none // 1 = elective // 2 = urgent
    int slotType; // 0 = none // 1 = elective // 2 = urgent normal // 3 = urgent overtime

}
