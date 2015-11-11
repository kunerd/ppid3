package de.henku.algorithm.id3_horizontal.communication;

import de.henku.algorithm.id3_horizontal.AdditionResults;


public interface SquareDivisionSenderAdapter {
    void handleMultiplicationForwardStep(SquareDivisionPojo pojo);

    void handleAdditionForwardStep(long squareID, AdditionResults pojo);

    void collectOutputShares(long squareID);
}
