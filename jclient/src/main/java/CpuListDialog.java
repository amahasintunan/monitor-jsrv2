/*
 * File.   CpuListDialog.java
 * Date.   03/16/2024
 * Author. Anan Mahasintunan
 * Description.
 *         Client Side - Linux Performance Monitor for CPU, Memory and Network.
 */

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.text.NumberFormat;
import java.util.logging.Logger;

import static java.awt.EventQueue.invokeLater;

public class CpuListDialog extends JDialog {
    private static final Logger logger = Logger.getLogger(CpuListDialog.class.getName());
    private static final long serialVersionUID = 1L;
    private static final int MAX_COLUMN_WIDTH = 200;
    private static final int DEFAULT_WIDTH = 188;
    private static final int DEFAULT_HEIGHT = 376;
    private JFrame m_frame = null;
    private String m_hostName = null;
    private JSONArray m_jsonCpus = null;
    private final JScrollPane scrollPane = new JScrollPane();
    private final JTable table = new JTable();
    private Object[][] m_data = null;
    private final Object[] m_colTypes = {Integer.class, String.class, Long.class, String.class};
    private final String[] m_columns = {
            MonitorBundle.get("cpu.list.column.num"),
            MonitorBundle.get("cpu.list.column.id"),
            MonitorBundle.get("cpu.list.column.usage"),
            MonitorBundle.get("cpu.list.column.description")
    };
    private int[] columnWidth = null;
    private JPopupMenu jPopupMenu;
    private JMenuItem jBarChartMenuItem;

    public CpuListDialog(JFrame frame, String hostName, JSONArray jsonCpus) {
        super(frame, "", false);
        m_frame = frame;
        m_hostName = hostName;
        m_jsonCpus = jsonCpus;
        init();
    }

    public void init() {
        JPanel p = new JPanel();
        p.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        int space = 10;
        c.insets = new Insets(space, space, space, space);
        c.fill = GridBagConstraints.BOTH;
        scrollPane.getViewport().add(table);
        c.weightx = 1.0;
        c.weighty = 1.0;
        p.add(scrollPane, c);
        getContentPane().add(p, BorderLayout.CENTER);
        populateTableFromJsonCpus(m_jsonCpus, m_hostName);
        setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        jPopupMenu = new JPopupMenu();
        jBarChartMenuItem = new JMenuItem(MonitorBundle.get("cpu.list.barchart"));
        jPopupMenu.add(jBarChartMenuItem);
    }

    public void populateTableFromJsonCpus(JSONArray jsonCpus, String hostName) {
        try {
            invokeLater(() -> {
                m_hostName = hostName;
                setTitle(MonitorBundle.format("cpu.list.title", hostName));
                int size = jsonCpus.size();
                m_jsonCpus = jsonCpus;
                m_data = new Object[size][7];
                NumberFormat nf = NumberFormat.getInstance();
                nf.setGroupingUsed(true);
                for (int i = 0; i < jsonCpus.size(); i++) {
                    JSONObject cpu = (JSONObject) jsonCpus.get(i);
                    if (i == 0) {
                        m_data[i][0] = "";
                        m_data[i][1] = MonitorBundle.get("cpu.list.id.aggregate");
                        m_data[i][2] = cpu.get("cpu");
                        m_data[i][3] = MonitorBundle.format("cpu.list.aggregate", jsonCpus.size() - 1);
                    } else {
                        m_data[i][0] = i;
                        m_data[i][1] = "cpu" + (i - 1);
                        m_data[i][2] = cpu.get("cpu" + (i - 1));
                        m_data[i][3] = "";
                    }
                }
                setTableData(m_columns, m_data, m_colTypes);
                int rowCount = table.getRowCount();
                MonitorTableCellRenderer tcr = new MonitorTableCellRenderer();
                for (int i = 1; i <= rowCount; i++) {
                    long useInPercent = (long) m_data[i - 1][2];
                    tcr.setColor(i - 1,
                            useInPercent >= MonitorConstants.WARNING_HIGH ? MonitorConstants.COLOR_WARNING_HIGH
                                    : useInPercent >= MonitorConstants.WARNING_LOW ? MonitorConstants.COLOR_WARNING_LOW
                                    : MonitorConstants.COLOR_NO_WARNING);
                    tcr.setHorizontalAlignment(SwingConstants.RIGHT);
                }
                TableColumnModel tcm = table.getColumnModel();
                tcm.getColumn(2).setCellRenderer(tcr);
                validate();
            });
        } catch (Exception ex) {
            logger.severe(MonitorBundle.format("log.exception", MonitorClient.TITLE_NAME, ex.getMessage()));
        }
    }

    public int getColumnHeaderWidth(TableColumn tableColumn, int column) {
        TableCellRenderer renderer = tableColumn.getHeaderRenderer();
        Object headerValue = tableColumn.getHeaderValue();
        if (renderer == null) {
            renderer = table.getTableHeader().getDefaultRenderer();
        }
        Component component = renderer.getTableCellRendererComponent(table, headerValue, false, false, -1, column);

        return component.getPreferredSize().width;
    }

    public void setPreferredColumnWitdth(JTable table) {
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        int columnCount = table.getColumnCount();
        if (columnCount < 1) {
            return;
        }
        if (columnWidth == null) {
            columnWidth = new int[columnCount];
            for (int column = 0; column < columnCount; column++) {
                TableColumn tableColumn = table.getColumnModel().getColumn(column);
                int columnHeaderWidth = getColumnHeaderWidth(tableColumn, column);
                int preferredColumnWidth = columnHeaderWidth;
                for (int row = 0; row < table.getRowCount(); row++) {
                    TableCellRenderer cellRenderer = table.getCellRenderer(row, column);
                    Component c = table.prepareRenderer(cellRenderer, row, column);
                    int columnWidth = c.getPreferredSize().width + table.getIntercellSpacing().width;
                    preferredColumnWidth = Math.max(preferredColumnWidth, columnWidth);
                    if (preferredColumnWidth < columnHeaderWidth) {
                        preferredColumnWidth = columnHeaderWidth;
                    }
                    if (preferredColumnWidth > MAX_COLUMN_WIDTH) {
                        preferredColumnWidth = MAX_COLUMN_WIDTH;
                        break;
                    }
                }
                tableColumn.setPreferredWidth(preferredColumnWidth);
                columnWidth[column] = preferredColumnWidth;
            }
        } else {
            for (int column = 0; column < columnCount; column++) {
                TableColumn tableColumn = table.getColumnModel().getColumn(column);
                tableColumn.setPreferredWidth(columnWidth[column]);
            }
        }
    }

    private void setTableData(final String[] column, final Object[][] data, final Object[] colTypes) {
        try {
            if (column != null && data != null) {
                TableModel dataModel = new MonitorTableModel(column, data, colTypes);
                table.setModel(dataModel);
                setPreferredColumnWitdth(table);
            }
        } catch (Exception ex) {
            logger.severe(MonitorBundle.format("log.exception", MonitorClient.TITLE_NAME, ex.getMessage()));
        }
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
