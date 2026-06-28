/**
 * @author anan.mahasintunan
 * file: MonitorProperties.java
 * date: 08/17/2025
 */

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

public class MonitorProperties {
    private static MonitorProperties s_properties;
    private Map<String, String> m_mapGlobalProps = null;

    private MonitorProperties() {
    }

    public static MonitorProperties getSingleProperties() {
        if (s_properties == null) {
            s_properties = new MonitorProperties();
        }
        return s_properties;
    }

    public String getMapProp(String key) {
        return m_mapGlobalProps.get(key);
    }

    public void loadProperties(String fileName) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        try (FileInputStream fin = new FileInputStream(new File(fileName))) {
            Document doc = db.parse(fin);
            traverseNode(doc);
        }
    }

    public void traverseNode(Node node) throws Exception {
        if (node == null) {
            return;
        }

        int type = node.getNodeType();
        switch (type) {
            case Node.DOCUMENT_NODE:
                Document doc = (Document) node;
                traverseNode(doc.getDocumentElement());
                break;
            case Node.ELEMENT_NODE:
                String elemName = node.getNodeName();
                if (elemName.equals(MonitorClient.GLOBAL_PROPS)) {
                    m_mapGlobalProps = new HashMap<>();
                }

                NodeList children = node.getChildNodes();
                int len = children.getLength();
                for (int i = 0; i < len; i++) {
                    traverseNode(children.item(i));
                }
                break;
            case Node.TEXT_NODE:
                Node parent = node.getParentNode();
                String parentName = parent.getNodeName();
                String textValue = node.getNodeValue();
                if (parentName.equals(MonitorClient.BROWSER_PATH)) {
                    m_mapGlobalProps.put(MonitorClient.BROWSER_PATH, textValue);
                }
        }
    }
}
