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

package de.henku.example.id3.utils;

import de.henku.algorithm.id3_horizontal.Attribute;
import de.henku.algorithm.id3_horizontal.DataLayer;
import de.henku.algorithm.id3_horizontal.communication.NodeValuePair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ListDataLayer<T extends HashMap<String, String>> implements DataLayer {

    private List<T> transactions;
    private Attribute classAttribute;

    public ListDataLayer(List<T> transactions, Attribute classAttribute) {
        this.transactions = transactions;

        this.classAttribute = classAttribute;
    }

    @Override
    public Map<Object, Long> countPerClassValue(
            List<NodeValuePair> path, String attrName, String attrValue) {

        List<T> subsetForPath = new ArrayList<>(transactions);

        for (NodeValuePair nvp : path) {
            subsetForPath = subsetForPath.stream()
                    .filter(p -> p.get(nvp.getNode()).equals(nvp.getValue()))
                    .collect(Collectors.toList());

        }

        List<T> subsetForValue = subsetForPath.stream()
                .filter(p -> p.get(attrName).equals(attrValue))
                .collect(Collectors.toList());

        String className = classAttribute.getName();
        Map<Object, Long> result = new ConcurrentHashMap<>();
        for (String classValue : classAttribute.getValues()) {
            long count = subsetForValue.stream()
                    .filter(p -> p.get(className).equals(classValue))
                    .count();

            result.put(classValue, count);
        }

        return result;
    }

}
