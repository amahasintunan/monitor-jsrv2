/*
 * File.   DiskUsageDialog.java
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
import java.awt.event.*;
import java.text.NumberFormat;
import java.util.logging.Logger;

import static java.awt.EventQueue.invokeLater;

public class DiskSpaceDialog extends JDialog implements MouseListener, ActionListener {
    private static final Logger logger = Logger.getLogger(DiskSpaceDialog.class.getName());
    private static final long serialVersionUID = 1L;
    private static final int MAX_COLUMN_WIDTH = 200;
    private static final int DEFAULT_WIDTH = 660;
    private static final int DEFAULT_HEIGHT = 400;
    private JFrame m_frame = null;
    private String m_hostName = null;
    private JSONArray m_jsonDisks = null;
    private final JScrollPane scrollPane = new JScrollPane();
    private final JTable table = new JTable();
    private Object[][] m_data = null;
    private final Object[] m_colTypes = {String.class, String.class, Long.class, Long.class, Long.class, Long.class,
            String.class};
    private final String[] m_columnKiB = buildColumnHeaders(MonitorConstants.KIB_UNIT);
    private final String[] m_columnMiB = buildColumnHeaders(MonitorConstants.MIB_UNIT);
    private final String[] m_columnGiB = buildColumnHeaders(MonitorConstants.GIB_UNIT);

    private static String[] buildColumnHeaders(String unit) {
        return new String[] {
                MonitorBundle.get("disk.column.filesystem"),
                MonitorBundle.get("disk.column.type"),
                MonitorBundle.format("disk.column.size", unit),
                MonitorBundle.format("disk.column.used", unit),
                MonitorBundle.format("disk.column.free", unit),
                MonitorBundle.get("disk.column.usepct"),
                MonitorBundle.get("disk.column.mountedon")
        };
    }
    private int[] columnWidth = null;
    private JPopupMenu jPopupMenu;
    private JRadioButtonMenuItem jKiBRadioButtonMenuItem;
    private JRadioButtonMenuItem jMiBRadioButtonMenuItem;
    private JRadioButtonMenuItem jGiBRadioButtonMenuItem;
    private DiskInfoDialog m_diskInfoDialog = null;
    private int m_selectedRow = -1;

    public DiskSpaceDialog(JFrame frame, String hostName, JSONArray jsonDisks) {
        super(frame, "", false);
        m_frame = frame;
        m_hostName = hostName;
        m_jsonDisks = jsonDisks;
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
        populateTableFromJsonDisks(m_jsonDisks, m_hostName);
        setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        table.addMouseListener(this);
        jPopupMenu = new JPopupMenu();
        jKiBRadioButtonMenuItem = new JRadioButtonMenuItem(MonitorConstants.KIB_UNIT);
        jKiBRadioButtonMenuItem.setSelected(true);
        jMiBRadioButtonMenuItem = new JRadioButtonMenuItem(MonitorConstants.MIB_UNIT);
        jMiBRadioButtonMenuItem.setSelected(false);
        jGiBRadioButtonMenuItem = new JRadioButtonMenuItem(MonitorConstants.GIB_UNIT);
        jGiBRadioButtonMenuItem.setSelected(false);
        jPopupMenu.add(jKiBRadioButtonMenuItem);
        jPopupMenu.add(jMiBRadioButtonMenuItem);
        jPopupMenu.add(jGiBRadioButtonMenuItem);
        jKiBRadioButtonMenuItem.addActionListener(this);
        jMiBRadioButtonMenuItem.addActionListener(this);
        jGiBRadioButtonMenuItem.addActionListener(this);
        addMouseListener(this);
    }

    public void populateTableFromJsonDisks(JSONArray jsonDisks, String hostName) {
        try {
            invokeLater(() -> {
                m_hostName = hostName;
                setTitle(MonitorBundle.format("disk.space.title", hostName));
                int size = jsonDisks.size();
                m_jsonDisks = jsonDisks;
                m_data = new Object[size][7];
                NumberFormat nf = NumberFormat.getInstance();
                nf.setGroupingUsed(true);
                for (int i = 0; i < jsonDisks.size(); i++) {
                    JSONObject disk = (JSONObject) jsonDisks.get(i);
                    m_data[i][0] = disk.get("fileSystem");
                    m_data[i][1] = disk.get("type");
                    if (jGiBRadioButtonMenuItem.isSelected()) {
                        nf.setMaximumFractionDigits(1);
                        nf.setMinimumFractionDigits(1);
                        m_data[i][2] = nf.format((double) (long) disk.get("1k-blocks") / MonitorConstants.ONE_MIB);
                        m_data[i][3] = nf.format((double) (long) disk.get("used") / MonitorConstants.ONE_MIB);
                        m_data[i][4] = nf.format((double) (long) disk.get("available") / MonitorConstants.ONE_MIB);
                    } else if (jMiBRadioButtonMenuItem.isSelected()) {
                        nf.setMaximumFractionDigits(1);
                        nf.setMinimumFractionDigits(1);
                        m_data[i][2] = nf.format((double) (long) disk.get("1k-blocks") / MonitorConstants.ONE_KIB);
                        m_data[i][3] = nf.format((double) (long) disk.get("used") / MonitorConstants.ONE_KIB);
                        m_data[i][4] = nf.format((double) (long) disk.get("available") / MonitorConstants.ONE_KIB);
                    } else {
                        nf.setMaximumFractionDigits(0);
                        nf.setMinimumFractionDigits(0);
                        m_data[i][2] = nf.format(disk.get("1k-blocks"));
                        m_data[i][3] = nf.format(disk.get("used"));
                        m_data[i][4] = nf.format(disk.get("available"));
                    }
                    m_data[i][5] = disk.get("use%");
                    m_data[i][6] = disk.get("mountedOn");
                }
                if (jGiBRadioButtonMenuItem.isSelected()) {
                    setTableData(m_columnGiB, m_data, m_colTypes);
                } else if (jMiBRadioButtonMenuItem.isSelected()) {
                    setTableData(m_columnMiB, m_data, m_colTypes);
                } else {
                    setTableData(m_columnKiB, m_data, m_colTypes);
                }
                int rowCount = table.getRowCount();
                MonitorTableCellRenderer tcr = new MonitorTableCellRenderer();
                for (int i = 1; i <= rowCount; i++) {
                    long useInPercent = (long) m_data[i - 1][5];
                    tcr.setColor(i - 1,
                            useInPercent >= MonitorConstants.WARNING_HIGH ? MonitorConstants.COLOR_WARNING_HIGH
                                    : useInPercent >= MonitorConstants.WARNING_LOW ? MonitorConstants.COLOR_WARNING_LOW
                                    : MonitorConstants.COLOR_NO_WARNING);
                    tcr.setHorizontalAlignment(SwingConstants.RIGHT);
                }
                TableColumnModel tcm = table.getColumnModel();
                tcm.getColumn(5).setCellRenderer(tcr);
                validate();
                // Update diskInfoDialog if visible with a latest disk stats
                if (m_diskInfoDialog != null && m_diskInfoDialog.isVisible() && m_selectedRow != -1) {
                    JSONObject disk = (JSONObject) m_jsonDisks.get(m_selectedRow);
                    String fileSystem = (String) disk.get("fileSystem");
                    String type = (String) disk.get("type");
                    long blocks = (long) disk.get("1k-blocks");
                    long used = (long) disk.get("used");
                    long available = (long) disk.get("available");
                    int useInPercent = (int) (long) disk.get("use%");
                    String mountedOn = (String) disk.get("mountedOn");
                    DiskStat ds = new DiskStat.DiskStatBuilder().fileSystem(fileSystem).type(type).blocks(blocks)
                            .used(used).available(available).useInPercent(useInPercent).mountedOn(mountedOn).build();
                    String diskUnit = jGiBRadioButtonMenuItem.isSelected() ? MonitorConstants.GIB_UNIT
                            : jMiBRadioButtonMenuItem.isSelected() ? MonitorConstants.MIB_UNIT
                            : MonitorConstants.KIB_UNIT;
                    m_diskInfoDialog.updateDiskInfo(ds, diskUnit, m_hostName);
                }
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

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
            if (e.getSource() == table) {
                try {
                    m_selectedRow = table.getSelectedRow();
                    if (m_selectedRow == -1) {
                        return;
                    }
                    String title = MonitorBundle.format("disk.info.title", m_hostName);
                    JSONObject disk = (JSONObject) m_jsonDisks.get(m_selectedRow);
                    String fileSystem = (String) disk.get("fileSystem");
                    String type = (String) disk.get("type");
                    long blocks = (long) disk.get("1k-blocks");
                    long used = (long) disk.get("used");
                    long available = (long) disk.get("available");
                    int useInPercent = (int) (long) disk.get("use%");
                    String mountedOn = (String) disk.get("mountedOn");
                    DiskStat ds = new DiskStat.DiskStatBuilder().fileSystem(fileSystem).type(type).blocks(blocks)
                            .used(used).available(available).useInPercent(useInPercent).mountedOn(mountedOn).build();
                    String diskUnit = jGiBRadioButtonMenuItem.isSelected() ? MonitorConstants.GIB_UNIT
                            : jMiBRadioButtonMenuItem.isSelected() ? MonitorConstants.MIB_UNIT
                            : MonitorConstants.KIB_UNIT;
                    m_diskInfoDialog = new DiskInfoDialog(this, title, ds, diskUnit);
                    centerDialog(m_diskInfoDialog);
                } catch (Exception ex) {
                    logger.severe("mouseClicked() " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        }
    }

    public void centerDialog(JDialog dialog) {
        Dimension dialogSize = dialog.getSize();
        Dimension size = getSize();
        Point loc = getLocation();
        dialog.setLocation((size.width - dialogSize.width) / 2 + loc.x, (size.height - dialogSize.height) / 2 + loc.y);
        dialog.setModal(true);
        dialog.setVisible(true);
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

    @Override
    public void actionPerformed(ActionEvent e) {
        Object obj = e.getSource();
        if (obj == jKiBRadioButtonMenuItem) {
            jKiBRadioButtonMenuItem.setSelected(true);
            jMiBRadioButtonMenuItem.setSelected(false);
            jGiBRadioButtonMenuItem.setSelected(false);
        } else if (obj == jMiBRadioButtonMenuItem) {
            jKiBRadioButtonMenuItem.setSelected(false);
            jMiBRadioButtonMenuItem.setSelected(true);
            jGiBRadioButtonMenuItem.setSelected(false);
        } else if (obj == jGiBRadioButtonMenuItem) {
            jKiBRadioButtonMenuItem.setSelected(false);
            jMiBRadioButtonMenuItem.setSelected(false);
            jGiBRadioButtonMenuItem.setSelected(true);
        }
    }

    private void showPopupMenu(MouseEvent e) {
        if (e.isPopupTrigger()) {
            jPopupMenu.show(e.getComponent(), e.getX(), e.getY());
        }
    }

}
