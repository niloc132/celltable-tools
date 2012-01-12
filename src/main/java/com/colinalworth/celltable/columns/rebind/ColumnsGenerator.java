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
package com.colinalworth.celltable.columns.rebind;

import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

import com.colinalworth.celltable.columns.client.HasDataFlushableEditor;
import com.colinalworth.celltable.columns.rebind.model.ColumnSetModel;
import com.colinalworth.celltable.columns.rebind.model.ColumnSetModel.ColumnModel;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.TreeLogger.Type;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JPrimitiveType;
import com.google.gwt.core.ext.typeinfo.JType;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import com.google.gwt.dev.util.Name;
import com.google.gwt.user.cellview.client.AbstractCellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.rebind.ClassSourceFileComposerFactory;
import com.google.gwt.user.rebind.SourceWriter;

/**
 * @author colin
 *
 */
public class ColumnsGenerator extends Generator {
	//private TreeLogger logger;
	private GeneratorContext context;

	private Set<String> names = new HashSet<String>();

	@Override
	public String generate(TreeLogger logger, GeneratorContext context, String typeName) throws UnableToCompleteException {
		//this.logger = logger;
		this.context = context;

		TypeOracle oracle = context.getTypeOracle();
		JClassType toGenerate = oracle.findType(typeName).isInterface();
		if (toGenerate == null) {
			logger.log(TreeLogger.ERROR, typeName + " is not an interface type");
			throw new UnableToCompleteException();
		}

		String packageName = toGenerate.getPackage().getName();
		String simpleSourceName = toGenerate.getName().replace('.', '_') + "_Impl";
		PrintWriter pw = context.tryCreate(logger, packageName, simpleSourceName);
		if (pw == null) {
			return packageName + "." + simpleSourceName;
		}

		ColumnSetModel columnSet = new ColumnSetModel(toGenerate, context, logger, names);

		//public class X implements X {
		ClassSourceFileComposerFactory factory = new ClassSourceFileComposerFactory(packageName, simpleSourceName);
		factory.addImplementedInterface(typeName);

		factory.addImport(Name.getSourceNameForClass(GWT.class));
		factory.addImport(Name.getSourceNameForClass(AbstractCellTable.class));
		factory.addImport(Name.getSourceNameForClass(HasDataFlushableEditor.class));
		factory.addImport(Name.getSourceNameForClass(Column.class));
		factory.addImport(Name.getSourceNameForClass(HasHorizontalAlignment.class));
		factory.addImport(Name.getSourceNameForClass(HasVerticalAlignment.class));
		factory.addImport(Name.getSourceNameForClass(FieldUpdater.class));
		factory.addImport(columnSet.getBeanName());

		SourceWriter sw = factory.createSourceWriter(context, pw);


		//wire up the factory, if any
		if (columnSet.hasFactory()) {
			names.add("factory");
			sw.println("private %1$s factory;", columnSet.getFactoryClassName());
			sw.println("public void setFactory(%1$s factory) {", columnSet.getFactoryClassName());
			sw.indent();
			sw.println("assert factory != null && this.factory == null : \"Factory cannot be reset, and factory cannot be set as null\";");
			sw.println("this.factory = factory;");
			sw.outdent();
			sw.println("}");
		}

		// generate column methods
		for (ColumnModel c : columnSet.getColumnModels()) {
			// make the field 
			// TODO: no sense in building multiple copies, right?
			sw.println("private %1$s %2$s;", c.getCellClassName(), c.getCellFieldName());

			sw.println("private Column<%1$s,%2$s> %3$s;", columnSet.getBeanName(), c.getCellDataTypeName(), c.getColumnFieldName());
			sw.println();

			// make the method: public MyCell myDataMember() {
			//sw.println("@Override");//jdk 5 doesnt like this
			sw.println("public %1$s %2$s() {", c.getCellClassName(), c.getMethodName());
			sw.indent();
			sw.println("if (%s == null) {", c.getCellFieldName());
			sw.indent();

			//create the cell
			sw.println("%1$s = %2$s;", c.getCellFieldName(), c.getCellCreateExpression());

			//create the column - probably should be done later in the case of using HasDataFlushableEditor
			sw.println("%1$s = new Column<%2$s,%3$s> (%4$s) {", c.getColumnFieldName(), columnSet.getBeanName(), c.getCellDataTypeName(), c.getCellFieldName());
			sw.indent();

			sw.println("@Override");
			sw.println("public %1$s getValue(%2$s bean) {", c.getCellDataTypeName(), columnSet.getBeanName());
			sw.indent();
			sw.println("return %1$s;", c.getGetterInModel("bean"));
			sw.outdent();
			sw.println("}");

			sw.outdent();// end anon Column class
			sw.println("};");

			// Refactor at least this part out, in anticipation of a proper link to the Editor framework
			// TODO this is done by replacement right now, fix that.
			if (c.isEditable()) {
				if (!c.hasCustomFieldUpdater()) {
					sw.println("%1$s.setFieldUpdater(new FieldUpdater<%2$s,%3$s>() {", c.getColumnFieldName(), columnSet.getBeanName(), c.getCellDataTypeName());
					sw.indent();

					sw.println("public void update(int index, %1$s object, %2$s value) {", columnSet.getBeanName(), c.getCellDataTypeName());
					sw.indent();
					sw.println("%1$s;", c.getSetterInModel("object", "value"));
					sw.outdent();
					sw.println("}");

					sw.outdent();// end anon FieldUpdater class
					sw.println("});");
				} else {
					sw.println("%1$s.setFieldUpdater(GWT.<%2$s>create(%2$s.class));", c.getColumnFieldName(), c.getFieldUpdaterType().getQualifiedSourceName());
				}
			}
			sw.println("%1$s.setHorizontalAlignment(%2$s);", c.getColumnFieldName(), c.getHorizontalAlignment());
			sw.println("%1$s.setVerticalAlignment(%2$s);", c.getColumnFieldName(), c.getVerticalAlignment());

			if (supportsSortable()) {
				sw.println("%1$s.setSortable(%2$s);", c.getColumnFieldName(), c.isSortable());
			} else {
				if (c.isSortable()) {
					logger.log(Type.WARN, "Your version of GWT does not appear to support Column.setSortable, compilation may fail.");
				}
			}
			//end column creation/setup

			sw.outdent();
			sw.println("}");// end column/cell creation
			sw.println("return %s;", c.getCellFieldName());
			sw.outdent();
			sw.println("}");
		}

		// generate configure methods

		// simple overload
		sw.println("public final void configure(AbstractCellTable<%1$s> table) {", columnSet.getBeanName());
		sw.indent();
		sw.println("configure(table, null);");
		sw.outdent();
		sw.println("}");

		// actual heavy-lifting one
		sw.println("public final void configure(AbstractCellTable<%1$s> table, HasDataFlushableEditor<%1$s> ed) {", columnSet.getBeanName());
		sw.indent();
		if (columnSet.hasFactory()) {
			sw.println("assert factory != null : \"setFactory() must be called before configure() can be called.\";");
		}
		for (ColumnModel c : columnSet.getColumnModels()) {
			//wire up the cell and column
			sw.println("%1$s();", c.getMethodName());

			if (c.isEditable() && !c.hasCustomFieldUpdater()) {
				// if there is an editor, replace the FieldUpdater
				sw.println("if (ed != null) {");
				sw.indent();
				sw.println("final FieldUpdater<%1$s, %2$s> wrapped = %3$s.getFieldUpdater();", columnSet.getBeanName() , c.getCellDataTypeName() ,c.getColumnFieldName());
				sw.println("%1$s.setFieldUpdater(ed.new PendingFieldUpdateChange<%2$s>(){", c.getColumnFieldName(), c.getCellDataTypeName());
				sw.indent();
				sw.println("public void commit(int index, %1$s object, %2$s value) {", columnSet.getBeanName(), c.getCellDataTypeName());
				sw.indent();
				sw.println("wrapped.update(index, object, value);");
				sw.outdent();
				sw.println("}");
				sw.outdent();
				sw.println("});");
				sw.outdent();
				sw.println("}");
			}

			// attach the column
			sw.println("table.addColumn(%1$s, %2$s);", c.getColumnFieldName(), c.getHeaderValue());
		}
		sw.outdent();
		sw.println("}");

		sw.println("public String[] getPaths() {");
		sw.indent();
		sw.println("return %1$s;", columnSet.getPaths());
		sw.outdent();
		sw.println("}");

		sw.commit(logger);

		return factory.getCreatedClassName();
	}

	/**
	 * @return
	 */
	private boolean supportsSortable() {
		return context.getTypeOracle().findType(Name.getSourceNameForClass(Column.class)).findMethod("setSortable", new JType[] {JPrimitiveType.BOOLEAN}) != null;
	}
}
