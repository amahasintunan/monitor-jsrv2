/*
 * File.   MonitorTableModel.java
 * Date.   03/16/2024
 * Author. Anan Mahasintunan
 * Description.
 *         Client Side - Linux Performance Monitor for CPU, Memory and Network.
 */

import javax.swing.table.AbstractTableModel;
import java.util.logging.Logger;

public class MonitorTableModel extends AbstractTableModel {
    private static final Logger logger = Logger.getLogger(MonitorTableModel.class.getName());
    private static final long serialVersionUID = 1L;
    String[] m_column;
    Object[][] m_data;
    Object[] m_colTypes;

    public MonitorTableModel(String[] column, Object[][] data, Object[] colTypes) {
        m_column = column;
        m_data = data;
        m_colTypes = colTypes;
    }

    public int getColumnCount() {
        return (m_column != null) ? m_column.length : 0;
    }

    public int getRowCount() {
        return (m_data != null) ? m_data.length : 0;
    }

    public Object getValueAt(int row, int col) {
        try {
            return (m_data != null) ? m_data[row][col] : null;
        } catch (Exception ex) {
            logger.severe("getValueAt() row=" + row + ", col=" + col);
            throw new IllegalArgumentException(MonitorBundle.get("table.error.getvalue"));
        }
    }

    public String getColumnName(int col) {
        try {
            return (m_column != null) ? m_column[col] : "";
        } catch (Exception ex) {
            logger.severe("getColumnName() col=" + col);
            throw new IllegalArgumentException(MonitorBundle.get("table.error.getcolumn"));
        }
    }

    public Class<Object> getColumnClass(int col) {
        if (m_colTypes == null) {
            return Object.class;
        } else {
            @SuppressWarnings("unchecked")
            Class<Object> cls = (Class<Object>) m_colTypes[col];
            return cls;
        }
    }

}
