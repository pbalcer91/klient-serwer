package com.studies;

public class Account {
    private double balance;
    private long accountNumber;

    public Account(double balance, long accountNumber) {
        this.balance = balance;
        this.accountNumber = accountNumber;
    }

    public double getBalance() {
        return balance;
    }

    public long getAccountNumber() { return accountNumber; }
}
