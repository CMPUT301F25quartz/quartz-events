package com.example.ajilore.code.ui.profile;

public class Entrant {
    private String name;
    private String email;
    private String phone; //optional
    private String role;

    //firestore constructor
    public Entrant(){ }

    public Entrant(String name, String email, String phone){
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.role = "entrant";
    }

    public String getName(){
        return name;
    }

    public String getEmail(){
        return email;
    }

    public String getPhone(){
        return phone;
    }
    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {

        this.email = email;
    }

    public void setPhone(String phone) {

        this.phone = phone;
    }

    public String getRole(){
        return role;
    }

    public void setRole(String role){
        this.role = role;
    }

}
