package com.studies;

import java.io.File;
import java.io.IOException;

public class Account {
    private long accountNumber;
    private String PIN;
    private double balance;

    private File history;

    private String firstName;
    private String lastName;
    private String idNumber;

    public Account(long accountNumber, String PIN, String firstName, String lastName, String idNumber) {
        this.accountNumber = accountNumber;
        this.PIN = PIN;
        this.balance = 0.0;
        this.firstName = firstName;
        this.lastName = lastName;
        this.idNumber = idNumber;

        File historyFile = new File(accountNumber + "history.txt");

        try {
            historyFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.history = historyFile;
    }

    public Account(long accountNumber, String PIN, double balance, String firstName, String lastName, String idNumber) {
        this.accountNumber = accountNumber;
        this.PIN = PIN;
        this.balance = balance;
        this.firstName = firstName;
        this.lastName = lastName;
        this.idNumber = idNumber;
    }

    public long getAccountNumber() {
        return accountNumber;
    }

    public String getPIN() {
        return PIN;
    }

    public void setPIN(String PIN) {
        this.PIN = PIN;
    }

    public double getBalance() {
        return balance;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getIdNumber() {
        return idNumber;
    }

    public void outgoingTransfer(double amount) { balance -= amount; }

    public void incomingTransfer(double amount) {
        balance += amount;
    }
}
