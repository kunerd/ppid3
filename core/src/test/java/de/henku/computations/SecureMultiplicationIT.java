package de.henku.computations;

import static org.junit.Assert.assertEquals;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import de.henku.computations.SecureComputationMaster;
import de.henku.computations.SecureMultiplication;
import de.henku.utils.IntegrationTest;
import de.henku.jpaillier.KeyPair;
import de.henku.jpaillier.KeyPairBuilder;
import de.henku.jpaillier.PublicKey;

@Category(IntegrationTest.class)
@RunWith(Parameterized.class)
public class SecureMultiplicationIT {

	private KeyPair keyPair;
	private PublicKey publicKey;
	private List<Long> inputValues;
	private long expectedResult;

	@Before
	public void init() {
		KeyPairBuilder factory = new KeyPairBuilder();
		this.keyPair = factory.generateKeyPair();
		this.publicKey = keyPair.getPublicKey();
	}

	@Parameterized.Parameters
	public static Collection<Object[]> partyInputs() {
		LinkedList<Long> twoParties = new LinkedList<Long>();
		twoParties.add(Long.valueOf(10));
		twoParties.add(Long.valueOf(5));
		
		LinkedList<Long> fourParties = new LinkedList<Long>();
		fourParties.add(Long.valueOf(10));
		fourParties.add(Long.valueOf(5));
		fourParties.add(Long.valueOf(12));
		fourParties.add(Long.valueOf(4));

		return Arrays.asList(new Object[][] { 
				createTestParameter(twoParties),
				createTestParameter(fourParties)
			});
	}

	public SecureMultiplicationIT(List<Long> inputValues, long expectedResult) {
		this.inputValues = inputValues;
		this.expectedResult = expectedResult;
	}
	
	@Test
	public void multiplicationChecker() {
		LinkedList<SecureMultiplication> slaves = new LinkedList<SecureMultiplication>();
		BigInteger masterInput = BigInteger.valueOf(inputValues.get(0));

		SecureComputationMaster master = new SecureComputationMaster(masterInput, keyPair);

		BigInteger helper = master.startEncryptedComputation();

		// forward steps
		for (Long input : inputValues.subList(1, inputValues.size())) {
			SecureMultiplication slave = new SecureMultiplication(BigInteger.valueOf(input),
					publicKey);

			slaves.add(slave);
			helper = slave.forwardStep(helper);
		}

		// forward steps
		for (SecureMultiplication slave : slaves) {
			helper = slave.backwardStep(helper);
		}

		master.decryptAndSetOutputShare(helper);

		BigInteger actual = master.getOutputShare();

		for (SecureMultiplication slave : slaves) {
			BigInteger outputShare = slave.getOutputShare();
			BigInteger n = publicKey.getN();
			
			actual = actual.add(outputShare).mod(n);
		}

		assertEquals(expectedResult, actual.longValue());
	}
	
	private static long multHelper(List<Long> list){
		long prod = 1;
		
		for (Long input : list) {
			prod *= input;
		}
	
		return prod;
	}
	
	private static Object[] createTestParameter(List<Long> list) {

		return new Object[] { list, multHelper(list) };
	}
}
