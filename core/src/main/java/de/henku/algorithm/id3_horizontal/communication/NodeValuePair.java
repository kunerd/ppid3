package de.henku.algorithm.id3_horizontal.communication;

public class NodeValuePair {
    private String node;
    private String value;

    public NodeValuePair(String node, String value) {
        this.node = node;
        this.value = value;
    }

    public String getNode() {
        return node;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "{ " + node + ", " + value + " }";
    }
}
