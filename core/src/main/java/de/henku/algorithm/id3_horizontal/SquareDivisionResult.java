package de.henku.algorithm.id3_horizontal;

import java.math.BigInteger;

/**
 * Created by kunerd on 12.11.15.
 */
public class SquareDivisionResult {

    private BigInteger outputShareZ;
    private BigInteger outputShareW;
    private Object classValue;

    public SquareDivisionResult(BigInteger outputShareZ, BigInteger outputShareW, Object classValue) {
        this.outputShareZ = outputShareZ;
        this.outputShareW = outputShareW;
        this.classValue = classValue;
    }

    public BigInteger getOutputShareZ() {
        return outputShareZ;
    }

    public BigInteger getOutputShareW() {
        return outputShareW;
    }

    public Object getClassValue() { return classValue; }

}
