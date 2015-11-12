/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Hendrik Kunert
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package de.henku.computations;

import de.henku.jpaillier.KeyPair;
import de.henku.jpaillier.PublicKey;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.math.BigInteger;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({PublicKey.class, KeyPair.class})
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
