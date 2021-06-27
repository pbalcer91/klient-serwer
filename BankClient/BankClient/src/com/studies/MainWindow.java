package com.studies;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainWindow extends JFrame implements ActionListener {
    private Account account;
    private LoginScreen parent;

    private BufferedImage logo;

    private JLabel logoLabel;
    private JButton transferButton;
    private JButton historyButton;
    private JButton accountInfoButton;
    private JButton clientInfoButton;
    private JButton changePINButton;
    private JButton updateButton;
    private JButton logoutButton;
    private JLabel balanceInfo;
    private JLabel appInfo;
    private JTextArea displayScreen;

    private Socket socket;
    private InputStreamReader inputStreamReader = null;
    private OutputStreamWriter outputStreamWriter = null;

    private BufferedReader bufferedReader = null;
    private BufferedWriter bufferedWriter = null;

    private String lastUpdate;

    public MainWindow(Account account, LoginScreen parent) throws IOException {
        setLayout(new FlowLayout());
        setSize(new Dimension(550, 400));
        setTitle("BankApp");
        //setUndecorated(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        this.account = account;
        this.parent = parent;
        this.lastUpdate = new SimpleDateFormat("HH:mm:ss").format(new Date());

        GridBagConstraints layout = new GridBagConstraints();

        try {
            logo = ImageIO.read(new File("logo.png"));
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null,
                    "Błąd aplikacji",
                    "Błąd",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        logoLabel = new JLabel(new ImageIcon(logo));
        logoLabel.setPreferredSize(new Dimension(550, 100));
        transferButton = new JButton("Przelew");
        historyButton = new JButton("Historia transakcji");
        accountInfoButton = new JButton("Informacje o koncie");
        clientInfoButton = new JButton("Dane posiadacza konta");
        changePINButton = new JButton("Zmień PIN");
        updateButton = new JButton("Aktualizuj");
        logoutButton = new JButton("Wyloguj");
        balanceInfo = new JLabel("Stan konta: " + String.format("%.2f", account.getBalance()) +"zł");
        balanceInfo.setFont(new Font("BALANCE", Font.BOLD, 20));
        balanceInfo.setHorizontalAlignment(JLabel.CENTER);
        balanceInfo.setPreferredSize(new Dimension(550, 20));
        appInfo = new JLabel("Ostatnia migracja danych: " + lastUpdate);
        displayScreen = new JTextArea(7, 40);
        displayScreen.setEditable(false);

        transferButton.addActionListener(this);
        historyButton.addActionListener(this);
        accountInfoButton.addActionListener(this);
        clientInfoButton.addActionListener(this);
        changePINButton.addActionListener(this);
        updateButton.addActionListener(this);
        logoutButton.addActionListener(this);

        add(logoLabel);
        add(transferButton, layout);
        add(historyButton, layout);
        add(accountInfoButton, layout);
        add(clientInfoButton, layout);
        add(changePINButton, layout);
        add(balanceInfo);
        add(displayScreen, layout);
        add(updateButton, layout);
        add(appInfo, layout);
        add(logoutButton, layout);

        try {
            this.socket = new Socket("192.168.56.102", 1234);

            inputStreamReader = new InputStreamReader(socket.getInputStream());
            outputStreamWriter = new OutputStreamWriter(socket.getOutputStream());

            bufferedReader = new BufferedReader(inputStreamReader);
            bufferedWriter = new BufferedWriter(outputStreamWriter);

            bufferedWriter.write("HISTORIA");
            bufferedWriter.newLine();

            bufferedWriter.write(String.valueOf(account.getAccountNumber()));
            bufferedWriter.newLine();

            bufferedWriter.flush();

            String serverRespond = bufferedReader.readLine();

            int historyCount = Integer.parseInt(serverRespond);

            if (historyCount > 0)
                displayScreen.append(HistoryScreen.putSeparator());

            for (int i = 0; i < 3; i++) {
                String historyPosition = bufferedReader.readLine();
                displayScreen.append(historyPosition + "\n");
                displayScreen.append(HistoryScreen.putSeparator());
            }

        } catch (IOException e) {
            JOptionPane.showMessageDialog(null,
                    "Serwer nie odpowiada",
                    "Błąd",
                    JOptionPane.ERROR_MESSAGE);
        }

        try {
            if (socket != null) {
                System.out.println("[CLIENT]: Disconnected");
                socket.close();
            }

            if (inputStreamReader != null)
                inputStreamReader.close();

            if (outputStreamWriter != null)
                outputStreamWriter.close();

            if (bufferedReader != null)
                bufferedReader.close();

            if (bufferedWriter != null)
                bufferedWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        setVisible(true);
    }

    public static boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException exception) {
            return false;
        }
    }

    public void updateAccountInfo() {
        try {
            this.socket = new Socket("192.168.56.102", 1234);

            inputStreamReader = new InputStreamReader(socket.getInputStream());
            outputStreamWriter = new OutputStreamWriter(socket.getOutputStream());

            bufferedReader = new BufferedReader(inputStreamReader);
            bufferedWriter = new BufferedWriter(outputStreamWriter);

            bufferedWriter.write("AKTUALIZACJA");
            bufferedWriter.newLine();

            bufferedWriter.write(String.valueOf(account.getAccountNumber()));
            bufferedWriter.newLine();

            bufferedWriter.flush();

            String serverBalance = bufferedReader.readLine();

            ArrayList<String> serverLastTransactions = new ArrayList<>();

            String serverRespond = bufferedReader.readLine();

            int historyCount = Integer.parseInt(serverRespond);

            for (int i = 0; i < historyCount; i++) {
                String serverHistory = bufferedReader.readLine();
                serverLastTransactions.add(serverHistory);

                if (i == 2)
                    break;
            }

            displayScreen.setText(null);

            if (serverLastTransactions.size() > 0)
                displayScreen.append(HistoryScreen.putSeparator());

            for (int i = 0; i < 3; i++) {
                displayScreen.append(serverLastTransactions.get(i) + "\n");
                displayScreen.append(HistoryScreen.putSeparator());
            }

            double balance = Double.parseDouble(serverBalance);
            balanceInfo.setText("Stan konta: " + String.format("%.2f", balance) +"zł");

            lastUpdate = new SimpleDateFormat("HH:mm:ss").format(new Date());
            appInfo.setText("Ostatnia migracja danych: " + lastUpdate);

            System.out.println("[CLIENT]: Update completed");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null,
                    "Serwer nie odpowiada",
                    "Błąd",
                    JOptionPane.ERROR_MESSAGE);
        }

        try {
            if (socket != null) {
                System.out.println("[CLIENT]: Disconnected");
                socket.close();
            }

            if (inputStreamReader != null)
                inputStreamReader.close();

            if (outputStreamWriter != null)
                outputStreamWriter.close();

            if (bufferedReader != null)
                bufferedReader.close();

            if (bufferedWriter != null)
                bufferedWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        JButton pressedButton = (JButton) actionEvent.getSource();

        if (pressedButton.equals(transferButton) ) {
            TransferScreen transfer = new TransferScreen(account, this);
            setVisible(false);
            return;
        }

        if (pressedButton.equals(historyButton) ) {
            HistoryScreen history = new HistoryScreen(account, this);
            setVisible(false);
            return;
        }

        if (pressedButton.equals(accountInfoButton) ) {
            JOptionPane.showMessageDialog(null,
                    "Numer konta: " + account.getAccountNumber(),
                    "Informacje o koncie",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        if (pressedButton.equals(clientInfoButton) ) {
            JOptionPane.showMessageDialog(null,
                    "Imię: " + account.getFirstName()
                    + "\nNazwisko: " + account.getLastName()
                    + "\nNumer ID: " + account.getIdNumber(),
                    "Dane klienta",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        if (pressedButton.equals(changePINButton) ) {
            ChangePinScreen changePin = new ChangePinScreen(account, this);
            setVisible(false);
            return;
        }

        if (pressedButton.equals(updateButton) ) {
            updateAccountInfo();
            return;
        }

        if (pressedButton.equals(logoutButton) ) {

            if (JOptionPane.showConfirmDialog(new JFrame("Wylogowanie"),"Czy na pewno chcesz się wylogować?","BankApp",
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {

                parent.setVisible(true);
                this.dispose();
            }
        }
    }
}
