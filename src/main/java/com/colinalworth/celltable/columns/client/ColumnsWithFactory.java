/**
 *  Copyright 2011 Colin Alworth
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package com.colinalworth.celltable.columns.client;

/**
 * Works exactly like {@link Columns}, except that a factory type (and instance) may be specified.
 * Cell instances will be obtained from identically named methods in the factory class instead of
 * from GWT.create(). Only methods from ColumnsWithFactory subtypes that have a matching instance
 * in the class F will be obtained from the factory - if there is a method in the interface that 
 * does not have a corresponding method in the factory, the Cell will be GWT.create'd.
 * 
 * Factory must be set before the Cell instance is obtained from the method call or configure() is 
 * called
 * 
 * @author Colin Alworth
 *
 */
public interface ColumnsWithFactory<T, F> extends Columns<T> {
	/**
	 * Sets the factory that should be used to get some or all of the Cell instances.
	 * 
	 * May only be called once per instance.
	 * 
	 * @param factory
	 */
	void setFactory(F factory);
}
