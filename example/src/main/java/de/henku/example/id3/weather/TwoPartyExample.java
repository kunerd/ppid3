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

package de.henku.example.id3.weather;

import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.swing.mxGraphComponent;
import de.henku.algorithm.id3_horizontal.Attribute;
import de.henku.algorithm.id3_horizontal.DataLayer;
import de.henku.algorithm.id3_horizontal.SecureID3;
import de.henku.algorithm.id3_horizontal.SquareDivisionLastController;
import de.henku.algorithm.id3_horizontal.communication.NodeValuePair;
import de.henku.algorithm.id3_horizontal.tree.ID3Node;
import de.henku.example.id3.utils.ListAttributeBuilder;
import de.henku.example.id3.utils.ListDataLayer;
import de.henku.jpaillier.KeyPair;
import de.henku.jpaillier.KeyPairBuilder;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.ext.JGraphXAdapter;
import org.jgrapht.graph.SimpleGraph;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TwoPartyExample {

    public static void main(String[] args) {
        List<ListRow> transactions = loadData();
        List<Attribute> attributes = extractAttributes(transactions);
        // extract class attribute
        Attribute playBall = new ListAttributeBuilder<ListRow>("playBall").from_transactions(transactions);

        System.out.println("### show transaction of parties 1 and 2 ###");
        int half = transactions.size() / 2;
        List<ListRow> transactions1 = transactions.subList(0, half);
        List<ListRow> transactions2 = transactions.subList(half, transactions.size());
        System.out.println(transactions1);
        System.out.println(transactions2);
        System.out.println();

        KeyPair keyPair = new KeyPairBuilder().bits(512)
                .generateKeyPair();

        DataLayer dataLayerSlave = new ListDataLayer<>(transactions2, playBall);
        SquareDivisionLastController slave =
                new SquareDivisionLastController(dataLayerSlave, keyPair.getPublicKey());

        DataLayer dataLayerMaster = new ListDataLayer<>(transactions1, playBall);

        SecureID3 id3 = new SecureID3(dataLayerMaster, slave, keyPair);
        slave.setReceiver(id3.getController());

        List<NodeValuePair> path = new ArrayList<>();
        ID3Node tree = id3.run(attributes, path);

        System.out.println(tree);
        visualizeTree(tree);
    }

    private static void visualizeTree(ID3Node tree) {
        UndirectedGraph<String, String> g =
                new SimpleGraph<>(String.class);

        convertID3NodeToJGraph(g, tree);

        JFrame frame = new JFrame();
        frame.setTitle("JGraphT Adapter to JGraph Demo");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        JGraphXAdapter<String, String> jgxAdapter = new JGraphXAdapter<>(g);

        frame.getContentPane().add(new mxGraphComponent(jgxAdapter));

        mxHierarchicalLayout layout = new mxHierarchicalLayout(jgxAdapter);
        layout.execute(jgxAdapter.getDefaultParent());

        frame.pack();
        frame.setVisible(true);
    }

    private static List<Attribute> extractAttributes(List<ListRow> transactions) {
        Attribute outlook = new ListAttributeBuilder<ListRow>("outlook").from_transactions(transactions);
        Attribute temperature = new ListAttributeBuilder<ListRow>("temperature").from_transactions(transactions);
        Attribute humidity = new ListAttributeBuilder<ListRow>("humidity").from_transactions(transactions);
        Attribute wind = new ListAttributeBuilder<ListRow>("wind").from_transactions(transactions);

        System.out.println("### show attribute with values ###");
        System.out.println(outlook);
        System.out.println(temperature);
        System.out.println(humidity);
        System.out.println(wind);
        System.out.println();

        System.out.println("### show attributes list ###");
        List<Attribute> attributes = Arrays.asList(outlook, temperature, humidity, wind);
        System.out.println(attributes);
        System.out.println();
        return attributes;
    }

    private static List<ListRow> loadData() {
        List<ListRow> transactions = new ArrayList<>();

        transactions.add(new ListRow("D1", "Sunny", "Hot", "High", "Weak", "No"));
        transactions.add(new ListRow("D2", "Sunny", "Hot", "High", "Strong", "No"));
        transactions.add(new ListRow("D3", "Overcast", "Hot", "High", "Weak", "Yes"));
        transactions.add(new ListRow("D4", "Rain", "Mild", "High", "Weak", "Yes"));
        transactions.add(new ListRow("D5", "Rain", "Cool", "Normal", "Weak", "Yes"));
        transactions.add(new ListRow("D6", "Rain", "Cool", "Normal", "Strong", "No"));
        transactions.add(new ListRow("D7", "Overcast", "Cool", "Normal", "Strong", "Yes"));
        transactions.add(new ListRow("D8", "Sunny", "Mild", "High", "Weak", "No"));
        transactions.add(new ListRow("D9", "Sunny", "Cool", "Normal", "Weak", "Yes"));
        transactions.add(new ListRow("D10", "Rain", "Mild", "Normal", "Weak", "Yes"));
        transactions.add(new ListRow("D11", "Sunny", "Mild", "Normal", "Strong", "Yes"));
        transactions.add(new ListRow("D12", "Overcast", "Mild", "High", "Strong", "Yes"));
        transactions.add(new ListRow("D13", "Overcast", "Hot", "Normal", "Weak", "Yes"));
        transactions.add(new ListRow("D14", "Rain", "Mild", "High", "Strong", "No"));
        return transactions;
    }

    private static String convertID3NodeToJGraph(UndirectedGraph<String, String> g, ID3Node tree) {
        g.addVertex(tree.getLabel().toString());

        for (Object edge : tree.getEdges()) {
            g.addEdge(tree.getLabel().toString(), convertID3NodeToJGraph(g, tree.getChild(edge)), edge.toString());
        }

        return tree.getLabel().toString();
    }

}
