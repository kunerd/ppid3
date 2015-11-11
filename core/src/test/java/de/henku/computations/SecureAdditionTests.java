package de.henku.computations;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigInteger;
import java.security.SecureRandom;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import de.henku.jpaillier.PublicKey;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ PublicKey.class, SecureAddition.class })
public class SecureAdditionTests {

	private SecureAddition subject;
	private PublicKey publicKey;
	private BigInteger nSquared;
	private BigInteger ciphertext;
	private BigInteger privateInput;
	private BigInteger previousPartyResult;
	private BigInteger product;

	@Before
	public void init() {
		publicKey = PowerMockito.mock(PublicKey.class);
		nSquared = mock(BigInteger.class);
		privateInput = mock(BigInteger.class);
		ciphertext = mock(BigInteger.class);
		previousPartyResult = mock(BigInteger.class);
		product = mock(BigInteger.class);

		subject = new SecureAddition(privateInput, publicKey);

		when(publicKey.getnSquared()).thenReturn(nSquared);
		when(publicKey.encrypt(privateInput)).thenReturn(ciphertext);
		when(ciphertext.multiply(previousPartyResult)).thenReturn(product);

	}

	@Test
	public void forwardAdditionEncryptsPrivateInput() {
		subject.forwardStep(previousPartyResult);

		verify(publicKey).encrypt(privateInput);
	}

	@Test
	public void forwardAdditionMultipliesEncryptedPrivateInputWithPreviousePartyValue() {
		subject.forwardStep(previousPartyResult);

		verify(ciphertext).multiply(previousPartyResult);
	}

	@Test
	public void forwardAdditionComputesModuloOfProduct() {
		subject.forwardStep(previousPartyResult);

		verify(product).mod(nSquared);
	}

	@Test
	public void forwardAdditionReturnsModuloProduct() {
		BigInteger expected = mock(BigInteger.class);

		when(product.mod(nSquared)).thenReturn(expected);

		BigInteger actual = subject.forwardStep(previousPartyResult);

		assertEquals(expected, actual);
	}

	//
	@Test
	public void backwardAdditionGeneratesOutputShare() throws Exception {
		BigInteger expected = mock(BigInteger.class);
		SecureRandom secureRand = mock(SecureRandom.class);
		BigInteger outputShare = mock(BigInteger.class);

		PowerMockito.whenNew(SecureRandom.class).withNoArguments()
				.thenReturn(secureRand);

		PowerMockito.whenNew(BigInteger.class).withAnyArguments()
				.thenReturn(outputShare);

		when(outputShare.mod(nSquared)).thenReturn(expected);

		subject.backwardStep(previousPartyResult);

		// TODO: remove hard coded values
		// PowerMockito.verifyNew(BigInteger.class)
		// .withArguments(512, 64, secureRand);

		BigInteger actual = subject.getOutputShare();
		assertEquals(expected, actual);
	}

	@Test
	public void backwardAdditionComputesOutputShareModNSquare()
			throws Exception {
		BigInteger outputShare = mock(BigInteger.class);
		SecureRandom secureRand = mock(SecureRandom.class);

		PowerMockito.whenNew(SecureRandom.class).withNoArguments()
				.thenReturn(secureRand);
		PowerMockito.whenNew(BigInteger.class).withAnyArguments()
				.thenReturn(outputShare);

		when(outputShare.mod(nSquared)).thenReturn(outputShare);

		subject.backwardStep(previousPartyResult);

		verify(outputShare).mod(nSquared);
	}

	@Test
	public void backwardAdditionComputesTheInverseOfOutoutShare()
			throws Exception {
		BigInteger outputShare = mock(BigInteger.class);
		SecureRandom secureRand = mock(SecureRandom.class);

		PowerMockito.whenNew(SecureRandom.class).withNoArguments()
				.thenReturn(secureRand);
		PowerMockito.whenNew(BigInteger.class).withAnyArguments()
				.thenReturn(outputShare);
		when(outputShare.mod(nSquared)).thenReturn(outputShare);

		subject.backwardStep(previousPartyResult);

		verify(outputShare).modInverse(nSquared);
	}

	@Test
	public void backwardAdditionPowerTheInverseToPreviousPartyValue()
			throws Exception {
		BigInteger outputShare = mock(BigInteger.class);
		BigInteger inverseShare = mock(BigInteger.class);
		SecureRandom secureRand = mock(SecureRandom.class);

		PowerMockito.whenNew(SecureRandom.class).withNoArguments()
				.thenReturn(secureRand);
		PowerMockito.whenNew(BigInteger.class).withAnyArguments()
				.thenReturn(outputShare);
		when(outputShare.mod(nSquared)).thenReturn(outputShare);
		when(outputShare.modInverse(nSquared)).thenReturn(inverseShare);

		subject.backwardStep(previousPartyResult);

		verify(previousPartyResult).modPow(inverseShare, nSquared);
	}

	@Test
	public void backwardAdditionReturnsPreviousPartyValuePoweredByInverseOutputShare()
			throws Exception {
		BigInteger outputShare = mock(BigInteger.class);
		BigInteger inverseShare = mock(BigInteger.class);
		SecureRandom secureRand = mock(SecureRandom.class);
		BigInteger expected = mock(BigInteger.class);

		PowerMockito.whenNew(SecureRandom.class).withNoArguments()
				.thenReturn(secureRand);
		PowerMockito.whenNew(BigInteger.class).withAnyArguments()
				.thenReturn(outputShare);
		when(outputShare.mod(nSquared)).thenReturn(outputShare);
		when(outputShare.modInverse(nSquared)).thenReturn(inverseShare);
		when(previousPartyResult.modPow(inverseShare, nSquared)).thenReturn(
				expected);

		BigInteger actual = subject.backwardStep(previousPartyResult);

		assertEquals(expected, actual);
	}
}