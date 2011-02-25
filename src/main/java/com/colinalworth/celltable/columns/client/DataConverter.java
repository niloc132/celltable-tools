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
 * Allows {@link com.google.gwt.cell.client.Cell} instances generated from {@link Columns} 
 * interfaces to accept a different datatype than the bean used in the
 * {@link com.google.gwt.user.cellview.client.CellTable} provides. Designed to allow, along with the
 * optional Class<?> parameter of @Editable, complexity that some Column instances might require to
 * still be provided without complicating creation of all columns.
 * 
 * @author colin
 *
 * @param <M> the type of data in the bean used to represent the row
 * @param <C> the type of data that the {@link com.google.gwt.cell.client.Cell} expects
 */
public interface DataConverter<M,C> {
	/**
	 * Converts data in the model into something that can be displayed.
	 * 
	 * @param model
	 * @return
	 */
	C fromModelToCell(M model);
	/**
	 * Converts data in the Cell to something the model can correctly store. If cell method is not
	 * decorated with @Editable, will be ignored
	 * 
	 * @param cell
	 * @return
	 */
	M fromCellToModel(C cell);
}
