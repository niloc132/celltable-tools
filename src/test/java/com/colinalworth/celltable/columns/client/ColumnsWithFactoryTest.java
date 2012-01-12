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
import com.google.gwt.editor.client.Editor.Path;
import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.user.cellview.client.AbstractCellTable;
import com.google.gwt.user.cellview.client.CellTable;

/**
 * 
 * @author colin
 *
 */
public class ColumnsWithFactoryTest extends GWTTestCase {

	@Override
	public String getModuleName() {
		return "com.colinalworth.celltable.columns.ColumnsTest";
	}

	interface IBeanModel {
		String getStringProp();
		//void setStringProp(String val);
	}
	interface SimpleColumnsWithFactory extends ColumnsWithFactory<IBeanModel, SimpleFactory> {
		@Path("stringProp")
		TextCell notFactory();

		@Path("stringProp")
		TextCell withFactory();
	}
	class SimpleFactory {
		private TextCell factoryInstance;
		public SimpleFactory(TextCell cell) {
			factoryInstance = cell;
		}
		public TextCell withFactory() {
			return factoryInstance;
		}
	}
	public void testSimpleReadonlyColumnsWithFactory() {
		TextCell cell = new TextCell();
		SimpleFactory f = new SimpleFactory(cell);

		SimpleColumnsWithFactory columns = GWT.create(SimpleColumnsWithFactory.class);
		columns.setFactory(f);

		assertEquals(cell,columns.withFactory());

		CellTable<IBeanModel> table = new CellTable<ColumnsWithFactoryTest.IBeanModel>();
		columns.configure(table);

		assertEquals(2, getColumnCount(table));
	}

	public void testBreaksWithNoFactory() {
		SimpleColumnsWithFactory columns = GWT.create(SimpleColumnsWithFactory.class);

		CellTable<IBeanModel> table = new CellTable<ColumnsWithFactoryTest.IBeanModel>();

		try {
			columns.configure(table);
			fail("Fail - exception should occur");
		} catch (AssertionError ex) {
			//pass
		}
	}

	public void testBreaksWithNullFactory() {
		SimpleColumnsWithFactory columns = GWT.create(SimpleColumnsWithFactory.class);

		try {
			columns.setFactory(null);
			fail("Fail - exception should occur");
		} catch (AssertionError ex) {
			//pass
		}
	}

	public void testBreaksWithDoubleFactory() {
		TextCell cell = new TextCell();
		SimpleFactory f1 = new SimpleFactory(cell);

		SimpleFactory f2 = new SimpleFactory(cell);

		SimpleColumnsWithFactory columns = GWT.create(SimpleColumnsWithFactory.class);

		try {
			columns.setFactory(f1);
			columns.setFactory(f2);
			fail("Fail - exception should occur");
		} catch (AssertionError ex) {
			//pass
		}
	}

	private native int getColumnCount(AbstractCellTable<?> table) /*-{
		return table.@com.google.gwt.user.cellview.client.AbstractCellTable::columns.@java.util.List::size()();
	}-*/;
}