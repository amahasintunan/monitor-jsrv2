/*
 * File.   SetupDialog.java
 * Date.   03/16/2024
 * Author. Anan Mahasintunan
 * Description.
 *         Setup dialog with protocol-aware connectivity test.
 *         Supports UDP, TCP, HTTP, and gRPC transports.
 */

import com.jmonitor.proto.MonitorServiceGrpc;
import com.jmonitor.proto.MonitorStatsRequest;
import com.jmonitor.proto.MonitorStatsResponse;
import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class SetupDialog extends JDialog implements ActionListener, DocumentListener {
    private static final Logger logger = Logger.getLogger(SetupDialog.class.getName());
    private static final long serialVersionUID = 1L;
    private static final int DEFAULT_WIDTH = 410;
    private static final int DEFAULT_HEIGHT = 218;
    private JLabel jServerLabel;
    private JTextField jServerTextField;
    private JLabel jPortLabel;
    private JTextField jPortTextField;
    private JLabel jStatusLabel;
    private JLabel jStatusResultLabel;
    private JButton jTestButton;
    private JButton jTestAndSetButton;
    private JButton jCancelButton;
    private final String m_title;
    private String m_hostName;
    private int m_portNumber;
    private final MonitorClient m_parent;

    public SetupDialog(Frame parent, String hostname, int port) {
        this(parent, false, hostname, port);
    }

    public SetupDialog(Frame parent, boolean modal, String hostName, int portNumber) {
        super(parent, modal);
        this.m_parent = (MonitorClient) parent;
        this.m_title = MonitorClient.TITLE_NAME;
        this.m_hostName = hostName;
        this.m_portNumber = portNumber;
        setPreferredSize(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT));
        try {
            init();
            pack();
        } catch (Exception ex) {
            logger.severe(MonitorBundle.format("log.exception", m_title, ex.getMessage()));
        }
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        Object obj = evt.getSource();
        if (obj == jTestButton) {
            testServer();
        } else if (obj == jTestAndSetButton) {
            testAndSetServer();
        } else if (obj == jCancelButton) {
            dispose();
        }
    }

    private void init() throws Exception {
        // Center of the BorderLayout
        JPanel pCenter = new JPanel();
        pCenter.setLayout(new GridBagLayout());
        GridBagConstraints cCenter = new GridBagConstraints();
        cCenter.insets = new Insets(10, 0, 5, 0);
        cCenter.fill = GridBagConstraints.BOTH;
        jServerLabel = new JLabel(MonitorBundle.get("setup.label.host") + " ");
        jServerTextField = new JTextField();
        jServerTextField.setText(m_hostName);
        jServerTextField.setPreferredSize(new Dimension(320, 26));
        jPortLabel = new JLabel(MonitorBundle.get("setup.label.port") + " ");
        jPortTextField = new JTextField();
        jPortTextField.setText(Integer.toString(m_portNumber));
        jPortTextField.setPreferredSize(new Dimension(320, 26));
        jStatusLabel = new JLabel(MonitorBundle.get("setup.label.status") + " ");
        jStatusResultLabel = new JLabel(MonitorBundle.get("setup.status.initial"));
        jStatusResultLabel.setPreferredSize(new Dimension(320, 26));
        cCenter.gridx = 0;
        cCenter.gridy = 0;
        pCenter.add(jServerLabel, cCenter);
        cCenter.gridx = 1;
        pCenter.add(jServerTextField, cCenter);
        cCenter.gridx = 0;
        cCenter.gridy = 1;
        pCenter.add(jPortLabel, cCenter);
        cCenter.gridx = 1;
        pCenter.add(jPortTextField, cCenter);
        cCenter.gridx = 0;
        cCenter.gridy = 2;
        pCenter.add(jStatusLabel, cCenter);
        cCenter.gridx = 1;
        pCenter.add(jStatusResultLabel, cCenter);

        // South of the BorderLayout
        JPanel pSouth = new JPanel();
        pSouth.setLayout(new GridBagLayout());
        GridBagConstraints cSouth = new GridBagConstraints();
        cSouth.ipadx = 10;
        cSouth.insets = new Insets(5, 0, 10, 0);
        cSouth.gridx = 0;
        cSouth.gridy = 0;
        jTestButton = new JButton(MonitorBundle.get("setup.button.test"));
        getRootPane().setDefaultButton(jTestButton);
        jTestAndSetButton = new JButton(MonitorBundle.get("setup.button.testandset"));
        jCancelButton = new JButton(MonitorBundle.get("setup.button.cancel"));
        JPanel jSouthPanel = new JPanel();
        jSouthPanel.add(jTestButton);
        jSouthPanel.add(jTestAndSetButton);
        jSouthPanel.add(jCancelButton);
        pSouth.add(jSouthPanel, cSouth);

        setButtonEnabledOrDisabled();
        getContentPane().add(pCenter, BorderLayout.CENTER);
        getContentPane().add(pSouth, BorderLayout.SOUTH);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(m_title + MonitorBundle.get("setup.title.suffix"));
        jTestButton.addActionListener(this);
        jTestAndSetButton.addActionListener(this);
        jCancelButton.addActionListener(this);
        jServerTextField.getDocument().addDocumentListener(this);
        jPortTextField.getDocument().addDocumentListener(this);
    }

    public boolean testServer() {
        Protocol protocol = m_parent.getProtocol();
        switch (protocol) {
            case UDP:  return testServerUdp();
            case TCP:  return testServerTcp();
            case HTTP: return testServerHttp();
            case GRPC: return testServerGrpc();
            default:   return false;
        }
    }

    private boolean parseHostAndPort() {
        m_hostName = jServerTextField.getText().trim();
        try {
            m_portNumber = Integer.parseInt(jPortTextField.getText().trim());
        } catch (NumberFormatException nfe) {
            String message = MonitorBundle.get("setup.status.invalid.port");
            jStatusResultLabel.setText(message);
            jStatusResultLabel.setIcon(new ImageIcon(getClass().getResource("error.png")));
            return false;
        }
        return true;
    }

    private void showSuccess() {
        jStatusResultLabel.setIcon(new ImageIcon(getClass().getResource("success.png")));
        jStatusResultLabel.setText(MonitorBundle.get("setup.status.available"));
    }

    private void showFailure() {
        jStatusResultLabel.setIcon(new ImageIcon(getClass().getResource("error.png")));
        jStatusResultLabel.setText(MonitorBundle.get("setup.status.unavailable"));
    }

    // -- UDP test ---------------------------------------------------
    private boolean testServerUdp() {
        boolean result = false;
        if (!parseHostAndPort()) return false;
        DatagramSocket socket = null;
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            InetAddress address = InetAddress.getByName(m_hostName);
            socket = new DatagramSocket();
            socket.setSoTimeout(m_parent.getSocketTimeoutSec() * 1000);
            DatagramPacket request = new DatagramPacket(new byte[1], 1, address, m_portNumber);
            socket.send(request);
            byte[] buffer = new byte[1024];
            DatagramPacket response = new DatagramPacket(buffer, buffer.length);
            boolean isReceived = false;
            for (int i = 1; i <= MonitorClient.NUM_RETRIES; i++) {
                try {
                    socket.receive(response);
                    isReceived = true;
                    break;
                } catch (SocketTimeoutException ste) {
                }
            }
            if (isReceived) {
                showSuccess();
                result = true;
            } else {
                showFailure();
            }
        } catch (Exception ex) {
            logger.severe(MonitorBundle.format("log.exception", MonitorClient.TITLE_NAME, ex.getMessage()));
            showFailure();
        } finally {
            if (socket != null) socket.close();
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
        return result;
    }

    // -- TCP test ---------------------------------------------------
    private boolean testServerTcp() {
        boolean result = false;
        if (!parseHostAndPort()) return false;
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        Socket socket = null;
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(m_hostName, m_portNumber),
                    m_parent.getSocketTimeoutSec() * 1000);
            socket.setSoTimeout(m_parent.getSocketTimeoutSec() * 1000);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            String response = in.readLine();
            if (response != null && !response.isEmpty()) {
                showSuccess();
                result = true;
            } else {
                showFailure();
            }
        } catch (Exception ex) {
            logger.severe(MonitorBundle.format("log.exception", MonitorClient.TITLE_NAME, ex.getMessage()));
            showFailure();
        } finally {
            if (socket != null) {
                try { socket.close(); } catch (Exception e) { }
            }
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
        return result;
    }

    // -- HTTP test --------------------------------------------------
    private boolean testServerHttp() {
        boolean result = false;
        if (!parseHostAndPort()) return false;
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            HttpClient httpClient = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(m_parent.getSocketTimeoutSec()))
                    .build();
            String uri = "http://" + m_hostName + ":" + m_portNumber + m_parent.getApiPath();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(uri))
                    .timeout(Duration.ofSeconds(m_parent.getSocketTimeoutSec()))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                showSuccess();
                result = true;
            } else {
                showFailure();
            }
        } catch (Exception ex) {
            logger.severe(MonitorBundle.format("log.exception", MonitorClient.TITLE_NAME, ex.getMessage()));
            showFailure();
        } finally {
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
        return result;
    }

    // -- gRPC test --------------------------------------------------
    private boolean testServerGrpc() {
        boolean result = false;
        if (!parseHostAndPort()) return false;
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        String target = m_hostName + ":" + m_portNumber;
        ManagedChannel channel = Grpc.newChannelBuilder(target, InsecureChannelCredentials.create()).build();
        try {
            MonitorServiceGrpc.MonitorServiceBlockingStub stub = MonitorServiceGrpc.newBlockingStub(channel);
            boolean isReceived = false;
            for (int i = 1; i <= MonitorClient.NUM_RETRIES; i++) {
                try {
                    MonitorStatsResponse response = stub
                            .withDeadlineAfter(m_parent.getGrpcTimeoutSeconds() * 1000L, TimeUnit.MILLISECONDS)
                            .getStats(MonitorStatsRequest.getDefaultInstance());
                    if (response != null) {
                        isReceived = true;
                        break;
                    }
                } catch (StatusRuntimeException e) {
                    // retry on gRPC status error
                }
            }
            if (isReceived) {
                showSuccess();
                result = true;
            } else {
                showFailure();
            }
        } finally {
            channel.shutdownNow();
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
        return result;
    }

    public void testAndSetServer() {
        boolean result = testServer();
        String message;
        if (result) {
            m_parent.rerun(m_hostName, m_portNumber);
            message = MonitorBundle.get("setup.message.set");
            JOptionPane.showMessageDialog(this, message, MonitorClient.TITLE_NAME, JOptionPane.INFORMATION_MESSAGE);
        } else {
            message = MonitorBundle.get("setup.message.notset");
            JOptionPane.showMessageDialog(this, message, MonitorClient.TITLE_NAME, JOptionPane.WARNING_MESSAGE);
        }
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        setButtonEnabledOrDisabled();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        setButtonEnabledOrDisabled();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        setButtonEnabledOrDisabled();
    }

    private void setButtonEnabledOrDisabled() {
        boolean enabled = getButtonsEnabled();
        jTestButton.setEnabled(enabled);
        jTestAndSetButton.setEnabled(enabled);
    }

    private boolean getButtonsEnabled() {
        return jServerTextField.getText().length() > 0 && jPortTextField.getText().length() > 0;
    }
}
