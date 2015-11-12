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

import de.henku.computations.SecureComputationMaster;
import de.henku.jpaillier.KeyPair;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SecureSquareDivisionMaster {

    private final static BigInteger TWO = BigInteger.valueOf(2);

    private final KeyPair keyPair;

    private final FactoryHelper factoryHelper;
    // TODO rename
    private SecureComputationMaster z;
    private SecureComputationMaster w;

    private Object classValue = null;
//    private int classValueCount = 0;

    private final ConcurrentHashMap<Object, SecureComputationMaster> multiplications =
            new ConcurrentHashMap<>();

    SecureSquareDivisionMaster(KeyPair keyPair, FactoryHelper factoryHelper) {
        this.keyPair = keyPair;
        this.factoryHelper = factoryHelper;
    }

    public SecureSquareDivisionMaster(KeyPair keyPair) {
        this(keyPair, new FactoryHelper());
    }

    public List<MultiplicationResult> createMultiplications(Map<Object, Long> counts) {
        List<MultiplicationResult> results = new ArrayList<>();

        for (Object cv : counts.keySet()) {
            long count = counts.get(cv);

            if ( count != 0l ) {
                classValue = cv;
//                classValueCount++;
            }

            SecureComputationMaster m = factoryHelper.finalize(count, keyPair);
            multiplications.put(cv, m);

            BigInteger result = m.startEncryptedComputation();

            results.add(new MultiplicationResult(cv, result));
        }

        return results;
    }

    public AdditionResults handleMultiplicationBackwardStep(List<MultiplicationResult> results) {

        for (MultiplicationResult r : results) {
            Object classValue = r.getClassValue();

            SecureComputationMaster m = multiplications.get(classValue);
            m.decryptAndSetOutputShare(r.getResult());
        }

        z = factoryHelper.finalize(calculateZ(), keyPair);
        w = factoryHelper.finalize(calculateW(), keyPair);

        return new AdditionResults(z.startEncryptedComputation(), w.startEncryptedComputation());
    }

    public void handleAdditionBackwardStep(AdditionResults results) {
        z.decryptAndSetOutputShare(results.getResultForZ());
        w.decryptAndSetOutputShare(results.getResultForW());
    }

    private BigInteger calculateZ() {
        BigInteger result = BigInteger.ZERO;

        for (SecureComputationMaster m : multiplications.values()) {
            BigInteger pi = m.getPrivateInput();
            BigInteger os = m.getOutputShare();

            result = result.add(pi.pow(2));
            result = result.add(os.multiply(TWO));
        }

        return result;
    }

    private BigInteger calculateW() {
        BigInteger result = BigInteger.ZERO;

        for (SecureComputationMaster m : multiplications.values()) {
            BigInteger privateInput = m.getPrivateInput();

            result = result.add(privateInput);
        }

        return result;
    }

    public GiniGainResult computeResult(List<SquareDivisionResult> outputShares) {
        BigInteger zResult = z.getOutputShare();
        BigInteger wResult = w.getOutputShare();

        BigInteger n = keyPair.getPublicKey().getN();

        Object cv = classValue;
//        if(classValueCount == 1) {
//            cv = classValue;
//        }

        for (SquareDivisionResult os : outputShares) {
            zResult = zResult.multiply(os.getOutputShareZ()).mod(n);
            wResult = wResult.multiply(os.getOutputShareW()).mod(n);

            if(os.getClassValue() != null) {
                cv = os.getClassValue();
            }
        }

        double r;
        if (wResult.equals(BigInteger.ZERO)) {
            r = 0d;
        } else {
            r = zResult.doubleValue() / wResult.doubleValue();
        }

        return new GiniGainResult(cv, r);
    }

    static class FactoryHelper {
        SecureComputationMaster finalize(long input, KeyPair kp) {
            return new SecureComputationMaster(input, kp);
        }

        SecureComputationMaster finalize(BigInteger input, KeyPair kp) {
            return new SecureComputationMaster(input, kp);
        }
    }
}
