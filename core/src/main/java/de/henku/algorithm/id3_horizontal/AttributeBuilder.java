package de.henku.algorithm.id3_horizontal;

import java.util.List;

public abstract class AttributeBuilder {
    private final String name;
    private List<String> values;

    public AttributeBuilder(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }

    public AttributeBuilder values(List<String> values) {
        this.values = values;
        return this;
    }

    public List<String> values() {
        return values;
    }

//	public Attribute from_transactions(List<Row> transactions) {
//		this.values = transactions.stream()
//				.map(p -> p.get(name))
//				.distinct()
//				.collect(Collectors.toList());
//		
//		return new Attribute(this);
//	}

//	public Attribute from_values(List<String> values) {
//		this.values = values;
//		
//		return new Attribute(this);
//	}
}
