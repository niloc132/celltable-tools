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
package com.colinalworth.celltable.columns.rebind.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import com.colinalworth.celltable.columns.client.Columns;
import com.colinalworth.celltable.columns.client.Columns.Alignment;
import com.colinalworth.celltable.columns.client.Columns.ConvertedWith;
import com.colinalworth.celltable.columns.client.Columns.Editable;
import com.colinalworth.celltable.columns.client.Columns.Header;
import com.colinalworth.celltable.columns.client.Columns.Sortable;
import com.colinalworth.celltable.columns.client.Columns.TranslatedHeader;
import com.colinalworth.celltable.columns.client.ColumnsWithFactory;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JMethod;
import com.google.gwt.core.ext.typeinfo.JType;
import com.google.gwt.dev.util.Name;
import com.google.gwt.editor.client.Editor.Path;
import com.google.gwt.editor.rebind.model.ModelUtils;

/**
 * @author colin
 *
 */
public class ColumnSetModel {
	private final JClassType beanType;
	//private final TreeLogger logger;
	private final GeneratorContext context;
	private final Set<String> names;

	private final JClassType factoryType;
	private final List<ColumnModel> columns;

	public ColumnSetModel(JClassType toGenerate, GeneratorContext context, TreeLogger logger, Set<String> names) {
		//this.logger = logger;
		this.context = context;
		this.names = names;

		this.beanType = getBeanType(toGenerate);
		this.factoryType = getFactoryType(toGenerate);

		this.columns = new ArrayList<ColumnSetModel.ColumnModel>(toGenerate.getMethods().length);
		for (JMethod method : toGenerate.getMethods()) {
			columns.add(new ColumnModel(method));
		}
	}

	private JClassType getBeanType(JClassType toGenerate) {
		JClassType columnsInterface = context.getTypeOracle().findType(Name.getSourceNameForClass(Columns.class));
		JClassType[] params = ModelUtils.findParameterizationOf(columnsInterface, toGenerate);
		assert params.length == 1 : "Too many generic params in Column<T>";
		return params[0];
	}
	/**
	 * Gets the type of factory this class needs to do its thing. Returns null if no factory is 
	 * required.
	 * @param toGenerate
	 * @return
	 */
	private JClassType getFactoryType(JClassType toGenerate) {
		JClassType columnsWithFactoryInterface = context.getTypeOracle().findType(Name.getSourceNameForClass(ColumnsWithFactory.class));
		JClassType[] params = ModelUtils.findParameterizationOf(columnsWithFactoryInterface, toGenerate);
		if (params != null) {
			assert params.length == 2 : "Exactly two generic params needed for ColumnsWithFactory<T, F>";
			return params[1];
		}
		return null;
	}

	/**
	 * @return
	 */
	public String getBeanName() {
		return beanType.getParameterizedQualifiedSourceName();
	}
	/**
	 * @return
	 */
	public List<ColumnModel> getColumnModels() {
		return columns;
	}

	public String getPaths() {
		//TODO make this static?
		StringBuilder sb = new StringBuilder("new String[] {");
		Set<String> paths = new HashSet<String>();

		for (ColumnModel c : getColumnModels()) {
			if (c.getPath() != null && c.getPath().length() != 0) {
				paths.add("\"" + c.getPath().replaceAll(Pattern.quote("\\"), "\\\\") + "\"");
			}
		}

		sb.append(StringUtils.join(paths, ","));

		return sb.append("}").toString();
	}

	public boolean hasFactory() {
		return getFactoryClass() != null;
	}

	public JClassType getFactoryClass() {
		return factoryType;
	}
	public String getFactoryClassName() {
		return getFactoryClass().getParameterizedQualifiedSourceName();
	}


	private String getUniqueName(String initialName) {
		if (!names.contains(initialName)) {
			names.add(initialName);
			return initialName;
		} else {
			int i = 1;
			do {
				i++;
			} while (names.contains(initialName + "_" + i));
			names.add(initialName + "_" + i);
			return initialName + "_" + i;
		}
	}

	public class ColumnModel {
		private final JMethod method;
		private final String cellFieldName;
		private final String columnFieldName;
		public ColumnModel(JMethod columnMethod) {
			this.method = columnMethod;
			this.cellFieldName = getUniqueName(method.getName());
			this.columnFieldName = getUniqueName(cellFieldName + "_column");
		}

		public String getMethodName() {
			return method.getName();
		}
		public String getPath() {
			if (method.isAnnotationPresent(Path.class)) {
				return method.getAnnotation(Path.class).value();
			}
			return getMethodName();
		}
		public String getCellFieldName() {
			return cellFieldName;
		}
		public String getColumnFieldName() {
			return columnFieldName;
		}

		public String getCellCreateExpression() {
			if (hasFactory()) {
				JMethod factoryMethod = getFactoryClass().findMethod(getMethodName(), new JType[] {});
				if (factoryMethod != null && factoryMethod.getReturnType().equals(getCellClass())) {
					return String.format("factory.%1$s()", getMethodName());
				}
			}

			return String.format("GWT.create(%1$s.class)", getCellClassName());
		}

		/**
		 * @return
		 */
		public String getCellClassName() {
			return getCellClass().getParameterizedQualifiedSourceName();
		}
		public JClassType getCellClass() {
			return method.getReturnType().isClassOrInterface();
		}
		/**
		 * @return
		 */
		public String getCellDataTypeName() {
			return getCellDataType().getParameterizedQualifiedSourceName();
		}
		public JType getCellDataType() {
			JClassType[] params = ModelUtils.findParameterizationOf(context.getTypeOracle().findType(Name.getSourceNameForClass(Cell.class)), getCellClass());
			assert params.length == 1 : "Too many generic params in Cell<T>";
			return params[0];
		}

		public String getGetterInModel(String model) {
			String getter = getGetterInModelWithoutDataConverter(model);
			if (method.isAnnotationPresent(ConvertedWith.class)) {
				return String.format("GWT.<%1$s>create(%1$s.class).fromModelToCell(%2$s)", Name.getSourceNameForClass(method.getAnnotation(ConvertedWith.class).value()), getter);
			}
			return getter;
		}

		private String getGetterInModelWithoutDataConverter(String model) {
			if (getPath().length() == 0) {
				return model;
			}
			String[] paths = getPath().split(Pattern.quote("."));
			StringBuilder sb = new StringBuilder(model);
			JClassType currentType = beanType;
			for (String path : paths) {
				if (currentType == null) {
					throw new RuntimeException("Tried to find " + path + " from " + getPath() + ", but found a non-class type");
				}
				// find the correct method, and pull up the return type in case there are more gets on the way
				JMethod m = getSimpleGetter(path, currentType);
				currentType = m.getReturnType().isClassOrInterface();

				sb.append(".").append(m.getName()).append("()");
			}

			return sb.toString();
		}

		public String getSetterInModel(String model, String value) {
			if (method.isAnnotationPresent(ConvertedWith.class)) {
				value = String.format("GWT.<%1$s>create(%1$s.class).fromCellToModel(%2$s)", Name.getSourceNameForClass(method.getAnnotation(ConvertedWith.class).value()), value);
			}
			String setter = getSetterInModelWithoutDataConverter(model, value);
			return setter;
		}

		private String getSetterInModelWithoutDataConverter(String model,
				String value) {
			if (getPath().length() == 0) {
				throw new RuntimeException("Cannot call setters for @Path(\"\") at this time.");
			}
			String[] paths = getPath().split(Pattern.quote("."));
			StringBuilder sb = new StringBuilder(model);
			JClassType currentType = beanType;
			for (String path : paths) {
				if (currentType == null) {
					throw new RuntimeException("Tried to find " + path + " from " + getPath() + ", but found a non-class type");
				}

				// see if we have a setter, append it, and return it
				if (currentType.getOverloads("set" + capitalize(path)).length != 0) {
					return sb.append(".").append("set").append(capitalize(path)).append("(").append(value).append(")").toString();
				}

				// ok, try a getter instead
				// find the current getter, and pull up the return type in case there are more gets on the way
				JMethod m = getSimpleGetter(path, currentType);
				currentType = m.getReturnType().isClassOrInterface();

				sb.append(".").append(m.getName()).append("()");
			}
			throw new RuntimeException("Tried to find setter in " + currentType.getName() + " for property " + paths[paths.length - 1] + " and failed.");
		}

		private JMethod getSimpleGetter(String propertyName, JClassType owningType) {
			String capProp = capitalize(propertyName);
			JMethod m;
			m = findGetMethod(owningType, "get" + capProp);
			if (m != null) {
				return m;
			}
			m = findGetMethod(owningType, "is" + capProp);
			if (m != null) {
				return m;
			} 
			m = findGetMethod(owningType, "has" + capProp);
			if (m != null) {
				return m;
			}
			//			JField field = owningType.getField(propertyName);
			//			if (field != null) {
			//				return 
			//			}
			throw new RuntimeException("Class " + owningType + " doesn't seem to have get/is/has methods for the property " + propertyName);
		}
		private JMethod findGetMethod(JClassType type, String method) {
			return type.findMethod(method, new JType[] {});
		}
		private String capitalize(String str) {
			return str.substring(0, 1).toUpperCase() + str.substring(1);
		}

		/**
		 * Creates the header string to be used in the table column.<br>
		 * Handles translation if requested via {@link TranslatedHeader} 
		 * @return
		 */
		public String getHeaderValue() {
			//TODO finish this logic
			//TODO escape strings
			if (method.isAnnotationPresent(TranslatedHeader.class)) {
				TranslatedHeader translatedHeader = method.getAnnotation(TranslatedHeader.class);
				String constant = "\"" + translatedHeader.constant() + "\"" ;
				return String.format("GWT.<%1$s>create(%1$s.class).getLocalizedHeader(%2$s)", Name.getSourceNameForClass(translatedHeader.translator()), constant);
			} else if (method.isAnnotationPresent(Header.class)) {
				Header header = method.getAnnotation(Header.class);
				return "\"" + header.value() + "\"";
			}
			return "\"" + getMethodName() + "\"";
		}

		/**
		 * @return
		 */
		public boolean isEditable() {
			return method.isAnnotationPresent(Editable.class);
		}

		/**
		 * @return
		 */
		public String getHorizontalAlignment() {
			if (method.isAnnotationPresent(Alignment.class)) {
				Alignment a = method.getAnnotation(Alignment.class);
				switch (a.horizontal()) {
				case CENTER:
					return "HasHorizontalAlignment.ALIGN_CENTER";
				case JUSTIFY:
					return "HasHorizontalAlignment.ALIGN_JUSTIFY";
				case LEFT:
					return "HasHorizontalAlignment.ALIGN_LEFT";
				case RIGHT:
					return "HasHorizontalAlignment.ALIGN_RIGHT";
				}
			}
			return "null";
		}
		/**
		 * @return
		 */
		public Object getVerticalAlignment() {
			if (method.isAnnotationPresent(Alignment.class)) {
				Alignment a = method.getAnnotation(Alignment.class);
				switch (a.vertical()) {
				case BOTTOM:
					return "HasVerticalAlignment.ALIGN_BOTTOM";
				case MIDDLE:
					return "HasVerticalAlignment.ALIGN_MIDDLE";
				case TOP:
					return "HasVerticalAlignment.ALIGN_TOP";
				}
			}
			return "null";
		}

		/**
		 * @return
		 */
		public boolean isSortable() {
			return method.isAnnotationPresent(Sortable.class);
		}

		/**
		 * @return
		 */
		public JClassType getFieldUpdaterType() {
			assert isEditable() : "Cannot get a FieldUpdater type if not marked as @Editable";
			return context.getTypeOracle().findType(Name.getSourceNameForClass(method.getAnnotation(Editable.class).value()));
		}

		/**
		 * @return
		 */
		public boolean hasCustomFieldUpdater() {
			return !getFieldUpdaterType().equals(context.getTypeOracle().findType(Name.getSourceNameForClass(FieldUpdater.class)));
		}
	}
}