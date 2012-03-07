package cz.cvut.fit.gephi.multimode;

import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeRow;
import org.gephi.graph.api.Node;

/**
 *
 * @author Jaroslav Kuchar
 */
public class Utils {

    /**
     * get value of atrribute colmn from node
     * @param node
     * @param column
     * @return value of attribute
     */
    public static Object getValue(Node node, AttributeColumn column) {
        return (((AttributeRow) (node.getNodeData().getAttributes())).getValue(column));
    }
}
