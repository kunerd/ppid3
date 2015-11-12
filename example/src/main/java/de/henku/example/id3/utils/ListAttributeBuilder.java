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
