package de.henku.algorithm.id3_horizontal;

import de.henku.computations.SecureComputationMaster;
import de.henku.jpaillier.KeyPair;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SecureSquareDivisionMaster {

    private final static BigInteger TWO = BigInteger.valueOf(2);

    private final KeyPair keyPair;

    private final FactoryHelper factoryHelper;
    // TODO rename
    private SecureComputationMaster z;
    private SecureComputationMaster w;

    private Object classValue = null;
//    private int classValueCount = 0;

    private final ConcurrentHashMap<Object, SecureComputationMaster> multiplications =
            new ConcurrentHashMap<>();

    SecureSquareDivisionMaster(KeyPair keyPair, FactoryHelper factoryHelper) {
        this.keyPair = keyPair;
        this.factoryHelper = factoryHelper;
    }

    public SecureSquareDivisionMaster(KeyPair keyPair) {
        this(keyPair, new FactoryHelper());
    }

    public List<MultiplicationResult> createMultiplications(Map<Object, Long> counts) {
        List<MultiplicationResult> results = new ArrayList<>();

        for (Object cv : counts.keySet()) {
            long count = counts.get(cv);

            if ( count != 0l ) {
                classValue = cv;
//                classValueCount++;
            }

            SecureComputationMaster m = factoryHelper.finalize(count, keyPair);
            multiplications.put(cv, m);

            BigInteger result = m.startEncryptedComputation();

            results.add(new MultiplicationResult(cv, result));
        }

        return results;
    }

    public AdditionResults handleMultiplicationBackwardStep(List<MultiplicationResult> results) {

        for (MultiplicationResult r : results) {
            Object classValue = r.getClassValue();

            SecureComputationMaster m = multiplications.get(classValue);
            m.decryptAndSetOutputShare(r.getResult());
        }

        z = factoryHelper.finalize(calculateZ(), keyPair);
        w = factoryHelper.finalize(calculateW(), keyPair);

        return new AdditionResults(z.startEncryptedComputation(), w.startEncryptedComputation());
    }

    public void handleAdditionBackwardStep(AdditionResults results) {
        z.decryptAndSetOutputShare(results.getResultForZ());
        w.decryptAndSetOutputShare(results.getResultForW());
    }

    private BigInteger calculateZ() {
        BigInteger result = BigInteger.ZERO;

        for (SecureComputationMaster m : multiplications.values()) {
            BigInteger pi = m.getPrivateInput();
            BigInteger os = m.getOutputShare();

            result = result.add(pi.pow(2));
            result = result.add(os.multiply(TWO));
        }

        return result;
    }

    private BigInteger calculateW() {
        BigInteger result = BigInteger.ZERO;

        for (SecureComputationMaster m : multiplications.values()) {
            BigInteger privateInput = m.getPrivateInput();

            result = result.add(privateInput);
        }

        return result;
    }

    public GiniGainResult computeResult(List<SquareDivisionResult> outputShares) {
        BigInteger zResult = z.getOutputShare();
        BigInteger wResult = w.getOutputShare();

        BigInteger n = keyPair.getPublicKey().getN();

        Object cv = classValue;
//        if(classValueCount == 1) {
//            cv = classValue;
//        }

        for (SquareDivisionResult os : outputShares) {
            zResult = zResult.multiply(os.getOutputShareZ()).mod(n);
            wResult = wResult.multiply(os.getOutputShareW()).mod(n);

            if(os.getClassValue() != null) {
                cv = os.getClassValue();
            }
        }

        double r;
        if (wResult.equals(BigInteger.ZERO)) {
            r = 0d;
        } else {
            r = zResult.doubleValue() / wResult.doubleValue();
        }

        return new GiniGainResult(cv, r);
    }

    static class FactoryHelper {
        SecureComputationMaster finalize(long input, KeyPair kp) {
            return new SecureComputationMaster(input, kp);
        }

        SecureComputationMaster finalize(BigInteger input, KeyPair kp) {
            return new SecureComputationMaster(input, kp);
        }
    }
}
