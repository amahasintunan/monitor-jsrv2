/*
 * File.   AboutDialog.java
 * Date.   08/17/2025
 * Author. Anan Mahasintunan
 * Description.
 *         Client Side - Linux Performance Monitor for CPU, Memory and Network.
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.font.TextAttribute;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.logging.Logger;

public class AboutDialog extends JDialog implements ActionListener, MouseListener {
    private static final Logger logger = Logger.getLogger(AboutDialog.class.getName());
    private static final long serialVersionUID = 1L;
    private static final int DEFAULT_WIDTH = 360;
    private static final int DEFAULT_HEIGHT = 250;
    private JLabel jLabel1;
    private JLabel jLabel2;
    private JLabel jLabel3;
    private JLabel jAuthorLabel;
    private JLabel jLabel4;
    private JButton jOKButton;
    private final String m_title;

    public AboutDialog(Frame parent) {
        this(parent, false);
    }

    public AboutDialog(Frame parent, boolean modal) {
        super(parent, modal);
        this.m_title = MonitorClient.TITLE_NAME;
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
        dispose();
    }

    @Override
    public void mouseClicked(MouseEvent evt) {
        String author = MonitorBundle.get("about.author.url");
        String cmd = MonitorResource.getBrowserPath();

        if (!Desktop.isDesktopSupported()) {
            return;
        }
        try {
            Desktop desktop = Desktop.getDesktop();
            if (jAuthorLabel == evt.getSource()) {
                if (desktop.isSupported(Desktop.Action.BROWSE)) {
                    desktop.browse(new URI(author));
                } else {
                    String[] command = {cmd, author};
                    Runtime.getRuntime().exec(command);
                }
            }
        } catch (IOException | URISyntaxException ex) {
            logger.severe(MonitorBundle.format("log.exception", m_title, ex.getMessage()));
        }
    }

    @Override
    public void mousePressed(MouseEvent arg0) {
    }

    @Override
    public void mouseReleased(MouseEvent arg0) {
    }

    @Override
    public void mouseEntered(MouseEvent arg0) {
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    @Override
    public void mouseExited(MouseEvent arg0) {
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    private void init() throws Exception {
        // Center of the BorderLayout
        JPanel pCenter = new JPanel();
        pCenter.setLayout(new GridBagLayout());
        GridBagConstraints cCenter = new GridBagConstraints();
        cCenter.insets = new Insets(10, 0, 5, 0); // top, left, bottom, right
        cCenter.fill = GridBagConstraints.BOTH;
        jLabel1 = new JLabel();
        jLabel2 = new JLabel();
        jLabel3 = new JLabel();
        jAuthorLabel = new JLabel();
        jLabel4 = new JLabel();
        jLabel1.setHorizontalAlignment(SwingConstants.CENTER);
        jLabel1.setIcon(new ImageIcon(getClass().getResource("monitor.png")));
        jLabel1.setText(MonitorBundle.get("app.label.monitor.client"));
        jLabel1.setFont(new Font("Lucida Grande", 0, 14));
        jLabel1.setMaximumSize(new Dimension(320, 40));
        jLabel1.setMinimumSize(new Dimension(320, 40));
        jLabel1.setPreferredSize(new Dimension(320, 40));
        jLabel2.setHorizontalAlignment(SwingConstants.CENTER);
        jLabel2.setText(MonitorBundle.get("app.label.description"));
        jLabel2.setFont(new Font("Lucida Grande", 0, 12));
        jLabel3.setHorizontalAlignment(SwingConstants.CENTER);
        jLabel3.setText(MonitorBundle.get("app.label.version"));
        jLabel3.setFont(new Font("Lucida Grande", 0, 12));
        jLabel4.setHorizontalAlignment(SwingConstants.CENTER);
        jLabel4.setText(MonitorBundle.get("about.feedback"));
        jLabel4.setFont(new Font("Lucida Grande", 0, 12));
        Font font = jAuthorLabel.getFont();
        Map attrs = font.getAttributes();
        attrs.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON); // workaround underline text for JLabel
        attrs.put(TextAttribute.FAMILY, "Lucida Grande");
        jAuthorLabel.setForeground(new Color(0, 0, 204));
        jAuthorLabel.setHorizontalAlignment(SwingConstants.CENTER);
        jAuthorLabel.setText(MonitorBundle.get("about.author"));
        jAuthorLabel.setFont(font.deriveFont(attrs));
        cCenter.gridx = 0;
        cCenter.gridy = 0;
        pCenter.add(jLabel1, cCenter);
        cCenter.gridy = 1;
        pCenter.add(jLabel2, cCenter);
        cCenter.gridy = 2;
        pCenter.add(jLabel3, cCenter);
        cCenter.gridy = 3;
        pCenter.add(jLabel4, cCenter);
        cCenter.gridy = 4;
        pCenter.add(jAuthorLabel, cCenter);

        // South of the BorderLayout
        JPanel pSouth = new JPanel();
        pSouth.setLayout(new GridBagLayout());
        GridBagConstraints cSouth = new GridBagConstraints();
        cSouth.ipadx = 10;
        cSouth.insets = new Insets(5, 0, 10, 0); // top, left, bottom, right
        cSouth.gridx = 0;
        cSouth.gridy = 0;
        jOKButton = new JButton(MonitorBundle.get("about.ok"));
        jOKButton.setMnemonic('o');
        jOKButton.setText(MonitorBundle.get("about.ok"));
        jOKButton.setPreferredSize(new Dimension(72, 26));
        pSouth.add(this.jOKButton, cSouth);

        getContentPane().add(pCenter, BorderLayout.CENTER);
        getContentPane().add(pSouth, BorderLayout.SOUTH);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(m_title + MonitorBundle.get("about.title.suffix"));
        jOKButton.addActionListener(this);
        jAuthorLabel.addMouseListener(this);
    }

}
