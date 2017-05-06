import java.io.*;
import java.net.*;
import java.util.Vector;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class KlientGUI extends JFrame {

    private JTextArea text, logs;
    private JPanel panelGraczy, panelGry, panelBoczny, panelLogowania, panelDodatkowy;
    private JTextField host, input;
    private JButton btnHowToPlay, btnAddToSerwer, btnReady, btnLogon;
    private JLabel lbStatus;

    private String hostname = "localhost:2345";
    private boolean isConnected = false;

    private PanelPlayer[] panelGracza = new PanelPlayer[Config.MAX_PLAYERS];

    private Zadanie zadanie;
    private Klient watekKlienta;
    private int idPlayer;

    public KlientGUI() {
        super("Klient " + Config.VERSION);
        setSize(880, 600);
        setMinimumSize(new Dimension(640, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // -- panel z graczami
        panelGraczy = new JPanel(new GridLayout(Config.MAX_PLAYERS, 0));
        //panelGraczy.setLayout(new GridLayout(7, 0));
        for (int i = 0; i < Config.MAX_PLAYERS; i++) {
            panelGracza[i] = new PanelPlayer(i);
            panelGracza[i].setPreferredSize(new Dimension(40, 40));
            panelGraczy.add(panelGracza[i]);
        }

        // -- panel gry
        panelGry = new JPanel(new BorderLayout());
        panelGry.setBackground(Color.LIGHT_GRAY);

        text = new JTextArea();
        text.setText("");
        text.setWrapStyleWord(true);
        text.setLineWrap(true);
        text.setOpaque(false);
        text.setEditable(false);
        text.setFocusable(false);
        text.setFont(new Font("Verdana", Font.PLAIN, 22));
        text.setBorder(new EmptyBorder(8, 12, 8, 8));

        input = new JTextField();
        input.setFont(new Font("Verdana", Font.PLAIN, 26));
        input.setPreferredSize(new Dimension(450, 42));
        input.setEnabled(false);

        Obsluga obsluga = new Obsluga();
        input.addKeyListener(obsluga);

        logs = new JTextArea();
        logs.setText("");
        logs.setWrapStyleWord(true);
        logs.setLineWrap(true);
        logs.setOpaque(false);
        logs.setEditable(false);
        logs.setFocusable(false);
        logs.setBorder(new EmptyBorder(4, 4, 4, 4));

        panelGry.add(text, BorderLayout.CENTER);
        panelGry.add(input, BorderLayout.SOUTH);

        // -- panel po prawej stronie
        panelBoczny = new JPanel(new BorderLayout());

        // ---- panel dane do logowania + informacje
        panelLogowania = new JPanel(new GridLayout(2, 2));
        panelLogowania.setBorder(new EmptyBorder(12, 10, 12, 10));

        host = new JTextField(hostname);
        host.setFont(new Font("Verdana", Font.PLAIN, 12));
        lbStatus = new JLabel("Status: niepolaczony", SwingConstants.RIGHT);
        lbStatus.setForeground(Color.RED);

        panelLogowania.add(new JLabel("Serwer (host:port)"));
        panelLogowania.add(new JLabel(Config.VERSION, SwingConstants.RIGHT));
        panelLogowania.add(host);
        panelLogowania.add(lbStatus);

        // ---- panel z przyciskami (prawy dolny róg)
        panelDodatkowy = new JPanel(new GridLayout(2, 2));

        btnHowToPlay = new JButton("Jak grac?");
        btnHowToPlay.setEnabled(false); // tmp
        btnAddToSerwer = new JButton("Dodaj tekst do gry");
        btnAddToSerwer.addActionListener(obsluga);
        btnAddToSerwer.setEnabled(false);
        btnReady = new JButton("Gotowosc");
        btnReady.addActionListener(obsluga);
        btnReady.setEnabled(false);
        btnLogon = new JButton("Polacz");
        btnLogon.setPreferredSize(new Dimension(42, 42));
        btnLogon.addActionListener(obsluga);

        panelDodatkowy.add(btnLogon);
        panelDodatkowy.add(btnAddToSerwer);
        panelDodatkowy.add(btnReady);
        panelDodatkowy.add(btnHowToPlay);

        // ---- panel (opakowanie) dla panelu z informacjami i przyciskami 
        JPanel opakowanie = new JPanel();
        opakowanie.setLayout(new BoxLayout(opakowanie, BoxLayout.Y_AXIS));

        opakowanie.add(panelLogowania);
        opakowanie.add(panelDodatkowy);

        panelBoczny.add(new JScrollPane(logs), BorderLayout.CENTER);
        panelBoczny.add(opakowanie, BorderLayout.SOUTH);

        // -- rozmieszczenie paneli
        add(panelGraczy, BorderLayout.NORTH);
        add(panelGry, BorderLayout.CENTER);
        add(panelBoczny, BorderLayout.EAST);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                if (isConnected) {
                    try {
                        watekKlienta.sendToSerwer.writeObject(new Packet(Command.LOGOUT));
                        watekKlienta.sendToSerwer.flush();
                    } catch (IOException ex) {
                        addLog(ex.toString());
                    }
                }
                setVisible(false);
                System.exit(0);
            }
        });
        setVisible(true);
    }

    private void display() {

        JTextArea ta = new JTextArea();
        ta.setLineWrap(true);
        ta.setPreferredSize(new Dimension(600, 300));

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel("Tresc zadania:"), BorderLayout.NORTH);
        panel.add(new JScrollPane(ta), BorderLayout.CENTER);

        int result = JOptionPane.showConfirmDialog(null, panel, "Dodaj prosbe z tekstem do serwera",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                watekKlienta.sendToSerwer.writeObject(new Packet(Command.SEND_TEXT, ta.getText()));
                watekKlienta.sendToSerwer.flush();
            } catch (IOException ex) {
                addLog(ex.toString());
            }
        } else {
            try {
                watekKlienta.sendToSerwer.writeObject(new Packet(Command.SEND_TEXT, ""));
                watekKlienta.sendToSerwer.flush();
            } catch (IOException ex) {
                addLog(ex.toString());
            }
        }
    }

    private class Obsluga extends KeyAdapter implements ActionListener {

        public void actionPerformed(ActionEvent e) {

            if (e.getSource() == btnAddToSerwer) {
                try {
                    watekKlienta.sendToSerwer.writeObject(new Packet(Command.SEND_TEXT_REQUEST));
                    watekKlienta.sendToSerwer.flush();
                } catch (IOException ex) {
                    addLog(ex.toString());
                }
            } else if (e.getSource() == btnReady) {
                try {
                    watekKlienta.sendToSerwer.writeObject(new Packet(Command.CHANGE_READY));
                    watekKlienta.sendToSerwer.flush();
                } catch (IOException ex) {
                    addLog(ex.toString());
                }
            } else if (e.getSource() == btnLogon) {
                if (!isConnected) {
                    watekKlienta = new Klient();
                    watekKlienta.start();
                } else {
                    try {
                        watekKlienta.sendToSerwer.writeObject(new Packet(Command.LOGOUT));
                        watekKlienta.sendToSerwer.flush();
                    } catch (IOException ex) {
                        addLog(ex.toString());
                    }
                }
            }
        }

        public void keyReleased(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                if (zadanie.ifEqualsGoNext(input.getText())) {

                    panelGracza[idPlayer].progress.setValue(zadanie.getProgress());

                    try {
                        watekKlienta.sendToSerwer
                                .writeObject(new Packet(Command.PROGRESS, idPlayer, zadanie.getProgress()));
                        watekKlienta.sendToSerwer.flush();
                    } catch (IOException ex) {
                        addLog(ex.toString());
                    }

                    if (zadanie.isSuccess) {
                        input.setEnabled(false);
                        try {
                            watekKlienta.sendToSerwer.writeObject(new Packet(Command.WIN, idPlayer, ""));
                            watekKlienta.sendToSerwer.flush();
                        } catch (IOException ex) {
                            addLog(ex.toString());
                        }
                        text.setText("");
                        input.setText("");
                    } else {
                        text.replaceRange(null, 0, input.getText().length());
                        input.setText("");
                    }
                }
            }
        }
    }

    private class Klient extends Thread {

        private Socket socket;
        private ObjectInputStream receiveFromSerwer;
        private ObjectOutputStream sendToSerwer;

        public void run() {
            try {
                String[] hostParameters = host.getText().split(":", 2);
                socket = new Socket(hostParameters[0], new Integer(hostParameters[1]));
                sendToSerwer = new ObjectOutputStream(socket.getOutputStream());
                receiveFromSerwer = new ObjectInputStream(socket.getInputStream());

                isConnected = true;
                btnLogon.setText("Rozlacz");
                lbStatus.setText("Status: polaczony");
                lbStatus.setForeground(Color.decode("#006600"));
                btnAddToSerwer.setEnabled(true);

                sendToSerwer.writeObject(new Packet(Command.LOGIN_REQUEST));
                sendToSerwer.flush();

                Packet packet = null;
                while (isConnected) {

                    try {
                        packet = (Packet) receiveFromSerwer.readObject();
                        if (packet != null) {

                            Command command = packet.getCommand();
                            switch (command) {

                            case LOGIN_RESPONSE:
                                // podanie nazwy użytkownika
                                String nick = JOptionPane.showInputDialog(null, "Podaj nick (max. 6 znakow): ");
                                nick = nick.trim().toUpperCase();
                                if (nick.equals("")) {
                                    sendToSerwer.writeObject(new Packet(Command.LOGOUT));
                                    sendToSerwer.flush();
                                    addLog("Niepoprawny nick, zostales rozlaczony.");
                                } else {
                                    // jeżeli nazwa użytkownika spełnia wymagania to.. poinformuj serwer
                                    if (nick.length() > 6)
                                        nick = nick.substring(0, 6);

                                    sendToSerwer.writeObject(new Packet(Command.NICK_SET, nick));
                                    sendToSerwer.flush();
                                    idPlayer = packet.getPlayerId();
                                    btnReady.setEnabled(true);
                                }
                                break;

                            case LOGOUT:
                                // ustawienia ui klienta po wylogowaniu
                                String message = packet.getParameter();
                                if (message != null && !message.isEmpty())
                                    addLog(message);

                                isConnected = false;
                                btnLogon.setText("Polacz");
                                btnLogon.setEnabled(true);
                                lbStatus.setText("Status: niepolaczony");
                                lbStatus.setForeground(Color.RED);
                                btnReady.setEnabled(false);
                                btnAddToSerwer.setEnabled(false);
                                input.setEnabled(false);
                                input.setText("");
                                text.setText("");
                                for (PanelPlayer pp : panelGracza) {
                                    pp.leave();
                                    pp.progress.setValue(0);
                                }
                                break;

                            case LOGOUT_PLAYER_NOTIFY:
                                // ustawienia ui panela gracza, który się wylogował
                                int playerId = packet.getPlayerId();

                                if (playerId != -1) {
                                    panelGracza[playerId].join("-");
                                    panelGracza[playerId].setReadiness(false);
                                }
                                break;

                            case UPDATE_PLAYERS_LIST:
                                // wczytanie nazw graczy do paneli
                                PacketWithPlayersList extendedPacket = (PacketWithPlayersList) packet;
                                for (Player player : extendedPacket.getPlayers()) {
                                    panelGracza[player.getId()].join(player.getNick());
                                }
                                break;

                            case CHANGE_READY:
                                // przełącznik koloru gotowości danego użytkownika
                                int senderId = packet.getPlayerId();
                                boolean isReady = packet.getExtra();
                                panelGracza[senderId].setReadiness(isReady);
                                break;

                            case START_GAME:
                                // rozpoczęcie gry
                                PacketWithTask task = (PacketWithTask) packet;
                                zadanie = task.getZadanie();

                                btnReady.setEnabled(false);
                                btnLogon.setEnabled(false);

                                text.setText(zadanie.getText());
                                input.setEnabled(true);
                                input.requestFocus();
                                break;

                            case PROGRESS:
                                // zmiana wartości progresu danego użytkownika
                                panelGracza[packet.getPlayerId()].progress.setValue(packet.getProgress());
                                break;

                            case WIN:
                                // poinformowanie o ukończeniu zadania przez danego użytkownika
                                panelGracza[packet.getPlayerId()].setPlace(packet.getProgress());
                                addLog("Gracz " + panelGracza[packet.getPlayerId()].labelWithNick.getText()
                                        + " juz skonczyl!");
                                break;

                            case SEND_TEXT_RESPONSE:
                                // odpowiedź serwera na prośbę o pozwolenie na przesłanie tekstu do serwera
                                boolean isAllowed = packet.getExtra();
                                if (isAllowed)
                                    display();
                                else
                                    addLog("Serwer odmowil zadanie o pozwolenia na przeslanie pliku.");
                                break;

                            case RESET:
                                // ogłoszenie wyników użytkowników
                                String content = "Tablica wynikow:";
                                int counter = 1;
                                PacketWithPlayersList players = (PacketWithPlayersList) packet;
                                for (Player player : players.getPlayers()) {
                                    panelGracza[player.idPlayer].progress.setValue(0);
                                    ;
                                    panelGracza[player.idPlayer].setPlace(""); //tmp
                                    content += "\n" + (counter++) + ". " + player.nick;
                                }
                                addLog(content);

                                // zresetowanie ui paneli graczy
                                btnLogon.setEnabled(true);
                                btnReady.setEnabled(true);
                                for (PanelPlayer pp : panelGracza) {
                                    pp.setReadiness(false);
                                }
                                break;
                            }
                        }
                    } catch (ClassNotFoundException ex) {
                    }
                }
            } catch (UnknownHostException e) {
                addLog("Blad polaczenia!");
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Prawdopodobnie serwer nie jest wlaczony.", "Blad", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            } catch (NullPointerException e) {
                addLog(e.toString());
            } finally {
                try {
                    receiveFromSerwer.close();
                    sendToSerwer.close();
                    socket.close();
                } catch (IOException e) {
                } catch (NullPointerException e) {
                }
            }
        }
    }

    private class PanelPlayer extends JPanel {

        public JProgressBar progress;
        JPanel color, panelWithNick;
        JLabel labelWithNick, labelWithPlace;
        JButton[] btns = new JButton[3];
        int panelId;

        public PanelPlayer(int idColor) {
            super(new BorderLayout());
            this.panelId = idColor;

            color = new JPanel();
            color.setBackground(this.getColorById(idColor));
            color.setPreferredSize(new Dimension(40, 40));

            labelWithPlace = new JLabel("", SwingConstants.CENTER);
            labelWithPlace.setFont(new Font("Consolas", Font.PLAIN, 22));

            color.add(labelWithPlace);

            panelWithNick = new JPanel();
            panelWithNick.setBackground(Color.decode("#ffcccc"));

            labelWithNick = new JLabel("", SwingConstants.CENTER);
            labelWithNick.setFont(new Font("Consolas", Font.PLAIN, 22));
            labelWithNick.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

            panelWithNick.add(labelWithNick);

            JPanel btnsBox = new JPanel(new GridLayout(0, 4));

            JLabel labelWithAP = new JLabel("", SwingConstants.CENTER); // 0
            labelWithAP.setFont(new Font("Consolas", Font.PLAIN, 22));
            labelWithAP.setPreferredSize(new Dimension(40, 40));

            btnsBox.add(labelWithAP, BorderLayout.LINE_START);

            int counter = 0;
            Umiejetnosc umiejetnosc = new Umiejetnosc();
            for (int i = 0, k = btns.length; i < k; i++) {
                btns[i] = new JButton();
                btns[i].setPreferredSize(new Dimension(40, 40));
                btnsBox.add(btns[i], BorderLayout.LINE_START);
                btns[i].setText(Integer.toString(counter));
                btns[i].setFont(new Font("Consolas", Font.PLAIN, 11));
                btns[i].setOpaque(true);
                //btn.setEnabled(false);
                btns[i].addActionListener(umiejetnosc);
                counter++;
            }

            JPanel left = new JPanel(new BorderLayout());
            left.setPreferredSize(new Dimension(340, 340));

            progress = new JProgressBar();

            // -- rozmieszczenie paneli (z wylaczeniem progressbara)
            left.add(color, BorderLayout.LINE_START);
            left.add(panelWithNick, BorderLayout.CENTER);
            left.add(btnsBox, BorderLayout.LINE_END);

            // -- rozmieszczenie paneli
            add(left, BorderLayout.LINE_START);
            add(progress, BorderLayout.CENTER);
        }

        private Color getColorById(int id) {
            switch (id) {
            case 0:
                return Color.decode("#6077E0"); // niebieski
            case 1:
                return Color.decode("#ED094A"); // czerwony
            case 2:
                return Color.decode("#4DBD02"); // zielony
            case 3:
                return Color.decode("#E231E2"); // fioletowy
            case 4:
                return Color.decode("#37C6C7"); // błękitny
            case 5:
                return Color.decode("#FF8B17"); // pomaranczowy
            default:
                return Color.GRAY;
            }
        }

        public void setReadiness(boolean isReady) {
            this.panelWithNick.setBackground(Color.decode(isReady ? "#ccffcc" : "#ffcccc"));
        }

        // public JButton[] getButtons() {
        //     return this.btns;
        // }

        public void join(String nick) {
            this.labelWithNick.setText(nick);
        }

        public void leave() {
            this.labelWithNick.setText("");
            this.panelWithNick.setBackground(Color.decode("#ffcccc"));
        }

        public void setPlace(int place) {
            this.labelWithPlace.setText(Integer.toString(place));
        }

        public void setPlace(String text) {
            this.labelWithPlace.setText(text);
        }

        private class Umiejetnosc implements ActionListener {

            public void actionPerformed(ActionEvent e) {

                if (e.getSource() == btns[0]) {
                    addLog(Command.DEBUFF_INVISIBILITY + "\nSender: " + idPlayer + "\nTarget: " + panelId);
                } else if (e.getSource() == btns[1]) {
                    addLog(Command.DEBUFF_REVERSE + "\nSender: " + idPlayer + "\nTarget: " + panelId);
                } else if (e.getSource() == btns[2]) {
                    addLog(Command.DEBUFF_SHUFFLE + "\nSender: " + idPlayer + "\nTarget: " + panelId);
                }
            }
        }
    }

    private void addLog(String content) {
        logs.append(content + "\n");
        logs.setCaretPosition(logs.getDocument().getLength());
    }

    public static void main(String[] args) {
        new KlientGUI();
    }
}