package com.studies;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;

public class TransferScreen extends JFrame implements ActionListener {
    private Account account;
    private MainWindow parent;

    private MainWindow.transferType type;

    private JLabel amountLabel;
    private JTextField amountField;
    private JButton confirmButton;
    private JButton cancelButton;

    private Socket socket;
    private InputStreamReader inputStreamReader = null;
    private OutputStreamWriter outputStreamWriter = null;

    private BufferedReader bufferedReader = null;
    private BufferedWriter bufferedWriter = null;

    public TransferScreen(Account account, MainWindow.transferType type, MainWindow parent) {
        this.account = account;
        this.type = type;
        this.parent = parent;

        String message = (type == MainWindow.transferType.DEPOSIT ?
                "Podaj kwotę do wpłaty:" : "Podaj kwotę do wypłaty:");

        setLayout(new GridLayout(2, 2, 20, 10));
        setSize(new Dimension(375, 75));
        setUndecorated(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        GridBagConstraints layout = new GridBagConstraints();

        amountLabel = new JLabel(message);
        amountField = new JTextField(10);
        confirmButton = new JButton("Zatwierdź");
        cancelButton = new JButton("Anuluj");

        confirmButton.addActionListener(this);
        cancelButton.addActionListener(this);

        add(amountLabel, layout);
        add(amountField, layout);
        add(confirmButton, layout);
        add(cancelButton, layout);

        setVisible(true);
    }

    private void clearFields() {
        amountField.setText("");
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        JButton pressedButton = (JButton)actionEvent.getSource();

        if (pressedButton.equals(cancelButton)) {
            this.parent.setVisible(true);
            this.dispose();

            return;
        }

        if (pressedButton.equals(confirmButton)) {
            String amountToCheck = amountField.getText();

            if (amountField.getText().isEmpty()) {
                JOptionPane.showMessageDialog(null,
                        "Nie wprowadzono kwoty przelewu",
                        "Błąd",
                        JOptionPane.ERROR_MESSAGE);

                clearFields();
                return;
            }

            if (Integer.parseInt(amountField.getText()) <= 0.0) {
                JOptionPane.showMessageDialog(null,
                        "Kwota musi być większa od 0",
                        "Zła wartość",
                        JOptionPane.WARNING_MESSAGE);

                clearFields();
                return;
            }

            if (!MainWindow.isNumeric(amountField.getText())) {
                JOptionPane.showMessageDialog(null,
                        "Kwota musi być liczbą",
                        "Zły format",
                        JOptionPane.WARNING_MESSAGE);

                clearFields();
                return;
            }

            if (Double.parseDouble(amountToCheck) > account.getBalance()
            && type.equals(MainWindow.transferType.WITHDRAW)) {
                JOptionPane.showMessageDialog(null,
                        "Brak wystarczających środków na koncie",
                        "Błąd",
                        JOptionPane.ERROR_MESSAGE);

                clearFields();
                return;
            }

            try {
                this.socket = new Socket("192.168.56.102", 1234);

                inputStreamReader = new InputStreamReader(socket.getInputStream());
                outputStreamWriter = new OutputStreamWriter(socket.getOutputStream());

                bufferedReader = new BufferedReader(inputStreamReader);
                bufferedWriter = new BufferedWriter(outputStreamWriter);

                if (type == MainWindow.transferType.WITHDRAW) {
                    bufferedWriter.write("WYPLATA");
                    bufferedWriter.newLine();

                    bufferedWriter.write(String.valueOf(account.getAccountNumber()));
                    bufferedWriter.newLine();

                    bufferedWriter.write(amountToCheck);
                    bufferedWriter.newLine();

                    bufferedWriter.flush();

                    String serverRespond = bufferedReader.readLine();

                    if (serverRespond.equals("WYPLATA UDANA")) {
                        System.out.println("[CLIENT]: Transfer succeeded");

                        JOptionPane.showMessageDialog(null,
                                "Wypłacono " + String.format("%.2f", Double.parseDouble(amountToCheck)) + "zł",
                                "Wypłata",
                                JOptionPane.INFORMATION_MESSAGE);
                    }
                }

                if (type == MainWindow.transferType.DEPOSIT) {
                    bufferedWriter.write("WPLATA");
                    bufferedWriter.newLine();

                    bufferedWriter.write(String.valueOf(account.getAccountNumber()));
                    bufferedWriter.newLine();

                    bufferedWriter.write(amountToCheck);
                    bufferedWriter.newLine();

                    bufferedWriter.flush();

                    String serverRespond = bufferedReader.readLine();

                    if (serverRespond.equals("WPLATA UDANA")) {
                        System.out.println("[CLIENT]: Transfer succeeded");

                        JOptionPane.showMessageDialog(null,
                            "Wpłacono " + String.format("%.2f", Double.parseDouble(amountToCheck)) + "zł",
                            "Wpłata",
                            JOptionPane.INFORMATION_MESSAGE);
                    }
                }

                this.dispose();
                parent.logout();
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
    }
}
