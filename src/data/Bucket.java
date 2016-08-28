package data;

import java.util.ArrayList;
import java.util.HashMap;

public class Bucket<K, V, D, E> {

	private K key;
	private V value;
	private D delta;
	private D priority;
	private ArrayList<E> links;
	private int length;
	private ArrayList<Integer> mirrors;
	private int master;
	private HashMap<Integer, D> offset;// Caching mechanisms for the master 

	public void setKey(K key) {
		this.key = key;
	}

	public void setLinks(ArrayList<E> links) {
		this.links = links;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public void setMirrors(ArrayList<Integer> mirrors) {
		this.mirrors = mirrors;
	}

	public void setMaster(int master) {
		this.master = master;
	}

	public Bucket() {
		links = new ArrayList<E>();
		mirrors = new ArrayList<Integer>();
		offset = new HashMap<Integer, D>();
	}
	public V getValue() {
		return value;
	}

	public void setValue(V value) {
		this.value = value;
	}

	public D getDelta() {
		return delta;
	}

	public void setDelta(D delta) {
		this.delta = delta;
	}

	public D getPriority() {
		return priority;
	}

	public void setPriority(D priority) {
		this.priority = priority;
	}

	public HashMap<Integer, D> getOffset() {
		return offset;
	}

	public void setOffset(HashMap<Integer, D> offset) {
		this.offset = offset;
	}

	public K getKey() {
		return key;
	}

	public ArrayList<E> getLinks() {
		return links;
	}

	public int getLength() {
		return length;
	}

	public ArrayList<Integer> getMirrors() {
		return mirrors;
	}

	public int getMaster() {
		return master;
	}

}
