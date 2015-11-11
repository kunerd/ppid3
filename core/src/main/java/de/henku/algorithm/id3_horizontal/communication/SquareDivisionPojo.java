package de.henku.algorithm.id3_horizontal.communication;

import java.util.List;

import de.henku.algorithm.id3_horizontal.MultiplicationResult;

public class SquareDivisionPojo {

	private long id;
	private String attrName;
	private String attrValue;
	private List<NodeValuePair> path;
	private List<MultiplicationResult> results;
	
	public SquareDivisionPojo(
			long id,
			String attrName,
			String attrValue,
			List<NodeValuePair> path,
			List<MultiplicationResult> results) {

		this.id = id;
		this.attrName = attrName;
		this.attrValue = attrValue;
		this.path = path;
		this.results = results;
	}
	
	public long getId() {
		return id;
	}
	
	public String getAttrName() {
		return attrName;
	}
	
	public String getAttrValue() {
		return attrValue;
	}
	
	public List<NodeValuePair> getPath() {
		return path;
	}
	
	public List<MultiplicationResult> getResults() {
		return results;
	}
}
