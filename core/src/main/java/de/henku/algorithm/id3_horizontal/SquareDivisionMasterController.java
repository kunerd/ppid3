package de.henku.algorithm.id3_horizontal;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import de.henku.algorithm.id3_horizontal.communication.NodeValuePair;
import de.henku.algorithm.id3_horizontal.communication.SquareDivisionPojo;
import de.henku.algorithm.id3_horizontal.communication.SquareDivisionReceiverAdapter;
import de.henku.algorithm.id3_horizontal.communication.SquareDivisionSenderAdapter;
import de.henku.jpaillier.KeyPair;

public class SquareDivisionMasterController implements
		SquareDivisionReceiverAdapter {

	private AtomicLong idCounter = new AtomicLong();
	private final SquareDivisionSenderAdapter sender;

	private ConcurrentHashMap<Long, SecureSquareDivisionMaster> divisions = new ConcurrentHashMap<Long, SecureSquareDivisionMaster>();

	private ConcurrentHashMap<Long, CompletableFuture<Double>> futures = new ConcurrentHashMap<Long, CompletableFuture<Double>>();

	private KeyPair keyPair;

	private final DataLayer dataLayer;
	private final FactoryHelper squareDivisionFactory;

	SquareDivisionMasterController(DataLayer dataLayer,
			SquareDivisionSenderAdapter sender, KeyPair keyPair,
			FactoryHelper helper) {

		this.dataLayer = dataLayer;
		this.sender = sender;
		this.keyPair = keyPair;
		this.squareDivisionFactory = helper;
	}

	public SquareDivisionMasterController(DataLayer dataLayer,
			SquareDivisionSenderAdapter sender, KeyPair keyPair) {
		this(dataLayer, sender, keyPair, new FactoryHelper());
	}

	public CompletableFuture<Double> compute(String attrName, String attrValue,
			List<NodeValuePair> path) {

		long id = idCounter.getAndIncrement();

		SecureSquareDivisionMaster d = squareDivisionFactory.finalize(keyPair);
		divisions.put(id, d);

		List<Pair<String, Long>> cpcv = dataLayer.countPerClassValue(path,
				attrName, attrValue);
		
		List<MultiplicationResult> results = d.createMultiplications(cpcv);

		SquareDivisionPojo pojo = new SquareDivisionPojo(id, attrName,
				attrValue, path, results);

		CompletableFuture<Double> f = new CompletableFuture<Double>();
		futures.put(id, f);
		
		sender.handleMultiplicationForwardStep(pojo);

		return f;
	}

	public void handleMultiplicationBackwardStep(SquareDivisionPojo data) {
		long id = data.getId();

		SecureSquareDivisionMaster sD = divisions.get(id);
		AdditionResults r = sD.handleMultiplicationBackwardStep(data
				.getResults());

		sender.handleAdditionForwardStep(id, r);
	}

	@Override
	public void handleAdditionBackwardStep(
			long squareID,
			AdditionResults results) {
		SecureSquareDivisionMaster d = divisions.get(squareID);
		d.handleAdditionBackwardStep(results);

		sender.collectOutputShares(squareID);
	}

	@Override
	public void handleCollectOutputShares(long squareID,
			List<AdditionResults> outputShares) {
		SecureSquareDivisionMaster d = divisions.get(squareID);
		
		Double result = d.computeResult(outputShares);
		
		CompletableFuture<Double> f = futures.get(squareID);
		f.complete(result);
	}

	static class FactoryHelper {
		SecureSquareDivisionMaster finalize(KeyPair kp) {
			return new SecureSquareDivisionMaster(kp);
		}
	}
}
