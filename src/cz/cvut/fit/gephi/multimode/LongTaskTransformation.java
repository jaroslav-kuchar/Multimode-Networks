package cz.cvut.fit.gephi.multimode;

import java.util.*;
import javax.swing.JComboBox;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.graph.api.*;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;
import org.openide.util.Lookup;

/**
 *
 * @author Jaroslav Kuchar
 */
public class LongTaskTransformation implements LongTask, Runnable {

    private ProgressTicket progressTicket;
    private boolean cancelled = false;
    private JComboBox columns;
    private JComboBox firstMatrixCombo;
    private JComboBox secondMatrixCombo;
    private boolean removeEdges = true;
    private boolean removeNodes = true;

    public LongTaskTransformation(JComboBox columns, JComboBox firstMatrixCombo, JComboBox secondMatrixCombo, boolean removeEdges, boolean removeNodes) {
        this.columns = columns;
        this.firstMatrixCombo = firstMatrixCombo;
        this.secondMatrixCombo = secondMatrixCombo;
        this.removeEdges = removeEdges;
        this.removeNodes = removeNodes;
    }

    @Override
    public void run() {
        // get selected combination
        ValueCombination firstCombination = (ValueCombination) firstMatrixCombo.getSelectedItem();
        ValueCombination secondCombination = (ValueCombination) secondMatrixCombo.getSelectedItem();

        // number of tickets
        Progress.start(progressTicket, 5);

        // graph
        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        GraphModel graphModel = graphController.getModel();
        Graph graph = graphModel.getGraphVisible();
        //Graph graph = graphModel.getUndirectedGraphVisible();
        Node[] nodes = graph.getNodes().toArray();

        // first matrix axis
        List<Node> firstHorizontal = new ArrayList<Node>();
        List<Node> firstVertical = new ArrayList<Node>();
        List<Node> secondHorizontal = new ArrayList<Node>();
        List<Node> secondVertical = new ArrayList<Node>();
        for (Node n : nodes) {
            String nodeValue = Utils.getValue(n, (AttributeColumn) columns.getSelectedItem()).toString();
            // first matrix axis
            if (nodeValue.equals(firstCombination.getFirst())) {
                firstVertical.add(n);
            }
            if (nodeValue.equals(firstCombination.getSecond())) {
                firstHorizontal.add(n);
                secondVertical.add(n);
            }
            // second matrix axis
            /*
             * if (nodeValue.equals(secondCombination.getFirst())) {
             * secondVertical.add(n); }
             *
             */
            if (nodeValue.equals(secondCombination.getSecond())) {
                secondHorizontal.add(n);
            }
        }

        if (cancelled) {
            return;
        }
        Progress.progress(progressTicket);

        // first matrix
        Matrix firstMatrix = new Matrix(firstVertical.size(), firstHorizontal.size());
        for (int i = 0; i < firstVertical.size(); i++) {
            Set<Node> intersection = new HashSet<Node>(Arrays.asList(graph.getNeighbors(firstVertical.get(i)).toArray()));
            if (intersection != null && intersection.size() > 0) {
                try {
                    intersection.retainAll(firstHorizontal);
                    for (Node neighbour : intersection) {
                        firstMatrix.set(i, firstHorizontal.indexOf(neighbour), 1);
                    }
                } catch (UnsupportedOperationException ex) {
                    System.out.println("exception");
                    // TODO - exception handler
                }
            }
        }
        // second matrix
        Matrix secondMatrix = new Matrix(secondVertical.size(), secondHorizontal.size());
        for (int i = 0; i < secondVertical.size(); i++) {

            Set<Node> intersection = new HashSet<Node>(Arrays.asList(graph.getNeighbors(secondVertical.get(i)).toArray()));
            if (intersection != null && intersection.size() > 0) {
                try {
                    intersection.retainAll(secondHorizontal);
                    for (Node neighbour : intersection) {
                        secondMatrix.set(i, secondHorizontal.indexOf(neighbour), 1);
                    }
                } catch (UnsupportedOperationException ex) {
                    System.out.println("exception");
                    // TODO - exception handler
                }
            }
        }
        if (cancelled) {
            return;
        }
        Progress.progress(progressTicket, "Multiplication");
        //System.out.println(firstVertical);
        //System.out.println(firstHorizontal);
        //System.out.println(firstMatrix);
        //System.out.println("");
        //System.out.println(secondVertical);
        //System.out.println(secondHorizontal);
        //System.out.println(secondMatrix);

        //long start = System.currentTimeMillis();
        //Matrix result = firstMatrix.times(secondMatrix);
        //long end = System.currentTimeMillis();
        //System.out.println("start "+start+" end "+end+" = "+ (end-start));
        
        //start = System.currentTimeMillis();
        Matrix result = firstMatrix.timesParallel(secondMatrix);
        //end = System.currentTimeMillis();
        //System.out.println("start "+start+" end "+end+" = "+ (end-start));
        
        //System.out.println(result);
        if (cancelled) {
            return;
        }
        Progress.progress(progressTicket, "Removing nodes/edges");


        if (removeNodes) {
            for (Node n : firstHorizontal) {
                graph.removeNode(n);
            }
        } else {
            if (removeEdges) {
                for (int i = 0; i < firstMatrix.getM(); i++) {
                    for (int j = 0; j < firstMatrix.getN(); j++) {
                        if (graph.contains(firstVertical.get(i)) && graph.contains(firstHorizontal.get(j)) && graph.getEdge(firstVertical.get(i), firstHorizontal.get(j)) != null && firstMatrix.get(i, j) > 0) {
                            graph.removeEdge(graph.getEdge(firstVertical.get(i), firstHorizontal.get(j)));
                        }
                    }
                }

                for (int i = 0; i < secondMatrix.getM(); i++) {
                    for (int j = 0; j < secondMatrix.getN(); j++) {
                        if (graph.contains(secondVertical.get(i)) && graph.contains(secondHorizontal.get(j)) && graph.getEdge(secondVertical.get(i), secondHorizontal.get(j)) != null && secondMatrix.get(i, j) > 0) {
                            graph.removeEdge(graph.getEdge(secondVertical.get(i), secondHorizontal.get(j)));
                        }
                    }
                }
            }
        }

        if (cancelled) {
            return;
        }
        Progress.progress(progressTicket, "Creating new edges");

        Edge ee = null;
        for (int i = 0; i < result.getM(); i++) {
            for (int j = 0; j < result.getN(); j++) {
                if (graph.contains(firstVertical.get(i)) && graph.contains(secondHorizontal.get(j)) && graph.getEdge(firstVertical.get(i), secondHorizontal.get(j)) == null && result.get(i, j) > 0) {
                    ee = graphModel.factory().newEdge(firstVertical.get(i), secondHorizontal.get(j), (float) result.get(i, j), false);
                    if (!ee.isSelfLoop()) {
                        ee.getEdgeData().setLabel(firstCombination.getFirst() + "-" + secondCombination.getSecond());
                        graph.addEdge(ee);
                    }
                }
            }
        }

        if (columns.getItemCount() > 0 && (removeEdges || removeNodes)) {
            firstMatrixCombo.removeAllItems();
            secondMatrixCombo.removeAllItems();
            columns.setSelectedIndex(columns.getSelectedIndex());
        }
        Progress.finish(progressTicket);
    }

    @Override
    public boolean cancel() {
        cancelled = true;
        return true;
    }

    @Override
    public void setProgressTicket(ProgressTicket pt) {
        this.progressTicket = pt;
    }
}
