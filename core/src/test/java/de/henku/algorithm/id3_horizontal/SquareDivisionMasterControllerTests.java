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

package de.henku.algorithm.id3_horizontal;

import de.henku.algorithm.id3_horizontal.communication.NodeValuePair;
import de.henku.algorithm.id3_horizontal.communication.SquareDivisionPojo;
import de.henku.algorithm.id3_horizontal.communication.SquareDivisionSenderAdapter;
import de.henku.jpaillier.KeyPair;
import de.henku.jpaillier.KeyPairBuilder;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class SquareDivisionMasterControllerTests {

    private SquareDivisionMasterController subject;
    private SquareDivisionMasterController.FactoryHelper factoryHelper;
    private KeyPair keyPair;
    private DataLayer dataLayerMock;
    private SquareDivisionSenderAdapter senderMock;
    private SecureSquareDivisionMaster squareDivisionMock;

    @Before
    public void beforeEach() {
        dataLayerMock = mock(DataLayer.class);
        senderMock = mock(SquareDivisionSenderAdapter.class);

        keyPair = new KeyPairBuilder().generateKeyPair();
        factoryHelper = mock(SquareDivisionMasterController.FactoryHelper.class);

        squareDivisionMock = mock(SecureSquareDivisionMaster.class);
        SecureSquareDivisionMaster sdMock2 = mock(SecureSquareDivisionMaster.class);
        when(factoryHelper.finalize(keyPair)).thenReturn(squareDivisionMock, sdMock2);

        subject = new SquareDivisionMasterController(dataLayerMock, senderMock,
                keyPair, factoryHelper);
    }

    @Test
    public void compute_createsNewSquareDivisionMasterInstance() {
        List<NodeValuePair> path = new ArrayList<NodeValuePair>();
        subject.compute("attrName", "attrValue", path);

        verify(factoryHelper).finalize(keyPair);
    }

    @Test
    public void compute_createsMultiplications() {
        List<NodeValuePair> path = new ArrayList<NodeValuePair>();
        subject.compute("attrName", "attrValue", path);

        Map<Object, Long> il = new ConcurrentHashMap<>();
        List<MultiplicationResult> ol = new ArrayList<MultiplicationResult>();

        when(squareDivisionMock.createMultiplications(il)).thenReturn(ol);

        verify(squareDivisionMock).createMultiplications(il);
    }

    @Test
    public void compute_callsSenderAdapter() {
        List<NodeValuePair> path = new ArrayList<NodeValuePair>();

        subject.compute("attrName", "attrValue", path);

        Map<Object, Long> il = new ConcurrentHashMap<>();
        List<MultiplicationResult> ol = new ArrayList<MultiplicationResult>();
        when(squareDivisionMock.createMultiplications(il)).thenReturn(ol);

        ArgumentCaptor<SquareDivisionPojo> squareDivisionCaptor = ArgumentCaptor.forClass(SquareDivisionPojo.class);
        verify(senderMock).handleMultiplicationForwardStep(squareDivisionCaptor.capture());

        SquareDivisionPojo a = squareDivisionCaptor.getValue();
        assertEquals(0, a.getId());
        assertEquals("attrName", a.getAttrName());
        assertEquals("attrValue", a.getAttrValue());
        assertEquals(path, a.getPath());
        assertEquals(ol, a.getResults());
    }

    @Test
    public void multiplicationBackwardStep_forwardsCallToCorrectSquareDivisionInstance() {
        List<NodeValuePair> path = new ArrayList<NodeValuePair>();
        subject.compute("attrName", "attrValue", path);
        subject.compute("attrName", "attrValue", path);

        List<MultiplicationResult> mr = new ArrayList<MultiplicationResult>();
        SquareDivisionPojo pojo = new SquareDivisionPojo(0, "attrName", "attrValue", path, mr);

        subject.handleMultiplicationBackwardStep(pojo);

        verify(squareDivisionMock).handleMultiplicationBackwardStep(mr);
    }

    @Test
    public void multiplicationBackwardStep_callsSenderAdapter() {
        List<NodeValuePair> path = new ArrayList<NodeValuePair>();
        subject.compute("attrName", "attrValue", path);
        subject.compute("attrName", "attrValue", path);

        List<MultiplicationResult> mr = new ArrayList<MultiplicationResult>();
        SquareDivisionPojo pojo = new SquareDivisionPojo(0, "attrName", "attrValue", path, mr);

        AdditionResults ar = mock(AdditionResults.class);
        when(squareDivisionMock.handleMultiplicationBackwardStep(mr)).thenReturn(ar);

        subject.handleMultiplicationBackwardStep(pojo);
        verify(senderMock).handleAdditionForwardStep(0, ar);
    }

    @Test
    public void additionBackwardStep_forwardsCallToCorrectSquareDivisionInstance() {
        List<NodeValuePair> path = new ArrayList<NodeValuePair>();
        subject.compute("attrName", "attrValue", path);
        subject.compute("attrName", "attrValue", path);

        AdditionResults results = mock(AdditionResults.class);
        subject.handleAdditionBackwardStep(0, results);

        verify(squareDivisionMock).handleAdditionBackwardStep(results);
    }

    @Test
    public void additionBackwardstep_callsSenderAdapter() {
        List<NodeValuePair> path = new ArrayList<NodeValuePair>();
        subject.compute("attrName", "attrValue", path);
        subject.compute("attrName", "attrValue", path);

        AdditionResults results = mock(AdditionResults.class);
        subject.handleAdditionBackwardStep(0, results);

        verify(senderMock).collectOutputShares(0);
    }

    @Test
    public void compute_returnsFuture() {
        List<NodeValuePair> path = new ArrayList<NodeValuePair>();
        Future<GiniGainResult> f = subject.compute("attrName", "attrValue", path);

        assertNotNull(f);
    }

    @Test
    public void collectOutputShares_completesCorrectFuture() {
        List<NodeValuePair> path = new ArrayList<NodeValuePair>();
        Future<GiniGainResult> f = subject.compute("attrName", "attrValue", path);
        subject.compute("attrName", "attrValue", path);

        List<SquareDivisionResult> ar = new ArrayList<>();
        subject.handleCollectOutputShares(0, ar);

        assertTrue(f.isDone());
        assertFalse(f.isCancelled());
    }

    @Test
    public void collectOutputShares_completesFutureWithCorrectResult() throws InterruptedException, ExecutionException {
        List<NodeValuePair> path = new ArrayList<NodeValuePair>();
        Future<GiniGainResult> f = subject.compute("attrName", "attrValue", path);
        subject.compute("attrName", "attrValue", path);

        GiniGainResult e = new GiniGainResult("tt", 0.01);
        List<SquareDivisionResult> ar = new ArrayList<>();
        when(squareDivisionMock.computeResult(ar)).thenReturn(e);

        subject.handleCollectOutputShares(0, ar);

        assertEquals(e, f.get());
    }
}
