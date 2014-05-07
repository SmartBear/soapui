/*
 *  SoapUI, copyright (C) 2004-2014 smartbear.com
 *
 *  SoapUI is free software; you can redistribute it and/or modify it under the
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  SoapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
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
