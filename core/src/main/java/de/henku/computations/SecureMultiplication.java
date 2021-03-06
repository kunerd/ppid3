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

import de.henku.jpaillier.PublicKey;

import java.math.BigInteger;

/**
 * A class for securely computing the product of the input from two or more parties.
 * <p>
 * For detailed usage instructions see {@link SecureComputationMaster}.
 *
 * @see AbstractSecureComputationSlave
 */
public class SecureMultiplication extends AbstractSecureComputationSlave {

    /**
     * Creates a new instance with the input, that has to be kept private and
     * the public key for the encryption.
     * <p>
     * Transforms the {@code privateInput} into a {@link BigInteger} and passes
     * it together with the {@code publicKey} to the super constructor of
     * {@link AbstractSecureComputationSlave}.
     *
     * @param privateInput The input that should be kept private.
     * @param publicKey    The public key for the encryption.
     * @see AbstractSecureComputationSlave
     */
    public SecureMultiplication(long privateInput, PublicKey publicKey) {
        super(BigInteger.valueOf(privateInput), publicKey);
    }

    /**
     * Creates a new instance with the input, that has to be kept private and
     * the public key for the encryption.
     * <p>
     * Passes the received parameter one-to-one to the super constructor of
     * {@link AbstractSecureComputationSlave}.
     *
     * @param privateInput The input that should be kept private.
     * @param publicKey    The public key for the encryption.
     * @see AbstractSecureComputationSlave
     */
    public SecureMultiplication(BigInteger privateInput, PublicKey publicKey) {
        super(privateInput, publicKey);
    }

    @Override
    public BigInteger forwardStep(BigInteger previousPartyResult) {
        BigInteger nSquared = publicKey.getnSquared();

        return previousPartyResult.modPow(privateInput, nSquared);
    }

    /**
     * {@inheritDoc}
     * <p>
     * It also generates the random output share, which is later used to compute
     * the final result of the multiplication.
     */
    @Override
    public BigInteger backwardStep(BigInteger previousPartyResult) {
        outputShare = generateOutputShare();

        BigInteger nSquared = publicKey.getnSquared();
        BigInteger eos = publicKey.encrypt(outputShare);
        BigInteger ieos = eos.modInverse(nSquared);
        BigInteger result = previousPartyResult.multiply(ieos);
        return result.mod(nSquared);
    }
}