package de.henku.algorithm.id3_horizontal;

import de.henku.algorithm.id3_horizontal.communication.NodeValuePair;

import java.util.List;
import java.util.Map;

public interface DataLayer {
    Map<Object, Long> countPerClassValue(
            List<NodeValuePair> path,
            String attrName,
            String attrValue
    );
}
