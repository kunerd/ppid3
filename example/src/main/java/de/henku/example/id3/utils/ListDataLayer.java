package de.henku.example.id3.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import de.henku.algorithm.id3_horizontal.Attribute;
import de.henku.algorithm.id3_horizontal.DataLayer;
import de.henku.algorithm.id3_horizontal.Pair;
import de.henku.algorithm.id3_horizontal.communication.NodeValuePair;

public class ListDataLayer<T extends HashMap<String, String>> implements DataLayer {
	
	private List<T> transactions;
	private Attribute classAttribute;
	
	public ListDataLayer(List<T> transactions, Attribute classAttribute) {
		this.transactions = transactions;
		
		this.classAttribute = classAttribute;
	}

	@Override
	public List<Pair<String, Long>> countPerClassValue(
			List<NodeValuePair> path, String attrName, String attrValue) {

		List<T> subsetForPath = new ArrayList<>(transactions);
		
		for (NodeValuePair nvp : path) {
			subsetForPath = subsetForPath.stream()
					.filter(p -> p.get(nvp.getNode()).equals(nvp.getValue()))
					.collect(Collectors.toList());
			
		}
		
		List<T> subsetForValue = subsetForPath.stream()
				.filter( p -> p.get(attrName).equals(attrValue))
				.collect(Collectors.toList());

		String className = classAttribute.getName();
		List<Pair<String, Long>> result = new ArrayList<>();
		for (String classValue : classAttribute.getValues()) {
			long count = subsetForValue.stream()
					.filter( p -> p.get(className).equals(classValue))
					.count();

			result.add(new Pair<>(classValue, count));
		}

		return result;
	}

}
