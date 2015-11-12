package de.henku.algorithm.id3_horizontal.communication;

import de.henku.algorithm.id3_horizontal.AdditionResults;
import de.henku.algorithm.id3_horizontal.SquareDivisionResult;

import java.util.List;

public interface SquareDivisionReceiverAdapter {
    void handleMultiplicationBackwardStep(SquareDivisionPojo data);

    void handleAdditionBackwardStep(long squareID,
                                    AdditionResults results);

    void handleCollectOutputShares(long squareID,
                                   List<SquareDivisionResult> outputShares);
}
