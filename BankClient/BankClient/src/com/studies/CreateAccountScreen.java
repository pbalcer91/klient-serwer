package com.studies;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;

public class CreateAccountScreen extends JFrame implements ActionListener {
    private LoginScreen parent;

    private BufferedImage logo;

    private JLabel logoLabel;
    private JLabel firstNameLabel;
    private JTextField firstNameField;
    private JLabel lastNameLabel;
    private JTextField lastNameField;
    private JLabel idNumberLabel;
    private JTextField idNumberField;
    private JLabel pinLabel;
    private JPasswordField pinField;
    private JButton confirmButton;
    private JButton cancelButton;

    private Socket socket;
    private InputStreamReader inputStreamReader = null;
    private OutputStreamWriter outputStreamWriter = null;

    private BufferedReader bufferedReader = null;
    private BufferedWriter bufferedWriter = null;

    public CreateAccountScreen(LoginScreen parent) {
        this.parent = parent;

        setLayout(new FlowLayout());
        setSize(new Dimension(150, 360));
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
        firstNameLabel = new JLabel("Podaj imię:");
        firstNameField = new JTextField(10);
        firstNameField.setHorizontalAlignment(JTextField.CENTER);
        lastNameLabel = new JLabel("Podaj nazwisko:");
        lastNameField = new JTextField(10);
        lastNameField.setHorizontalAlignment(JTextField.CENTER);
        idNumberLabel = new JLabel("Podaj numer ID:");
        idNumberField = new JTextField(10);
        idNumberField.setHorizontalAlignment(JTextField.CENTER);
        pinLabel = new JLabel("Utwórz PIN:");
        pinField = new JPasswordField(10);
        pinField.setHorizontalAlignment(JTextField.CENTER);

        confirmButton = new JButton("Zatwierdź");
        cancelButton = new JButton("Anuluj");

        confirmButton.addActionListener(this);
        cancelButton.addActionListener(this);

        add(logoLabel);
        add(firstNameLabel, layout);
        add(firstNameField, layout);
        add(lastNameLabel, layout);
        add(lastNameField, layout);
        add(idNumberLabel, layout);
        add(idNumberField, layout);
        add(pinLabel, layout);
        add(pinField, layout);
        add(confirmButton, layout);
        add(cancelButton, layout);

        setVisible(true);
    }

    private void clearFields() {
        firstNameField.setText("");
        lastNameField.setText("");
        idNumberField.setText("");
        pinField.setText("");
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
            String firstName = firstNameField.getText();
            String lastName = lastNameField.getText();
            String idNumber = idNumberField.getText();
            String PIN = String.valueOf(pinField.getPassword());

            if (firstNameField.getText().isEmpty() || lastNameField.getText().isEmpty()
                    || idNumberField.getText().isEmpty() || String.valueOf(pinField.getPassword()).isEmpty()) {
                JOptionPane.showMessageDialog(null,
                        "Wszystkie pola muszą być uzupełnione",
                        "Błąd",
                        JOptionPane.ERROR_MESSAGE);

                clearFields();
                return;
            }

            if (idNumber.length() != 9) {
                JOptionPane.showMessageDialog(null,
                        "Numer ID musi skłądać się z 9 znaków",
                        "Błąd",
                        JOptionPane.ERROR_MESSAGE);

                clearFields();
                return;
            }

            if (PIN.length() != 4 || !MainWindow.isNumeric(PIN)) {
                JOptionPane.showMessageDialog(null,
                        "PIN musi składać się z 4 cyfr",
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

                bufferedWriter.write("NOWE KONTO");
                bufferedWriter.newLine();

                bufferedWriter.write(firstName);
                bufferedWriter.newLine();

                bufferedWriter.write(lastName);
                bufferedWriter.newLine();

                bufferedWriter.write(idNumber);
                bufferedWriter.newLine();

                bufferedWriter.write(PIN);
                bufferedWriter.newLine();

                bufferedWriter.flush();

                String serverRespond = bufferedReader.readLine();

                if (serverRespond.equals("KONTO UTWORZONE")) {

                    System.out.println("[CLIENT]: Account created");

                    JOptionPane.showMessageDialog(null,
                            "Utworzono nowe konto",
                            "Witamy",
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
