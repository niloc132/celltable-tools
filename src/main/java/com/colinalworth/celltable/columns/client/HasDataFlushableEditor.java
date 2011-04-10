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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.editor.client.EditorDelegate;
import com.google.gwt.editor.client.ValueAwareEditor;
import com.google.gwt.editor.client.adapters.EditorSource;
import com.google.gwt.editor.client.adapters.ListEditor;
import com.google.gwt.user.client.Command;
import com.google.gwt.view.client.HasData;

/**
 * Extended version of {@link com.google.gwt.editor.client.adapters.HasDataEditor<T>}. Able to have
 * data flushed from here, back into the real data
 * 
 * Instead of creating FieldUpdater instances that directly modify the data, use a subclass of
 * PendingFieldUpdateChange, with commit() doing the actual changing.
 * 
 * @author colin
 *
 */
public class HasDataFlushableEditor<T> extends ListEditor<T, ValueAwareEditor<T>> {
	static class HasDataEditorSource<T> extends EditorSource<ValueAwareEditor<T>> {
		private final HasData<T> data;

		public HasDataEditorSource(HasData<T> data) {
			this.data = data;
		}

		@Override
		public IndexedEditor<T> create(int index) {
			return new IndexedEditor<T>(index, data);
		}

		@Override
		public void setIndex(ValueAwareEditor<T> editor, int index) {
			((IndexedEditor<T>) editor).setIndex(index);
		}

		/* (non-Javadoc)
		 * @see com.google.gwt.editor.client.adapters.EditorSource#dispose(com.google.gwt.editor.client.Editor)
		 */
		@Override
		public void dispose(ValueAwareEditor<T> subEditor) {
			data.setRowCount(data.getRowCount() - 1);
			((IndexedEditor<T>)subEditor).remove();
		}
	}

	static class IndexedEditor<Q> implements ValueAwareEditor<Q> {
		private int index;
		private Q value;
		private final HasData<Q> data;

		IndexedEditor(int index, HasData<Q> data) {
			this.index = index;
			this.data = data;
		}

		public Q getValue() {
			return value;
		}

		public void setIndex(int index) {
			this.index = index;
			push();
		}

		public void setValue(Q value) {
			this.value = value;
			push();
		}

		private void push() {
			data.setRowData(index, Collections.singletonList(value));
		}

		public void flush() {

		}

		public void onPropertyChange(String... paths) {

		}

		public void setDelegate(EditorDelegate<Q> delegate) {

		}
		public void remove() {
		}
	}

	/**
	 * Create a HasDataFlushableEditor backed by a HasData.
	 * 
	 * @param <T> the type of data to be edited
	 * @param data the HasData that is displaying the data
	 * @return a instance of a HasDataFlushableEditor
	 */
	public static <T> HasDataFlushableEditor<T> of(HasData<T> data) {
		return new HasDataFlushableEditor<T>(data);
	}

	/**
	 * Prevent subclassing.
	 */
	HasDataFlushableEditor(HasData<T> data) {
		super(new HasDataEditorSource<T>(data));
	}
	private List<Command> changes = new ArrayList<Command>();

	@Override
	public void flush() {
		// flush item changes
		for (Command change : changes) {
			change.execute();
		}


		// flush the list changes
		super.flush();
	}

	public void addChange(Command change) {
		changes.add(change);
	}

	public abstract class PendingFieldUpdateChange<C> implements FieldUpdater<T, C> {
		public void update(final int index, final T object, final C value) {
			addChange(new Command() {
				public void execute() {
					commit(index, object, value);
				}
			});
		}
		public abstract void commit(int index, T object, C value);
	}
}
