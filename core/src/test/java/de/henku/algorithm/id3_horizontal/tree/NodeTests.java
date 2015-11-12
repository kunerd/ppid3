/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Hendrik Kunert
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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