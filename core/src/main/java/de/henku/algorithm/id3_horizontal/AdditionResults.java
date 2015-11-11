package de.henku.algorithm.id3_horizontal;

import java.math.BigInteger;

public class AdditionResults {

    private BigInteger resultForZ;
    private BigInteger resultForW;

    public AdditionResults(BigInteger resultForZ, BigInteger resultForW) {
        this.resultForZ = resultForZ;
        this.resultForW = resultForW;
    }

    public BigInteger getResultForZ() {
        return resultForZ;
    }

    public BigInteger getResultForW() {
        return resultForW;
    }
}
