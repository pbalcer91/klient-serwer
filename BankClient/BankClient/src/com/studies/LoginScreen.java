package com.studies;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;

public class LoginScreen extends JFrame implements ActionListener {

    private BufferedImage logo;

    private JLabel logoLabel;
    private JLabel accountNumberLabel;
    private JTextField accountNumberField;
    private JLabel pinLabel;
    private JPasswordField pinField;
    private JButton okButton;
    private JButton createAccountButton;
    private JButton exitButton;

    private Socket socket = null;
    private InputStreamReader inputStreamReader = null;
    private OutputStreamWriter outputStreamWriter = null;

    private BufferedReader bufferedReader = null;
    private BufferedWriter bufferedWriter = null;

    public LoginScreen() {
        setLayout(new FlowLayout());

        setSize(new Dimension(150, 300));
        setUndecorated(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

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
        accountNumberLabel = new JLabel("Podaj numer konta");
        accountNumberField = new JTextField(10);
        accountNumberField.setHorizontalAlignment(JTextField.CENTER);
        pinLabel = new JLabel("Podaj numer PIN");
        pinField = new JPasswordField(10);
        pinField.setHorizontalAlignment(JTextField.CENTER);
        okButton = new JButton("Zatwierdź");
        createAccountButton = new JButton("Nowe konto");
        exitButton = new JButton("Wyjdź");

        okButton.addActionListener(this);
        createAccountButton.addActionListener(this);
        exitButton.addActionListener(this);

        add(logoLabel);
        add(accountNumberLabel, layout);
        add(accountNumberField, layout);
        add(pinLabel, layout);
        add(pinField, layout);
        add(okButton, layout);
        add(createAccountButton, layout);
        add(exitButton, layout);

        setVisible(true);
    }

    private void clearFields() {
        accountNumberField.setText("");
        pinField.setText("");
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        JButton pressedButton = (JButton) actionEvent.getSource();

        if (pressedButton.equals(createAccountButton)) {
            CreateAccountScreen createAccount = new CreateAccountScreen(this);
            this.setVisible(false);
        }

        if (pressedButton.equals(okButton)) {
            String accountNumberToCheck = accountNumberField.getText();
            String pinNumberToCheck = String.valueOf(pinField.getPassword());

            if (accountNumberField.getText().isEmpty() || String.valueOf(pinField.getPassword()).isEmpty()) {
                JOptionPane.showMessageDialog(null,
                        "Nie wprowadzono numeru konta, lub/i numeru PIN",
                        "Błąd",
                        JOptionPane.ERROR_MESSAGE);

                clearFields();
                return;
            }

            try {
                socket = new Socket("192.168.56.102", 1234);

                inputStreamReader = new InputStreamReader(socket.getInputStream());
                outputStreamWriter = new OutputStreamWriter(socket.getOutputStream());

                bufferedReader = new BufferedReader(inputStreamReader);
                bufferedWriter = new BufferedWriter(outputStreamWriter);

                bufferedWriter.write("LOGIN");
                bufferedWriter.newLine();

                bufferedWriter.write(accountNumberToCheck);
                bufferedWriter.newLine();

                bufferedWriter.write(pinNumberToCheck);
                bufferedWriter.newLine();

                bufferedWriter.flush();

                String serverRespond = bufferedReader.readLine();

                if (serverRespond.equals("PIN ZGODNY")) {
                    long accountNumber = Long.parseLong(bufferedReader.readLine());
                    String pinNumber = bufferedReader.readLine();
                    double balance = Double.parseDouble(bufferedReader.readLine());
                    String firstName = bufferedReader.readLine();
                    String lastName = bufferedReader.readLine();
                    String idNumber = bufferedReader.readLine();

                    MainWindow clientApp = new MainWindow(
                            new Account(accountNumber,
                                    pinNumber,
                                    balance,
                                    firstName,
                                    lastName,
                                    idNumber),
                            this
                    );

                    this.setVisible(false);
                }

                if (serverRespond.equals("PIN NIEZGODNY")) {
                    JOptionPane.showMessageDialog(null,
                            "Błędny numer PIN",
                            "Błąd",
                            JOptionPane.ERROR_MESSAGE);
                }

                if (serverRespond.equals("BRAK KONTA")) {
                    JOptionPane.showMessageDialog(null,
                            "Brak konta o podanym numerze",
                            "Błąd",
                            JOptionPane.ERROR_MESSAGE);
                }

                clearFields();

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

        if (pressedButton.equals(exitButton)) {
            if (JOptionPane.showConfirmDialog(new JFrame("Wyjście"),"Czy na pewno chcesz wyjśc z aplikacji?","BankApp",
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {

                System.exit(0);
            }
        }
    }
}
