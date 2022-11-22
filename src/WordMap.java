import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class WordMap<K,V> implements Map<K,V> {
    @Override
    public int size() { }
    @Override
    public boolean isEmpty() { }
    @Override
    public boolean containsKey(Object key) { }
    @Override
    public boolean containsValue(Object value) { }
    @Override
    public V get(Object key) {}
    @Override
    public V put(K key, V value) {}
    @Override
    public V remove(Object key) {}
    @Override
    public void putAll(Map<? extends K,? extends V> m) {}
    @Override
    public void clear() {}
    @Override
    public Set<K> keySet() {}
    @Override
    public Collection<V> values() {}
    @Override
    public Set<Map.Entry<K,V>> entrySet() {}
}
