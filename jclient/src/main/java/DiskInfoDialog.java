
/*
 * File.   DiskInfoDialog.java
 * Date.   03/16/2024
 * Author. Anan Mahasintunan
 * Description.
 *         Client Side - Linux Performance Monitor for CPU, Memory and Network.
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.text.NumberFormat;
import java.util.logging.Logger;

public class DiskInfoDialog extends JDialog implements ActionListener, ComponentListener {
    private static final Logger logger = Logger.getLogger(DiskInfoDialog.class.getName());
    private static final long serialVersionUID = 1L;
    private static final int DEFAULT_WIDTH = 480;
    private static final int DEFAULT_HEIGHT = 240;
    private static final int BUTTON_GAP = 6;
    private static final String FILE_SYSTEM = MonitorBundle.get("disk.info.label.filesystem");
    private static final String TYPE = MonitorBundle.get("disk.info.label.type");
    private static final String SIZE = MonitorBundle.get("disk.info.label.size");
    private static final String USED = MonitorBundle.get("disk.info.label.used");
    private static final String FREE = MonitorBundle.get("disk.info.label.free");
    private static final String MOUNTED_ON = MonitorBundle.get("disk.info.label.mountedon");
    private final JPanel panel1 = new JPanel();
    private final BorderLayout borderLayout1 = new BorderLayout();
    private final JPanel jDiskInfoPanel = new JPanel();
    private final JLabel jFileSystemLabel = new JLabel(FILE_SYSTEM);
    private final JLabel jTypeLabel = new JLabel(TYPE);
    private final JLabel jSizeLabel = new JLabel();
    private final JLabel jUsedLabel = new JLabel();
    private final JLabel jFreeLabel = new JLabel();
    private final JLabel jMountedOnLabel = new JLabel(MOUNTED_ON);
    private final JLabel jFileSystemLabel2 = new JLabel();
    private final JLabel jTypeLabel2 = new JLabel();
    private final JLabel jSizeLabel2 = new JLabel();
    private final JLabel jUsedLabel2 = new JLabel();
    private final JLabel jFreeLabel2 = new JLabel();
    private final JLabel jMountedOnLabel2 = new JLabel();
    private ColorCanvas canvas = null;
    private final String m_title;
    private final DiskStat m_ds;
    private final String m_diskUnit;
    private JButton jRotateLeftButton;
    private JButton jRotateRightButton;
    protected static final Color COLOR_USED = new Color(204, 0, 0); // Color.red;
    protected static final Color COLOR_FREE = new Color(0, 0, 204); // Color.blue;
    protected int m_usedPct = 0;
    protected int m_freePct = 0;
    protected int m_angle = 90;

    public DiskInfoDialog(JDialog parent, String title, DiskStat ds, String diskUnit) {
        super(parent, title, false);
        m_title = title;
        m_ds = ds;
        m_diskUnit = diskUnit;
        try {
            init();
            pack();
            setSize(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT));
            setMinimumSize(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT));
        } catch (Exception ex) {
            logger.severe(MonitorBundle.format("log.exception", m_title, ex.getMessage()));
            ex.printStackTrace();
        }
    }

    void init() throws Exception {
        setTitle(m_title);
        setResizable(true);
        panel1.setLayout(borderLayout1);
        jFileSystemLabel.setBounds(new Rectangle(148, 15, 120, 17));
        jTypeLabel.setBounds(new Rectangle(148, 37, 120, 17));
        jSizeLabel.setBounds(new Rectangle(148, 59, 120, 17));
        jUsedLabel.setBounds(new Rectangle(148, 81, 120, 17));
        jFreeLabel.setBounds(new Rectangle(148, 103, 120, 17));
        jMountedOnLabel.setBounds(new Rectangle(148, 125, 120, 17));
        jFileSystemLabel2.setBounds(new Rectangle(248, 15, 240, 17));
        jTypeLabel2.setBounds(new Rectangle(248, 37, 240, 17));
        jSizeLabel2.setBounds(new Rectangle(248, 59, 240, 17));
        jUsedLabel2.setBounds(new Rectangle(248, 81, 240, 17));
        jFreeLabel2.setBounds(new Rectangle(248, 103, 240, 17));
        jMountedOnLabel2.setBounds(new Rectangle(248, 125, 240, 17));
        jFileSystemLabel2.setText(m_ds.getFileSystem());
        jTypeLabel2.setText(m_ds.getType());
        displayDiskInfo(m_ds.getBlocks(), m_ds.getUsed(), m_ds.getAvailable(), m_ds.getUseInPercent(), m_diskUnit);
        jMountedOnLabel2.setText(m_ds.getMountedOn());
        jUsedLabel2.setForeground(COLOR_USED);
        jFreeLabel2.setForeground(COLOR_FREE);
        jDiskInfoPanel.setLayout(null);
        getContentPane().add(panel1);
        panel1.add(jDiskInfoPanel, BorderLayout.CENTER);
        canvas = new ColorCanvas(this);
        jDiskInfoPanel.add(canvas);
        jDiskInfoPanel.add(jFileSystemLabel);
        jDiskInfoPanel.add(jTypeLabel);
        jDiskInfoPanel.add(jSizeLabel);
        jDiskInfoPanel.add(jUsedLabel);
        jDiskInfoPanel.add(jFreeLabel);
        jDiskInfoPanel.add(jMountedOnLabel);
        jDiskInfoPanel.add(jFileSystemLabel2);
        jDiskInfoPanel.add(jTypeLabel2);
        jDiskInfoPanel.add(jSizeLabel2);
        jDiskInfoPanel.add(jUsedLabel2);
        jDiskInfoPanel.add(jFreeLabel2);
        jDiskInfoPanel.add(jMountedOnLabel2);
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
        jDiskInfoPanel.add(jRotateLeftButton);
        jDiskInfoPanel.add(jRotateRightButton);
        addComponentListener(this);
        jRotateLeftButton.addActionListener(this);
        jRotateRightButton.addActionListener(this);
    }

    public void updateDiskInfo(DiskStat ds, String diskUnit, String hostName) {
        displayDiskInfo(ds.getBlocks(), ds.getUsed(), ds.getAvailable(), ds.getUseInPercent(), diskUnit);
        canvas.repaint();
    }

    private void displayDiskInfo(double size, double used, double available, int useInPercent, String unit) {
        NumberFormat nf = NumberFormat.getInstance();
        nf.setGroupingUsed(true);
        if (unit.equals(MonitorConstants.GIB_UNIT)) {
            size = size / MonitorConstants.ONE_MIB;
            used = used / MonitorConstants.ONE_MIB;
            available = available / MonitorConstants.ONE_MIB;
            nf.setMaximumFractionDigits(1);
            nf.setMinimumFractionDigits(1);
        } else if (unit.equals(MonitorConstants.MIB_UNIT)) {
            size = size / MonitorConstants.ONE_KIB;
            used = used / MonitorConstants.ONE_KIB;
            available = available / MonitorConstants.ONE_KIB;
            nf.setMaximumFractionDigits(1);
            nf.setMinimumFractionDigits(1);
        } else {
            nf.setMaximumFractionDigits(0);
            nf.setMinimumFractionDigits(0);
        }
        m_usedPct = m_ds.useInPercent;
        m_freePct = 100 - m_usedPct;
        jSizeLabel.setText(MonitorBundle.format("disk.info.format.size", SIZE, unit));
        jSizeLabel2.setText(nf.format(size));
        jUsedLabel.setText(MonitorBundle.format("disk.info.format.size", USED, unit));
        jUsedLabel2.setText(MonitorBundle.format("disk.info.format.value", nf.format(used), useInPercent));
        jFreeLabel.setText(MonitorBundle.format("disk.info.format.size", FREE, unit));
        jFreeLabel2.setText(MonitorBundle.format("disk.info.format.value", nf.format(available), 100 - useInPercent));
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
        }
    }

    @Override
    public void componentResized(ComponentEvent e) {
        int w = getWidth() - 248;
        w = Math.max(w, 240);
        jFileSystemLabel2.setBounds(new Rectangle(248, 15, w, 17));
        jMountedOnLabel2.setBounds(new Rectangle(248, 125, w, 17));
        jTypeLabel2.setBounds(new Rectangle(248, 37, w, 17));
        jFileSystemLabel2.setText(m_ds.getFileSystem());
        jMountedOnLabel2.setText(m_ds.getMountedOn());
        jTypeLabel2.setText(m_ds.getType());
        int x = (getWidth() - (2 * 88 + BUTTON_GAP)) / 2;
        int x2 = x + 88 + BUTTON_GAP;
        jRotateLeftButton.setBounds(new Rectangle(x, 158, 88, 28));
        jRotateRightButton.setBounds(new Rectangle(x2, 158, 88, 28));
    }

    @Override
    public void componentMoved(ComponentEvent e) {
    }

    @Override
    public void componentShown(ComponentEvent e) {
    }

    @Override
    public void componentHidden(ComponentEvent e) {
    }
}

class ColorCanvas extends Canvas {
    private static final long serialVersionUID = 1L;
    private final DiskInfoDialog m_dlg;

    public ColorCanvas(DiskInfoDialog dlg) {
        m_dlg = dlg;
        setSize(120, 120);
    }

    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g; // Type-cast the parameter to Graphics2D
        g2.setColor(DiskInfoDialog.COLOR_FREE);
        g2.fillArc(20, 20, 100, 100, m_dlg.m_angle, (int) (m_dlg.m_freePct / 100.0 * 360.0 + 0.5));
        g2.setColor(DiskInfoDialog.COLOR_USED);
        g2.fillArc(20, 20, 100, 100, m_dlg.m_angle, (int) (m_dlg.m_usedPct / 100.0 * 360.0 + 0.5) * -1);

    }
}
