package de.henku.algorithm.id3_horizontal;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import de.henku.computations.SecureComputationMaster;
import de.henku.jpaillier.KeyPair;

public class SecureSquareDivisionMaster {
	
	private final static BigInteger TWO = BigInteger.valueOf(2);

	private final KeyPair keyPair;

	private final FactoryHelper factoryHelper;
	// TODO rename
	private SecureComputationMaster z;
	private SecureComputationMaster w;
	
	private final ConcurrentHashMap<Object, SecureComputationMaster> multiplications =
			new ConcurrentHashMap<Object, SecureComputationMaster>();
	
	SecureSquareDivisionMaster(KeyPair keyPair, FactoryHelper factoryHelper) {
		this.keyPair = keyPair;
		this.factoryHelper = factoryHelper;
	}
	
	public SecureSquareDivisionMaster(KeyPair keyPair) {
		this(keyPair, new FactoryHelper());
	}

	public List<MultiplicationResult> createMultiplications(Map<Object, Long> counts) {
		List<MultiplicationResult> results = new ArrayList<MultiplicationResult>();
		
		for (Object classValue : counts.keySet()) {
			long count = counts.get(classValue);
			
			SecureComputationMaster m = factoryHelper.finalize(count, keyPair);
			multiplications.put(classValue, m);

			BigInteger result = m.startEncryptedComputation();

			results.add(new MultiplicationResult(classValue, result));
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

	public Double computeResult(List<AdditionResults> outputShares) {
		BigInteger zResult = z.getOutputShare();
		BigInteger wResult = w.getOutputShare();
		
		BigInteger n = keyPair.getPublicKey().getN();

		for (AdditionResults os : outputShares) {
			zResult = zResult.multiply(os.getResultForZ()).mod(n);
			wResult = wResult.multiply(os.getResultForW()).mod(n);
		}

		if(wResult.equals(BigInteger.ZERO)) {
			return 0d;
		}
		return zResult.doubleValue() / wResult.doubleValue();
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
