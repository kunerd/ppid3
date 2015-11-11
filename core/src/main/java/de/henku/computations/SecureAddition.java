package de.henku.computations;

import de.henku.jpaillier.PublicKey;

import java.math.BigInteger;

/**
 * A class for securely computing the sum of the input from two or more parties.
 * <p>
 * For detailed usage instructions see {@link SecureComputationMaster}.
 *
 * @see AbstractSecureComputationSlave
 */
public class SecureAddition extends AbstractSecureComputationSlave {

    /**
     * Creates a new instance with the input, that has to be kept private and
     * the public key for the encryption.
     * <p>
     * Transforms the {@code privateInput} into a {@link BigInteger} and passes
     * it together with the {@code publicKey} to the super contructor of
     * {@link AbstractSecureComputationSlave}.
     *
     * @param privateInput The input that should be kept private.
     * @param publicKey    The public key for the encryption.
     * @see AbstractSecureComputationSlave
     */
    public SecureAddition(long privateInput, PublicKey publicKey) {
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
    public SecureAddition(BigInteger privateInput, PublicKey publicKey) {
        super(privateInput, publicKey);
    }

    @Override
    public BigInteger forwardStep(BigInteger previousPartyResult) {
        BigInteger e = publicKey.encrypt(privateInput);

        BigInteger nSquared = publicKey.getnSquared();
        return e.multiply(previousPartyResult).mod(nSquared);
    }

    /**
     * {@inheritDoc}
     * <p>
     * It also generates the random output share, which is later used to compute
     * the final result of the addition.
     */
    @Override
    public BigInteger backwardStep(BigInteger previousPartyResult) {
        outputShare = generateOutputShare();

        BigInteger inverse = outputShare.modInverse(this.publicKey
                .getnSquared());

        BigInteger result = previousPartyResult.modPow(inverse,
                this.publicKey.getnSquared());

        return result;
    }
}