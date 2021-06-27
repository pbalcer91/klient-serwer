package com.studies;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;

public class ChangePinScreen extends JFrame implements ActionListener {
    private Account account;
    private MainWindow parent;

    private BufferedImage logo;

    private JLabel logoLabel;
    private JLabel oldPinLabel;
    private JPasswordField oldPinField;
    private JLabel newPinLabel;
    private JPasswordField newPinField;
    private JLabel repeatPinLabel;
    private JPasswordField repeatPinField;
    private JButton confirmButton;
    private JButton cancelButton;

    private Socket socket;
    private InputStreamReader inputStreamReader = null;
    private OutputStreamWriter outputStreamWriter = null;

    private BufferedReader bufferedReader = null;
    private BufferedWriter bufferedWriter = null;

    public ChangePinScreen(Account account, MainWindow parent) {
        this.account = account;
        this.parent = parent;

        setLayout(new FlowLayout());
        setSize(new Dimension(150, 310));
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
        oldPinLabel = new JLabel("Aktualny PIN:");
        oldPinField = new JPasswordField(10);
        oldPinField.setHorizontalAlignment(JTextField.CENTER);
        newPinLabel = new JLabel("Nowy PIN:");
        newPinField = new JPasswordField(10);
        newPinField.setHorizontalAlignment(JTextField.CENTER);
        repeatPinLabel = new JLabel("Powtórz nowy PIN:");
        repeatPinField = new JPasswordField(10);
        repeatPinField.setHorizontalAlignment(JTextField.CENTER);

        confirmButton = new JButton("Zatwierdź");
        cancelButton = new JButton("Anuluj");

        confirmButton.addActionListener(this);
        cancelButton.addActionListener(this);

        add(logoLabel);
        add(oldPinLabel, layout);
        add(oldPinField, layout);
        add(newPinLabel, layout);
        add(newPinField, layout);
        add(repeatPinLabel, layout);
        add(repeatPinField, layout);
        add(confirmButton, layout);
        add(cancelButton, layout);

        setVisible(true);
    }

    private void clearFields() {
        oldPinField.setText("");
        newPinField.setText("");
        repeatPinField.setText("");
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
            String oldPin = String.valueOf(oldPinField.getPassword());
            String newPin = String.valueOf(newPinField.getPassword());

            if (String.valueOf(oldPinField.getPassword()).isEmpty()
                    || String.valueOf(newPinField.getPassword()).isEmpty()
                    || String.valueOf(repeatPinField.getPassword()).isEmpty()) {
                JOptionPane.showMessageDialog(null,
                        "Wszystkie pola muszą być uzupełnione",
                        "Błąd",
                        JOptionPane.ERROR_MESSAGE);

                clearFields();
                return;
            }

            if (!oldPin.equals(account.getPIN())) {
                JOptionPane.showMessageDialog(null,
                        "Niepoprawny PIN",
                        "Błąd",
                        JOptionPane.ERROR_MESSAGE);

                clearFields();
                return;
            }

            if (newPin.length() != 4 || !MainWindow.isNumeric(newPin)) {
                JOptionPane.showMessageDialog(null,
                        "PIN musi składać się z 4 cyfr",
                        "Błąd",
                        JOptionPane.ERROR_MESSAGE);

                clearFields();
                return;
            }

            if (!newPin.equals(String.valueOf(repeatPinField.getPassword()))) {
                JOptionPane.showMessageDialog(null,
                        "Niepoprawnie powtórzono nowy PIN",
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

                bufferedWriter.write("ZMIEN PIN");
                bufferedWriter.newLine();

                bufferedWriter.write(newPin);
                bufferedWriter.newLine();

                bufferedWriter.write(String.valueOf(account.getAccountNumber()));
                bufferedWriter.newLine();

                bufferedWriter.flush();

                String serverRespond = bufferedReader.readLine();

                if (serverRespond.equals("ZMIANA UDANA")) {
                    account.setPIN(newPin);

                    System.out.println("[CLIENT]: PIN change succeeded");

                    JOptionPane.showMessageDialog(null,
                            "Ustawiono nowy PIN",
                            "Zmiana PIN",
                            JOptionPane.INFORMATION_MESSAGE);

                    parent.setVisible(true);
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
