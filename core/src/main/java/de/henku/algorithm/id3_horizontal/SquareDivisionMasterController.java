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

package de.henku.algorithm.id3_horizontal;

import de.henku.algorithm.id3_horizontal.communication.NodeValuePair;
import de.henku.algorithm.id3_horizontal.communication.SquareDivisionPojo;
import de.henku.algorithm.id3_horizontal.communication.SquareDivisionReceiverAdapter;
import de.henku.algorithm.id3_horizontal.communication.SquareDivisionSenderAdapter;
import de.henku.jpaillier.KeyPair;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class SquareDivisionMasterController implements
        SquareDivisionReceiverAdapter {

    private AtomicLong idCounter = new AtomicLong();
    private final SquareDivisionSenderAdapter sender;

    private ConcurrentHashMap<Long, SecureSquareDivisionMaster> divisions = new ConcurrentHashMap<>();

    private ConcurrentHashMap<Long, CompletableFuture<GiniGainResult>> futures = new ConcurrentHashMap<>();

    private KeyPair keyPair;

    private final DataLayer dataLayer;
    private final FactoryHelper squareDivisionFactory;

    SquareDivisionMasterController(DataLayer dataLayer,
                                   SquareDivisionSenderAdapter sender, KeyPair keyPair,
                                   FactoryHelper helper) {

        this.dataLayer = dataLayer;
        this.sender = sender;
        this.keyPair = keyPair;
        this.squareDivisionFactory = helper;
    }

    public SquareDivisionMasterController(DataLayer dataLayer,
                                          SquareDivisionSenderAdapter sender, KeyPair keyPair) {
        this(dataLayer, sender, keyPair, new FactoryHelper());
    }

    public CompletableFuture<GiniGainResult> compute(String attrName, String attrValue,
                                             List<NodeValuePair> path) {

        long id = idCounter.getAndIncrement();

        SecureSquareDivisionMaster d = squareDivisionFactory.finalize(keyPair);
        divisions.put(id, d);

        Map<Object, Long> cpcv = dataLayer.countPerClassValue(path,
                attrName, attrValue);

        List<MultiplicationResult> results = d.createMultiplications(cpcv);

        SquareDivisionPojo pojo = new SquareDivisionPojo(id, attrName,
                attrValue, path, results);

        CompletableFuture<GiniGainResult> f = new CompletableFuture<>();
        futures.put(id, f);

        sender.handleMultiplicationForwardStep(pojo);

        return f;
    }

    public void handleMultiplicationBackwardStep(SquareDivisionPojo data) {
        long id = data.getId();

        SecureSquareDivisionMaster sD = divisions.get(id);
        AdditionResults r = sD.handleMultiplicationBackwardStep(data
                .getResults());

        sender.handleAdditionForwardStep(id, r);
    }

    @Override
    public void handleAdditionBackwardStep(
            long squareID,
            AdditionResults results) {
        SecureSquareDivisionMaster d = divisions.get(squareID);
        d.handleAdditionBackwardStep(results);

        sender.collectOutputShares(squareID);
    }

    @Override
    public void handleCollectOutputShares(long squareID,
                                          List<SquareDivisionResult> outputShares) {
        SecureSquareDivisionMaster d = divisions.get(squareID);

        GiniGainResult r = d.computeResult(outputShares);

        CompletableFuture<GiniGainResult> f = futures.get(squareID);
        f.complete(r);
    }

    static class FactoryHelper {
        SecureSquareDivisionMaster finalize(KeyPair kp) {
            return new SecureSquareDivisionMaster(kp);
        }
    }
}
