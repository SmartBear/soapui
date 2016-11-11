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

public abstract class AbstractBeforeModelItemDropHandler<T1 extends ModelItem, T2 extends ModelItem> extends
        AbstractModelItemDropHandler<T1, T2> {
    protected AbstractBeforeModelItemDropHandler(Class<T1> sourceClass, Class<T2> targetClass) {
        super(sourceClass, targetClass);
    }

    @Override
    boolean canCopyAfter(T1 source, T2 target) {
        return false;
    }

    @Override
    boolean canMoveAfter(T1 source, T2 target) {
        return false;
    }

    @Override
    boolean copyAfter(T1 source, T2 target) {
        return false;
    }

    @Override
    String getCopyAfterInfo(T1 source, T2 target) {
        return null;
    }

    @Override
    String getMoveAfterInfo(T1 source, T2 target) {
        return null;
    }

    @Override
    boolean moveAfter(T1 source, T2 target) {
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
