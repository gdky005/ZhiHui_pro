package cc.zkteam.zkinfocollectpro.localserver;import java.util.ArrayList;import java.util.Collection;import java.util.LinkedHashMap;import java.util.Map;/** * An LRU cache, based on <code>LinkedHashMap</code>. * <p> * <p> * This cache has a fixed maximum number of elements (<code>cacheSize</code>). * If the cache is full and another entry is added, the LRU (least recently used) entry is dropped. * <p> * <p> * This class is thread-safe. All methods of this class are synchronized. * <p> * <p> * Author: Christian d'Heureuse, Inventec Informatik AG, Zurich, Switzerland<br> * Multi-licensed: EPL / LGPL / GPL / AL / BSD. */public class JMLRUCache<K, V> {    private static final float hashTableLoadFactor = 0.75f;    public LinkedHashMap<K, V> map;    private int cacheSize;    private OnLruRemoveListener onLruRemoveListener;    /**     * Creates a new LRU cache.     *     * @param cacheSize the maximum number of entries that will be kept in this cache.     */    public JMLRUCache(int cacheSize) {        this.cacheSize = cacheSize;        int hashTableCapacity = (int) Math.ceil(cacheSize / hashTableLoadFactor) + 1;        map = new LinkedHashMap<K, V>(hashTableCapacity, hashTableLoadFactor, true) {            // (an anonymous inner class)            private static final long serialVersionUID = 1;//            @Override//            public boolean removeEldestEntry(Entry<K, V> eldest) {//                boolean state = size() > JMLRUCache.this.cacheSize;//                if (state) needRemoveEntry(eldest);//                return size() > JMLRUCache.this.cacheSize;//            }        };    }    /**     * Retrieves an entry from the cache.<br>     * The retrieved entry becomes the MRU (most recently used) entry.     *     * @param key the key whose associated value is to be returned.     * @return the value associated to this key, or null if no value with this key exists in the cache.     */    public synchronized V get(K key) {//        Logger.i("Lru get:" + key);        return map.get(key);    }    /**     * Adds an entry to this cache.     * The new entry becomes the MRU (most recently used) entry.     * If an entry with the specified key already exists in the cache, it is replaced by the new entry.     * If the cache is full, the LRU (least recently used) entry is removed from the cache.     *     * @param key   the key with which the specified value is to be associated.     * @param value a value to be associated with the specified key.     */    public synchronized void put(K key, V value) {//        Logger.i("Lru put: key:" + key + ", value;" + value);        map.put(key, value);    }    /**     * Clears the cache.     */    public synchronized void clear() {        map.clear();    }    /**     * Returns the number of used entries in the cache.     *     * @return the number of entries currently in the cache.     */    public synchronized int usedEntries() {        return map.size();    }    /**     * Returns a <code>Collection</code> that contains a copy of all cache entries.     *     * @return a <code>Collection</code> with a copy of the cache content.     */    public synchronized Collection<Map.Entry<K, V>> getAll() {        return new ArrayList<>(map.entrySet());    }    public void setOnLruRemoveListener(OnLruRemoveListener onLruRemoveListener) {        this.onLruRemoveListener = onLruRemoveListener;    }    private synchronized Map.Entry<K, V> needRemoveEntry(Map.Entry<K, V> eldest) {//        Logger.i("Lru 移除的 key: " + eldest.getKey() + ", value: " + eldest.getValue());        if (onLruRemoveListener != null)            onLruRemoveListener.needRemoveEntry(eldest);        return eldest;    }    public interface OnLruRemoveListener {        void needRemoveEntry(Map.Entry eldest);    }} // end class LRUCache