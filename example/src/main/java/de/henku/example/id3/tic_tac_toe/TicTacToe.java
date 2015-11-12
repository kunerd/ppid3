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

package de.henku.example.id3.tic_tac_toe;

import com.opencsv.CSVReader;
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

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TicTacToe {

    public static final String DATASET_URL = "de/henku/example/id3/tic-tac-toe.data";

    public static void main(String[] args) throws IOException {
        List<TTTListRow> transactions = loadData();
        Collections.shuffle(transactions);

        // load attributes
        List<Attribute> attributes = extractAttributes(transactions);

        // load class attribute
        Attribute playBall = new ListAttributeBuilder<TTTListRow>("result").from_transactions(transactions);

        int half = transactions.size() / 2;
        List<TTTListRow> transactions1 = transactions.subList(0, half);
        List<TTTListRow> transactions2 = transactions.subList(half, transactions.size());

        KeyPair keyPair = new KeyPairBuilder().bits(128)
                .generateKeyPair();

        DataLayer dataLayerSlave = new ListDataLayer<>(transactions2, playBall);
        SquareDivisionLastController slave =
                new SquareDivisionLastController(dataLayerSlave, keyPair.getPublicKey());

        DataLayer dataLayerMaster = new ListDataLayer<>(transactions1, playBall);

        SecureID3 id3 = new SecureID3(dataLayerMaster, slave, keyPair);
        slave.setReceiver(id3.getController());

        List<NodeValuePair> path = new ArrayList<>();
        ID3Node tree = id3.run(attributes, path);

        // show resulting tree
        System.out.println(tree);
    }

    private static List<Attribute> extractAttributes(List<TTTListRow> transactions) {
        Attribute tl = new ListAttributeBuilder<TTTListRow>("tl").from_transactions(transactions);
        Attribute tm = new ListAttributeBuilder<TTTListRow>("tm").from_transactions(transactions);
        Attribute tr = new ListAttributeBuilder<TTTListRow>("tr").from_transactions(transactions);
        Attribute ml = new ListAttributeBuilder<TTTListRow>("ml").from_transactions(transactions);
        Attribute mm = new ListAttributeBuilder<TTTListRow>("mm").from_transactions(transactions);
        Attribute mr = new ListAttributeBuilder<TTTListRow>("mr").from_transactions(transactions);
        Attribute bl = new ListAttributeBuilder<TTTListRow>("bl").from_transactions(transactions);
        Attribute bm = new ListAttributeBuilder<TTTListRow>("bm").from_transactions(transactions);
        Attribute br = new ListAttributeBuilder<TTTListRow>("br").from_transactions(transactions);

        return Arrays.asList(
                tl,
                tm,
                tr,
                ml,
                mm,
                mr,
                bl,
                bm,
                br
        );
    }

    private static List<TTTListRow> loadData() throws IOException {
        List<TTTListRow> transactions = new ArrayList<>();

        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        URL url = classloader.getResource(DATASET_URL);
        assert url != null;
        File file = new File(url.getFile());
        CSVReader reader = new CSVReader(new FileReader(file));

        String[] nextLine;
        while ((nextLine = reader.readNext()) != null) {
            if (nextLine.length != 10) {
                throw new IOException("not enough rows");
            }

            transactions.add(new TTTListRow(
                    nextLine[0],
                    nextLine[1],
                    nextLine[2],
                    nextLine[3],
                    nextLine[4],
                    nextLine[5],
                    nextLine[6],
                    nextLine[7],
                    nextLine[8],
                    nextLine[9]
            ));
        }

        return transactions;
    }

}
