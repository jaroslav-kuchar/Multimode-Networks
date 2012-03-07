package cz.cvut.fit.gephi.multimode;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import javax.swing.*;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.graph.api.*;
import org.gephi.tools.spi.Tool;
import org.gephi.tools.spi.ToolUI;
import org.gephi.utils.longtask.api.LongTaskExecutor;
import org.openide.util.Lookup;

/**
 *
 * @author Jaroslav Kuchar
 */
public class MultiModeUI implements ToolUI {

    private JComboBox attributes;
    private JComboBox firstMatrix;
    private JComboBox secondMatrix;
    private JButton start;
    private JCheckBox removeEdges;
    private JCheckBox removeNodes;

    @Override
    public JPanel getPropertiesBar(Tool tool) {        
        JPanel panel = new JPanel(new FlowLayout());
        panel.add(new JLabel("Attr.:"));

        AttributeModel am = Lookup.getDefault().lookup(AttributeController.class).getModel();
        // TODO - remove Id and Label columns            
        //columns = new JComboBox(am.getNodeTable().getColumns());
        // get all attributes without "Id" and "Label"        
        attributes = new JComboBox();
        attributes.setToolTipText("Attribute column which represents node type.");
        for (AttributeColumn ac : am.getNodeTable().getColumns()) {
            if (!ac.getTitle().equals("Id") && !ac.getTitle().equals("Label")) {
                attributes.addItem(ac);
            }
        }

        attributes.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                firstMatrix.removeAllItems();
                secondMatrix.removeAllItems();                
                AttributeColumn col = (AttributeColumn) attributes.getSelectedItem();

                // load all possible values of attribute
                GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
                Graph graph = graphController.getModel().getGraphVisible();
                Node[] nodes = graph.getNodes().toArray();
                Set<String> vals = new HashSet<String>();
                for (Node n : nodes) {
                    vals.add(Utils.getValue(n, col).toString());
                }                
                // add combinations
                for (String left : vals) {
                    for (String right : vals) {
                        firstMatrix.addItem(new ValueCombination(left, right));
                    }
                }                
                if (firstMatrix.getItemCount() > 0) {
                    firstMatrix.setSelectedIndex(0);
                }                
            }
        });
        panel.add(attributes);


        panel.add(new JLabel("1st:"));
        firstMatrix = new JComboBox();
        firstMatrix.setToolTipText("First adjacency matrix. From which node type to which node type is first adjacency matrix.");
        firstMatrix.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                // TODO reaction on selected first matrix                
                secondMatrix.removeAllItems();
                AttributeColumn col = (AttributeColumn)attributes.getSelectedItem();

                // load all possible values of attribute
                GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
                Graph graph = graphController.getModel().getGraphVisible();
                Node[] nodes = graph.getNodes().toArray();
                Set<String> vals = new HashSet<String>();
                for (Node n : nodes) {
                    vals.add(Utils.getValue(n, col).toString());
                }
                // add combination with right dimension on left side
                for (String left : vals) {
                    if (firstMatrix.getSelectedItem()!=null && left.equals(((ValueCombination) firstMatrix.getSelectedItem()).getSecond())) {

                        for (String right : vals) {
                            secondMatrix.addItem(new ValueCombination(left, right));
                        }
                    }
                }
                if (secondMatrix.getItemCount() > 0) {
                    secondMatrix.setSelectedIndex(0);
                }

                start.setEnabled(true);
            }
        });


        panel.add(firstMatrix);

        panel.add(new JLabel("2nd:"));
        secondMatrix = new JComboBox();
        secondMatrix.setToolTipText("Second adjacency matrix. From which node type to which node type is second adjacency matrix.");
        panel.add(secondMatrix);


        removeEdges = new JCheckBox("Remove Edges");
        removeEdges.setToolTipText("Remove affected edges.");
        removeEdges.setSelected(false);
        panel.add(removeEdges);
        
        removeNodes = new JCheckBox("Remove Nodes");
        removeNodes.setToolTipText("Remove affected Nodes.");
        removeNodes.setSelected(false);
        panel.add(removeNodes);


        start = new JButton("Run");
        start.setEnabled(false);
        start.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                // start long task and inject parameters
                LongTaskExecutor executor = new LongTaskExecutor(true);
                LongTaskTransformation longTask = new LongTaskTransformation(attributes, firstMatrix, secondMatrix, removeEdges.isSelected(), removeNodes.isSelected());
                executor.execute(longTask, longTask, "Transformation...", null);
            }
        });
        panel.add(start);

        return panel;
    }

    @Override
    public Icon getIcon() {        
        // http://led24.de/iconset/ 
        return new ImageIcon(getClass().getResource("/cz/cvut/fit/gephi/multimode/resources/lightning.png"));
    }

    @Override
    public String getName() {
        return "Multimode transformation";
    }

    @Override
    public String getDescription() {
        return "Enables transformation of multimode networks";
    }

    @Override
    public int getPosition() {
        return 1000;
    }
}
