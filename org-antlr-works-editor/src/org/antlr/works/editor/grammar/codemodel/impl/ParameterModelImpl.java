/*
 *  Copyright (c) 2012 Sam Harwell, Tunnel Vision Laboratories LLC
 *  All rights reserved.
 *
 *  The source code of this document is proprietary work, and is not licensed for
 *  distribution. For information about licensing, contact Sam Harwell at:
 *      sam@tunnelvisionlabs.com
 */
package org.antlr.works.editor.grammar.codemodel.impl;

import org.antlr.works.editor.grammar.codemodel.ParameterModel;

/**
 *
 * @author Sam Harwell
 */
public class ParameterModelImpl extends AbstractAttributeModel implements ParameterModel {

    public ParameterModelImpl(String name, String type, FileModelImpl file) {
        super(name, type, file);
    }

}
