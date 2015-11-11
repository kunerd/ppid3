package de.henku.algorithm.id3_horizontal.communication;

import de.henku.algorithm.id3_horizontal.AdditionResults;


public interface SquareDivisionSenderAdapter {

	public void handleMultiplicationForwardStep(SquareDivisionPojo pojo);
	public void handleAdditionForwardStep(long squareID, AdditionResults pojo);
	public void collectOutputShares(long squareID);
}
