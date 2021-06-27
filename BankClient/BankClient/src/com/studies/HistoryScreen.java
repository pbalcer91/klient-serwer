package com.studies;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;

public class HistoryScreen extends JFrame implements ActionListener {
    private Account account;
    private MainWindow parent;

    private BufferedImage logo;

    private JLabel logoLabel;
    private JLabel historyLabel;
    private JTextArea transactions;
    private JScrollPane areaBox;

    private JButton backButton;

    private Socket socket;
    private InputStreamReader inputStreamReader = null;
    private OutputStreamWriter outputStreamWriter = null;

    private BufferedReader bufferedReader = null;
    private BufferedWriter bufferedWriter = null;

    public HistoryScreen(Account account, MainWindow parent) {
        this.account = account;
        this.parent = parent;

        setLayout(new FlowLayout());
        setSize(new Dimension(575, 410));
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
        logoLabel.setPreferredSize(new Dimension(575, 100));
        historyLabel = new JLabel("Historia transakcji");
        historyLabel.setHorizontalAlignment(JLabel.CENTER);
        transactions = new JTextArea(15, 50);
        transactions.setEditable(false);
        areaBox = new JScrollPane();
        areaBox.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        areaBox.getViewport().add(transactions);

        backButton = new JButton("Wróć");

        backButton.addActionListener(this);

        add(logoLabel);
        add(historyLabel, layout);
        add(areaBox, layout);
        add(backButton, layout);

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
                transactions.append(putSeparator());

            for (int i = 0; i < historyCount; i++) {
                String historyPosition = bufferedReader.readLine();
                transactions.append(historyPosition + "\n");
                transactions.append(putSeparator());
            }

            System.out.println("[CLIENT]: History received");

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

    public static String putSeparator() {
        String result = "";

        for (int i = 0; i < 67; i++) {
            result += "- ";
        }

        result += "\n";
        return result;
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        this.parent.setVisible(true);
        this.dispose();
    }
}
