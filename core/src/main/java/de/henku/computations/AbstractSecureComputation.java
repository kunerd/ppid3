package de.henku.computations;

import de.henku.jpaillier.PublicKey;

import java.math.BigInteger;

public abstract class AbstractSecureComputation {

    protected final BigInteger privateInput;
    protected BigInteger outputShare;


    public AbstractSecureComputation(BigInteger privateInput) {
        this.privateInput = privateInput;
    }

    public BigInteger getPrivateInput() {
        return privateInput;
    }

    public BigInteger getOutputShare() {
        return outputShare;
    }

    public abstract PublicKey getPublicKey();
}
