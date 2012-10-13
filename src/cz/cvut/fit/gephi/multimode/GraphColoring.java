package cz.cvut.fit.gephi.multimode;

import java.util.ArrayList;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.attributes.api.AttributeOrigin;
import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.Node;
import org.openide.util.Lookup;

/**
 *
 * @author Jaroslav Kuchar
 */
public class GraphColoring {

    private Graph graph;
    private Node[] nodes;

    public GraphColoring() {
        // load all possible values of attribute
        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        graph = graphController.getModel().getGraphVisible();
        nodes = graph.getNodes().toArray();
    }

    /**
     * graph coloring for bipartite networks Implementation inspired by:
     * http://pages.cs.wisc.edu/~elgar/cs577/Bipartite/Bipartite.java
     *
     * @return true if bipartite
     */
    public boolean bipartite() {
        // setup
        boolean bipartite = true;
        boolean done = false;

        // attribute column
        AttributeModel attributeModel = Lookup.getDefault().lookup(AttributeController.class).getModel();
        AttributeTable nodeTable = attributeModel.getNodeTable();
        AttributeColumn colorColumn = nodeTable.getColumn("NodeColor-Multimode");
        if (colorColumn == null) {
            colorColumn = nodeTable.addColumn("NodeColor-Multimode", "Node Color Multimode", AttributeType.STRING, AttributeOrigin.COMPUTED, NodeColor.BLACK.getValue());
        }

        // prepare
        Node current = null;
        NodeColor nextColor = NodeColor.RED;

        // queue
        ArrayList<Node> queue = new ArrayList<Node>(graph.getNodeCount());
        // iterate all
        for (int i = 0; i < nodes.length; i++) {
            // if node is black
            if (((String) Utils.getValue(nodes[i], colorColumn)).equals(NodeColor.BLACK.getValue())) {
                // set to red
                nodes[i].getAttributes().setValue(colorColumn.getIndex(), NodeColor.RED.getValue());
                // add to the queue
                queue.add(nodes[i]);
                // iterate all nodes in queue
                while (!queue.isEmpty()) {
                    // current
                    current = queue.remove(0);
                    // if red - change to blue
                    if (((String) Utils.getValue(current, colorColumn)).equals(NodeColor.RED.getValue())) {
                        nextColor = NodeColor.BLUE;
                    } else {
                        nextColor = NodeColor.RED;
                    }
                    // get all neighbours
                    for (Node n : graph.getNeighbors(current)) {
                        // if not yey processed
                        if (((String) Utils.getValue(n, colorColumn)).equals(NodeColor.BLACK.getValue())) {
                            // set color
                            n.getAttributes().setValue(colorColumn.getIndex(), nextColor.getValue());
                            // add to the queue
                            queue.add(n);
                        }
                    }
                }
            }
        }
        // check bipartite
        bipartite = true;
        for (int i = 0; i < nodes.length && bipartite; i++) {
            for (Node n : graph.getNeighbors(nodes[i])) {
                // check colors of neighbours
                if (((String) Utils.getValue(n, colorColumn)).equals(((String) Utils.getValue(nodes[i], colorColumn)))) {
                    bipartite = false;
                }
            }
        }
        return bipartite;
    }

    /**
     * Node colors enum
     */
    protected enum NodeColor {

        BLACK("black"), BLUE("blue"), RED("red");
        private String value;

        NodeColor(String value) {
            this.value = value;
        }

        String getValue() {
            return value;
        }
    }
}
