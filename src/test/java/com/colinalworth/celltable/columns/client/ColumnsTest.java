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

import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.user.cellview.client.CellTable;

/**
 * 
 * @author colin
 *
 */
public class ColumnsTest extends GWTTestCase {
	interface IBeanModel {
		String getStringProp();
		void setStringProp(String val);


	}
	interface NoPaths extends Columns<IBeanModel> {
		TextCell stringProp();
	}

	@Override
	public String getModuleName() {
		return "com.colinalworth.celltable.columns.ColumnsTest";
	}

	public void testSimpleReadOnlyCellTable() {
		NoPaths cols = GWT.create(NoPaths.class);
		CellTable<IBeanModel> cellTable = new CellTable<ColumnsTest.IBeanModel>();
		cols.configure(cellTable);
		assert getColumnCount(cellTable) == 1;
	}

	private native int getColumnCount(CellTable<?> table) /*-{
		table.@com.google.gwt.user.cellview.client.CellTable::columns.@java.util.ArrayList::size()();
	}-*/;
}