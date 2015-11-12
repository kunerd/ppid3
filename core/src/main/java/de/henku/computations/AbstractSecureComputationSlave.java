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
import java.security.SecureRandom;

/**
 * Abstract class for secure computation slave instances.
 *
 * @see SecureAddition
 * @see SecureMultiplication
 */
public abstract class AbstractSecureComputationSlave extends AbstractSecureComputation {
    protected PublicKey publicKey;

    /**
     * Creates a new instance with the input, that has to be kept private and
     * the public key for the encryption.
     *
     * @param privateInput The input that should be kept private.
     * @param publicKey    The public key for the encryption.
     */
    public AbstractSecureComputationSlave(BigInteger privateInput, PublicKey publicKey) {
        super(privateInput);
        this.publicKey = publicKey;
    }

    protected BigInteger generateOutputShare() {
        BigInteger os = new BigInteger(512, new SecureRandom());

        BigInteger nSquared = publicKey.getnSquared();
        return os.mod(nSquared);
    }

    @Override
    public PublicKey getPublicKey() {
        return publicKey;
    }

    /**
     * Computes the forward step of a secure computation operation.
     * <p>
     * For detailed usage information consult the documentation of
     * {@link SecureComputationMaster}.
     *
     * @param previousPartyResult The intermediate result computed by the
     *                            previous party.
     * @return The next computed intermediate result.
     * @see SecureAddition
     * @see SecureMultiplication
     * @see SecureComputationMaster
     */
    public abstract BigInteger forwardStep(BigInteger previousPartyResult);

    /**
     * Computes the backward step of a secure computation operation.
     * <p>
     * For detailed usage information consult the documentation of
     * {@link SecureComputationMaster}.
     *
     * @param previousPartyResult The intermediate result computed by the
     *                            previous party.
     * @return The next computed intermediate result.
     * @see SecureAddition
     * @see SecureMultiplication
     * @see SecureComputationMaster
     */
    public abstract BigInteger backwardStep(BigInteger previousPartyResult);
}