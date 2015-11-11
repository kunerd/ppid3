package de.henku.algorithm.id3_horizontal.communication;

import java.util.List;

import de.henku.algorithm.id3_horizontal.AdditionResults;

public interface SquareDivisionReceiverAdapter {
	public void handleMultiplicationBackwardStep(SquareDivisionPojo data);

	public void handleAdditionBackwardStep(long squareID,
			AdditionResults results);

	public void handleCollectOutputShares(long squareID,
			List<AdditionResults> outputShares);
}
