/*
 * Copyright (c) 2022-2023 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.tommyettinger.textra.utils;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.IntArray;
import regexodus.Category;
import regexodus.Compatibility;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

/** An unordered map where the keys are case-insensitive Strings and the values are unboxed ints. Null keys are not
 * allowed. No allocation is done except when growing the table size.
 * <p>
 * This class performs fast contains and remove (typically O(1), worst case O(n) but that is rare in practice). Add may
 * be slightly slower, depending on hash collisions. Hashcodes are rehashed to reduce collisions and the need to resize.
 * Load factors greater than 0.91 greatly increase the chances to resize to the next higher POT size.
 * <p>
 * This implementation uses linear probing with the backward shift algorithm for removal. Hashcodes are rehashed using
 * a form of random hashing; the hash seed changes every time {@link #resize(int)} gets called, which means if resize()
 * has to be called early due to frequent collisions, the hashes will change when the multiplier does, and that may help
 * alleviate the collisions. Linear probing continues to work even when all hashCodes collide, just more slowly.
 * <br>
 * This implementation is closely based on {@link com.badlogic.gdx.utils.ObjectIntMap} from libGDX, but also uses ideas
 * from jdkgdxds, such as the randomized hashing (and the case-insensitive matching in general).
 * @author Nathan Sweet
 * @author Tommy Ettinger */
public class CaseInsensitiveIntMap implements Iterable<CaseInsensitiveIntMap.Entry> {
	public int size;

	protected String[] keyTable;
	protected int[] valueTable;

	protected float loadFactor;
	protected int threshold;

	/**
	 * A bitmask used to confine hashcodes to the size of the table. Must be all 1-bits in its low positions, ie a
	 * power of two minus 1. In {@link #place(String)}, this is used to get the relevant low bits of a hash.
	 */
	protected int mask;

	protected transient Entries entries1, entries2;
	protected transient Values values1, values2;
	protected transient Keys keys1, keys2;

	/**
	 * Used to establish the size of a hash table.
	 * The table size will always be a power of two, and should be the next power of two that is at least equal
	 * to {@code capacity / loadFactor}.
	 *
	 * @param capacity   the amount of items the hash table should be able to hold
	 * @param loadFactor between 0.0 (exclusive) and 1.0 (inclusive); the fraction of how much of the table can be filled
	 * @return the size of a hash table that can handle the specified capacity with the given loadFactor
	 */
	public static int tableSize (int capacity, float loadFactor) {
		if (capacity < 0) {
			throw new IllegalArgumentException("capacity must be >= 0: " + capacity);
		}
		int tableSize = 1 << -Integer.numberOfLeadingZeros(Math.max(2, (int)Math.ceil(capacity / loadFactor)) - 1);
		if (tableSize > 1 << 30 || tableSize < 0) {
			throw new IllegalArgumentException("The required capacity is too large: " + capacity);
		}
		return tableSize;
	}

	/** Creates a new map with an initial capacity of 51 and a load factor of 0.6. */
	public CaseInsensitiveIntMap() {
		this(51, 0.6f);
	}

	/** Creates a new map with a load factor of 0.6 .
	 * @param initialCapacity The backing array size is initialCapacity / loadFactor, increased to the next power of two. */
	public CaseInsensitiveIntMap(int initialCapacity) {
		this(initialCapacity, 0.6f);
	}

	/** Creates a new map with the specified initial capacity and load factor. This map will hold initialCapacity items before
	 * growing the backing table.
	 * @param initialCapacity The backing array size is initialCapacity / loadFactor, increased to the next power of two. */
	public CaseInsensitiveIntMap(int initialCapacity, float loadFactor) {
		if (loadFactor <= 0f || loadFactor >= 1f)
			throw new IllegalArgumentException("loadFactor must be > 0 and < 1: " + loadFactor);
		this.loadFactor = loadFactor;

		int tableSize = tableSize(initialCapacity, loadFactor);
		threshold = (int)(tableSize * loadFactor);
		mask = tableSize - 1;

		keyTable = new String[tableSize];
		valueTable = new int[tableSize];
	}

	/** Creates a new map and puts key-value pairs sequentially from the two given arrays until either array is
	 * exhausted. The initial capacity will be the length of the shorter of the two arrays, and the load factor will be
	 * 0.6 . */
	public CaseInsensitiveIntMap(String[] keys, int[] values) {
		this.loadFactor = 0.6f;
		final int len = Math.min(keys.length, values.length);

		int tableSize = tableSize(len, loadFactor);
		threshold = (int)(tableSize * loadFactor);
		mask = tableSize - 1;

		keyTable = new String[tableSize];
		valueTable = new int[tableSize];

		String key;
		for (int i = 0; i < len; i++) {
			key = keys[i];
			if (key != null) put(key, values[i]);
		}
	}

	/** Creates a new map identical to the specified map. */
	public CaseInsensitiveIntMap(CaseInsensitiveIntMap map) {
		this((int)(map.keyTable.length * map.loadFactor), map.loadFactor);
		System.arraycopy(map.keyTable, 0, keyTable, 0, map.keyTable.length);
		System.arraycopy(map.valueTable, 0, valueTable, 0, map.valueTable.length);
		size = map.size;
	}

	/** Returns an index &gt;= 0 and &lt;= {@link #mask} for the specified {@code item}.
	 */
	protected int place (String item) {
		return hashCodeIgnoreCase(item, mask) & mask;
	}

	/** Returns the index of the key if already present, else ~index for the next empty index. This can be overridden in this
	 * package to compare for equality differently than {@link Object#equals(Object)}. */
	int locateKey (String key) {
		if (key == null) throw new IllegalArgumentException("key cannot be null.");
		String[] keyTable = this.keyTable;
		for (int i = place(key);; i = i + 1 & mask) {
			String other = keyTable[i];
			if (other == null) return ~i; // Empty space is available.
			if (other.equalsIgnoreCase(key)) return i; // Same key was found.
		}
	}

	public void put (String key, int value) {
		int i = locateKey(key);
		if (i >= 0) { // Existing key was found.
			valueTable[i] = value;
			return;
		}
		i = ~i; // Empty space was found.
		keyTable[i] = key;
		valueTable[i] = value;
		if (++size >= threshold) resize(keyTable.length << 1);
	}

	/** Returns the old value associated with the specified key, or the specified default value. */
	public int put (String key, int value, int defaultValue) {
		int i = locateKey(key);
		if (i >= 0) { // Existing key was found.
			int oldValue = valueTable[i];
			valueTable[i] = value;
			return oldValue;
		}
		i = ~i; // Empty space was found.
		keyTable[i] = key;
		valueTable[i] = value;
		if (++size >= threshold) resize(keyTable.length << 1);
		return defaultValue;
	}

	/** Puts keys with values in sequential pairs from the two arrays given, until either array is exhausted. */
	public void putAll (String[] keys, int[] values) {
		final int len = Math.min(keys.length, values.length);
		ensureCapacity(len);
		String key;
		for (int i = 0; i < len; i++) {
			key = keys[i];
			if (key != null) put(key, values[i]);
		}
	}

	public void putAll (CaseInsensitiveIntMap map) {
		ensureCapacity(map.size);
		String[] keyTable = map.keyTable;
		int[] valueTable = map.valueTable;
		String key;
		for (int i = 0, n = keyTable.length; i < n; i++) {
			key = keyTable[i];
			if (key != null) put(key, valueTable[i]);
		}
	}

	/** Skips checks for existing keys, doesn't increment size. */
	protected void putResize (String key, int value) {
		String[] keyTable = this.keyTable;
		for (int i = place(key);; i = (i + 1) & mask) {
			if (keyTable[i] == null) {
				keyTable[i] = key;
				valueTable[i] = value;
				return;
			}
		}
	}

	/** Returns the value for the specified key, or the default value if the key is not in the map. */
	public int get (String key, int defaultValue) {
		int i = locateKey(key);
		return i < 0 ? defaultValue : valueTable[i];
	}

	/** Returns the key's current value and increments the stored value. If the key is not in the map, defaultValue + increment is
	 * put into the map and defaultValue is returned. */
	public int getAndIncrement (String key, int defaultValue, int increment) {
		int i = locateKey(key);
		if (i >= 0) { // Existing key was found.
			int oldValue = valueTable[i];
			valueTable[i] += increment;
			return oldValue;
		}
		i = ~i; // Empty space was found.
		keyTable[i] = key;
		valueTable[i] = defaultValue + increment;
		if (++size >= threshold) resize(keyTable.length << 1);
		return defaultValue;
	}

	/** Returns the value for the removed key, or the default value if the key is not in the map. */
	public int remove (String key, int defaultValue) {
		int i = locateKey(key);
		if (i < 0) return defaultValue;
		String[] keyTable = this.keyTable;
		int[] valueTable = this.valueTable;
		int oldValue = valueTable[i];
		int mask = this.mask, next = i + 1 & mask;
		while ((key = keyTable[next]) != null) {
			int placement = place(key);
			if ((next - placement & mask) > (i - placement & mask)) {
				keyTable[i] = key;
				valueTable[i] = valueTable[next];
				i = next;
			}
			next = next + 1 & mask;
		}
		keyTable[i] = null;
		size--;
		return oldValue;
	}

	/** Returns true if the map has one or more items. */
	public boolean notEmpty () {
		return size > 0;
	}

	/** Returns true if the map is empty. */
	public boolean isEmpty () {
		return size == 0;
	}

	/** Reduces the size of the backing arrays to be the specified capacity / loadFactor, or less. If the capacity is already less,
	 * nothing is done. If the map contains more items than the specified capacity, the next highest power of two capacity is used
	 * instead. */
	public void shrink (int maximumCapacity) {
		if (maximumCapacity < 0) throw new IllegalArgumentException("maximumCapacity must be >= 0: " + maximumCapacity);
		int tableSize = tableSize(maximumCapacity, loadFactor);
		if (keyTable.length > tableSize) resize(tableSize);
	}

	/** Clears the map and reduces the size of the backing arrays to be the specified capacity / loadFactor, if they are larger. */
	public void clear (int maximumCapacity) {
		int tableSize = tableSize(maximumCapacity, loadFactor);
		if (keyTable.length <= tableSize) {
			clear();
			return;
		}
		size = 0;
		resize(tableSize);
	}

	public void clear () {
		if (size == 0) return;
		size = 0;
		Arrays.fill(keyTable, null);
	}

	/** Returns true if the specified value is in the map. Note this traverses the entire map and compares every value, which may
	 * be an expensive operation. */
	public boolean containsValue (int value) {
		String[] keyTable = this.keyTable;
		int[] valueTable = this.valueTable;
		for (int i = valueTable.length - 1; i >= 0; i--)
			if (keyTable[i] != null && valueTable[i] == value) return true;
		return false;
	}

	public boolean containsKey (String key) {
		return locateKey(key) >= 0;
	}

	/** Returns the key for the specified value, or null if it is not in the map. Note this traverses the entire map and compares
	 * every value, which may be an expensive operation. */
	public String findKey (int value) {
		String[] keyTable = this.keyTable;
		int[] valueTable = this.valueTable;
		for (int i = valueTable.length - 1; i >= 0; i--) {
			String key = keyTable[i];
			if (key != null && valueTable[i] == value) return key;
		}
		return null;
	}

	/** Increases the size of the backing array to accommodate the specified number of additional items / loadFactor. Useful before
	 * adding many items to avoid multiple backing array resizes. */
	public void ensureCapacity (int additionalCapacity) {
		int tableSize = tableSize(size + additionalCapacity, loadFactor);
		if (keyTable.length < tableSize) resize(tableSize);
	}

	final void resize (int newSize) {
		int oldCapacity = keyTable.length;
		threshold = (int)(newSize * loadFactor);
		mask = newSize - 1;

		String[] oldKeyTable = keyTable;
		int[] oldValueTable = valueTable;

		keyTable = new String[newSize];
		valueTable = new int[newSize];

		if (size > 0) {
			for (int i = 0; i < oldCapacity; i++) {
				String key = oldKeyTable[i];
				if (key != null) putResize(key, oldValueTable[i]);
			}
		}
	}

	public int hashCode () {
		int h = size;
		String[] keyTable = this.keyTable;
		int[] valueTable = this.valueTable;
		for (int i = 0, n = keyTable.length; i < n; i++) {
			String key = keyTable[i];
			if (key != null) h ^= hashCodeIgnoreCase(key) ^ valueTable[i];
		}
		return h;
	}

	public boolean equals (Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof CaseInsensitiveIntMap)) return false;
		CaseInsensitiveIntMap other = (CaseInsensitiveIntMap)obj;
		if (other.size != size) return false;
		String[] keyTable = this.keyTable;
		int[] valueTable = this.valueTable;
		for (int i = 0, n = keyTable.length; i < n; i++) {
			String key = keyTable[i];
			if (key != null) {
				int otherValue = other.get(key, -1);
				if (otherValue == -1 && !other.containsKey(key)) return false;
				if (otherValue != valueTable[i]) return false;
			}
		}
		return true;
	}

	public String toString (String separator) {
		return toString(separator, false);
	}

	public String toString () {
		return toString(", ", true);
	}

	public String toString (String separator, boolean braces) {
		if (size == 0) return braces ? "{}" : "";
		StringBuilder buffer = new StringBuilder(32);
		if (braces) buffer.append('{');
		String[] keyTable = this.keyTable;
		int[] valueTable = this.valueTable;
		int i = keyTable.length;
		while (i-- > 0) {
			String key = keyTable[i];
			if (key == null) continue;
			buffer.append(key);
			buffer.append('=');
			buffer.append(valueTable[i]);
			break;
		}
		while (i-- > 0) {
			String key = keyTable[i];
			if (key == null) continue;
			buffer.append(separator);
			buffer.append(key);
			buffer.append('=');
			buffer.append(valueTable[i]);
		}
		if (braces) buffer.append('}');
		return buffer.toString();
	}

	public Entries iterator () {
		return entries();
	}

	/** Returns an iterator for the entries in the map. Remove is supported.
	 * Use the {@link Entries} constructor for nested or multithreaded iteration. */
	public Entries entries () {
		if (entries1 == null) {
			entries1 = new Entries(this);
			entries2 = new Entries(this);
		}
		if (!entries1.valid) {
			entries1.reset();
			entries1.valid = true;
			entries2.valid = false;
			return entries1;
		}
		entries2.reset();
		entries2.valid = true;
		entries1.valid = false;
		return entries2;
	}

	/** Returns an iterator for the values in the map. Remove is supported.
	 */
	public Values values () {
		if (values1 == null) {
			values1 = new Values(this);
			values2 = new Values(this);
		}
		if (!values1.valid) {
			values1.reset();
			values1.valid = true;
			values2.valid = false;
			return values1;
		}
		values2.reset();
		values2.valid = true;
		values1.valid = false;
		return values2;
	}

	/** Returns an iterator for the keys in the map. Remove is supported.
	 */
	public Keys keys () {
		if (keys1 == null) {
			keys1 = new Keys(this);
			keys2 = new Keys(this);
		}
		if (!keys1.valid) {
			keys1.reset();
			keys1.valid = true;
			keys2.valid = false;
			return keys1;
		}
		keys2.reset();
		keys2.valid = true;
		keys1.valid = false;
		return keys2;
	}

	public static class Entry {
		public String key;
		public int value;

		public String toString () {
			return key + "=" + value;
		}
	}

	private static class MapIterator {
		public boolean hasNext;

		final CaseInsensitiveIntMap map;
		int nextIndex, currentIndex;
		boolean valid = true;

		public MapIterator (CaseInsensitiveIntMap map) {
			this.map = map;
			reset();
		}

		public void reset () {
			currentIndex = -1;
			nextIndex = -1;
			findNextIndex();
		}

		void findNextIndex () {
			String[] keyTable = map.keyTable;
			for (int n = keyTable.length; ++nextIndex < n;) {
				if (keyTable[nextIndex] != null) {
					hasNext = true;
					return;
				}
			}
			hasNext = false;
		}

		public void remove () {
			int i = currentIndex;
			if (i < 0) throw new IllegalStateException("next must be called before remove.");
			String[] keyTable = map.keyTable;
			int[] valueTable = map.valueTable;
			int mask = map.mask, next = i + 1 & mask;
			String key;
			while ((key = keyTable[next]) != null) {
				int placement = map.place(key);
				if ((next - placement & mask) > (i - placement & mask)) {
					keyTable[i] = key;
					valueTable[i] = valueTable[next];
					i = next;
				}
				next = next + 1 & mask;
			}
			keyTable[i] = null;
			map.size--;
			if (i != currentIndex) --nextIndex;
			currentIndex = -1;
		}
	}

	public static class Entries extends MapIterator implements Iterable<Entry>, Iterator<Entry> {
		Entry entry = new Entry();

		public Entries (CaseInsensitiveIntMap map) {
			super(map);
		}

		/** Note the same entry instance is returned each time this method is called. */
		public Entry next () {
			if (!hasNext) throw new NoSuchElementException();
			if (!valid) throw new GdxRuntimeException("#iterator() cannot be used nested.");
			String[] keyTable = map.keyTable;
			entry.key = keyTable[nextIndex];
			entry.value = map.valueTable[nextIndex];
			currentIndex = nextIndex;
			findNextIndex();
			return entry;
		}

		public boolean hasNext () {
			if (!valid) throw new GdxRuntimeException("#iterator() cannot be used nested.");
			return hasNext;
		}

		public Entries iterator () {
			return this;
		}
	}

	public static class Values extends MapIterator {
		public Values (CaseInsensitiveIntMap map) {
			super(map);
		}

		public boolean hasNext () {
			if (!valid) throw new GdxRuntimeException("#iterator() cannot be used nested.");
			return hasNext;
		}

		public int next () {
			if (!hasNext) throw new NoSuchElementException();
			if (!valid) throw new GdxRuntimeException("#iterator() cannot be used nested.");
			int value = map.valueTable[nextIndex];
			currentIndex = nextIndex;
			findNextIndex();
			return value;
		}

		public Values iterator () {
			return this;
		}

		/** Returns a new array containing the remaining values. */
		public IntArray toArray () {
			IntArray array = new IntArray(true, map.size);
			while (hasNext)
				array.add(next());
			return array;
		}

		/** Adds the remaining values to the specified array. */
		public IntArray toArray (IntArray array) {
			while (hasNext)
				array.add(next());
			return array;
		}
	}

	public static class Keys extends MapIterator implements Iterable<String>, Iterator<String> {
		public Keys (CaseInsensitiveIntMap map) {
			super(map);
		}

		public boolean hasNext () {
			if (!valid) throw new GdxRuntimeException("#iterator() cannot be used nested.");
			return hasNext;
		}

		public String next () {
			if (!hasNext) throw new NoSuchElementException();
			if (!valid) throw new GdxRuntimeException("#iterator() cannot be used nested.");
			String key = map.keyTable[nextIndex];
			currentIndex = nextIndex;
			findNextIndex();
			return key;
		}

		public Keys iterator () {
			return this;
		}

		/** Returns a new array containing the remaining keys. */
		public Array<String> toArray () {
			return toArray(new Array<String>(true, map.size, String.class));
		}

		/** Adds the remaining keys to the array. */
		public Array<String> toArray (Array<String> array) {
			while (hasNext)
				array.add(next());
			return array;
		}
	}

	/**
	 * Simple 32-bit multiplicative hashing with a tiny mix at the end. This gets the hash as if all cased letters have
	 * been converted to upper case by {@link Category#caseUp(char)}; this should be correct for all alphabets in
	 * Unicode except Georgian. Typically, place() methods in Sets and Maps here that want case-insensitive hashing
	 * would use this with {@code (hashCodeIgnoreCase(text) >>> shift)} or
	 * {@code (hashCodeIgnoreCase(text) & mask)}.
	 *
	 * @param data a non-null CharSequence; often a String, but this has no trouble with a StringBuilder
	 * @return an int hashCode; quality should be similarly good across any bits
	 */
	public static int hashCodeIgnoreCase (final CharSequence data) {
		return hashCodeIgnoreCase(data, 908697017);
	}

	/**
	 * Simple 32-bit multiplicative hashing with a tiny mix at the end. This gets the hash as if all cased letters have
	 * been converted to upper case by {@link Category#caseUp(char)}; this should be correct for all alphabets in
	 * Unicode except Georgian. Typically, place() methods in Sets and Maps here that want case-insensitive hashing
	 * would use this with {@code (hashCodeIgnoreCase(text, seed) >>> shift)} or
	 * {@code (hashCodeIgnoreCase(text, seed) & mask)}.
	 *
	 * @param data a non-null CharSequence; often a String, but this has no trouble with a StringBuilder
	 * @param seed any int; must be the same between calls if two equivalent values for {@code data} must be the same
	 * @return an int hashCode; quality should be similarly good across any bits
	 */
	public static int hashCodeIgnoreCase (final CharSequence data, int seed) {
		if(data == null) return 0;
		final int len = data.length();
		seed ^= len;
		for (int p = 0; p < len; p++) {
			seed = Compatibility.imul(-594347645, seed + Category.caseUp(data.charAt(p)));
		}
		return seed^(seed<<27|seed>>> 5)^(seed<< 9|seed>>>23);
	}
}
