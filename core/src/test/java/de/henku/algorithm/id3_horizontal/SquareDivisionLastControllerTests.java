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
import de.henku.algorithm.id3_horizontal.communication.SquareDivisionReceiverAdapter;
import de.henku.jpaillier.PublicKey;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class SquareDivisionLastControllerTests {

    private PublicKey publicKeyMock;
    private DataLayer dataLayerMock;
    private SquareDivisionLastController subject;
    private SquareDivisionLastController.FactoryHelper factoryHelperMock;
    private SecureSquareDivisionSlave compMock1, compMock2;
    private SquareDivisionReceiverAdapter recieverMock;

    @Before
    public void beforeEach() {
        dataLayerMock = mock(DataLayer.class);
        publicKeyMock = mock(PublicKey.class);

        compMock1 = mock(SecureSquareDivisionSlave.class);
        compMock2 = mock(SecureSquareDivisionSlave.class);
        factoryHelperMock = mock(SquareDivisionLastController.FactoryHelper.class);
        when(factoryHelperMock.finalize(publicKeyMock)).thenReturn(compMock1, compMock2);

        subject = new SquareDivisionLastController(dataLayerMock, publicKeyMock, factoryHelperMock);

        recieverMock = mock(SquareDivisionReceiverAdapter.class);
        subject.setReceiver(recieverMock);
    }

    @Test
    public void multiplicationForwardStep_createsSquareDivisionSlave() {
        List<NodeValuePair> path = new ArrayList<>();

        List<MultiplicationResult> results = new ArrayList<>();
        SquareDivisionPojo pojo = new SquareDivisionPojo(0, "attrName", "attrValue", path, results);

        subject.handleMultiplicationForwardStep(pojo);

        verify(factoryHelperMock).finalize(publicKeyMock);
    }

    @Test
    public void multiplicationForwardStep_computesForwardStepOnCreatedInstance() {
        List<NodeValuePair> path = new ArrayList<>();

        List<MultiplicationResult> results = new ArrayList<>();
        SquareDivisionPojo pojo = new SquareDivisionPojo(0, "attrName", "attrValue", path, results);

        Map<Object, Long> counts = new ConcurrentHashMap<>();
        when(dataLayerMock.countPerClassValue(path, "attrName", "attrValue")).thenReturn(counts);

        subject.handleMultiplicationForwardStep(pojo);

        verify(compMock1).handleMultiplicationForwardStep(counts, results);

    }

    @Test
    public void multiplicationForwardStep_computesBackwardStepOnCreatedInstance() {
        List<NodeValuePair> path = new ArrayList<>();

        List<MultiplicationResult> results = new ArrayList<>();
        SquareDivisionPojo pojo = new SquareDivisionPojo(0, "attrName", "attrValue", path, results);

        Map<Object, Long> counts = new ConcurrentHashMap<>();
        when(dataLayerMock.countPerClassValue(path, "attrName", "attrValue")).thenReturn(counts);

        List<MultiplicationResult> forwardResults = new ArrayList<>();
        when(compMock1.handleMultiplicationForwardStep(counts, results)).thenReturn(forwardResults);
        subject.handleMultiplicationForwardStep(pojo);

        verify(compMock1).handleMultiplicationBackwardStep(forwardResults);

    }

    @Test
    public void multiplicationForwardStep_callsRecieverAdapterWithCorrectValues() {
        List<NodeValuePair> path = new ArrayList<>();

        List<MultiplicationResult> results = new ArrayList<>();
        SquareDivisionPojo pojo = new SquareDivisionPojo(0, "attrName", "attrValue", path, results);

        Map<Object, Long> counts = new ConcurrentHashMap<>();
        when(dataLayerMock.countPerClassValue(path, "attrName", "attrValue")).thenReturn(counts);

        List<MultiplicationResult> forwardResults = new ArrayList<>();
        when(compMock1.handleMultiplicationForwardStep(counts, results)).thenReturn(forwardResults);

        List<MultiplicationResult> backResults = new ArrayList<>();
        when(compMock1.handleMultiplicationBackwardStep(forwardResults)).thenReturn(backResults);

        subject.handleMultiplicationForwardStep(pojo);

        ArgumentCaptor<SquareDivisionPojo> squareDivisionCaptor = ArgumentCaptor.forClass(SquareDivisionPojo.class);
        verify(recieverMock).handleMultiplicationBackwardStep(squareDivisionCaptor.capture());

        SquareDivisionPojo a = squareDivisionCaptor.getValue();
        assertEquals(0, a.getId());
        assertEquals("attrName", a.getAttrName());
        assertEquals("attrValue", a.getAttrValue());
        assertEquals(path, a.getPath());
        assertEquals(backResults, a.getResults());
    }

    @Test
    public void additionForwardStep_computesForwardStepOnCorrectInstance() {
        List<NodeValuePair> path = new ArrayList<>();

        List<MultiplicationResult> mr = new ArrayList<>();
        SquareDivisionPojo pojo1 = new SquareDivisionPojo(0, "attrName", "attrValue", path, mr);
        SquareDivisionPojo pojo2 = new SquareDivisionPojo(1, "attrName", "attrValue", path, mr);

        subject.handleMultiplicationForwardStep(pojo1);
        subject.handleMultiplicationForwardStep(pojo2);

        AdditionResults results = mock(AdditionResults.class);

        subject.handleAdditionForwardStep(0, results);
        verify(compMock1).handleAdditionForwardStep(results);
        verify(compMock2, never()).handleAdditionBackwardStep(results);

        subject.handleAdditionForwardStep(1, results);
        verify(compMock2).handleAdditionForwardStep(results);
        verify(compMock1, never()).handleAdditionBackwardStep(results);
    }

    @Test
    public void additionForwardStep_computesBackwardStepOnCorrectInstance() {
        List<NodeValuePair> path = new ArrayList<>();

        List<MultiplicationResult> mr = new ArrayList<>();
        SquareDivisionPojo pojo1 = new SquareDivisionPojo(0, "attrName", "attrValue", path, mr);
        SquareDivisionPojo pojo2 = new SquareDivisionPojo(1, "attrName", "attrValue", path, mr);

        subject.handleMultiplicationForwardStep(pojo1);
        subject.handleMultiplicationForwardStep(pojo2);

        AdditionResults results = mock(AdditionResults.class);
        AdditionResults frMock1 = mock(AdditionResults.class);
        AdditionResults frMock2 = mock(AdditionResults.class);
        when(compMock1.handleAdditionForwardStep(results)).thenReturn(frMock1);
        when(compMock2.handleAdditionForwardStep(results)).thenReturn(frMock2);

        subject.handleAdditionForwardStep(0, results);
        verify(compMock1).handleAdditionBackwardStep(frMock1);
        verify(compMock2, never()).handleAdditionBackwardStep(frMock1);

        subject.handleAdditionForwardStep(1, results);
        verify(compMock2).handleAdditionBackwardStep(frMock2);
        verify(compMock1, never()).handleAdditionBackwardStep(frMock2);
    }

    @Test
    public void additionForwardStep_callsRecieverAdapterWithCorrectValues() {
        List<NodeValuePair> path = new ArrayList<>();

        List<MultiplicationResult> mr = new ArrayList<>();
        SquareDivisionPojo pojo1 = new SquareDivisionPojo(0, "attrName", "attrValue", path, mr);
        SquareDivisionPojo pojo2 = new SquareDivisionPojo(1, "attrName", "attrValue", path, mr);

        subject.handleMultiplicationForwardStep(pojo1);
        subject.handleMultiplicationForwardStep(pojo2);

        AdditionResults results = mock(AdditionResults.class);

        AdditionResults frMock1 = mock(AdditionResults.class);
        AdditionResults frMock2 = mock(AdditionResults.class);
        when(compMock1.handleAdditionForwardStep(results)).thenReturn(frMock1);
        when(compMock2.handleAdditionForwardStep(results)).thenReturn(frMock2);

        AdditionResults brMock1 = mock(AdditionResults.class);
        AdditionResults brMock2 = mock(AdditionResults.class);
        when(compMock1.handleAdditionBackwardStep(frMock1)).thenReturn(brMock1);
        when(compMock2.handleAdditionBackwardStep(frMock2)).thenReturn(brMock2);

        subject.handleAdditionForwardStep(0, results);
        verify(recieverMock).handleAdditionBackwardStep(0, brMock1);

        subject.handleAdditionForwardStep(1, results);
        verify(recieverMock).handleAdditionBackwardStep(1, brMock2);
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void collectOutputShares_callsRecieverWithCorrectValues() {
        List<NodeValuePair> path = new ArrayList<>();

        List<MultiplicationResult> mr = new ArrayList<>();
        SquareDivisionPojo pojo1 = new SquareDivisionPojo(0, "attrName", "attrValue", path, mr);
        SquareDivisionPojo pojo2 = new SquareDivisionPojo(1, "attrName", "attrValue", path, mr);

        subject.handleMultiplicationForwardStep(pojo1);
        subject.handleMultiplicationForwardStep(pojo2);

        SquareDivisionResult e1 = mock(SquareDivisionResult.class);
        when(compMock1.getAdditionOutputShares()).thenReturn(e1);
        subject.collectOutputShares(0);

        ArgumentCaptor<List<SquareDivisionResult>> argument = ArgumentCaptor.forClass((Class) List.class);
        verify(recieverMock).handleCollectOutputShares(eq(0l), argument.capture());
        SquareDivisionResult a1 = argument.getValue().get(0);
        assertEquals(e1, a1);

        SquareDivisionResult e2 = mock(SquareDivisionResult.class);
        when(compMock2.getAdditionOutputShares()).thenReturn(e2);
        subject.collectOutputShares(1l);

        argument = ArgumentCaptor.forClass((Class) List.class);
        verify(recieverMock).handleCollectOutputShares(eq(1l), argument.capture());
        SquareDivisionResult a2 = argument.getValue().get(0);
        assertEquals(e2, a2);
    }
}
