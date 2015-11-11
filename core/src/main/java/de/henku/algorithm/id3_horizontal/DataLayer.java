package de.henku.algorithm.id3_horizontal;

import java.util.List;
import java.util.Map;

import de.henku.algorithm.id3_horizontal.communication.NodeValuePair;

public interface DataLayer<T> {
	Map<T, Long> countPerClassValue(
			List<NodeValuePair> path,
			String attrName,
			String attrValue
	);
}
