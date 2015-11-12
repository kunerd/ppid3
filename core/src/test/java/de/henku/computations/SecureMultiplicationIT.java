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

package de.henku.computations;

import de.henku.jpaillier.KeyPair;
import de.henku.jpaillier.KeyPairBuilder;
import de.henku.jpaillier.PublicKey;
import de.henku.utils.IntegrationTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;

@Category(IntegrationTest.class)
@RunWith(Parameterized.class)
public class SecureMultiplicationIT {

    private KeyPair keyPair;
    private PublicKey publicKey;
    private List<Long> inputValues;
    private long expectedResult;

    @Before
    public void init() {
        KeyPairBuilder factory = new KeyPairBuilder();
        this.keyPair = factory.generateKeyPair();
        this.publicKey = keyPair.getPublicKey();
    }

    @Parameterized.Parameters
    public static Collection<Object[]> partyInputs() {
        LinkedList<Long> twoParties = new LinkedList<Long>();
        twoParties.add(Long.valueOf(10));
        twoParties.add(Long.valueOf(5));

        LinkedList<Long> fourParties = new LinkedList<Long>();
        fourParties.add(Long.valueOf(10));
        fourParties.add(Long.valueOf(5));
        fourParties.add(Long.valueOf(12));
        fourParties.add(Long.valueOf(4));

        return Arrays.asList(new Object[][]{
                createTestParameter(twoParties),
                createTestParameter(fourParties)
        });
    }

    public SecureMultiplicationIT(List<Long> inputValues, long expectedResult) {
        this.inputValues = inputValues;
        this.expectedResult = expectedResult;
    }

    @Test
    public void multiplicationChecker() {
        LinkedList<SecureMultiplication> slaves = new LinkedList<SecureMultiplication>();
        BigInteger masterInput = BigInteger.valueOf(inputValues.get(0));

        SecureComputationMaster master = new SecureComputationMaster(masterInput, keyPair);

        BigInteger helper = master.startEncryptedComputation();

        // forward steps
        for (Long input : inputValues.subList(1, inputValues.size())) {
            SecureMultiplication slave = new SecureMultiplication(BigInteger.valueOf(input),
                    publicKey);

            slaves.add(slave);
            helper = slave.forwardStep(helper);
        }

        // forward steps
        for (SecureMultiplication slave : slaves) {
            helper = slave.backwardStep(helper);
        }

        master.decryptAndSetOutputShare(helper);

        BigInteger actual = master.getOutputShare();

        for (SecureMultiplication slave : slaves) {
            BigInteger outputShare = slave.getOutputShare();
            BigInteger n = publicKey.getN();

            actual = actual.add(outputShare).mod(n);
        }

        assertEquals(expectedResult, actual.longValue());
    }

    private static long multHelper(List<Long> list) {
        long prod = 1;

        for (Long input : list) {
            prod *= input;
        }

        return prod;
    }

    private static Object[] createTestParameter(List<Long> list) {

        return new Object[]{list, multHelper(list)};
    }
}
