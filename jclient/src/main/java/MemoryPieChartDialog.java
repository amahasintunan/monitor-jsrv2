/*
 * File.   MemoryPieChartDialog.java
 * Date.   03/16/2024
 * Author. Anan Mahasintunan
 * Description.
 *         Client Side - Linux Performance Monitor for CPU, Memory and Network.
 */

import org.json.simple.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.logging.Logger;

public class MemoryPieChartDialog extends JDialog implements MouseListener, ActionListener {
    private static final Logger logger = Logger.getLogger(MemoryPieChartDialog.class.getName());
    private static final long serialVersionUID = 1L;
    private static final int DEFAULT_WIDTH = 340;
    private static final int DEFAULT_HEIGHT = 380;
    private static final int BUTTON_GAP = 6;
    protected static final Color COLOR_USED = new Color(204, 0, 0); // Color.red;
    protected static final Color COLOR_FREE = new Color(0, 0, 204); // Color.blue;
    protected static final Color COLOR_AVAILABLE = COLOR_FREE;
    protected static final Color COLOR_UNAVAILABLE = COLOR_USED;
    private JFrame m_frame = null;
    private String m_hostName = null;
    private JSONObject m_jsonMemory = null;
    private MemoryColorCanvas canvas = null;
    private JButton jRotateLeftButton;
    private JButton jRotateRightButton;
    private JPopupMenu jPopupMenu;
    private JRadioButtonMenuItem jFreeRadioButtonMenuItem;
    private JRadioButtonMenuItem jAvailableRadioButtonMenuItem;
    protected int[] values = null;
    protected String[] names = null;
    protected long memTotal;
    protected long memFree;
    protected long memAvailable;
    protected int m_angle = 90;
    protected JLabel jTotalMemoryLabel = new JLabel();
    protected JLabel jFirstMemoryLabel = new JLabel();
    protected JLabel jSecondMemoryLabel = new JLabel();

    protected boolean m_freeMemory = true;

    public MemoryPieChartDialog(JFrame frame, String hostName, JSONObject jsonMemory) {
        super(frame, "", false);
        m_frame = frame;
        m_hostName = hostName;
        m_jsonMemory = jsonMemory;
        init();
        setSize(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT));
    }

    public void init() {
        canvas = new MemoryColorCanvas(this);
        getContentPane().add(canvas);
        // North of the BorderLayout
        JPanel pNorth = new JPanel();
        jTotalMemoryLabel = new JLabel();
        jTotalMemoryLabel.setText("");
        jTotalMemoryLabel.setFont(new Font("Lucida Grande", 0, 16));
        pNorth.add(jTotalMemoryLabel);
        jFirstMemoryLabel = new JLabel();
        jFirstMemoryLabel.setText("");
        jTotalMemoryLabel.setFont(new Font("Lucida Grande", 0, 16));
        pNorth.add(jFirstMemoryLabel);
        jSecondMemoryLabel = new JLabel();
        jSecondMemoryLabel.setText("");
        jTotalMemoryLabel.setFont(new Font("Lucida Grande", 0, 16));
        pNorth.add(jSecondMemoryLabel);
        // South of the BorderLayout
        JPanel pSouth = new JPanel();
        jRotateLeftButton = new JButton();
        jRotateRightButton = new JButton();
        jRotateLeftButton.setIcon(new ImageIcon(getClass().getResource("rotate_left.png")));
        jRotateRightButton.setIcon(new ImageIcon(getClass().getResource("rotate_right.png")));
        jRotateLeftButton.setPreferredSize(new Dimension(72, 26));
        jRotateRightButton.setPreferredSize(new Dimension(72, 26));
        int x = (DEFAULT_WIDTH - (2 * 88 + BUTTON_GAP)) / 2;
        int x2 = x + 88 + BUTTON_GAP;
        jRotateLeftButton.setBounds(new Rectangle(x, 158, 88, 28));
        jRotateRightButton.setBounds(new Rectangle(x2, 158, 88, 28));
        pSouth.add(jRotateLeftButton);
        pSouth.add(jRotateRightButton);
        jRotateLeftButton.addActionListener(this);
        jRotateRightButton.addActionListener(this);
        getContentPane().add(pNorth, BorderLayout.NORTH);
        getContentPane().add(pSouth, BorderLayout.SOUTH);
        jPopupMenu = new JPopupMenu();
        jFreeRadioButtonMenuItem = new JRadioButtonMenuItem(MonitorBundle.get("memory.chart.free"));
        jFreeRadioButtonMenuItem.setSelected(true);
        jAvailableRadioButtonMenuItem = new JRadioButtonMenuItem(MonitorBundle.get("memory.chart.available"));
        jAvailableRadioButtonMenuItem.setSelected(false);
        jPopupMenu.add(jFreeRadioButtonMenuItem);
        jPopupMenu.add(jAvailableRadioButtonMenuItem);
        jFreeRadioButtonMenuItem.addActionListener(this);
        jAvailableRadioButtonMenuItem.addActionListener(this);
        addMouseListener(this);
        populateDataFromJsonMemory(m_jsonMemory, m_hostName);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object obj = e.getSource();
        if (obj == jRotateLeftButton) {
            m_angle += 90;
            if (m_angle >= 360) {
                m_angle = 0;
            }
            canvas.repaint();
        } else if (obj == jRotateRightButton) {
            m_angle -= 90;
            if (m_angle <= -360) {
                m_angle = 0;
            }
            canvas.repaint();
        } else if (obj == jFreeRadioButtonMenuItem) {
            jFreeRadioButtonMenuItem.setSelected(true);
            jAvailableRadioButtonMenuItem.setSelected(false);
            m_freeMemory = true;
        } else if (obj == jAvailableRadioButtonMenuItem) {
            jFreeRadioButtonMenuItem.setSelected(false);
            jAvailableRadioButtonMenuItem.setSelected(true);
            m_freeMemory = false;
        }
    }

    public void populateDataFromJsonMemory(JSONObject jsonMemory, String hostName) {
        setTitle(MonitorBundle.format("memory.chart.title", hostName));
        m_hostName = hostName;
        m_jsonMemory = jsonMemory;
        memTotal = (long) jsonMemory.get("memTotal");
        memFree = (long) jsonMemory.get("memFree");
        memAvailable = (long) jsonMemory.get("memAvailable");
        canvas.repaint();
    }

    /**
     * Overridden so we can exit when window is closed
     */
    protected void processWindowEvent(WindowEvent e) {
        super.processWindowEvent(e);
        if (e.getID() == WindowEvent.WINDOW_CLOSING) {
            cancelFrame();
        }
    }

    public void cancelFrame() {
        dispose();
    }

    private void showPopupMenu(MouseEvent e) {
        if (e.isPopupTrigger()) {
            jPopupMenu.show(e.getComponent(), e.getX(), e.getY());
        }
    }
    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        showPopupMenu(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }
}

class MemoryColorCanvas extends Canvas {
    private static final long serialVersionUID = 1L;
    private final MemoryPieChartDialog m_dlg;

    public MemoryColorCanvas(MemoryPieChartDialog dlg) {
        m_dlg = dlg;
        setSize(dlg.getWidth(), dlg.getHeight());
    }

    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g; // Type-cast the parameter to Graphics2D
        int width = m_dlg.getWidth() - 80;
        int height = m_dlg.getHeight() - 140;
        if (m_dlg.memTotal <= 0) {
            return;
        }
        int usedPct = (int) ((m_dlg.memTotal - m_dlg.memFree) * 100 / m_dlg.memTotal);
        int availablePct = (int) ((m_dlg.memAvailable) * 100 / m_dlg.memTotal);
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
        nf.setMaximumFractionDigits(0);
        nf.setMinimumFractionDigits(0);
        String memTotalStr;
        long memTotalInBytes = m_dlg.memTotal * MonitorConstants.ONE_KIB; // memTotal is in KB
        if (memTotalInBytes >= MonitorConstants.ONE_GIB) {
            String memTotal = nf.format((double) memTotalInBytes / MonitorConstants.ONE_GIB);
            memTotalStr = MonitorBundle.format("memory.chart.size.gib", memTotal);
        } else if (memTotalInBytes >= MonitorConstants.ONE_MIB) {
            String memTotal = nf.format((double) memTotalInBytes / MonitorConstants.ONE_MIB);
            memTotalStr = MonitorBundle.format("memory.chart.size.mib", memTotal);
        } else {
            String memTotal = nf.format((double) memTotalInBytes / MonitorConstants.ONE_KIB);
            memTotalStr = MonitorBundle.format("memory.chart.size.kib", memTotal);
        }
        m_dlg.jTotalMemoryLabel.setText(MonitorBundle.format("memory.chart.total", memTotalStr));
        if (m_dlg.m_freeMemory) {
            m_dlg.jFirstMemoryLabel.setForeground(MemoryPieChartDialog.COLOR_FREE);
            m_dlg.jFirstMemoryLabel.setText(MonitorBundle.format("memory.chart.label.free", 100 - usedPct));
            m_dlg.jSecondMemoryLabel.setForeground(MemoryPieChartDialog.COLOR_USED);
            m_dlg.jSecondMemoryLabel.setText(MonitorBundle.format("memory.chart.label.used", usedPct));
            g2.setColor(MemoryPieChartDialog.COLOR_FREE);
            g2.fillArc(40, 20, width, height, m_dlg.m_angle, (int) ((100 - usedPct) / 100.0 * 360.0 + 0.5));
            g2.setColor(MemoryPieChartDialog.COLOR_USED);
            g2.fillArc(40, 20, width, height, m_dlg.m_angle, (int) (usedPct / 100.0 * 360.0 + 0.5) * -1);
        } else {
            m_dlg.jFirstMemoryLabel.setForeground(MemoryPieChartDialog.COLOR_AVAILABLE);
            m_dlg.jFirstMemoryLabel.setText(MonitorBundle.format("memory.chart.label.available", availablePct));
            m_dlg.jSecondMemoryLabel.setForeground(MemoryPieChartDialog.COLOR_UNAVAILABLE);
            m_dlg.jSecondMemoryLabel.setText(MonitorBundle.format("memory.chart.label.unavailable", 100 - availablePct));
            g2.setColor(MemoryPieChartDialog.COLOR_AVAILABLE);
            g2.fillArc(40, 20, width, height, m_dlg.m_angle, (int) (availablePct / 100.0 * 360.0 + 0.5));
            g2.setColor(MemoryPieChartDialog.COLOR_UNAVAILABLE);
            g2.fillArc(40, 20, width, height, m_dlg.m_angle, (int) ((100 - availablePct) / 100.0 * 360.0 + 0.5) * -1);
        }
    }

}
