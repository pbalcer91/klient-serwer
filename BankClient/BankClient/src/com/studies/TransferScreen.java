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

    private JLabel receiverAccountNumberLabel;
    private JTextField receiverAccountNumberField;
    private JLabel amountLabel;
    private JTextField amountField;
    private JButton confirmButton;
    private JButton cancelButton;

    private Socket socket;
    private InputStreamReader inputStreamReader = null;
    private OutputStreamWriter outputStreamWriter = null;

    private BufferedReader bufferedReader = null;
    private BufferedWriter bufferedWriter = null;

    public TransferScreen(Account account, MainWindow parent) {
        this.account = account;
        this.parent = parent;

        setLayout(new GridLayout(3, 2, 20, 10));
        setSize(new Dimension(350, 150));
        setUndecorated(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        GridBagConstraints layout = new GridBagConstraints();

        receiverAccountNumberLabel = new JLabel("Numer konta odbiorcy:");
        receiverAccountNumberField = new JTextField(10);
        amountLabel = new JLabel("Podaj kwotę:");
        amountField = new JTextField(10);
        confirmButton = new JButton("Zatwierdź");
        cancelButton = new JButton("Anuluj");

        confirmButton.addActionListener(this);
        cancelButton.addActionListener(this);

        add(receiverAccountNumberLabel, layout);
        add(receiverAccountNumberField, layout);
        add(amountLabel, layout);
        add(amountField, layout);
        add(confirmButton, layout);
        add(cancelButton, layout);

        setVisible(true);
    }

    private void clearFields() {
        receiverAccountNumberField.setText("");
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
            String receiverAccountNumberToCheck = receiverAccountNumberField.getText();
            String senderAccountNumber = String.valueOf(account.getAccountNumber());
            String amountToCheck = amountField.getText();

            if (receiverAccountNumberField.getText().isEmpty() || amountField.getText().isEmpty()) {
                JOptionPane.showMessageDialog(null,
                        "Nie wprowadzono numeru konta odbiorcy, lub/i kwoty przelewu",
                        "Błąd",
                        JOptionPane.ERROR_MESSAGE);

                clearFields();
                return;
            }

            if (receiverAccountNumberField.getText().equals(String.valueOf(account.getAccountNumber()))) {
                JOptionPane.showMessageDialog(null,
                        "Brak możliwości przelewu na własne konto",
                        "Błąd",
                        JOptionPane.ERROR_MESSAGE);

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

            if (Integer.parseInt(amountField.getText()) <= 0.0) {
                JOptionPane.showMessageDialog(null,
                        "Kwota musi być większa od 0",
                        "Zła wartość",
                        JOptionPane.WARNING_MESSAGE);

                clearFields();
                return;
            }

            if (Double.parseDouble(amountToCheck) > account.getBalance()) {

                JOptionPane.showMessageDialog(null,
                        "Brak wystarczających środków na koncie",
                        "Błąd",
                        JOptionPane.ERROR_MESSAGE);

                clearFields();
                return;
            }

            String enteredPIN = JOptionPane.showInputDialog(null,
                    "Podaj PIN, by zatwierdzić",
                    "Potwierdzenie",
                    JOptionPane.INFORMATION_MESSAGE
                    );

            if (enteredPIN == null)
                return;

            if (!enteredPIN.equals(account.getPIN())) {
                JOptionPane.showMessageDialog(null,
                        "Błędny PIN",
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

                bufferedWriter.write("TRANSFER");
                bufferedWriter.newLine();

                bufferedWriter.write(receiverAccountNumberToCheck);
                bufferedWriter.newLine();

                bufferedWriter.write(senderAccountNumber);
                bufferedWriter.newLine();

                bufferedWriter.write(amountToCheck);
                bufferedWriter.newLine();

                bufferedWriter.flush();

                String serverRespond = bufferedReader.readLine();

                if (serverRespond.equals("BRAK KONTA")) {
                    JOptionPane.showMessageDialog(null,
                            "Brak konta o podanym numerze",
                            "Błąd",
                            JOptionPane.ERROR_MESSAGE);

                }

                if (serverRespond.equals("PRZELEW UDANY")
                        && Double.parseDouble(amountToCheck) <= account.getBalance()) {

                    System.out.println("[CLIENT]: Transfer succeeded");

                    JOptionPane.showMessageDialog(null,
                            "Przelew wysłany",
                            "Przelew wychodzący",
                            JOptionPane.INFORMATION_MESSAGE);

                    parent.setVisible(true);
                    parent.updateAccountInfo();
                    this.dispose();
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
        }
    }
}
