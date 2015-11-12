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
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.math.BigInteger;
import java.security.SecureRandom;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

@RunWith(PowerMockRunner.class)
@PrepareForTest({PublicKey.class, SecureMultiplication.class})
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
