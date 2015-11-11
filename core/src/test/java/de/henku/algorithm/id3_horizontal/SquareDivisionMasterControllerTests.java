package de.henku.algorithm.id3_horizontal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import de.henku.algorithm.id3_horizontal.communication.NodeValuePair;
import de.henku.algorithm.id3_horizontal.communication.SquareDivisionPojo;
import de.henku.algorithm.id3_horizontal.communication.SquareDivisionSenderAdapter;
import de.henku.jpaillier.KeyPair;
import de.henku.jpaillier.KeyPairBuilder;

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
	public void compute_returnsFuture() {
		List<NodeValuePair> path = new ArrayList<NodeValuePair>();
		Future<Double> f = subject.compute("attrName", "attrValue", path);

		assertNotNull(f);
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
	public void multiplicationBackwardStep_forwardsCallToCorrectSquareDivisionInstance(){
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
	public void collectOutputShares_completesCorrectFuture() {
		List<NodeValuePair> path = new ArrayList<NodeValuePair>();
		Future<Double> f = subject.compute("attrName", "attrValue", path);
		subject.compute("attrName", "attrValue", path);

		List<AdditionResults> ar = new ArrayList<AdditionResults>();
		subject.handleCollectOutputShares(0, ar);
		
		assertTrue(f.isDone());
		assertFalse(f.isCancelled());
	}
	
	@Test
	public void collectOutputShares_completesFutureWithCorrectResult() throws InterruptedException, ExecutionException {
		List<NodeValuePair> path = new ArrayList<NodeValuePair>();
		Future<Double> f = subject.compute("attrName", "attrValue", path);
		subject.compute("attrName", "attrValue", path);

		Double e = 0.01;
		List<AdditionResults> ar = new ArrayList<AdditionResults>();
		when(squareDivisionMock.computeResult(ar)).thenReturn(e);

		subject.handleCollectOutputShares(0, ar);
		
		assertEquals(e, f.get());
	}
}
