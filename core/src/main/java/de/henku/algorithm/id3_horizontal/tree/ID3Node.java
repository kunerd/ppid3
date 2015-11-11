package de.henku.algorithm.id3_horizontal.tree;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ID3Node {

    private Object label;
    private ConcurrentHashMap<Object, ID3Node> children = new ConcurrentHashMap<>();

    public ID3Node(Object label) {
        this.label = label;
    }

    public Object getLabel() {
        return label;
    }

    public void add(Object edge, ID3Node node) {
        children.put(edge, node);
    }

    public Set<Object> getEdges() {
        return children.keySet();
    }

    public Collection<ID3Node> getChildren() {
        return children.values();
    }

    public ID3Node getChild(Object edge) {
        return children.get(edge);
    }

    @Override
    public String toString() {
        return toString(new StringBuilder(), "", false);
    }

    private String toString(StringBuilder in, String value, boolean last) {
        StringBuilder b = new StringBuilder();

        b.append(in.toString());

        if (value.equals("")) {
            b.append(this.getLabel());
            b.append("\n");
        } else {
            if (last) {
                b.append("└╴ ");
                b.append(value);
                b.append(" -> ");
                in.append("     ");
            } else {
                b.append("├╴ ");
                b.append(value);
                b.append(" -> ");
                in.append("│    ");
            }

            b.append(this.getLabel());
            b.append("\n");

            if (last && getEdges().isEmpty()) {
                b.append(in.toString());
                b.append("\n");
            }
        }

        Iterator<Object> iter = getEdges().iterator();
        while (iter.hasNext()) {
            Object e = iter.next();

            b.append(getChild(e).toString(new StringBuilder(in), e.toString(), !iter.hasNext()));
        }
        return b.toString();
    }
}
