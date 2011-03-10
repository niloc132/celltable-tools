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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;

/**
 * Simplifies creation of {@link com.google.gwt.user.cellview.client.Column} objects for a 
 * {@link CellTable}. Interfaces extending this may be declared and requested using GWT.create, and
 * used to add columns to the CellTable.
 * 
 * Declared methods should return a type which implements {@link com.google.gwt.cell.client.Cell},
 * and may be named for the properties available in the data to be drawn by the CellTable instance.
 * As with the {@link com.google.gwt.editor.client.Editor} framework and its implementors, these 
 * methods may be named as desired, with a {@link com.google.gwt.editor.client.Editor.Path}
 * annotation declaring how the data may be accessed. Again, as with Editor, the path provided may
 * be blank, indicating the row itself, or may reference nested properties.
 * 
 * Cell instances will be created through the use of GWT.create, allowing for replacement or 
 * generation. If you wish to provide your own implementation, either create the Column object
 * manually, or see {@link ColumnsWithFactory} for a way to provide a factory instance.
 * 
 * Once a Cell has been created, either by calling the declared methods directly, or by calling
 * configure(...), it will be retained, so that subsequent calls will get the same instance. At this
 * time there is no support for getting access to the Column instances.
 * 
 * Further extending a subinterface of Columns to add more columns is not supported at this time.
 * 
 * @author Colin Alworth
 * 
 * @param <T> The type of data that will be rendered in the CellTable
 *
 */
public interface Columns<T> {
	/**
	 * Adds each of the Cells to the given table, wrapped in a Column with the options specified
	 * configured on them. Columns are added in the order they are declared.
	 * 
	 * @param cellTable
	 */
	void configure(CellTable<T> cellTable);

	/**
	 * As configure(CellTable<T>), except will also bind the columns to the editor, so that when the
	 * editor system flushes, the data will be applied to the model.
	 * 
	 * Because data will not be applied to the model until the flush occurs, some data can be
	 * somewhat inconsistent - the Cells will need to track their changed data correctly until the
	 * data is flushed, and other cells won't be able to get the updated value until then.
	 * 
	 * TODO Consider adding a configuration option to allow immediate flushing (other than not
	 * passing an editor)
	 * 
	 * @param cellTable
	 * @param editor
	 */
	void configure(CellTable<T> cellTable, HasDataFlushableEditor<T> editor);


	/**
	 * As in the Editor Driver classes, returns the property names that are accessed by this
	 * instance. At this time, primitive properties are returned, unlike in the Driver classes, and
	 * nested properties may be returned in an inconsistent manner.
	 * @return
	 */
	String[] getPaths();



	/**
	 * Sets the alignment of the {@link Column}, as would be done using setVerticalAlignment and
	 * setHorizontalAlignment.
	 *
	 */
	@Documented
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface Alignment {
		public enum HorizontalAlign { CENTER, JUSTIFY, LEFT, RIGHT, NONE }
		public enum VerticalAlign { TOP, MIDDLE, BOTTOM, NONE }
		HorizontalAlign horizontal() default HorizontalAlign.NONE;
		VerticalAlign vertical() default VerticalAlign.NONE;
	}

	/**
	 * Sets a static string to be used as the header of the column to be generated. Will be used
	 * when the column is added to the CellTable using one of the configure() methods.
	 * 
	 * At this time, cannot be internationalized.
	 *
	 */
	@Documented
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface Header {
		String value();
	}

	/**
	 * Marks the Column as Editable, so a {@link FieldUpdater} will be created and used to call the
	 * matching set method for the property this column is assigned to.
	 * 
	 * Will not work for @Path(""). Nested paths are supported.
	 * 
	 * There is a known issue in GWT (http://code.google.com/p/google-web-toolkit/issues/detail?id=5981)
	 * where a CellTable attached to an Editor through HasDataFlushableEditor cannot have values changed. This
	 * annotation does nothing to help with the issue.
	 * 
	 * 
	 * This can also be applied to the entire type
	 *
	 */
	@Documented
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface Editable {
		/**
		 * If a {@link FieldUpdater} type is specified, @Path and @ConvertedWith will be ignored
		 * when data is being saved - the specified type must handle all of this.
		 * 
		 * Specified instance will be created with GWT.create().
		 */
		@SuppressWarnings("rawtypes")
		Class<? extends FieldUpdater> value() default FieldUpdater.class;
	}

	/**
	 * Allows a type implementing {@link DataConverter} to be provided which will accept data from
	 * the get method this Column is mapped to and transform it before passing it the Cell instance
	 * and back again. The given type will be created using GWT.create as needed and not cached.
	 */
	@Documented
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface ConvertedWith {
		Class<? extends DataConverter<?,?>> value();
	}

	/**
	 * Indicates that the Column should be set as sortable for the client.
	 * 
	 * Supported by GWT 2.2 and later.
	 */
	@Documented
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface Sortable {
	}
}
