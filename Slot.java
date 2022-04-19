public class Slot {

    double startTime;               // start time of the slot (in hours)
    double appTime;                 // appointment time of the slot, dependant on type and rule (in hours)
    int patientType;                // type of slot (0=none, 1=elective, 2=urgent within normal working hours, 3=urgent in overtime)
    int slotType;                   // (0=none, 1=elective, 2=urgent)

}
