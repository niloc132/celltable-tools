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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.editor.client.adapters.HasDataEditor;
import com.google.gwt.user.cellview.client.CellTable;

/**
 * @author colin
 *
 */
public interface Columns<T> {
	void configure(CellTable<T> cellTable);
	void configure(CellTable<T> cellTable, HasDataEditor<T> editor);
	String[] getPaths();



	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface Alignment {
		public enum HorizontalAlign { CENTER, JUSTIFY, LEFT, RIGHT, NONE }
		public enum VerticalAlign { TOP, MIDDLE, BOTTOM, NONE }
		HorizontalAlign horizontal() default HorizontalAlign.NONE;
		VerticalAlign vertical() default VerticalAlign.NONE;
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface Header {
		String value();
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface Editable {
		@SuppressWarnings("rawtypes")
		Class<FieldUpdater> value() default FieldUpdater.class;
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface ConvertedWith {
		Class<DataConverter<?,?>> value();
	}
}
