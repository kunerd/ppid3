package de.henku.algorithm.id3_horizontal.tree;

import org.junit.Before;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class NodeTests {

    private static final String LABEL_1 = "node 1";
    private static final String LABEL_2 = "node 2";
    private static final String LABEL_3 = "node 3";

    private static final String EDGE_1 = "edge 1";
    private static final String EDGE_2 = "edge 2";

    private ID3Node subject;

    @Before
    public void beforeEach() {
        subject = new ID3Node(LABEL_1);
    }

    @Test
    public void hasLabel() {
        assertEquals(LABEL_1, subject.getLabel());
    }

    @Test
    public void afterAdd_containsEdge() {
        subject.add(EDGE_1, new ID3Node(LABEL_2));

        assertTrue(subject.getEdges().contains(EDGE_1));
    }

    @Test
    public void afterAdd_containsNode() {
        ID3Node child = new ID3Node(LABEL_2);
        subject.add(EDGE_1, child);

        assertTrue(subject.getChildren().contains(child));
    }

    @Test
    public void afterAdd_containsNodeForEdge() {
        ID3Node child = new ID3Node(LABEL_2);
        subject.add(EDGE_1, child);

        assertEquals(child, subject.getChild(EDGE_1));
    }

    @Test
    public void afterMultipleAdds_containsMultipleEdges() {
        subject.add(EDGE_1, new ID3Node(LABEL_1));
        subject.add(EDGE_2, new ID3Node(LABEL_2));

        Set<Object> edges = subject.getEdges();

        assertTrue(edges.contains(EDGE_1));
        assertTrue(edges.contains(EDGE_2));
    }

    @Test
    public void afterMultipleAdds_containsMultipleNodesForEdges() {
        ID3Node child1 = new ID3Node(LABEL_1);
        ID3Node child2 = new ID3Node(LABEL_2);

        subject.add(EDGE_1, child1);
        subject.add(EDGE_2, child2);

        assertEquals(child1, subject.getChild(EDGE_1));
        assertEquals(child2, subject.getChild(EDGE_2));
    }

    @Test
    public void afterMultipleAdds_replacePreviouseNode() {
        ID3Node child = new ID3Node(LABEL_3);

        subject.add(EDGE_1, new ID3Node(LABEL_3));
        subject.add(EDGE_1, child);

        assertTrue(subject.getChildren().contains(child));
    }
}