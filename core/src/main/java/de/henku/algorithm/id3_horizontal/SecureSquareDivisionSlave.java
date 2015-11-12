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

import de.henku.computations.SecureAddition;
import de.henku.computations.SecureMultiplication;
import de.henku.jpaillier.PublicKey;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SecureSquareDivisionSlave {

    private final static BigInteger TWO = BigInteger.valueOf(2);

    private final PublicKey publicKey;

    private SecureAddition z;
    private SecureAddition w;

    private final List<BigInteger> inputs = new ArrayList<>();
    private final List<BigInteger> multiplicationOutputShares = new ArrayList<>();

    private final ConcurrentHashMap<Object, SecureMultiplication> multiplications =
            new ConcurrentHashMap<>();

    private Object classValue = null;
    private int classValueCount = 0;

    public SecureSquareDivisionSlave(PublicKey publicKey) {
        this.publicKey = publicKey;
    }

    public List<MultiplicationResult> handleMultiplicationForwardStep(Map<Object, Long> counts, List<MultiplicationResult> prevResults) {
        List<MultiplicationResult> results = new ArrayList<>();

        for (MultiplicationResult r : prevResults) {
            Object cv = r.getClassValue();
            long count = counts.get(cv);

            if ( count != 0l ) {
                classValue = cv;
                classValueCount++;
            }

            inputs.add(BigInteger.valueOf(count));

            SecureMultiplication m = new SecureMultiplication(
                    BigInteger.valueOf(count), publicKey);

            multiplications.put(cv, m);

            BigInteger result = m.forwardStep(r.getResult());

            results.add(new MultiplicationResult(cv, result));
        }

        return results;
    }

    public List<MultiplicationResult> handleMultiplicationBackwardStep(List<MultiplicationResult> prevResults) {

        List<MultiplicationResult> results = new ArrayList<>();

        for (MultiplicationResult r : prevResults) {
            Object classValue = r.getClassValue();

            SecureMultiplication m = multiplications.get(classValue);
            BigInteger newResult = m.backwardStep(r.getResult());

            results.add(new MultiplicationResult(classValue, newResult));

            multiplicationOutputShares.add(m.getOutputShare());
        }

        return results;
    }

    public AdditionResults handleAdditionForwardStep(AdditionResults data) {
        z = new SecureAddition(calculateZ(), publicKey);
        w = new SecureAddition(calculateW(), publicKey);

        BigInteger zFR = z.forwardStep(data.getResultForZ());
        BigInteger wFR = w.forwardStep(data.getResultForW());

        return new AdditionResults(zFR, wFR);
    }

    public AdditionResults handleAdditionBackwardStep(AdditionResults data) {
        BigInteger zBR = z.backwardStep(data.getResultForZ());
        BigInteger wBR = w.backwardStep(data.getResultForW());

        return new AdditionResults(zBR, wBR);
    }

    private BigInteger calculateZ() {
        BigInteger result = BigInteger.ZERO;

        for (BigInteger input : inputs) {
            result = result.add(input.pow(2));
        }

        for (BigInteger os : multiplicationOutputShares) {
            result = result.add(os.multiply(TWO));
        }

        return result;
    }

    private BigInteger calculateW() {
        BigInteger result = BigInteger.ZERO;

        for (BigInteger input : inputs) {
            result = result.add(input);
        }

        return result;
    }

    public SquareDivisionResult getAdditionOutputShares() {
//        if(classValueCount == 1) {
            return new SquareDivisionResult(z.getOutputShare(), w.getOutputShare(), classValue);
//        }
//        return new SquareDivisionResult(z.getOutputShare(), w.getOutputShare(), null);
    }

}
