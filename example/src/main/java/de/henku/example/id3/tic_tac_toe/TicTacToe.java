package de.henku.example.id3.tic_tac_toe;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.opencsv.CSVReader;
import de.henku.example.id3.utils.ListAttributeBuilder;
import de.henku.example.id3.utils.ListDataLayer;

import de.henku.algorithm.id3_horizontal.Attribute;
import de.henku.algorithm.id3_horizontal.DataLayer;
import de.henku.algorithm.id3_horizontal.SecureID3;
import de.henku.algorithm.id3_horizontal.SquareDivisionLastController;
import de.henku.algorithm.id3_horizontal.communication.NodeValuePair;
import de.henku.algorithm.id3_horizontal.communication.SquareDivisionSenderAdapter;
import de.henku.algorithm.id3_horizontal.tree.ID3Node;
import de.henku.jpaillier.KeyPair;
import de.henku.jpaillier.KeyPairBuilder;

public class TicTacToe {

    public static void main(String[] args) throws IOException {
        List<TTTListRow> transactions = loadData();
        Collections.shuffle(transactions);

//        transactions = transactions.subList(0, 84);

        // load attributes
        List<Attribute> attributes = extractAttributes(transactions);

        // load class attribute
        Attribute playBall = new ListAttributeBuilder("result").from_transactions(transactions);

        int half = transactions.size() / 2;
        List<TTTListRow> transactions1 = transactions.subList(0, half);
        List<TTTListRow> transactions2 = transactions.subList(half, transactions.size());

        KeyPair keyPair = new KeyPairBuilder().bits(128)
                .generateKeyPair();

        DataLayer dataLayerSlave = new ListDataLayer(transactions2, playBall);
        SquareDivisionLastController slave =
                new SquareDivisionLastController(dataLayerSlave, keyPair.getPublicKey());

        DataLayer dataLayerMaster = new ListDataLayer(transactions1, playBall);
        List<SquareDivisionSenderAdapter> mSenders = new ArrayList<SquareDivisionSenderAdapter>();
        mSenders.add(slave);

        SecureID3 id3 = new SecureID3(dataLayerMaster, slave, keyPair);
        slave.setReceiver(id3.getController());

        List<NodeValuePair> path = new ArrayList<NodeValuePair>();
        ID3Node tree = id3.run(attributes, path);

        // show resulting tree
        System.out.println(tree);
    }

    private static List<Attribute> extractAttributes(List<TTTListRow> transactions) {
        Attribute tl = new ListAttributeBuilder("tl").from_transactions(transactions);
        Attribute tm = new ListAttributeBuilder("tm").from_transactions(transactions);
        Attribute tr = new ListAttributeBuilder("tr").from_transactions(transactions);
        Attribute ml = new ListAttributeBuilder("ml").from_transactions(transactions);
        Attribute mm = new ListAttributeBuilder("mm").from_transactions(transactions);
        Attribute mr = new ListAttributeBuilder("mr").from_transactions(transactions);
        Attribute bl = new ListAttributeBuilder("bl").from_transactions(transactions);
        Attribute bm = new ListAttributeBuilder("bm").from_transactions(transactions);
        Attribute br = new ListAttributeBuilder("br").from_transactions(transactions);

        List<Attribute> attributes = Arrays.asList(
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

        return attributes;
    }

    private static List<TTTListRow> loadData() throws IOException {
        List<TTTListRow> transactions = new ArrayList<>();

        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        URL url = classloader.getResource("de/henku/example/id3/tic-tac-toe.data");
        File file = new File(url.getFile());
        CSVReader reader = new CSVReader(new FileReader(file));

        String [] nextLine;
        while ((nextLine = reader.readNext()) != null) {
            if(nextLine.length != 10) {
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
