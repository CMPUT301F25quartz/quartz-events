package com.example.ajilore.code.ui.events.data;

public class Entrant {
    public final String uid;
    public final String nameOrUid;
    public final String responded;

    public Entrant(String uid, String name, String responded) {
        this.uid = uid;
        this.nameOrUid = (name == null || name.isEmpty()) ? uid : name;
        this.responded = (responded == null || responded.isEmpty()) ? "Pending" : responded;
    }
}
