/*
 * Copyright 2004-2005 The Apache Software Foundation or its licensors,
 *                     as applicable.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jackrabbit.core.xml;

import javax.jcr.ItemExistsException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.version.VersionException;

import org.apache.jackrabbit.core.BatchedItemOperations;
import org.apache.jackrabbit.core.NodeImpl;
import org.apache.jackrabbit.core.PropertyId;
import org.apache.jackrabbit.core.nodetype.EffectiveNodeType;
import org.apache.jackrabbit.core.nodetype.NodeTypeRegistry;
import org.apache.jackrabbit.core.nodetype.PropDef;
import org.apache.jackrabbit.core.state.NodeState;
import org.apache.jackrabbit.core.state.PropertyState;
import org.apache.jackrabbit.core.util.ReferenceChangeTracker;
import org.apache.jackrabbit.core.value.InternalValue;
import org.apache.jackrabbit.name.NamespaceResolver;
import org.apache.jackrabbit.name.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropInfo {

    private static Logger log = LoggerFactory.getLogger(PropInfo.class);

    private final QName name;

    private final int type;

    private final TextValue[] values;

    public PropInfo(QName name, int type, TextValue[] values) {
        this.name = name;
        this.type = type;
        this.values = values;
    }

    /**
     * Disposes all values contained in this property.
     */
    public void dispose() {
        for (int i = 0; i < values.length; i++) {
            values[i].dispose();
        }
    }

    private int getTargetType(PropDef def) {
        int target = def.getRequiredType();
        if (target != PropertyType.UNDEFINED) {
            return target;
        } else if (type != PropertyType.UNDEFINED) {
            return type;
        } else {
            return PropertyType.STRING;
        }
    }

    private PropDef getApplicablePropertyDef(EffectiveNodeType ent)
            throws ConstraintViolationException {
        if (values.length == 1) {
            // could be single- or multi-valued (n == 1)
            return ent.getApplicablePropertyDef(name, type);
        } else {
            // can only be multi-valued (n == 0 || n > 1)
            return ent.getApplicablePropertyDef(name, type, true);
        }
    }

    public void apply(NodeImpl node, NamespaceResolver resolver, ReferenceChangeTracker refTracker) throws RepositoryException, ConstraintViolationException, ValueFormatException, VersionException, LockException, ItemNotFoundException {
        // find applicable definition
        PropDef def = getApplicablePropertyDef(node.getEffectiveNodeType());
        if (def.isProtected()) {
            // skip protected property
            log.debug("skipping protected property " + name);
            return;
        }
        
        // convert serialized values to Value objects
        Value[] va = new Value[values.length];
        int targetType = getTargetType(def);
        for (int i = 0; i < values.length; i++) {
            va[i] = values[i].getValue(targetType, resolver);
        }
        
        // multi- or single-valued property?
        if (va.length == 1) {
            // could be single- or multi-valued (n == 1)
            try {
                // try setting single-value
                node.setProperty(name, va[0]);
            } catch (ValueFormatException vfe) {
                // try setting value array
                node.setProperty(name, va, type);
            } catch (ConstraintViolationException cve) {
                // try setting value array
                node.setProperty(name, va, type);
            }
        } else {
            // can only be multi-valued (n == 0 || n > 1)
            node.setProperty(name, va, type);
        }
        if (type == PropertyType.REFERENCE) {
            // store reference for later resolution
            refTracker.processedReference(node.getProperty(name));
        }
    }

    public void apply(NodeState node, BatchedItemOperations itemOps, NodeTypeRegistry ntReg, ReferenceChangeTracker refTracker) throws ItemNotFoundException, RepositoryException, ItemExistsException, ConstraintViolationException, ValueFormatException {
        PropertyState prop = null;
        PropDef def = null;

        if (node.hasPropertyName(name)) {
            // a property with that name already exists...
            PropertyId idExisting = new PropertyId(node.getNodeId(), name);
            prop = (PropertyState) itemOps.getItemState(idExisting);
            def = ntReg.getPropDef(prop.getDefinitionId());
            if (def.isProtected()) {
                // skip protected property
                log.debug("skipping protected property "
                        + itemOps.safeGetJCRPath(idExisting));
                return;
            }
            if (!def.isAutoCreated()
                    || (prop.getType() != type && type != PropertyType.UNDEFINED)
                    || def.isMultiple() != prop.isMultiValued()) {
                throw new ItemExistsException(itemOps.safeGetJCRPath(prop.getPropertyId()));
            }
        } else {
            // there's no property with that name,
            // find applicable definition
            def = getApplicablePropertyDef(itemOps.getEffectiveNodeType(node));
            if (def.isProtected()) {
                // skip protected property
                log.debug("skipping protected property " + name);
                return;
            }
            
            // create new property
            prop = itemOps.createPropertyState(node, name, type, def);
        }

        // check multi-valued characteristic
        if (values.length != 1 && !def.isMultiple()) {
            throw new ConstraintViolationException(itemOps.safeGetJCRPath(prop.getPropertyId())
                    + " is not multi-valued");
        }

        // convert serialized values to InternalValue objects
        int targetType = getTargetType(def);
        InternalValue[] iva = new InternalValue[values.length];
        for (int i = 0; i < values.length; i++) {
            iva[i] = values[i].getInternalValue(targetType);
        }

        // set values
        prop.setValues(iva);

        // make sure property is valid according to its definition
        itemOps.validate(prop);

        if (prop.getType() == PropertyType.REFERENCE) {
            // store reference for later resolution
            refTracker.processedReference(prop);
        }

        // store property
        itemOps.store(prop);
    }

}
