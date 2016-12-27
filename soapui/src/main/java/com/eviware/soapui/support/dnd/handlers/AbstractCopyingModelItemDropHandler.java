/*
 * SoapUI, Copyright (C) 2004-2016 SmartBear Software 
 *
 * Licensed under the EUPL, Version 1.1 or - as soon as they will be approved by the European Commission - subsequent 
 * versions of the EUPL (the "Licence"); 
 * You may not use this work except in compliance with the Licence. 
 * You may obtain a copy of the Licence at: 
 * 
 * http://ec.europa.eu/idabc/eupl 
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is 
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either 
 * express or implied. See the Licence for the specific language governing permissions and limitations 
 * under the Licence. 
 */

package com.eviware.soapui.support.dnd.handlers;

import com.eviware.soapui.model.ModelItem;

/**
 * ModelItemDropHandler that treats all move actions as copy actions and delegates method calls accordingly.
 *
 * @param <SourceClass> The class of the item being dragged.
 * @param <TargetClass> The class of the drop target.
 */
public abstract class AbstractCopyingModelItemDropHandler<SourceClass extends ModelItem, TargetClass extends ModelItem>
        extends AbstractModelItemDropHandler<SourceClass, TargetClass> {

    protected AbstractCopyingModelItemDropHandler(Class<SourceClass> sourceClass, Class<TargetClass> targetClass) {
        super(sourceClass, targetClass);
    }

    @Override
    final boolean canMoveBefore(SourceClass source, TargetClass target) {
        return canCopyBefore(source, target);
    }

    @Override
    final boolean canMoveOn(SourceClass source, TargetClass target) {
        return canCopyOn(source, target);
    }

    @Override
    final boolean moveBefore(SourceClass source, TargetClass target) {
        return canCopyBefore(source, target);
    }

    @Override
    final boolean moveOn(SourceClass source, TargetClass target) {
        return copyOn(source, target);
    }

    @Override
    final boolean canMoveAfter(SourceClass source, TargetClass target) {
        return canCopyAfter(source, target);
    }

    @Override
    final boolean moveAfter(SourceClass source, TargetClass target) {
        return copyAfter(source, target);
    }

    @Override
    final String getMoveBeforeInfo(SourceClass source, TargetClass target) {
        return getCopyBeforeInfo(source, target);
    }

    @Override
    final String getMoveOnInfo(SourceClass source, TargetClass target) {
        return getCopyOnInfo(source, target);
    }

    @Override
    final String getMoveAfterInfo(SourceClass source, TargetClass target) {
        return getCopyAfterInfo(source, target);
    }
}
