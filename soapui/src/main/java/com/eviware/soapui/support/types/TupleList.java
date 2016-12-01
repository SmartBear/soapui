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

package com.eviware.soapui.support.types;

import java.util.ArrayList;
import java.util.Collection;

public class TupleList<T1 extends Object, T2 extends Object> extends ArrayList<TupleList.Tuple> {
    public TupleList() {
    }

    public TupleList(Collection<? extends Tuple> c) {
        super(c);
    }

    public TupleList(int initialCapacity) {
        super(initialCapacity);
    }

    public void add(T1 value1, T2 value2) {
        add(new Tuple(value1, value2));
    }

    public class Tuple {
        private T1 value1;
        private T2 value2;

        public Tuple(T1 value1, T2 value2) {
            this.value1 = value1;
            this.value2 = value2;
        }

        public T1 getValue1() {
            return value1;
        }

        public void setValue1(T1 value1) {
            this.value1 = value1;
        }

        public T2 getValue2() {
            return value2;
        }

        public void setValue2(T2 value2) {
            this.value2 = value2;
        }

        public String toString() {
            return TupleList.this == null ? value1 + " : " + value2 : TupleList.this.toStringHandler(this);
        }
    }

    protected String toStringHandler(Tuple tuple) {
        return tuple.value1 + " : " + tuple.value2;
    }
}
