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

import java.util.Date;

import com.colinalworth.celltable.columns.client.Columns.Translations;
import com.google.gwt.cell.client.DateCell;
import com.google.gwt.cell.client.EditTextCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.NumberCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor.Path;
import com.google.gwt.i18n.client.Constants;
import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.user.cellview.client.CellTable;

/**
 * 
 * @author colin
 *
 */
public class ColumnsTest extends GWTTestCase {
	@Override
	public String getModuleName() {
		return "com.colinalworth.celltable.columns.ColumnsTest";
	}

	interface IBeanModel {
		String getStringProp();
		//void setStringProp(String val);
	}
	interface SimplePaths extends Columns<IBeanModel> {
		TextCell stringProp();
	}

	public void testSimpleReadOnlyCellTable() {
		SimplePaths cols = GWT.create(SimplePaths.class);
		CellTable<IBeanModel> cellTable = new CellTable<IBeanModel>();
		cols.configure(cellTable);
		assertEquals(1, cellTable.getColumnCount());
	}

	interface ComplexBeanModel {
		String getStringProp();
		Date getDateObj();
		int getIntPrimitive();

	}
	interface LotsOfPaths extends Columns<ComplexBeanModel> {
		@Path("stringProp")
		TextCell myStringColumn();
		@Path("dateObj")
		DateCell date();
		@Path("dateObj.year")
		NumberCell year();
	}
	public void testPaths() {
		LotsOfPaths c = GWT.create(LotsOfPaths.class);
		CellTable<ComplexBeanModel> cellTable = new CellTable<ColumnsTest.ComplexBeanModel>();

		c.configure(cellTable);
	}

	interface DataWithConverter extends Columns<ComplexBeanModel> {
		@ConvertedWith(DateToYearConverter.class)
		@Path("dateObj")
		TextCell formattedDateCell();
	}
	public static class DateToYearConverter implements DataConverter<Date, String> {
		@SuppressWarnings("deprecation")
		public Date fromCellToModel(String cell) {
			return new Date(Date.parse(cell));
		}
		@SuppressWarnings("deprecation")
		public String fromModelToCell(Date model) {
			return model.toGMTString();
		}
	}

	public void testDataConverter() {
		DataWithConverter c = GWT.create(DataWithConverter.class);
		CellTable<ComplexBeanModel> cellTable = new CellTable<ColumnsTest.ComplexBeanModel>();
		GWT.create(DateToYearConverter.class);
		c.configure(cellTable);
	}
	interface EditableBeanModel {
		void setStringProp(String value);
		String getStringProp();
	}
	interface SimpleEditableColumns extends Columns<EditableBeanModel> {
		@Editable
		EditTextCell stringProp();
	}


	public void testEditableColumns() {
		SimpleEditableColumns c = GWT.create(SimpleEditableColumns.class);
		CellTable<EditableBeanModel> cellTable = new CellTable<EditableBeanModel>();
		HasDataFlushableEditor<EditableBeanModel> editor = HasDataFlushableEditor.of(cellTable);

		c.configure(cellTable, editor);
	}


	interface DataWithFieldUpdater extends Columns<EditableBeanModel> {
		@Editable(EditableBeanModelFieldUpdater.class)
		EditTextCell stringProp();
	}
	static class EditableBeanModelFieldUpdater implements FieldUpdater<EditableBeanModel, String> {
		public void update(int arg0, EditableBeanModel arg1, String arg2) {
			arg1.setStringProp(arg2 + "...");
		}
	}
	public void testSpecifiedFieldUpdater() {
		DataWithFieldUpdater c = GWT.create(DataWithFieldUpdater.class);
		CellTable<EditableBeanModel> cellTable = new CellTable<EditableBeanModel>();
		HasDataFlushableEditor<EditableBeanModel> editor = HasDataFlushableEditor.of(cellTable);

		c.configure(cellTable, editor);
	}

	interface I18nBeanModel {
		String getTranslated();
		String getTranslationIgnored();
		String getUntranslated();
	}

	@Translations(TranslationStrings.class)
	interface I18nColumns extends Columns<I18nBeanModel> {
		@Header("translated")
		TextCell translated();
		@Header(value = "translationIgnored", skipI18n = true)
		TextCell translationIgnored();
		@Header(value = "untranslated", skipI18n = false)
		TextCell untranslated();
	}

	public void testI18nHeader() {
		I18nColumns c = GWT.create(I18nColumns.class);
		CellTable<I18nBeanModel> cellTable = new CellTable<I18nBeanModel>();

		c.configure(cellTable);
		assertEquals("translationOk", cellTable.getHeader(0).getValue());
		assertEquals("translationIgnored", cellTable.getHeader(1).getValue());
		assertEquals("untranslated", cellTable.getHeader(2).getValue());
	}

	interface TranslationStrings extends Constants {
		@DefaultStringValue("translationOk")
		String translated();
	}

	@Translations(EscapedStrings.class)
	interface EscapedLabelColumns extends Columns<EditableBeanModel> {
		@Header("withQuote")
		TextCell stringProp();
		
		@Header(value="With quote \" 2", skipI18n=true)
		@Path("stringProp")
		TextCell stringProp2();
	}
	interface EscapedStrings extends Constants {
		@DefaultStringValue("With quote \"")
		String withQuote();
	}
	public void testEscapedLabelChars() {
		EscapedLabelColumns c = GWT.create(EscapedLabelColumns.class);
		CellTable<EditableBeanModel> cellTable = new CellTable<EditableBeanModel>();
		c.configure(cellTable);

		assertEquals("With quote \"", cellTable.getHeader(0).getValue());
		assertEquals("With quote \" 2", cellTable.getHeader(1).getValue());
	}
}