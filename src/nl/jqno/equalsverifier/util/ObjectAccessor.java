/*
 * Copyright 2010 Jan Ouwens
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.jqno.equalsverifier.util;

import java.lang.reflect.Field;

/**
 * Wraps an object to provide reflective access to it. ObjectAccessor can
 * clone and scramble the wrapped object.
 * 
 * @param <T> The specified object's class.
 *
 * @author Jan Ouwens
 */
public class ObjectAccessor<T> {
	private final T object;
	private final Class<T> type;

	/**
	 * Factory method.
	 * 
	 * @param <T> {@link #object}'s type.
	 * @param object The object to wrap.
	 * @return An {@link ObjectAccessor} for {@link #object}.
	 */
	public static <T> ObjectAccessor<T> of(T object) {
		@SuppressWarnings("unchecked")
		Class<T> type = (Class<T>)object.getClass();
		return new ObjectAccessor<T>(object, type);
	}
	
	/**
	 * Factory method.
	 * 
	 * @param <T> {@link #object}'s type, or a supertype.
	 * @param object The object to wrap.
	 * @param type Superclass of {@link #object}'s type, as which it will be
	 * 			treated by {@link ObjectAccessor}.
	 * @return An {@link ObjectAccessor} for {@link #object}.
	 */
	public static <T> ObjectAccessor<T> of(T object, Class<T> type) {
		return new ObjectAccessor<T>(object, type);
	}
	
	/**
	 * Private constructor. Call {@link #of(Object)} to instantiate.
	 */
	private ObjectAccessor(T object, Class<T> type) {
		this.object = object;
		this.type = type;
	}
	
	/**
	 * Returns the wrapped object.
	 * 
	 * @return The wrapped object.
	 */
	public T get() {
		return object;
	}
	
	/**
	 * Returns a FieldAccessor for the wrapped object and the specified field.
	 * 
	 * @param field A field in T.
	 * @return A FieldAccessor for the wrapped object and the specified field.
	 */
	public FieldAccessor fieldAccessorFor(Field field) {
		return new FieldAccessor(object, field);
	}
	
	/**
	 * Creates a clone of the wrapped object.
	 * 
	 * Note: it does a "shallow" clone. Reference fields are copied, not cloned
	 * recursively.
	 * 
	 * @return A shallow clone.
	 */
	public T clone() {
		T clone = Instantiator.of(type).instantiate();
		return cloneInto(clone);
	}

	/**
	 * Creates a clone of the wrapped object, where the clone's type is a
	 * specified subclass of the wrapped object's class.
	 * 
	 * Note: it does a "shallow" clone. Reference fields are copied, not cloned
	 * recursively.
	 * 
	 * @param subclass A subclass of the wrapped object's class.
	 * @return A shallow clone.
	 */
	public <S extends T> S cloneIntoSubclass(Class<S> subclass) {
		S clone = Instantiator.of(subclass).instantiate();
		return cloneInto(clone);
	}
	
	/**
	 * Creates a clone of the wrapped object, where the clone's type is an
	 * anonymous subclass of the wrapped object's class.
	 * 
	 * Note: it does a "shallow" clone. Reference fields are copied, not cloned
	 * recursively.
	 * 
	 * @return A shallow clone.
	 */
	public T cloneIntoAnonymousSubclass() {
		T clone = Instantiator.of(type).instantiateAnonymousSubclass();
		return cloneInto(clone);
	}

	private <S> S cloneInto(S clone) {
		for (Field field : new FieldIterable(type)) {
			FieldAccessor accessor = new FieldAccessor(object, field);
			accessor.copyTo(clone);
		}
		return clone;
	}
	
	/**
	 * Modifies all fields of the wrapped object that are declared in T and in
	 * its superclasses.
	 * 
	 * This method is consistent: given two equal objects; after scrambling
	 * both objects, they remain equal to each other.
	 * 
	 * It cannot modifiy:
	 * 1. static final fields, and
	 * 2. final fields that are initialized to a compile-time constant in the
	 *		field declaration.
	 * These fields will be left unmodified.
	 * 
	 * @param prefabValues Prefabricated values to take values from.
	 */
	public void scramble(PrefabValues prefabValues) {
		for (Field field : new FieldIterable(type)) {
			FieldAccessor accessor = new FieldAccessor(object, field);
			accessor.changeField(prefabValues);
		}
	}
	
	/**
	 * Modifies all fields of the wrapped object that are declared in T, but
	 * not those inherited from superclasses.
	 * 
	 * This method is consistent: given two equal objects; after scrambling
	 * both objects, they remain equal to each other.
	 * 
	 * It cannot modifiy:
	 * 1. static final fields, and
	 * 2. final fields that are initialized to a compile-time constant in the
	 *		field declaration.
	 * These fields will be left unmodified.
	 * 
	 * @param prefabValues Prefabricated values to take values from.
	 */
	public void shallowScramble(PrefabValues prefabValues) {
		for (Field field : type.getDeclaredFields()) {
			FieldAccessor accessor = new FieldAccessor(object, field);
			accessor.changeField(prefabValues);
		}
	}
}