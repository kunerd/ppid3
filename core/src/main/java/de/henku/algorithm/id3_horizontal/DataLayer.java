package de.henku.algorithm.id3_horizontal;

import java.util.List;

import de.henku.algorithm.id3_horizontal.communication.NodeValuePair;

// FIXME rename
public interface DataLayer {
	
	// FIXME make types generic
	public List<Pair<String, Long>> countPerClassValue(
			List<NodeValuePair> path,
			String attrName,
			String attrValue
	);
}
