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

import de.henku.jpaillier.PublicKey;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.math.BigInteger;
import java.security.SecureRandom;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({PublicKey.class, SecureAddition.class})
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