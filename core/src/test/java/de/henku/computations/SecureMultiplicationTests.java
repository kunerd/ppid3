package de.henku.computations;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.math.BigInteger;
import java.security.SecureRandom;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import de.henku.jpaillier.PublicKey;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ PublicKey.class, SecureMultiplication.class })
public class SecureMultiplicationTests {

	private SecureMultiplication subject;
	private PublicKey publicKey;
	private BigInteger privateInput;
	private BigInteger previousPartyResult;
	private BigInteger nSquared;

	@Before
	public void init() {
		nSquared = mock(BigInteger.class);

		publicKey = PowerMockito.mock(PublicKey.class);
		Mockito.when(publicKey.getnSquared()).thenReturn(nSquared);

		privateInput = mock(BigInteger.class);
		previousPartyResult = mock(BigInteger.class);

		subject = new SecureMultiplication(privateInput, publicKey);
	}

	@Test
	public void forwardStepPowersPrivateInputToPreviousPartyResult()
			throws Exception {

		subject.forwardStep(previousPartyResult);

		Mockito.verify(previousPartyResult).modPow(privateInput, nSquared);
	}

	@Test
	public void forwardStepReturnsPreviousePartyResultPoweredByPrivateInput()
			throws Exception {
		BigInteger expected = mock(BigInteger.class);

		Mockito.when(previousPartyResult.modPow(privateInput, nSquared))
				.thenReturn(expected);

		BigInteger actual = subject.forwardStep(previousPartyResult);

		assertEquals(expected, actual);
	}

	@Test
	public void backwardStepGeneratesOutputShare() throws Exception {
		SecureRandom secureRand = mock(SecureRandom.class);
		BigInteger expected = mock(BigInteger.class);
		BigInteger outputShare = mock(BigInteger.class);
		BigInteger encryptedOutputShare = mock(BigInteger.class);
		BigInteger inverseOutputShare = mock(BigInteger.class);

		PowerMockito.whenNew(SecureRandom.class).withNoArguments()
				.thenReturn(secureRand);
		PowerMockito.whenNew(BigInteger.class).withAnyArguments()
				.thenReturn(outputShare);

		Mockito.when(outputShare.mod(nSquared)).thenReturn(outputShare);
		PowerMockito.when(publicKey.encrypt(outputShare)).thenReturn(
				encryptedOutputShare);
		Mockito.when(encryptedOutputShare.modInverse(nSquared)).thenReturn(
				inverseOutputShare);
		Mockito.when(previousPartyResult.multiply(inverseOutputShare))
				.thenReturn(expected);

		subject.backwardStep(previousPartyResult);
		BigInteger actual = subject.getOutputShare();

		PowerMockito.verifyNew(BigInteger.class).withArguments(512, secureRand);

		assertEquals(outputShare, actual);
		Mockito.verify(outputShare).mod(nSquared);
	}

	@Test
	public void backwardStepEncryptsOutputShare() throws Exception {
		SecureRandom secureRand = mock(SecureRandom.class);
		BigInteger expected = mock(BigInteger.class);
		BigInteger inverseOutputShare = mock(BigInteger.class);
		BigInteger outputShare = mock(BigInteger.class);
		BigInteger encryptedOutputShare = mock(BigInteger.class);

		PowerMockito.whenNew(SecureRandom.class).withNoArguments()
				.thenReturn(secureRand);
		PowerMockito.whenNew(BigInteger.class).withAnyArguments()
				.thenReturn(outputShare);

		Mockito.when(outputShare.mod(nSquared)).thenReturn(outputShare);
		PowerMockito.when(publicKey.encrypt(outputShare)).thenReturn(
				encryptedOutputShare);
		Mockito.when(encryptedOutputShare.modInverse(nSquared)).thenReturn(
				inverseOutputShare);
		Mockito.when(previousPartyResult.multiply(inverseOutputShare))
				.thenReturn(expected);
		// Mockito.when(expected.mod(nSquared)).thenReturn(expected);

		subject.backwardStep(previousPartyResult);

		Mockito.verify(publicKey).encrypt(outputShare);
	}

	@Test
	public void backwardStepComputesInverseOfEncryptsOutputShare()
			throws Exception {
		SecureRandom secureRand = mock(SecureRandom.class);
		BigInteger inverseOutputShare = mock(BigInteger.class);
		BigInteger expected = mock(BigInteger.class);
		BigInteger outputShare = mock(BigInteger.class);
		BigInteger encryptedOutputShare = mock(BigInteger.class);

		PowerMockito.whenNew(SecureRandom.class).withNoArguments()
				.thenReturn(secureRand);
		PowerMockito.whenNew(BigInteger.class).withAnyArguments()
				.thenReturn(outputShare);

		Mockito.when(outputShare.mod(nSquared)).thenReturn(outputShare);
		PowerMockito.when(publicKey.encrypt(outputShare)).thenReturn(
				encryptedOutputShare);
		Mockito.when(encryptedOutputShare.modInverse(nSquared)).thenReturn(
				inverseOutputShare);
		Mockito.when(previousPartyResult.multiply(inverseOutputShare))
				.thenReturn(expected);

		subject.backwardStep(previousPartyResult);

		Mockito.verify(encryptedOutputShare).modInverse(nSquared);
	}

	@Test
	public void backwardStepMultipliesInverseWithPreviousPartyResult()
			throws Exception {
		SecureRandom secureRand = mock(SecureRandom.class);
		BigInteger inverseOutputShare = mock(BigInteger.class);
		BigInteger expected = mock(BigInteger.class);
		BigInteger outputShare = mock(BigInteger.class);
		BigInteger encryptedOutputShare = mock(BigInteger.class);

		PowerMockito.whenNew(SecureRandom.class).withNoArguments()
				.thenReturn(secureRand);
		PowerMockito.whenNew(BigInteger.class).withAnyArguments()
				.thenReturn(outputShare);

		Mockito.when(outputShare.mod(nSquared)).thenReturn(outputShare);
		PowerMockito.when(publicKey.encrypt(outputShare)).thenReturn(
				encryptedOutputShare);
		Mockito.when(encryptedOutputShare.modInverse(nSquared)).thenReturn(
				inverseOutputShare);
		Mockito.when(previousPartyResult.multiply(inverseOutputShare))
				.thenReturn(expected);

		subject.backwardStep(previousPartyResult);

		Mockito.verify(previousPartyResult).multiply(inverseOutputShare);
	}

	@Test
	public void backwardStepComputesResultInModnSquared() throws Exception {
		SecureRandom secureRand = mock(SecureRandom.class);
		BigInteger inverseOutputShare = mock(BigInteger.class);
		BigInteger expected = mock(BigInteger.class);
		BigInteger outputShare = mock(BigInteger.class);
		BigInteger encryptedOutputShare = mock(BigInteger.class);

		PowerMockito.whenNew(SecureRandom.class).withNoArguments()
				.thenReturn(secureRand);
		PowerMockito.whenNew(BigInteger.class).withAnyArguments()
				.thenReturn(outputShare);

		Mockito.when(outputShare.mod(nSquared)).thenReturn(outputShare);
		PowerMockito.when(publicKey.encrypt(outputShare)).thenReturn(
				encryptedOutputShare);
		Mockito.when(encryptedOutputShare.modInverse(nSquared)).thenReturn(
				inverseOutputShare);
		Mockito.when(previousPartyResult.multiply(inverseOutputShare))
				.thenReturn(expected);

		subject.backwardStep(previousPartyResult);

		Mockito.verify(expected).mod(nSquared);
	}

	@Test
	public void backwardStepReturnsCorrectResult() throws Exception {
		SecureRandom secureRand = mock(SecureRandom.class);
		BigInteger inverseOutputShare = mock(BigInteger.class);
		BigInteger expected = mock(BigInteger.class);
		BigInteger outputShare = mock(BigInteger.class);
		BigInteger encryptedOutputShare = mock(BigInteger.class);

		PowerMockito.whenNew(SecureRandom.class).withNoArguments()
				.thenReturn(secureRand);
		PowerMockito.whenNew(BigInteger.class).withAnyArguments()
				.thenReturn(outputShare);

		Mockito.when(outputShare.mod(nSquared)).thenReturn(outputShare);
		PowerMockito.when(publicKey.encrypt(outputShare)).thenReturn(
				encryptedOutputShare);
		Mockito.when(encryptedOutputShare.modInverse(nSquared)).thenReturn(
				inverseOutputShare);
		Mockito.when(previousPartyResult.multiply(inverseOutputShare))
				.thenReturn(expected);
		Mockito.when(expected.mod(nSquared)).thenReturn(expected);

		BigInteger actual = subject.backwardStep(previousPartyResult);

		assertEquals(expected, actual);
	}

}
