package edu.ualberta.med.biobank.applicationevents;

public class VisitCreatedEvent extends BiobankEvent {

    private static final long serialVersionUID = 1L;

    private final String pnumber;
    private final Integer vnumber;

    public VisitCreatedEvent(String username, String pnumber, Integer vnumber) {
        super(username);
        this.pnumber = pnumber;
        this.vnumber = vnumber;
    }

    public String getPnumber() {
        return pnumber;
    }

    public Integer getVnumber() {
        return vnumber;
    }
}
