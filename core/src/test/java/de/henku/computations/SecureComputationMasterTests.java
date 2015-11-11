package de.henku.computations;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigInteger;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import de.henku.jpaillier.KeyPair;
import de.henku.jpaillier.PublicKey;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ PublicKey.class, KeyPair.class })
public class SecureComputationMasterTests {

	private SecureComputationMaster subject;
	private PublicKey publicKey;
	private KeyPair keyPair;
	private BigInteger privateInput;

	@Before
	public void init() {
		publicKey = PowerMockito.mock(PublicKey.class);

		keyPair = PowerMockito.mock(KeyPair.class);
		when(keyPair.getPublicKey()).thenReturn(publicKey);

		privateInput = mock(BigInteger.class);
		subject = new SecureComputationMaster(privateInput, keyPair);
	}

	@Test
	public void testStartEncryptedCalculation() {
		BigInteger expected = mock(BigInteger.class);

		PowerMockito.when(publicKey.encrypt(privateInput)).thenReturn(expected);

		BigInteger actual = subject.startEncryptedComputation();

		assertEquals(expected, actual);
		verify(publicKey).encrypt(privateInput);
	}

	@Test
	public void testDecryptAndSetOutputShareCallsDecryption() {
		BigInteger ciphertext = mock(BigInteger.class);
		BigInteger plaintext = mock(BigInteger.class);

		PowerMockito.when(keyPair.decrypt(ciphertext)).thenReturn(plaintext);

		subject.decryptAndSetOutputShare(ciphertext);

		verify(keyPair).decrypt(ciphertext);
	}

	@Test
	public void testDecryptAndSetOutputShareSetsOutputShare() {
		BigInteger ciphertext = mock(BigInteger.class);
		BigInteger decryptedOutputShare = mock(BigInteger.class);

		PowerMockito.when(keyPair.decrypt(ciphertext)).thenReturn(
				decryptedOutputShare);

		subject.decryptAndSetOutputShare(ciphertext);

		assertEquals(decryptedOutputShare, subject.getOutputShare());
	}
}
