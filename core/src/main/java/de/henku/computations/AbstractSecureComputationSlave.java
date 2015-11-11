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