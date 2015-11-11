package de.henku.integration.computations;

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

import de.henku.computations.SecureAddition;
import de.henku.computations.SecureComputationMaster;
import de.henku.utils.IntegrationTest;
import de.henku.jpaillier.KeyPair;
import de.henku.jpaillier.KeyPairBuilder;
import de.henku.jpaillier.PublicKey;

@Category(IntegrationTest.class)
@RunWith(Parameterized.class)
public class SecureAdditionIT {

	private static final BigInteger NEGATIVE_THRESHOLD = BigInteger
			.valueOf(Long.MAX_VALUE);
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

		LinkedList<Long> negativeResult = new LinkedList<Long>();
		negativeResult.add(Long.valueOf(10));
		negativeResult.add(Long.valueOf(-5));
		negativeResult.add(Long.valueOf(-6));

		LinkedList<Long> bigNegativeResult = new LinkedList<Long>();
		bigNegativeResult.add(Long.valueOf(Long.MIN_VALUE));
		bigNegativeResult.add(Long.valueOf(3));
		bigNegativeResult.add(Long.valueOf(2));

		return Arrays.asList(new Object[][] { createTestParameter(twoParties),
				createTestParameter(fourParties),
				createTestParameter(negativeResult),
				createTestParameter(bigNegativeResult)
		});
	}

	public SecureAdditionIT(List<Long> inputValues, long expectedResult) {
		this.inputValues = inputValues;
		this.expectedResult = expectedResult;
	}

	@Test
	public void additionChecker() {
		LinkedList<SecureAddition> slaves = new LinkedList<SecureAddition>();
		BigInteger masterInput = BigInteger.valueOf(inputValues.get(0));

		SecureComputationMaster master = new SecureComputationMaster(
				masterInput, keyPair);

		BigInteger helper = master.startEncryptedComputation();

		// forward steps
		for (Long input : inputValues.subList(1, inputValues.size())) {
			SecureAddition slave = new SecureAddition(
					BigInteger.valueOf(input), publicKey);

			slaves.add(slave);
			helper = slave.forwardStep(helper);
		}

		// backward steps
		for (SecureAddition slave : slaves) {
			helper = slave.backwardStep(helper);
		}

		master.decryptAndSetOutputShare(helper);

		BigInteger actual = master.getOutputShare();

		for (SecureAddition slave : slaves) {
			BigInteger outputShare = slave.getOutputShare();
			BigInteger n = publicKey.getN();

			actual = actual.multiply(outputShare).mod(n);
		}

		if (actual.compareTo(NEGATIVE_THRESHOLD) > 0) {
			actual = actual.subtract(publicKey.getN());
		}

		assertEquals(expectedResult, actual.longValue());
	}

	private static long sumHelper(List<Long> list) {
		long sum = 0;

		for (Long input : list) {
			sum += input;
		}

		return sum;
	}

	private static Object[] createTestParameter(List<Long> list) {
		return new Object[] { list, sumHelper(list) };
	}
}
