package de.henku.example.id3.utils;

import de.henku.algorithm.id3_horizontal.Attribute;
import de.henku.algorithm.id3_horizontal.AttributeBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class ListAttributeBuilder<T extends HashMap<String, String>> extends AttributeBuilder {

    public ListAttributeBuilder(String name) {
        super(name);
    }

    public Attribute from_transactions(List<T> transactions) {
        List<String> values = transactions.stream()
                .map(p -> p.get(super.name()))
                .distinct()
                .collect(Collectors.toList());

        super.values(values);

        return new Attribute(this);
    }

    public Attribute from_values(List<String> values) {
        super.values(values);

        return new Attribute(this);
    }

}
