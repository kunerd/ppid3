/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Hendrik Kunert
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
