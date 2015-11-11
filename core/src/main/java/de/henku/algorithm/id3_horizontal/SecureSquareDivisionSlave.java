package de.henku.algorithm.id3_horizontal;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import de.henku.computations.SecureAddition;
import de.henku.computations.SecureMultiplication;
import de.henku.jpaillier.PublicKey;

public class SecureSquareDivisionSlave {

	private final static BigInteger TWO = BigInteger.valueOf(2);

	private final PublicKey publicKey;

	private SecureAddition z;
	private SecureAddition w;

	private final List<BigInteger> inputs = new ArrayList<BigInteger>();
	private final List<BigInteger> multiplicationOutputShares = new ArrayList<BigInteger>();

	private final ConcurrentHashMap<String, SecureMultiplication> multiplications = 
			new ConcurrentHashMap<String, SecureMultiplication>();

	public SecureSquareDivisionSlave(PublicKey publicKey) {
		this.publicKey = publicKey;
	}
	
	// FIXME replace list
	public List<MultiplicationResult> handleMultiplicationForwardStep(List<Pair<String, Long>> counts, List<MultiplicationResult> prevResults) {	
	
		List<MultiplicationResult> results = new ArrayList<MultiplicationResult>();
		
		for (MultiplicationResult r : prevResults) {
			
			String classValue = r.getClassValue();
			
			// TODO refactor to hashmap 
			long count = counts.stream()
				.filter(p -> p.first.equals(classValue))
				.findFirst().get().second;

			inputs.add(BigInteger.valueOf(count));

			SecureMultiplication m = new SecureMultiplication(
					BigInteger.valueOf(count), publicKey);
			
			multiplications.put(classValue, m);
			
			BigInteger result = m.forwardStep(r.getResult());
			
			results.add(new MultiplicationResult(classValue, result));
		}
		
		return results;
	}
	
	public List<MultiplicationResult> handleMultiplicationBackwardStep(List<MultiplicationResult> prevResults) {
		
		List<MultiplicationResult> results = new ArrayList<MultiplicationResult>();

		for (MultiplicationResult r : prevResults) {
			String classValue = r.getClassValue();
			
			SecureMultiplication m = multiplications.get(classValue);
			BigInteger newResult = m.backwardStep(r.getResult());
			
			results.add(new MultiplicationResult(classValue, newResult));
			
			multiplicationOutputShares.add(m.getOutputShare());
		}
		
		return results;
	}
	
	public AdditionResults handleAdditionForwardStep(AdditionResults data) {
		z = new SecureAddition(calculateZ(), publicKey);
		w = new SecureAddition(calculateW(), publicKey);
		
		BigInteger zFR = z.forwardStep(data.getResultForZ());
		BigInteger wFR = w.forwardStep(data.getResultForW());
		
		return new AdditionResults(zFR, wFR);
	}
	
	public AdditionResults handleAdditionBackwardStep(AdditionResults data) {
		BigInteger zBR = z.backwardStep(data.getResultForZ());
		BigInteger wBR = w.backwardStep(data.getResultForW());
		
		return new AdditionResults(zBR, wBR);
	}

	private BigInteger calculateZ() {
		BigInteger result = BigInteger.ZERO;

		for (BigInteger input : inputs) {
			result = result.add(input.pow(2));
		}

		for (BigInteger os : multiplicationOutputShares) {
			result = result.add(os.multiply(TWO));
		}

		return result;
	}

	private BigInteger calculateW() {
		BigInteger result = BigInteger.ZERO;

		for (BigInteger input : inputs) {
			result = result.add(input);
		}

		return result;
	}

	public AdditionResults getAdditionOutputShares() {
		return new AdditionResults(z.getOutputShare(), w.getOutputShare());
	}

}
