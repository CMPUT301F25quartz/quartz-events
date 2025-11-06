package com.example.ajilore.code.ui.events.data;

public class Entrant {
    public final String uid;
    public final String nameOrUid;
    public final String displayStatus;

    public Entrant(String uid, String name, String displayStatus) {
        this.uid = uid;
        this.nameOrUid = (name == null || name.isEmpty()) ? uid : name;
        this.displayStatus = (displayStatus == null || displayStatus.isEmpty()) ? "Pending" : displayStatus;
    }
}
