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

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

/** An unordered map where the keys are case-insensitive Strings and the values are unboxed ints. Null keys are not
 * allowed. No allocation is done except when growing the table size.
 * <p>
 * This class performs fast contains and remove (typically O(1), worst case O(n) but that is rare in practice). Add may be
 * slightly slower, depending on hash collisions. Hashcodes are rehashed to reduce collisions and the need to resize. Load factors
 * greater than 0.91 greatly increase the chances to resize to the next higher POT size.
 * <p>
 * This implementation uses linear probing with the backward shift algorithm for removal. Hashcodes are rehashed using
 * a form of random hashing; a multiplier changes every time {@link #resize(int)} gets called, which means if resize()
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

	/** Used by {@link #place(String)} to bit shift the upper bits of a {@code long} into a usable range (&gt;= 0 and &lt;=
	 * {@link #mask}). The shift can be negative, which is convenient to match the number of bits in mask: if mask is a 7-bit
	 * number, a shift of -7 shifts the upper 7 bits into the lowest 7 positions. This class sets the shift &gt; 32 and &lt; 64,
	 * which if used with an int will still move the upper bits of an int to the lower bits due to Java's implicit modulus on
	 * shifts.
	 * <p>
	 * {@link #mask} can also be used to mask the low bits of a number, which may be faster for some hashcodes, if
	 * {@link #place(String)} is overridden. */
	protected int shift;

	/** A bitmask used to confine hashcodes to the size of the table. Must be all 1 bits in its low positions, ie a power of two
	 * minus 1. If {@link #place(String)} is overriden, this can be used instead of {@link #shift} to isolate usable bits of a
	 * hash. */
	protected int mask;
	/**
	 * Used by {@link #place(String)} to mix hashCode() results. Changes on every call to {@link #resize(int)} by default.
	 * This only needs to be serialized if the full key and value tables are serialized, or if the iteration order should be
	 * the same before and after serialization.
	 */
	protected int hashMultiplier = 0x12ED03;

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
		shift = Long.numberOfLeadingZeros(mask);

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
		shift = Long.numberOfLeadingZeros(mask);

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
		hashMultiplier = map.hashMultiplier;
		System.arraycopy(map.keyTable, 0, keyTable, 0, map.keyTable.length);
		System.arraycopy(map.valueTable, 0, valueTable, 0, map.valueTable.length);
		size = map.size;
	}

	/** Returns an index &gt;= 0 and &lt;= {@link #mask} for the specified {@code item}.
	 */
	protected int place (String item) {
//		final int n = item.length();
//		int h = n;
//		for (int i = 0; i < n; i++) {
//			h = 1003 * h ^ Category.caseFold(item.charAt(i));
//		}
//		return (int)(h * hashMultiplier >>> shift);
		return hashCodeIgnoreCase(item, hashMultiplier) & mask;
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
	private void putResize (String key, int value) {
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
		shift = Long.numberOfLeadingZeros(mask);

		hashMultiplier = GOOD_MULTIPLIERS[hashMultiplier * shift >>> 5 & 511];

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
			if (key != null) h += key.hashCode() + valueTable[i];
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
				int otherValue = other.get(key, 0);
				if (otherValue == 0 && !other.containsKey(key)) return false;
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

	private String toString (String separator, boolean braces) {
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

	static public class Entry {
		public String key;
		public int value;

		public String toString () {
			return key + "=" + value;
		}
	}

	static private class MapIterator {
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

	static public class Entries extends MapIterator implements Iterable<Entry>, Iterator<Entry> {
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

	static public class Values extends MapIterator {
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

	static public class Keys extends MapIterator implements Iterable<String>, Iterator<String> {
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

	public static final int[] GOOD_MULTIPLIERS = {
			0x00110427, 0x00144057, 0x001AFB2F, 0x001F1753, 0x00135205, 0x00176C45, 0x001E3A15, 0x001F406D,
			0x001DEF1D, 0x0018BD49, 0x001DE7A9, 0x00117949, 0x001BDC1D, 0x00190A37, 0x0014A839, 0x00108EB9,
			0x0019EB97, 0x0014A6B7, 0x001B3283, 0x001F890F, 0x001502ED, 0x00197DB1, 0x001A2447, 0x001F9159,
			0x001C8F59, 0x00115359, 0x00163683, 0x00121771, 0x001FC839, 0x001D782D, 0x0010B147, 0x0017A481,
			0x00146285, 0x001CDEFD, 0x001853BF, 0x001F1921, 0x001CE9E7, 0x001A3CF7, 0x001D6E03, 0x00192469,
			0x001746EF, 0x0013A80F, 0x00104C7B, 0x0012B54B, 0x00126351, 0x0010B375, 0x0014D59D, 0x00123A37,
			0x0015F383, 0x00134417, 0x0017E00B, 0x0013CDC5, 0x0016141B, 0x001FE2B7, 0x00199397, 0x00188BF9,
			0x0010BA01, 0x00145F47, 0x00197749, 0x001F55DF, 0x00128BA9, 0x001871B3, 0x0016B5DF, 0x00144577,
			0x0013FEDB, 0x001EA4FB, 0x0015E779, 0x001EA353, 0x001CFB85, 0x001B66E9, 0x001DDD59, 0x001493AD,
			0x0017EBF1, 0x00105079, 0x001D51C9, 0x0014BDFB, 0x001D41AD, 0x001B85D9, 0x0015E173, 0x00196513,
			0x001F7329, 0x00175EB9, 0x00199109, 0x0018EE5D, 0x0013C2EF, 0x00137FA3, 0x0011DE3B, 0x00167B03,
			0x00186BAF, 0x00174413, 0x00115527, 0x00155B3B, 0x00192E1F, 0x001262F1, 0x0015C417, 0x0016BC6D,
			0x0012B161, 0x0016BF83, 0x001FD2AF, 0x00164C1D, 0x001FC9CD, 0x001B0351, 0x0015C3DD, 0x0019A535,
			0x001C87D1, 0x001A5EFD, 0x001796CD, 0x001F8FFD, 0x00155AC7, 0x001B61A3, 0x0016A8C7, 0x0014598F,
			0x001C2379, 0x0013CAE9, 0x001711A3, 0x001A3051, 0x001207DB, 0x00177419, 0x0015682D, 0x00197E5D,
			0x001E71A3, 0x001FF6F7, 0x001D41F9, 0x001304B5, 0x001DAF93, 0x0010B893, 0x001D4F5F, 0x0011571B,
			0x00146829, 0x00127DE3, 0x001DFAB3, 0x0015D6FB, 0x0014C823, 0x00118E35, 0x0011FFF3, 0x00163C87,
			0x001EEAC1, 0x001774D9, 0x00178F45, 0x001A1355, 0x00163055, 0x001406AD, 0x001F837D, 0x001D7791,
			0x00132189, 0x001AAF61, 0x001A5DA1, 0x00195339, 0x001C0959, 0x00118555, 0x001F5089, 0x0014F9AD,
			0x0017AD03, 0x00149B21, 0x0015A77D, 0x0019598F, 0x001399E9, 0x0015F519, 0x0017B019, 0x0016DFCF,
			0x001E3727, 0x0014E715, 0x001E00A7, 0x001D2923, 0x0019DA5B, 0x001E999D, 0x001692CD, 0x0011675F,
			0x00154251, 0x001E1FD1, 0x001CA0E3, 0x00104C8F, 0x00172AEF, 0x001FB11D, 0x0011C82D, 0x00156639,
			0x0019C547, 0x001313B1, 0x00111491, 0x001B2013, 0x001C9161, 0x00174255, 0x001B9E9D, 0x00136EED,
			0x00180BB5, 0x0015CA1D, 0x001011B1, 0x001D4F13, 0x00167571, 0x0014C73D, 0x0013CE13, 0x0018AEFB,
			0x001AA60D, 0x0010B4F7, 0x001177A1, 0x001CB051, 0x001AD93D, 0x0011EE1D, 0x0014AAEF, 0x00156D99,
			0x00118A99, 0x001D4AB5, 0x0019386F, 0x001A6671, 0x001BB619, 0x0016AC51, 0x001A9B49, 0x001405C7,
			0x001F8297, 0x001FAAA3, 0x00165A91, 0x00198B9D, 0x001FAE8D, 0x001A1161, 0x001BCC61, 0x001289EF,
			0x0016359B, 0x0014A90D, 0x00116F1F, 0x001D1327, 0x00195907, 0x0010D205, 0x00160305, 0x00103CF9,
			0x001E52B3, 0x001531E7, 0x0018214F, 0x0018BA45, 0x001224C3, 0x00172017, 0x0016E997, 0x001E11A9,
			0x0018B621, 0x001C0415, 0x001FBD61, 0x0018F233, 0x001DFB27, 0x00149CA1, 0x0015727D, 0x001EDCFB,
			0x00137A97, 0x0010470F, 0x00193EFB, 0x00186D09, 0x001D5457, 0x001FF939, 0x001A125B, 0x001FC2B9,
			0x001DFED7, 0x0010E173, 0x001D8C45, 0x001A5F23, 0x001130B7, 0x001E7627, 0x001FFB7B, 0x00117FFB,
			0x001CE8C5, 0x00194911, 0x001C755F, 0x001FA005, 0x001AB7E3, 0x001B2267, 0x0015E959, 0x0011E587,
			0x0013A087, 0x0013E2FF, 0x001DEE81, 0x001E9C51, 0x0017582B, 0x001A987F, 0x0013110D, 0x00139D37,
			0x0013E6E9, 0x00146573, 0x00150CDD, 0x001A6D23, 0x00173335, 0x001519A9, 0x0012AF31, 0x0011BC6D,
			0x0012208B, 0x0015E777, 0x001D9D5B, 0x0010B5A3, 0x001D16C3, 0x001D747B, 0x001BAB07, 0x00110B4D,
			0x00169F97, 0x001D9863, 0x0019A897, 0x00117281, 0x001171AD, 0x001CFC1D, 0x0017A8A3, 0x001E22E5,
			0x0017FF21, 0x001BBED3, 0x00171397, 0x00141705, 0x001764F9, 0x001FD64D, 0x001E575F, 0x001AC54B,
			0x00184525, 0x00167C85, 0x001D0467, 0x0014849F, 0x00142D4D, 0x001E466F, 0x001CF5F3, 0x0012BB2B,
			0x00177A6D, 0x001F739D, 0x001E1CBB, 0x00110B4F, 0x001CCA97, 0x001A7A8B, 0x001EE27B, 0x001F10ED,
			0x0015E8E7, 0x00127213, 0x001FA37D, 0x001A5CCF, 0x00174AED, 0x0013CDB3, 0x001D0285, 0x00160E77,
			0x0012839D, 0x0019D48F, 0x00175D4B, 0x001EDD83, 0x001D28E9, 0x0019CD55, 0x0018A5B9, 0x001890DF,
			0x0011AA71, 0x001F5B39, 0x00161E59, 0x00126B73, 0x0019F94B, 0x001EFB05, 0x0018D0DB, 0x00161CB1,
			0x00172839, 0x0016A807, 0x0016DDB3, 0x001C29F3, 0x00130927, 0x00110933, 0x001D48AD, 0x001D771F,
			0x0015F46B, 0x0012F029, 0x001D0FB1, 0x001F2203, 0x0019C823, 0x001D3083, 0x0014D7F3, 0x0010980F,
			0x0012F39D, 0x0010973B, 0x001FE897, 0x001646C5, 0x0016B883, 0x00132743, 0x001BC7DD, 0x00177A59,
			0x00125625, 0x00102159, 0x00198BD7, 0x001D5929, 0x0012DB15, 0x0013F511, 0x00120391, 0x00139CEB,
			0x001DD079, 0x001C14A5, 0x00199D61, 0x0016AD25, 0x00189031, 0x00108961, 0x0012E565, 0x001C1FC9,
			0x00165357, 0x001036CD, 0x001CBFF9, 0x001D1677, 0x00111F07, 0x0016F10B, 0x00135FCB, 0x001039E3,
			0x0011AB31, 0x0018E81D, 0x001C5E1D, 0x0015E307, 0x001F24A5, 0x00133BA9, 0x001CBA2D, 0x0018AFF5,
			0x00110C6F, 0x00194F51, 0x001F1489, 0x0010BB83, 0x0011C7DF, 0x0018AD79, 0x001DC40D, 0x00182C73,
			0x001152D1, 0x00189D5D, 0x00135DE9, 0x0019E5CB, 0x0012D751, 0x00107A79, 0x001CFC6B, 0x0017874B,
			0x001673B5, 0x001BFC07, 0x00134693, 0x001CCB7D, 0x0012FC0D, 0x001084C9, 0x00109195, 0x0017C81B,
			0x001436DB, 0x00117511, 0x001B43AD, 0x0014A08B, 0x00110E77, 0x00198705, 0x001BF0A9, 0x0015B1A5,
			0x00147C69, 0x0011A699, 0x001283AF, 0x00192D37, 0x001A258D, 0x001E7977, 0x0016DDFF, 0x001B6795,
			0x00182DA7, 0x001FADDF, 0x0017708F, 0x0010CD6D, 0x001D6439, 0x001929E7, 0x0017A3BF, 0x001F8F4F,
			0x0012DD43, 0x0016A42F, 0x0019D07D, 0x0014FC61, 0x00103C4B, 0x00193B71, 0x001515F9, 0x001FD01F,
			0x001AABEB, 0x001D4B3B, 0x00163CC1, 0x001FFDBD, 0x00162D79, 0x001EBA0D, 0x001FDA6F, 0x00108105,
			0x0011F17F, 0x00108697, 0x00102E71, 0x001B618F, 0x001FFF2B, 0x001366B7, 0x0019D359, 0x001A0F6B,
			0x001790ED, 0x001CC927, 0x001E68E7, 0x0011B6DB, 0x0015E91F, 0x00155B4D, 0x00137107, 0x0011E479,
			0x001E5339, 0x0011A12D, 0x0012F0D5, 0x0011C939, 0x001F87A1, 0x001CEEB7, 0x001DAFB9, 0x00199E47,
			0x00162773, 0x00138E89, 0x001C365D, 0x001619D3, 0x001D56BF, 0x001405D9, 0x001D39D7, 0x00185F55,
			0x00181C09, 0x0010C3DD, 0x0010D7E3, 0x00109497, 0x0018B4FF, 0x00112CB9, 0x001FBBE1, 0x001D05F9,
			0x0018F571, 0x0011D957, 0x001CD6C9, 0x001D12DB, 0x001E982F, 0x001F2B93, 0x0015EF87, 0x0018A2DD,
	};

	/**
	 * Gets a 32-bit thoroughly-random hashCode from the given CharSequence, ignoring the case of any cased letters.
	 * Uses <a href="https://github.com/wangyi-fudan/wyhash">wyhash</a> version 4.2, but shrunk down to work on 16-bit
	 * char values instead of 64-bit long values. This gets the hash as if all cased letters have been
	 * converted to upper case by {@link Category#caseUp(char)}; this should be correct for all alphabets in
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
	 * Gets a 32-bit thoroughly-random hashCode from the given CharSequence, ignoring the case of any cased letters.
	 * UUses <a href="https://github.com/wangyi-fudan/wyhash">wyhash</a> version 4.2, but shrunk down to work on 16-bit
	 * char values instead of 64-bit long values. This gets the hash as if all cased letters have been
	 * converted to upper case by {@link Category#caseUp(char)}; this should be correct for all alphabets in
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
		int b0 = GOOD_MULTIPLIERS[(seed & 127)];
		int b1 = GOOD_MULTIPLIERS[(seed >>>  8 & 127)+128];
		int b2 = GOOD_MULTIPLIERS[(seed >>> 16 & 127)+256];
		int b3 = GOOD_MULTIPLIERS[(seed >>> 24 & 127)+384];
		int a, b;
		int p = 0;
		if(len<=2){
			if(len==2){ a=Category.caseUp(data.charAt(0)); b=Category.caseUp(data.charAt(1)); }
			else if(len==1){ a=Category.caseUp(data.charAt(0)); b=0;}
			else a=b=0;
		}
		else{
			int i=len;
			if(i>=6){
				int see1=seed, see2=seed;
				do{
					seed=(Category.caseUp(data.charAt(p  ))^b1)*(Category.caseUp(data.charAt(p+1))^seed);seed^=(seed<< 3|seed>>>29)^(seed<<24|seed>>> 8);
					see1=(Category.caseUp(data.charAt(p+2))^b2)*(Category.caseUp(data.charAt(p+3))^see1);see1^=(see1<<21|see1>>>11)^(see1<<15|see1>>>19);
					see2=(Category.caseUp(data.charAt(p+4))^b3)*(Category.caseUp(data.charAt(p+5))^see2);see2^=(see2<<26|see2>>> 6)^(see2<< 7|see2>>>25);
					p+=6;i-=6;
				}while(i>=6);
				seed^=see1^see2;
			}
			while((i>2)){
				seed=(Category.caseUp(data.charAt(p  ))^b1)*(Category.caseUp(data.charAt(p+1))^seed);seed^=(seed<< 3|seed>>>29)^(seed<<24|seed>>> 8);
				i-=2; p+=2;
			}
			a=Category.caseUp(data.charAt(len-2));
			b=Category.caseUp(data.charAt(len-1));
		}
		a*=b2;
		b^=seed+len;
		b=(b<< 3|b>>>29)^(a=(a<<24|a>>> 8)+b^b0)+(a<< 7|a>>>25);
		a=(a<<14|a>>>18)^(b=(b<<29|b>>> 3)+a^b1)+(b<<11|b>>>21);
		// I don't know if we need this level of robust mixing.
//		b=(b<<19|b>>>13)^(a=(a<< 5|a>>>27)+b^b2)+(a<<29|a>>> 3);
//		a=(a<<17|a>>>15)^(b=(b<<11|b>>>21)+a^b3)+(b<<23|b>>> 9);
		return a^(a<<27|a>>> 5)^(a<< 9|a>>>23);
	}

}
