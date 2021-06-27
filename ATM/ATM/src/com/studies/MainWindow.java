package com.studies;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.*;

public class MainWindow extends JFrame implements ActionListener {
    private LoginScreen parent;

    private Account account;

    private BufferedImage logo;

    private JLabel logoLabel;
    private JButton depositButton;
    private JButton balanceButton;
    private JButton withdrawButton;
    private JButton logoutButton;

    public enum transferType {
        DEPOSIT,
        WITHDRAW
    }

    public MainWindow(Account account, LoginScreen parent) {
        setLayout(new FlowLayout());
        setSize(new Dimension(350, 220));
        setTitle("ATM");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        this.account = account;
        this.parent = parent;

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
        logoLabel.setPreferredSize(new Dimension(350, 100));
        depositButton = new JButton("Wpłata");
        withdrawButton = new JButton("Wypłata");
        balanceButton = new JButton("Stan konta");
        logoutButton = new JButton("Wyloguj");

        depositButton.addActionListener(this);
        withdrawButton.addActionListener(this);
        balanceButton.addActionListener(this);
        logoutButton.addActionListener(this);

        add(logoLabel, layout);
        add(withdrawButton, layout);
        add(balanceButton, layout);
        add(depositButton, layout);
        add(logoutButton, layout);

        setVisible(true);
    }

    public static boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException exception) {
            JOptionPane.showMessageDialog(null,
                    "Kwota musi być liczbą",
                    "Zły format",
                    JOptionPane.WARNING_MESSAGE);

            return false;
        }
    }

    public void logout() {
        JOptionPane.showMessageDialog(null,
                "Zapraszamy ponownie",
                "Wyjście",
                JOptionPane.INFORMATION_MESSAGE);

        parent.setVisible(true);
        this.dispose();
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        JButton pressedButton = (JButton) actionEvent.getSource();

        if (pressedButton.equals(depositButton)) {
            TransferScreen deposit = new TransferScreen(account, transferType.DEPOSIT, this);
            setVisible(false);
            return;
        }

        if (pressedButton.equals(balanceButton)) {
            JOptionPane.showMessageDialog(null,
                    "Stan konta: " + String.format("%.2f", account.getBalance()) +"zł",
                    "Stan konta",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        if (pressedButton.equals(withdrawButton)) {
            TransferScreen withdraw = new TransferScreen(account, transferType.WITHDRAW, this);
            setVisible(false);
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
