package util;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class BlockingMap<K, V> {
    private Map<K, ArrayBlockingQueue<V>> map = new ConcurrentHashMap<>();

    private BlockingQueue<V> getQueue(K key, boolean replace) {
        return map.compute(key, (k, v) -> replace || v == null ? new ArrayBlockingQueue<>(1) : v);
    }

    public void put(K key, V value) {
        getQueue(key, true).add(value);
    }

    public V get(K key) {
        try {
			return getQueue(key, false).take();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
        return null;
    }

    public V get(K key, long timeout, TimeUnit unit) {
        try {
			return getQueue(key, false).poll(timeout, unit);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
        return null;
    }

	public boolean isEmpty() {
		return map.isEmpty();
	}

	public void clear() {
		map.clear();
	}

	public Set<K> keySet() {
		return map.keySet();
	}
	
	public Collection<ArrayBlockingQueue<V>> values() {
		return map.values();
	}
}
