/*
 * File.   MonitorTableCellRenderer.java
 * Date.   03/16/2024
 * Author. Anan Mahasintunan
 * Description.
 *         Client Side - Linux Performance Monitor for CPU, Memory and Network.
 */

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class MonitorTableCellRenderer extends DefaultTableCellRenderer {
    private static final long serialVersionUID = 1L;
    Map<Integer, Color> mapRowColor;

    public MonitorTableCellRenderer() {
        mapRowColor = new HashMap<>();
    }

    public void setColor(int row, Color color) {
        mapRowColor.put(row, color);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        Color color = mapRowColor.get(row);
        if (color != null) {
            setForeground(color);
        } else {
            setForeground(table.getBackground());
        }

        return this;
    }
}
