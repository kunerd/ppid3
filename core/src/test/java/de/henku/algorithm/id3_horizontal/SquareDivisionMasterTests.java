package de.henku.algorithm.id3_horizontal;

import de.henku.computations.SecureComputationMaster;
import de.henku.jpaillier.KeyPair;
import de.henku.jpaillier.KeyPairBuilder;
import org.junit.Before;
import org.junit.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class SquareDivisionMasterTests {

    private static final long INPUT_1 = 2l;
    private static final long INPUT_2 = 4l;

    private KeyPair keyPair;
    private SecureSquareDivisionMaster subject;
    private SecureSquareDivisionMaster.FactoryHelper factoryHelper;
    private SecureComputationMaster compMock2;
    private SecureComputationMaster compMock1;

    @Before
    public void beforeEach() {
        keyPair = new KeyPairBuilder().generateKeyPair();
        factoryHelper = mock(SecureSquareDivisionMaster.FactoryHelper.class);

        compMock1 = mock(SecureComputationMaster.class);
        compMock2 = mock(SecureComputationMaster.class);

        when(factoryHelper.finalize(INPUT_1, keyPair)).thenReturn(compMock1);
        when(factoryHelper.finalize(INPUT_2, keyPair)).thenReturn(compMock2);

        when(compMock1.getPrivateInput()).thenReturn(BigInteger.valueOf(INPUT_1));
        when(compMock2.getPrivateInput()).thenReturn(BigInteger.valueOf(INPUT_2));

        subject = new SecureSquareDivisionMaster(keyPair, factoryHelper);
    }

    @Test
    public void createMultiplications_createsOneSecureComputationForEachInput() {
        Map<Object, Long> counts = new ConcurrentHashMap<>();
        counts.put("classAttr1", INPUT_1);
        counts.put("classAttr2", INPUT_2);

        subject.createMultiplications(counts);

        verify(factoryHelper).finalize(2l, keyPair);
        verify(factoryHelper).finalize(4l, keyPair);
    }

    @Test
    public void createMultiplications_startsEncryptedComputationForEachInput() {
        Map<Object, Long> counts = new ConcurrentHashMap<>();
        counts.put("classAttr1", INPUT_1);
        counts.put("classAttr2", INPUT_2);


        subject.createMultiplications(counts);

        verify(compMock1).startEncryptedComputation();
        verify(compMock2).startEncryptedComputation();
    }

    @Test
    public void createMultiplications_createsOneMultiplicationResultForEachInput() {
        Map<Object, Long> counts = new ConcurrentHashMap<>();
        counts.put("classAttr1", INPUT_1);
        counts.put("classAttr2", INPUT_2);

        BigInteger r1 = mock(BigInteger.class);
        BigInteger r2 = mock(BigInteger.class);
        when(compMock1.startEncryptedComputation()).thenReturn(r1);
        when(compMock2.startEncryptedComputation()).thenReturn(r2);

        List<MultiplicationResult> results = subject.createMultiplications(counts);

        assertEquals(counts.size(), results.size());

        MultiplicationResult a1 = new MultiplicationResult("classAttr1", r1);
        MultiplicationResult a2 = new MultiplicationResult("classAttr2", r2);

        System.out.println(results);

        assertThat(results, containsInAnyOrder(a1, a2));
    }

    @Test
    public void multiplicationBackwardStep_decryptsEachMultiplication() {
        Map<Object, Long> counts = new ConcurrentHashMap<>();
        counts.put("classAttr1", INPUT_1);
        counts.put("classAttr2", INPUT_2);
        subject.createMultiplications(counts);

        BigInteger r1 = mock(BigInteger.class);
        BigInteger r2 = mock(BigInteger.class);

        List<MultiplicationResult> results = new ArrayList<MultiplicationResult>();
        results.add(new MultiplicationResult("classAttr1", r1));
        results.add(new MultiplicationResult("classAttr2", r2));

        when(compMock1.getOutputShare()).thenReturn(BigInteger.valueOf(5));
        when(compMock2.getOutputShare()).thenReturn(BigInteger.valueOf(3));

        SecureComputationMaster zMock = mock(SecureComputationMaster.class);
        SecureComputationMaster wMock = mock(SecureComputationMaster.class);
        when(factoryHelper.finalize(any(BigInteger.class), eq(keyPair))).thenReturn(zMock, wMock);

        subject.handleMultiplicationBackwardStep(results);

        verify(compMock1).decryptAndSetOutputShare(r1);
        verify(compMock2).decryptAndSetOutputShare(r2);
    }

    @Test
    public void multiplicationBackwardStep_computesCorrectZ() {
        Map<Object, Long> counts = new ConcurrentHashMap<>();
        counts.put("classAttr1", INPUT_1);
        counts.put("classAttr2", INPUT_2);
        subject.createMultiplications(counts);

        BigInteger r1 = mock(BigInteger.class);
        BigInteger r2 = mock(BigInteger.class);

        List<MultiplicationResult> results = new ArrayList<MultiplicationResult>();
        results.add(new MultiplicationResult("classAttr1", r1));
        results.add(new MultiplicationResult("classAttr2", r2));

        when(compMock1.getOutputShare()).thenReturn(BigInteger.valueOf(5));
        when(compMock2.getOutputShare()).thenReturn(BigInteger.valueOf(3));

        SecureComputationMaster zMock = mock(SecureComputationMaster.class);
        SecureComputationMaster wMock = mock(SecureComputationMaster.class);
        when(factoryHelper.finalize(any(BigInteger.class), eq(keyPair))).thenReturn(zMock, wMock);

        subject.handleMultiplicationBackwardStep(results);

        verify(factoryHelper).finalize(BigInteger.valueOf(36), keyPair);
    }

    @Test
    public void multiplicationBackwardStep_computesCorrectW() {
        Map<Object, Long> counts = new ConcurrentHashMap<>();
        counts.put("classAttr1", INPUT_1);
        counts.put("classAttr2", INPUT_2);
        subject.createMultiplications(counts);

        BigInteger r1 = mock(BigInteger.class);
        BigInteger r2 = mock(BigInteger.class);

        List<MultiplicationResult> results = new ArrayList<MultiplicationResult>();
        results.add(new MultiplicationResult("classAttr1", r1));
        results.add(new MultiplicationResult("classAttr2", r2));

        when(compMock1.getOutputShare()).thenReturn(BigInteger.valueOf(5));
        when(compMock2.getOutputShare()).thenReturn(BigInteger.valueOf(3));

        SecureComputationMaster zMock = mock(SecureComputationMaster.class);
        SecureComputationMaster wMock = mock(SecureComputationMaster.class);
        when(factoryHelper.finalize(any(BigInteger.class), eq(keyPair))).thenReturn(zMock, wMock);

        subject.handleMultiplicationBackwardStep(results);

        verify(factoryHelper).finalize(BigInteger.valueOf(6), keyPair);
    }

    @Test
    public void multiplicationBackwardStep_startsAdditionForZandW() {
        Map<Object, Long> counts = new ConcurrentHashMap<>();
        counts.put("classAttr1", INPUT_1);
        counts.put("classAttr2", INPUT_2);
        subject.createMultiplications(counts);

        BigInteger r1 = mock(BigInteger.class);
        BigInteger r2 = mock(BigInteger.class);

        List<MultiplicationResult> results = new ArrayList<MultiplicationResult>();
        results.add(new MultiplicationResult("classAttr1", r1));
        results.add(new MultiplicationResult("classAttr2", r2));

        when(compMock1.getOutputShare()).thenReturn(BigInteger.valueOf(5));
        when(compMock2.getOutputShare()).thenReturn(BigInteger.valueOf(3));

        SecureComputationMaster zMock = mock(SecureComputationMaster.class);
        SecureComputationMaster wMock = mock(SecureComputationMaster.class);
        when(factoryHelper.finalize(any(BigInteger.class), eq(keyPair))).thenReturn(zMock, wMock);

        BigInteger encryptedZMock = mock(BigInteger.class);
        when(zMock.startEncryptedComputation()).thenReturn(encryptedZMock);
        BigInteger encryptedWMock = mock(BigInteger.class);
        when(wMock.startEncryptedComputation()).thenReturn(encryptedWMock);

        AdditionResults a = subject.handleMultiplicationBackwardStep(results);

        assertEquals(encryptedZMock, a.getResultForZ());
        assertEquals(encryptedWMock, a.getResultForW());
    }

    @Test
    public void additionBackwardStep_decryptsZandW() {
        Map<Object, Long> counts = new ConcurrentHashMap<>();
        counts.put("classAttr1", INPUT_1);
        counts.put("classAttr2", INPUT_2);
        subject.createMultiplications(counts);

        BigInteger r1 = mock(BigInteger.class);
        BigInteger r2 = mock(BigInteger.class);

        List<MultiplicationResult> multResults = new ArrayList<MultiplicationResult>();
        multResults.add(new MultiplicationResult("classAttr1", r1));
        multResults.add(new MultiplicationResult("classAttr2", r2));

        when(compMock1.getOutputShare()).thenReturn(BigInteger.valueOf(5));
        when(compMock2.getOutputShare()).thenReturn(BigInteger.valueOf(3));

        SecureComputationMaster zMock = mock(SecureComputationMaster.class);
        SecureComputationMaster wMock = mock(SecureComputationMaster.class);
        when(factoryHelper.finalize(any(BigInteger.class), eq(keyPair))).thenReturn(zMock, wMock);

        BigInteger encryptedZMock = mock(BigInteger.class);
        when(zMock.startEncryptedComputation()).thenReturn(encryptedZMock);
        BigInteger encryptedWMock = mock(BigInteger.class);
        when(wMock.startEncryptedComputation()).thenReturn(encryptedWMock);

        subject.handleMultiplicationBackwardStep(multResults);

        BigInteger zr = mock(BigInteger.class);
        BigInteger wr = mock(BigInteger.class);
        AdditionResults addResults = new AdditionResults(zr, wr);
        subject.handleAdditionBackwardStep(addResults);

        verify(zMock).decryptAndSetOutputShare(zr);
        verify(wMock).decryptAndSetOutputShare(wr);
    }
}
