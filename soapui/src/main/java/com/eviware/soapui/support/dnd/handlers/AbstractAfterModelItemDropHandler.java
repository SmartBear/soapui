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

public abstract class AbstractAfterModelItemDropHandler<T1 extends ModelItem, T2 extends ModelItem> extends
        AbstractModelItemDropHandler<T1, T2> {
    protected AbstractAfterModelItemDropHandler(Class<T1> sourceClass, Class<T2> targetClass) {
        super(sourceClass, targetClass);
    }

    @Override
    boolean canCopyBefore(T1 source, T2 target) {
        return false;
    }

    @Override
    boolean canMoveBefore(T1 source, T2 target) {
        return false;
    }

    @Override
    boolean copyBefore(T1 source, T2 target) {
        return false;
    }

    @Override
    String getCopyBeforeInfo(T1 source, T2 target) {
        return null;
    }

    @Override
    String getMoveBeforeInfo(T1 source, T2 target) {
        return null;
    }

    @Override
    boolean moveBefore(T1 source, T2 target) {
        return false;
    }

    @Override
    boolean canCopyOn(T1 source, T2 target) {
        return false;
    }

    @Override
    boolean canMoveOn(T1 source, T2 target) {
        return false;
    }

    @Override
    boolean copyOn(T1 source, T2 target) {
        return false;
    }

    @Override
    String getCopyOnInfo(T1 source, T2 target) {
        return null;
    }

    @Override
    String getMoveOnInfo(T1 source, T2 target) {
        return null;
    }

    @Override
    boolean moveOn(T1 source, T2 target) {
        return false;
    }
}
