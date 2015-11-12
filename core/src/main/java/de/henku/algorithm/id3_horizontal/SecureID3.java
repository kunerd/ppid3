package de.henku.algorithm.id3_horizontal;

import de.henku.algorithm.id3_horizontal.communication.NodeValuePair;
import de.henku.algorithm.id3_horizontal.communication.SquareDivisionSenderAdapter;
import de.henku.algorithm.id3_horizontal.tree.ID3Node;
import de.henku.jpaillier.KeyPair;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class SecureID3 {

    private SquareDivisionMasterController controller;

    public SecureID3(
            DataLayer dataLayer,
            SquareDivisionSenderAdapter sender,
            KeyPair keyPair) {
        this.controller = new SquareDivisionMasterController(dataLayer, sender, keyPair);
    }

    public ID3Node run(List<Attribute> attributes, List<NodeValuePair> path) {

        double maxGain = 0;
        Attribute max = null;

        long classValueCount = 0;

        // FIXME: refactor class value assignment
        Object classValue = "empty";

        for (Attribute attribute : attributes) {
            GiniGainResult r = giniGain(attribute, path);
            double current = r.result;

            if (current == -1) {
                classValueCount++;
                classValue = r.classValue;

            } else {
                if (current > maxGain) {
                    max = attribute;
                    maxGain = current;
                }
            }
        }

        if (max == null || classValueCount == 1) {
            return new ID3Node(classValue.toString());
        }

        ID3Node n = new ID3Node(max.getName());
        for (String aV : max.getValues()) {

            List<Attribute> newAttributes = new ArrayList<>(attributes);
            newAttributes.remove(max);

            List<NodeValuePair> newPath = new ArrayList<>(path);
            newPath.add(new NodeValuePair(max.getName(), aV));

            if (!newAttributes.isEmpty()) {
                n.add(aV, run(newAttributes, newPath));
            }
        }

        return n;
    }

    private GiniGainResult giniGain(Attribute attribute, List<NodeValuePair> path) {
        double sum = 0;

        String attrName = attribute.getName();

        for (String attrValue : attribute.getValues()) {

            CompletableFuture<GiniGainResult> f = controller.compute(attrName, attrValue, path);

            try {
                GiniGainResult r = f.get();
                double gini = r.result;
                if (gini == 1) {
                    return new GiniGainResult(r.classValue, -1d);
                }
                sum += gini;
            } catch (InterruptedException | ExecutionException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return new GiniGainResult("", sum);
    }

    public SquareDivisionMasterController getController() {
        return controller;
    }
}
