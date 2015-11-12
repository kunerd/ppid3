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
import de.henku.jpaillier.PublicKey;

import java.math.BigInteger;

/**
 * A class for securely computing the sum or product of the input from two or
 * more parties.
 * <p>
 * As described above, this class is use for both, the secure addition and
 * multiplication. Its usage for either of these two methods is described below.
 * <p>
 * <h3> Secure Addition Flow</h3>
 * The computation of the sum of multiple parties requires one SecureComputationMaster
 * and at least one {@link SecureAddition} instance. The main flow to compute an
 * so called output share at each party is described as following:
 * <p>
 * <ol>
 * <li> startEncryptedComputation is called </li>
 * <li> forwardStep is called with the result of startEncryptedComputation </li>
 * <li> backwardStep is called with the result of forwardStep </li>
 * <li> decryptAndSetOutputShare is called with the result of forwardStep </li>
 * </ol>
 * <pre>
 * +----------------------------+      +-------------------+
 * |                         (1)|----->|(2)                |-----> ...
 * | SecureComputationMaster    |      |    SecureAddition |
 * |                         (4)|<-----|(3)                |<----- ...
 * +----------------------------+      +-------------------+
 * </pre>
 * After {@code decryptAndSetOutputShare} is called, every involved party has
 * generated a so called output share. These output shares do not contain any
 * private information. The sum is computed at the end as following:
 * <pre>
 * OS -> output share
 * N  -> involved number of parties
 * n  -> n from {@link PublicKey}
 * ----------------------------------------------------------------------------
 * sum = (masterOS * additionOS_1 * ... * additionOS_N) mod n
 * </pre>
 * <p>
 * <h3> Secure Multiplication Flow</h3>
 * The computation of the product of multiple parties requires one SecureComputationMaster
 * and at least one {@link SecureMultiplication} instance. The main flow to compute an
 * so called output share at each party is described as following:
 * <p>
 * <ol>
 * <li> startEncryptedComputation is called </li>
 * <li> forwardStep is called with the result of startEncryptedComputation </li>
 * <li> backwardStep is called with the result of forwardStep </li>
 * <li> decryptAndSetOutputShare is called with the result of forwardStep </li>
 * </ol>
 * <pre>
 * +----------------------------+      +-------------------------+
 * |                         (1)|----->|(2)                      |-----> ...
 * | SecureComputationMaster    |      |    SecureMultiplication |
 * |                         (4)|<-----|(3)                      |<----- ...
 * +----------------------------+      +-------------------------+
 * </pre>
 * After {@code decryptAndSetOutputShare} is called, every involved party has
 * generated a so called output share. These output shares do not contain any
 * private information. The sum is computed at the end as following:
 * <pre>
 * OS -> output share
 * N  -> involved number of parties
 * n  -> n from {@link PublicKey}
 * ----------------------------------------------------------------------------
 * sum = (masterOS + multiplicationOS_1 + ... + multiplicationOS_N) mod n
 * </pre>
 *
 * @see SecureAddition
 * @see SecureMultiplication
 */
public class SecureComputationMaster extends AbstractSecureComputation {

    private final KeyPair keyPair;

    /**
     * Creates a new instance with the input, that has to be kept private and
     * the key pair for the encryption and decryption.
     *
     * @param privateInput The input that should be kept private.
     * @param keyPair      The key pair for the encryption and decryption.
     */
    public SecureComputationMaster(long privateInput, KeyPair keyPair) {
        super(BigInteger.valueOf(privateInput));
        this.keyPair = keyPair;
    }

    /**
     * Creates a new instance with the input, that has to be kept private and
     * the key pair for the encryption and decryption.
     *
     * @param privateInput The input that should be kept private.
     * @param keyPair      The key pair for the encryption and decryption.
     */
    public SecureComputationMaster(BigInteger privateInput, KeyPair keyPair) {
        super(privateInput);
        this.keyPair = keyPair;
    }

    /**
     * Encrypts the private input using the public key.
     *
     * @return The encrypted private input.
     */
    public BigInteger startEncryptedComputation() {
        PublicKey publicKey = keyPair.getPublicKey();
        return publicKey.encrypt(privateInput);
    }

    /**
     * Decrypts the passed in parameter using the key pair and saves the result
     * as {@code outputShare} of this instance.
     *
     * @param ciphertext The intermediate result from a previous party.
     */
    public void decryptAndSetOutputShare(BigInteger ciphertext) {
        outputShare = keyPair.decrypt(ciphertext);
    }

    @Override
    public PublicKey getPublicKey() {
        return keyPair.getPublicKey();
    }

}