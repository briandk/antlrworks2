/*
 * [The "BSD license"]
 *  Copyright (c) 2012 Sam Harwell
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions
 *  are met:
 *  1. Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *  2. Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *  3. The name of the author may not be used to endorse or promote products
 *      derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 *  IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 *  OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 *  IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 *  INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 *  NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 *  THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.antlr.netbeans.semantics;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.openide.util.Parameters;

/**
 *
 * @author Sam Harwell
 */
public class ObjectDecorator<T> {
    private final Map<T, Map<ObjectProperty<?>, Object>> properties =
        new HashMap<T, Map<ObjectProperty<?>, Object>>();

    @NonNull
    public Map<? extends T, ? extends Map<? extends ObjectProperty<?>, ? extends Object>> getProperties() {
        return properties;
    }

    public void clear() {
        properties.clear();
    }

    @CheckForNull
    public <V> V getProperty(@NonNull T tree, @NonNull ObjectProperty<? extends V> property) {
        Parameters.notNull("tree", tree);
        Parameters.notNull("property", property);

        Map<ObjectProperty<?>, Object> nodeProperties = properties.get(tree);
        if (nodeProperties == null) {
            return property.getDefaultValue();
        }

        @SuppressWarnings("unchecked")
        V result = (V)nodeProperties.get(property);
        if (result == null) {
            return property.getDefaultValue();
        }

        return result;
    }

    @CheckForNull
    public <V> V putProperty(@NonNull T tree, @NonNull ObjectProperty<V> property, @NullAllowed V value) {
        Parameters.notNull("tree", tree);
        Parameters.notNull("property", property);

        Map<ObjectProperty<?>, Object> nodeProperties = properties.get(tree);
        if (nodeProperties == null) {
            nodeProperties = new HashMap<ObjectProperty<?>, Object>();
            properties.put(tree, nodeProperties);
        }

        @SuppressWarnings("unchecked")
        V previous = (V)nodeProperties.put(property, value);
        if (previous == null) {
            return property.getDefaultValue();
        }

        return previous;
    }

    @NonNull
    public Map<? extends ObjectProperty<?>, ? extends Object> getProperties(@NonNull T tree) {
        Parameters.notNull("tree", tree);

        Map<ObjectProperty<?>, Object> nodeProperties = this.properties.get(tree);
        if (nodeProperties == null) {
            return Collections.emptyMap();
        }

        return nodeProperties;
    }

    public void putProperties(@NonNull T tree, @NonNull Map<? extends ObjectProperty<?>, ? extends Object> properties) {
        Parameters.notNull("tree", tree);
        Parameters.notNull("properties", properties);

        Map<ObjectProperty<?>, Object> nodeProperties = this.properties.get(tree);
        if (nodeProperties == null) {
            nodeProperties = new HashMap<ObjectProperty<?>, Object>(properties);
            this.properties.put(tree, nodeProperties);
        } else {
            nodeProperties.putAll(properties);
        }
    }

    @CheckForNull
    public Map<? extends ObjectProperty<?>, ? extends Object> removeProperties(@NonNull T tree) {
        Parameters.notNull("tree", tree);

        return properties.remove(tree);
    }

}
