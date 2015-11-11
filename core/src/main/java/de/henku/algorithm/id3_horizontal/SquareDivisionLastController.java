package de.henku.algorithm.id3_horizontal;

import de.henku.algorithm.id3_horizontal.communication.NodeValuePair;
import de.henku.algorithm.id3_horizontal.communication.SquareDivisionPojo;
import de.henku.algorithm.id3_horizontal.communication.SquareDivisionReceiverAdapter;
import de.henku.algorithm.id3_horizontal.communication.SquareDivisionSenderAdapter;
import de.henku.jpaillier.PublicKey;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SquareDivisionLastController implements SquareDivisionSenderAdapter {

    private final DataLayer dataLayer;
    private final PublicKey publicKey;

    private SquareDivisionReceiverAdapter receiver;

    private ConcurrentHashMap<Long, SecureSquareDivisionSlave> divisions =
            new ConcurrentHashMap<>();

    private final FactoryHelper factoryHelper;

    SquareDivisionLastController(
            DataLayer dataLayer,
            PublicKey publicKey,
            FactoryHelper factoryHelper) {
        this.dataLayer = dataLayer;
        this.publicKey = publicKey;
        this.factoryHelper = factoryHelper;
    }

    public SquareDivisionLastController(DataLayer dataLayer, PublicKey publicKey) {
        this(dataLayer, publicKey, new FactoryHelper());
    }

    @Override
    public void handleMultiplicationForwardStep(SquareDivisionPojo pojo) {
        long id = pojo.getId();

        SecureSquareDivisionSlave d = factoryHelper.finalize(publicKey);
        divisions.put(id, d);

        String attrName = pojo.getAttrName();
        String attrValue = pojo.getAttrValue();
        List<NodeValuePair> path = pojo.getPath();
        Map<Object, Long> countPerValue = dataLayer.countPerClassValue(path, attrName, attrValue);

        List<MultiplicationResult> fr = d.handleMultiplicationForwardStep(countPerValue, pojo.getResults());
        List<MultiplicationResult> br = d.handleMultiplicationBackwardStep(fr);

        SquareDivisionPojo newPojo = new SquareDivisionPojo(id, attrName, attrValue, path, br);
        receiver.handleMultiplicationBackwardStep(newPojo);
    }

    @Override
    public void handleAdditionForwardStep(long squareID, AdditionResults pojo) {
        SecureSquareDivisionSlave d = divisions.get(squareID);

        AdditionResults fr = d.handleAdditionForwardStep(pojo);
        AdditionResults br = d.handleAdditionBackwardStep(fr);

        receiver.handleAdditionBackwardStep(squareID, br);
    }

    @Override
    public void collectOutputShares(long squareID) {
        SecureSquareDivisionSlave d = divisions.get(squareID);

        List<AdditionResults> outputShares = new ArrayList<>();
        outputShares.add(d.getAdditionOutputShares());

        receiver.handleCollectOutputShares(squareID, outputShares);
    }

    public void setReceiver(SquareDivisionReceiverAdapter receiver) {
        this.receiver = receiver;
    }

    static class FactoryHelper {
        SecureSquareDivisionSlave finalize(PublicKey pk) {
            return new SecureSquareDivisionSlave(pk);
        }
    }
}
