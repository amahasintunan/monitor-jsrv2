/*
 * File.   CpuBarChartDialog.java
 * Date.   03/16/2024
 * Author. Anan Mahasintunan
 * Description.
 *         Client Side - Linux Performance Monitor for CPU, Memory and Network.
 */

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.util.logging.Logger;

public class CpuBarChartDialog extends JDialog {
    private static final Logger logger = Logger.getLogger(CpuBarChartDialog.class.getName());
    private static final long serialVersionUID = 1L;
    private JFrame m_frame = null;
    private String m_hostName = null;
    private JSONArray m_jsonCpus = null;
    private CpuColorCanvas canvas = null;
    protected int[] values = null;
    protected String[] names = null;
    protected Color fgColor;
    protected Color bgColor;
    protected Color scaleColor;

    public CpuBarChartDialog(JFrame frame, String hostName, JSONArray jsonCpus) {
        super(frame, "", false);
        m_frame = frame;
        m_hostName = hostName;
        m_jsonCpus = jsonCpus;
        fgColor = ((MonitorClient) frame).getFgColor();
        bgColor = ((MonitorClient) frame).getBgColor();
        scaleColor = ((MonitorClient) frame).getScaleColor();
        getContentPane().setBackground(bgColor);
        init();
    }

    public void init() {
        canvas = new CpuColorCanvas(this);
        getContentPane().add(canvas);
        // setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        setSize(m_frame.getWidth(), m_frame.getHeight());
        populateDataFromJsonCpus(m_jsonCpus, m_hostName);
    }

    public void populateDataFromJsonCpus(JSONArray jsonCpus, String hostName) {
        setTitle(MonitorBundle.format("cpu.chart.title", hostName));
        m_hostName = hostName;
        m_jsonCpus = jsonCpus;
        int size = jsonCpus.size();
        if (size < 1) {
            logger.severe(MonitorBundle.format("log.cpu.array.empty", MonitorClient.TITLE_NAME));
            return;
        }
        values = new int[size];
        names = new String[size];
        for (int i = 0; i < size; i++) {
            JSONObject cpu = (JSONObject) jsonCpus.get(i);
            if (i == 0) {
                names[i] = MonitorBundle.get("cpu.chart.aggregate.label");
                values[i] = (int) (long) cpu.get("cpu");
            } else {
                names[i] = String.valueOf(i - 1);
                values[i] = (int) (long) cpu.get("cpu" + (i - 1));
            }
        }
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

    public void centerDialog(JDialog dialog) {
        Dimension dialogSize = dialog.getSize();
        Dimension size = getSize();
        Point loc = getLocation();
        dialog.setLocation((size.width - dialogSize.width) / 2 + loc.x, (size.height - dialogSize.height) / 2 + loc.y);
        dialog.setModal(true);
        dialog.setVisible(true);
    }

}

class CpuColorCanvas extends Canvas {
    private static final long serialVersionUID = 1L;
    private final CpuBarChartDialog m_dlg;

    public CpuColorCanvas(CpuBarChartDialog dlg) {
        m_dlg = dlg;
        setSize(dlg.getWidth(), dlg.getHeight());
    }

    public void paint(Graphics g) {
        int[] values = m_dlg.values;
        String[] names = m_dlg.names;
        if (values == null || values.length == 0) {
            return;
        }
        double minValue = 0.0;
        double maxValue = 100.0;
        Dimension d = getSize();
        int left = 32;
        int clientWidth = d.width - left;
        int clientHeight = d.height;
        int barWidth = clientWidth / values.length;
        Font font = new Font("SansSerif", Font.PLAIN, 12);
        FontMetrics fontMetrics = g.getFontMetrics(font);
        g.setFont(font);
        int bottom = fontMetrics.getHeight();
        double scale = (clientHeight - bottom) / (maxValue - minValue);
        int yScale = 10;
        int xScale = values.length;
        double cellWidth = (double) (clientWidth) / xScale;
        double cellHeight = (double) (clientHeight - bottom) / yScale;
        double[] xPointsScale = new double[xScale + 1];
        double[] yPointsScale = new double[yScale + 1];
        for (int x = 0; x < xPointsScale.length; x++) {
            xPointsScale[x] = left + (x * cellWidth);
        }
        for (int y = 0; y < yPointsScale.length; y++) {
            yPointsScale[y] = y * cellHeight;
        }
        for (int y = 1; y < yPointsScale.length - 1; y++) {
            for (int x = 0; x < xPointsScale.length - 1; x++) {
                g.setColor(m_dlg.scaleColor);
                g.drawLine((int) xPointsScale[x], (int) yPointsScale[y], (int) xPointsScale[x + 1],
                        (int) yPointsScale[y]);
            }
        }
        for (int y = 1; y < yPointsScale.length - 1; y++) {
            String percent = (yPointsScale.length - y - 1) * yScale + "%";
            g.setColor(m_dlg.scaleColor);
            g.drawString(percent, 0, (int) yPointsScale[y]);
        }
        for (int i = 0; i < values.length; i++) {
            int x = left + i * barWidth + 1;
            int y = 0;
            int height = (int) (values[i] * scale);
            if (values[i] >= 0)
                y = (int) ((maxValue - values[i]) * scale);
            else {
                y = (int) (maxValue * scale);
                height = -height;
            }
            if (values[i] >= MonitorConstants.WARNING_HIGH) {
                g.setColor(MonitorConstants.COLOR_WARNING_HIGH);
            } else if (values[i] >= MonitorConstants.WARNING_LOW) {
                g.setColor(MonitorConstants.COLOR_WARNING_LOW);
            } else {
                g.setColor(i == 0 ? MonitorConstants.COLOR_AGGREGATE : MonitorConstants.COLOR_INDIVIDUAL);
            }
            g.fillRect(x, y, barWidth - 3, height);
            g.setColor(m_dlg.fgColor);
            g.drawRect(x, y, barWidth - 3, height);
            int labelWidth = fontMetrics.stringWidth(names[i]);
            int x2 = left + i * barWidth + (barWidth - labelWidth) / 2;
            int y2 = clientHeight - fontMetrics.getDescent();
            g.drawString(names[i], x2, y2);
        }
    }

}
