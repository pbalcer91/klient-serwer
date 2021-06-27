package com.studies;

import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ClientHandler implements Runnable {
    private long lastNumber = 9020210000L;

    private ArrayList<Account> accounts;

    private Socket socket;
    private InputStreamReader inputStreamReader;
    private OutputStreamWriter outputStreamWriter;

    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;

    private enum TransferType {
        OUTGOING,
        INCOMING,
        DEPOSIT,
        WITHDRAW
    }

    ClientHandler(Socket socket) throws IOException {
        this.socket = socket;

        this.inputStreamReader = new InputStreamReader(socket.getInputStream());
        this.outputStreamWriter = new OutputStreamWriter(socket.getOutputStream());

        this.bufferedReader = new BufferedReader(inputStreamReader);
        this.bufferedWriter = new BufferedWriter(outputStreamWriter);

        accounts = new ArrayList<>();
    }

    private boolean getAccounts() {
        try {
            BufferedReader accountReader = new BufferedReader(new FileReader("accountsData.txt"));

            String accountNumber;
            String pinNumber;
            String balance;
            String firstName;
            String lastName;
            String idNumber;

            while (accountReader.readLine() != null) {
                accountNumber = accountReader.readLine();
                pinNumber = accountReader.readLine();
                balance = accountReader.readLine();
                firstName = accountReader.readLine();
                lastName = accountReader.readLine();
                idNumber = accountReader.readLine();

                accounts.add(new Account(Long.parseLong(accountNumber),
                        pinNumber,
                        Double.parseDouble(balance),
                        firstName,
                        lastName,
                        idNumber));
            }

            accountReader.close();
            return true;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean saveAccounts() {
        try {
            BufferedWriter accountsWriter = new BufferedWriter(new FileWriter("accountsData.txt"));

            for (int i = 0; i < accounts.size(); i++) {
                accountsWriter.write("\n" + accounts.get(i).getAccountNumber());
                accountsWriter.write("\n" + accounts.get(i).getPIN());
                accountsWriter.write("\n" + accounts.get(i).getBalance());
                accountsWriter.write("\n" + accounts.get(i).getFirstName());
                accountsWriter.write("\n" + accounts.get(i).getLastName());
                accountsWriter.write("\n" + accounts.get(i).getIdNumber() + "\n");
            }

            accountsWriter.close();
            return true;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean addToHistory(String accountNumber, TransferType type, String senderOrReceiver, double amount) {
        try {
            BufferedWriter accountsWriter = new BufferedWriter(new FileWriter(accountNumber + "history.txt", true));

            String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

            String typeString = "";

            switch (type) {
                case INCOMING:
                    typeString = "Przelew przychodzący";
                    senderOrReceiver = "Otrzymano od " + senderOrReceiver;
                    break;
                case OUTGOING:
                    typeString = "Przelew wychodzący";
                    senderOrReceiver = "Wysłano na " + senderOrReceiver;
                    break;
                case DEPOSIT:
                    typeString = "Wpłata";
                    senderOrReceiver = "Operacja ATM";
                    break;
                case WITHDRAW:
                    typeString = "Wypłata";
                    senderOrReceiver = "Operacja ATM";
                    break;
            }

            accountsWriter.write(date + " | " + typeString + " | " + senderOrReceiver + " | " + String.format("%.2f", amount) + "zł\n");

            accountsWriter.close();
            return true;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private int getAccountIndexByNumber(Long accountNumber) {
        for (int i = 0; i < accounts.size(); i++) {
            if (accounts.get(i).getAccountNumber() == accountNumber)
                return i;
        }

        return -1;
    }

    @Override
    public void run() {
        try {
            getAccounts();

            String clientMessage = bufferedReader.readLine();

            if (clientMessage.equals("LOGIN")) {
                System.out.println("[SERVER]: Client logged");
                String accountNumberToCheck = bufferedReader.readLine();
                String pinToCheck = bufferedReader.readLine();

                int accountIndex = getAccountIndexByNumber(Long.parseLong(accountNumberToCheck));

                if (accountIndex == -1) {
                    bufferedWriter.write("BRAK KONTA\n");
                    bufferedWriter.flush();
                    return;
                }

                if (accounts.get(accountIndex).getPIN().equals(pinToCheck)) {
                    bufferedWriter.write("PIN ZGODNY\n");
                    bufferedWriter.write(accounts.get(accountIndex).getAccountNumber() + "\n");
                    bufferedWriter.write(accounts.get(accountIndex).getPIN() + "\n");
                    bufferedWriter.write(accounts.get(accountIndex).getBalance() + "\n");
                    bufferedWriter.write(accounts.get(accountIndex).getFirstName() + "\n");
                    bufferedWriter.write(accounts.get(accountIndex).getLastName() + "\n");
                    bufferedWriter.write(accounts.get(accountIndex).getIdNumber() + "\n");
                    bufferedWriter.flush();
                    return;
                }

                bufferedWriter.write("PIN NIEZGODNY\n");
                bufferedWriter.flush();
                return;
            }

            if (clientMessage.equals("TRANSFER")) {
                String receiverAccountNumberToCheck = bufferedReader.readLine();
                String senderAccountNumberToCheck = bufferedReader.readLine();
                String amount = bufferedReader.readLine();

                int accountIndex = getAccountIndexByNumber(Long.parseLong(receiverAccountNumberToCheck));

                if (accountIndex == -1) {
                    bufferedWriter.write("BRAK KONTA\n");
                    bufferedWriter.flush();
                    return;
                }

                accounts.get(accountIndex).incomingTransfer(Double.parseDouble(amount));
                addToHistory(receiverAccountNumberToCheck, TransferType.INCOMING, senderAccountNumberToCheck, Double.parseDouble(amount));

                accounts.get(getAccountIndexByNumber(Long.parseLong(senderAccountNumberToCheck)))
                        .outgoingTransfer(Double.parseDouble(amount));
                addToHistory(senderAccountNumberToCheck, TransferType.OUTGOING, receiverAccountNumberToCheck, Double.parseDouble(amount));

                System.out.println("[SERVER]: Client transfered money");
                bufferedWriter.write("PRZELEW UDANY\n");
                bufferedWriter.flush();

                saveAccounts();
                return;
            }

            if (clientMessage.equals("WYPLATA")) {
                String accountNumber = bufferedReader.readLine();
                String amount = bufferedReader.readLine();

                int accountIndex = getAccountIndexByNumber(Long.parseLong(accountNumber));

                accounts.get(accountIndex).outgoingTransfer(Double.parseDouble(amount));
                addToHistory(accountNumber, TransferType.WITHDRAW, accountNumber, Double.parseDouble(amount));

                System.out.println("[SERVER]: Client withdrawn money");
                bufferedWriter.write("WYPLATA UDANA\n");
                bufferedWriter.flush();

                saveAccounts();
                return;
            }

            if (clientMessage.equals("WPLATA")) {
                String accountNumber = bufferedReader.readLine();
                String amount = bufferedReader.readLine();

                int accountIndex = getAccountIndexByNumber(Long.parseLong(accountNumber));

                accounts.get(accountIndex).incomingTransfer(Double.parseDouble(amount));
                addToHistory(accountNumber, TransferType.DEPOSIT, accountNumber, Double.parseDouble(amount));

                System.out.println("[SERVER]: Client withdrawn money");
                bufferedWriter.write("WPLATA UDANA\n");
                bufferedWriter.flush();

                saveAccounts();
                return;
            }

            if (clientMessage.equals("ZMIEN PIN")) {
                String newPin = bufferedReader.readLine();
                String accountNumber = bufferedReader.readLine();

                int accountIndex = getAccountIndexByNumber(Long.parseLong(accountNumber));

                accounts.get(accountIndex).setPIN(newPin);

                System.out.println("[SERVER]: Client changed PIN");
                bufferedWriter.write("ZMIANA UDANA\n");
                bufferedWriter.flush();

                saveAccounts();
                return;
            }

            if (clientMessage.equals("NOWE KONTO")) {
                String firstName = bufferedReader.readLine();
                String lastName = bufferedReader.readLine();
                String idNumber = bufferedReader.readLine();
                String PIN = bufferedReader.readLine();

                long newAccountNumber = lastNumber + accounts.size();

                accounts.add(new Account(newAccountNumber, PIN, firstName, lastName, idNumber));

                System.out.println("[SERVER]: New account created");
                bufferedWriter.write("KONTO UTWORZONE\n");
                bufferedWriter.flush();

                saveAccounts();
                return;
            }

            if (clientMessage.equals("HISTORIA")) {
                String accountNumber = bufferedReader.readLine();

                BufferedReader historyReader = new BufferedReader(new FileReader(accountNumber + "history.txt"));
                ArrayList<String> history = new ArrayList<>();

                String historyPosition = historyReader.readLine();

                while (historyPosition != null) {
                    history.add(historyPosition);
                    historyPosition = historyReader.readLine();
                }

                System.out.println("[SERVER]: History prepared");
                bufferedWriter.write(history.size() + "\n");

                for (int i = history.size() - 1; i >= 0; i--) {
                    bufferedWriter.write(history.get(i) + "\n");
                }

                System.out.println("[SERVER]: History delivered");
                bufferedWriter.flush();

                return;
            }

            if (clientMessage.equals("AKTUALIZACJA")) {
                String accountNumber = bufferedReader.readLine();

                BufferedReader historyReader = new BufferedReader(new FileReader(accountNumber + "history.txt"));
                ArrayList<String> history = new ArrayList<>();

                String historyPosition = historyReader.readLine();

                while (historyPosition != null) {
                    history.add(historyPosition);
                    historyPosition = historyReader.readLine();
                }

                System.out.println("[SERVER]: Update Data prepared");

                bufferedWriter.write(accounts.get(
                        getAccountIndexByNumber(Long.parseLong(accountNumber))).getBalance() + "\n");

                bufferedWriter.write(history.size() + "\n");

                for (int i = history.size() - 1; i >= history.size() - 3; i--) {
                    bufferedWriter.write(history.get(i) + "\n");

                    if (i == 0)
                        break;
                }

                System.out.println("[SERVER]: Update Data delivered");
                bufferedWriter.flush();

                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
