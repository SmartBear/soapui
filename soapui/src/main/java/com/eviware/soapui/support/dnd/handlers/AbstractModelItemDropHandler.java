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

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.support.dnd.DropType;
import com.eviware.soapui.support.dnd.ModelItemDropHandler;

import java.awt.dnd.DnDConstants;

public abstract class AbstractModelItemDropHandler<T1 extends ModelItem, T2 extends ModelItem> implements
        ModelItemDropHandler<ModelItem> {
    private final Class<T1> sourceClass;
    private final Class<T2> targetClass;

    protected AbstractModelItemDropHandler(Class<T1> sourceClass, Class<T2> targetClass) {
        this.sourceClass = sourceClass;
        this.targetClass = targetClass;
    }

    @SuppressWarnings("unchecked")
    public boolean canDrop(ModelItem source, ModelItem target, int dropAction, int dropType) {
        try {
            if (sourceClass.isAssignableFrom(source.getClass()) && targetClass.isAssignableFrom(target.getClass())) {
                T1 sourceItem = (T1) source;
                T2 targetItem = (T2) target;

                // System.out.println( "in canDrop for " +
                // sourceItem.getClass().getName() + " to "
                // + targetItem.getClass().getName() );

                if ((dropAction & DnDConstants.ACTION_COPY) != 0) {
                    return canCopy(sourceItem, targetItem, dropType);
                }

                if ((dropAction & DnDConstants.ACTION_MOVE) != 0) {
                    return canMove(sourceItem, targetItem, dropType);
                }
            }
        } catch (Exception e) {
            SoapUI.logError(e);
        }

        return false;
    }

    private boolean canMove(T1 sourceItem, T2 targetItem, int dropType) {
        switch (dropType) {
            case DropType.BEFORE:
                return canMoveBefore(sourceItem, targetItem);
            case DropType.AFTER:
                return canMoveAfter(sourceItem, targetItem);
            case DropType.ON:
                return canMoveOn(sourceItem, targetItem);
        }

        return false;
    }

    private boolean canCopy(T1 sourceItem, T2 targetItem, int dropType) {
        switch (dropType) {
            case DropType.BEFORE:
                return canCopyBefore(sourceItem, targetItem);
            case DropType.AFTER:
                return canCopyAfter(sourceItem, targetItem);
            case DropType.ON:
                return canCopyOn(sourceItem, targetItem);
        }

        return false;
    }

    abstract boolean canCopyBefore(T1 source, T2 target);

    abstract boolean canMoveBefore(T1 source, T2 target);

    abstract boolean canMoveOn(T1 source, T2 target);

    abstract boolean canCopyOn(T1 source, T2 target);

    abstract boolean copyBefore(T1 source, T2 target);

    abstract boolean moveBefore(T1 source, T2 target);

    abstract boolean moveOn(T1 source, T2 target);

    abstract boolean canCopyAfter(T1 source, T2 target);

    abstract boolean canMoveAfter(T1 source, T2 target);

    abstract boolean copyAfter(T1 source, T2 target);

    abstract boolean copyOn(T1 source, T2 target);

    abstract boolean moveAfter(T1 source, T2 target);

    abstract String getCopyBeforeInfo(T1 source, T2 target);

    abstract String getMoveBeforeInfo(T1 source, T2 target);

    abstract String getCopyOnInfo(T1 source, T2 target);

    abstract String getMoveOnInfo(T1 source, T2 target);

    abstract String getCopyAfterInfo(T1 source, T2 target);

    abstract String getMoveAfterInfo(T1 source, T2 target);

    @SuppressWarnings("unchecked")
    public String getDropInfo(ModelItem source, ModelItem target, int dropAction, int dropType) {
        try {
            if (sourceClass.isAssignableFrom(source.getClass()) && targetClass.isAssignableFrom(target.getClass())) {
                T1 sourceItem = (T1) source;
                T2 targetItem = (T2) target;

                if ((dropAction & DnDConstants.ACTION_COPY) != 0) {
                    return getCopyInfo(sourceItem, targetItem, dropType);
                }

                if ((dropAction & DnDConstants.ACTION_MOVE) != 0) {
                    return getMoveInfo(sourceItem, targetItem, dropType);
                }
            }
        } catch (Exception e) {
            SoapUI.logError(e);
        }

        return null;
    }

    private String getMoveInfo(T1 sourceItem, T2 targetItem, int dropType) {
        switch (dropType) {
            case DropType.BEFORE:
                return getMoveBeforeInfo(sourceItem, targetItem);
            case DropType.AFTER:
                return getMoveAfterInfo(sourceItem, targetItem);
            case DropType.ON:
                return getMoveOnInfo(sourceItem, targetItem);
        }

        return null;
    }

    private String getCopyInfo(T1 sourceItem, T2 targetItem, int dropType) {
        switch (dropType) {
            case DropType.BEFORE:
                return getCopyBeforeInfo(sourceItem, targetItem);
            case DropType.AFTER:
                return getCopyAfterInfo(sourceItem, targetItem);
            case DropType.ON:
                return getCopyOnInfo(sourceItem, targetItem);
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    public boolean drop(ModelItem source, ModelItem target, int dropAction, int dropType) {
        try {
            if (sourceClass.isAssignableFrom(source.getClass()) && targetClass.isAssignableFrom(target.getClass())) {
                T1 sourceItem = (T1) source;
                T2 targetItem = (T2) target;

                if ((dropAction & DnDConstants.ACTION_COPY) != 0 && canCopy(sourceItem, targetItem, dropType)) {
                    return copy(sourceItem, targetItem, dropType);
                }

                if ((dropAction & DnDConstants.ACTION_MOVE) != 0 && canMove(sourceItem, targetItem, dropType)) {
                    return move(sourceItem, targetItem, dropType);
                }
            }
        } catch (Exception e) {
            SoapUI.logError(e);
        }

        return false;
    }

    private boolean move(T1 sourceItem, T2 targetItem, int dropType) {
        switch (dropType) {
            case DropType.BEFORE:
                return moveBefore(sourceItem, targetItem);
            case DropType.AFTER:
                return moveAfter(sourceItem, targetItem);
            case DropType.ON:
                return moveOn(sourceItem, targetItem);
        }

        return false;
    }

    private boolean copy(T1 sourceItem, T2 targetItem, int dropType) {
        switch (dropType) {
            case DropType.BEFORE:
                return copyBefore(sourceItem, targetItem);
            case DropType.AFTER:
                return copyAfter(sourceItem, targetItem);
            case DropType.ON:
                return copyOn(sourceItem, targetItem);
        }

        return false;
    }

}
