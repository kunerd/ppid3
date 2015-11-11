package de.henku.algorithm.id3_horizontal;

import java.util.List;

public class Attribute {

	private final String name;
	private final List<String> values;
	
	public Attribute(AttributeBuilder b) {
		this.name = b.name();
		this.values = b.values();

	}
	
	public String getName() {
		return name;
	}
	
	public List<String> getValues() {
		return values;
	}
	
	@Override
	public String toString() {
		StringBuilder b = new StringBuilder(name);
		b.append(": ");
		b.append(values);
		return b.toString();
	}
}

