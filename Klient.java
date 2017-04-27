import java.io.*;
import java.net.*;
import java.util.Vector;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class Klient extends JFrame {

    private JTextArea text, logs;
    private JPanel panelGraczy, panelGry, panelBoczny, panelLogowania, panelDodatkowy;
    private JTextField host, input;
    private JButton btnHowToPlay, btnAddToSerwer, btnReady, btnLogon; 
    private JLabel lbStatus;

    private String hostname = "localhost:2345";
    private boolean isConnected = false;

    private PanelPlayer[] panelGracza = new PanelPlayer[6];
    public static Vector<Klient> clients = new Vector<Klient>(6);

	public Klient() {
        super("Klient v0.31");
        setSize(880, 600);
        setMinimumSize(new Dimension(640, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // -- panel z graczami
        panelGraczy = new JPanel(new GridLayout(6, 0));
        for (int i = 0; i < 6; i++) {
            panelGracza[i] = new PanelPlayer(i);
            panelGracza[i].setPreferredSize(new Dimension(40, 40));
            panelGraczy.add(panelGracza[i]);
        }

        // -- panel gry
        panelGry = new JPanel(new BorderLayout());
        panelGry.setBackground(Color.LIGHT_GRAY);

        text = new JTextArea();
        text.setText("zadanie.getText()");
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

        logs = new JTextArea();
        logs.setText("Logi aplikacji");
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
        panelLogowania.add(new JLabel("v0.31", SwingConstants.RIGHT));
        panelLogowania.add(host);
        panelLogowania.add(lbStatus);

        // ---- panel z przyciskami (prawy dolny róg)
        panelDodatkowy = new JPanel(new GridLayout(2, 2));
        
        Obsluga obsluga = new Obsluga();
        btnHowToPlay = new JButton("Jak grac?");
        btnAddToSerwer = new JButton("Dodaj tekst do gry");
        btnAddToSerwer.addActionListener(obsluga);
        btnAddToSerwer.setEnabled(false);
        btnReady = new JButton("Gotowy");
        btnReady.addActionListener(obsluga);
        btnReady.setEnabled(false);
        btnLogon = new JButton("Polacz");
        btnLogon.setPreferredSize(new Dimension(42, 42));
        btnLogon.addActionListener(obsluga);

        panelDodatkowy.add(btnLogon);
        panelDodatkowy.add(btnAddToSerwer);
        panelDodatkowy.add(btnReady);
        panelDodatkowy.add(btnHowToPlay);

        JPanel opakowanie = new JPanel();
        opakowanie.setLayout(new BoxLayout(opakowanie, BoxLayout.Y_AXIS));

        opakowanie.add(panelLogowania);
        opakowanie.add(panelDodatkowy);
        
        panelBoczny.add(new JScrollPane(logs), BorderLayout.CENTER);
        panelBoczny.add(opakowanie, BorderLayout.SOUTH);

        add(panelGraczy, BorderLayout.NORTH);
        add(panelGry, BorderLayout.CENTER);
        add(panelBoczny, BorderLayout.EAST);

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				setVisible(false);
				System.exit(0);
			}
		});

   		setVisible(true);
	}

    private void display() {
        
        JTextArea ta = new JTextArea();
		ta.setLineWrap(true);
        ta.setPreferredSize(new Dimension(300, 100));
        JTextField tf = new JTextField();

        JPanel panel = new JPanel(new BorderLayout());
        JPanel panel2 = new JPanel(new GridLayout(0, 1));

        panel.add(new JLabel("Tresc:"), BorderLayout.NORTH);
        panel.add(new JScrollPane(ta), BorderLayout.CENTER);
        panel2.add(new JLabel("Tytul (opcjonalnie):"));
        panel2.add(tf);

        panel.add(panel2, BorderLayout.SOUTH);
        
        int result = JOptionPane.showConfirmDialog(null, panel, "Dodaj prosbe z tekstem do serwera", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            System.out.println(tf.getText() + " : " + ta.getText());
        } else {
            System.out.println("cancel");
        }
    }

    private class Obsluga implements ActionListener {

		public void actionPerformed(ActionEvent e) {

            if (e.getSource() == btnAddToSerwer) {
                display();
            } else if (e.getSource() == btnReady) {
                panelGracza[0].changeReady();
                btnReady.setText(
                    panelGracza[0].getReady() ? "Niegotowy" : "Gotowy"
                );
            } else if (e.getSource() == btnLogon) {
                isConnected = !isConnected;
                if (isConnected) {
                    btnLogon.setText("Rozlacz");
                    lbStatus.setText("Status: polaczony");
                    lbStatus.setForeground(Color.decode("#006600"));
                    btnReady.setEnabled(true);
                    btnAddToSerwer.setEnabled(true);
                } else {
                    btnLogon.setText("Polacz");
                    lbStatus.setText("Status: niepolaczony");
                    lbStatus.setForeground(Color.RED);
                    btnReady.setEnabled(false);
                    btnAddToSerwer.setEnabled(false);
                }
            }
		}
    }

    public static void main(String[] args) {
        new Klient();
    }
}