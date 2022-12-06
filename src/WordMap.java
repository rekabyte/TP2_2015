import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("unchecked")
public class WordMap<K,V> implements Map<K,V> {
	public static class MapEntry<K,V> implements Map.Entry<K, V> {
		private K k;
		private V v;
		public MapEntry(K key, V value) {
			this.k = key;
			this.v = value;
		}
		public K getKey() { return this.k;}
		public V getValue() { return this.v;}
		public void setKey(K key) { this.k = key;}
		public V setValue(V value) {
			V retV = this.v;
			this.v = value;
			return retV;
		}
		//Debug function
		public String toString() {
			if(k != null) return "<"+k+","+v+">";
			else return "";
		}

	}

	//attributs:
	protected int n = 0; 			//nbre d'entrees dans la map
	protected int capacity;			//taille du tableau
	protected int prime;			//facteur prime

	private MapEntry<K,V>[] table;	//on utilise un tableau fixe de type MapEntry, on l'init dans la fct createTable()
	private MapEntry<K,V> DEFUNCT = new MapEntry<K,V>(null, null);	//Entree speciale qui indique qu'une case est vide

	//Constructeur qui initialise la map avec la capacite specifiee
	public WordMap(int cap) {
		this.prime = 41;
		this.capacity = cap;
		this.createTable();
	}

	//Fonction qui initialise le tableau qui contiendra nos MapEntry<K,V> avec l'attribut capacity
	protected void createTable() {
		table = (MapEntry<K,V>[]) new MapEntry[this.capacity];
	}

	@Override
	public int size() { return n;}
	@Override
	public boolean isEmpty() { 
		return n ==0;
	}

	@Override
	public boolean containsKey(Object key) { 
		return bucketGet(hashValue((K) key), (K) key) != null;
	}
	@Override
	public boolean containsValue(Object value) { 
		List<V> values = (ArrayList<V>) values();
		for(V v : values) 
			if(v.equals(value)) return true;
		return false;
	}
	@Override
	public V get(Object key) { 
		return bucketGet(hashValue((K) key), (K) key);
	}
	@Override
	public V put(K key, V value) {
		V retV = bucketPut(hashValue(key), key, value);
		if(n > capacity * 0.75)					//On laisse le facteur de charge en dessous de 75%
			resize(2 * capacity + 1);			//Si le facteur depasse 75% on redimensionne le tableau avec la capacite = capPrecedente * 2 + 1
		return retV;
	}
	@Override
	public V remove(Object key) { 
		return bucketRemove(hashValue((K) key), (K) key);
	}
	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		Iterator<? extends Entry<? extends K, ? extends V>> iterator =  m.entrySet().iterator();
		while(iterator.hasNext()) {
			Map.Entry<K, V> entry = (Entry<K, V>) iterator.next();
			put(entry.getKey(), entry.getValue());
		}
	}
	@Override
	public void clear() {
		for(K key : keySet()) this.bucketRemove(hashValue(key), key);
		if(!isEmpty()) System.out.println("The hashmap isnt empty");
	}
	@Override
	public Set<K> keySet() {
		Set<K> buffer = new HashSet<>();
		for(Entry<K, V> entry : entrySet()) buffer.add(entry.getKey());
		return buffer;
	}
	@Override
	public Collection<V> values() {
		List<V> buffer = new ArrayList<>();
		for(Entry<K, V> entry : entrySet()) buffer.add(entry.getValue());
		return buffer;

	}
	@Override				
	public Set<Map.Entry<K,V>> entrySet() {
		Set<Map.Entry<K, V>> buffer = new HashSet<>();
		for(int h = 0; h < capacity ; h++)
			if(!isAvailable(h)) buffer.add(table[h]);
		return buffer;
	}

	//Debug function
	public String toString() {
		String res = "[";
		for(int h = 0 ; h < capacity ; h++) 
			if(!isAvailable(h)) res += table[h].toString() + ", ";
		return res + "]";
	}

	//=====================FONCTIONS RAJOUTÉES (Pour gerer le sondage lineaire)=========================//
	private int hashValue(K key) {
		if(key instanceof String)  {						//Si c'est un String, on fait un hash a decalage cyclique
			int h = 0;
			for(int i = 0 ; i < ((String) key).length(); i++) {
				h = (h << 5) | (h >>> 27);
				h += (int)(((String) key).charAt(i));
			}
			return Math.abs(h % capacity);							//On compresse le hash et on le return
		} else {											//Si ce n'est pas un String, on utilise la fct hashCode par defaut de Java
			return key.hashCode() % capacity;
		}
	}

	private void resize(int newCap) {
		List<Map.Entry<K,V>> buffer = new ArrayList<>();
		for(Map.Entry<K, V> e : this.entrySet())
			buffer.add(e);
		this.capacity = newCap;
		this.createTable();									//Basee sur la nouvelle capacité;
		this.n = 0;
		for(Entry<K,V> e : buffer)
			put(e.getKey(), e.getValue());
	}

	//methodes en rapport avec buckets:
	private boolean isAvailable(int j) { return (table[j] == null || table[j] == DEFUNCT);}

	//cherche une entree avec une cle K dans le slot H, si on le trouve pas dans H, on continue le sondage
	//jusqu'a ce qu'on trouve k, ou jusqu'a ce qu'on tombe un SLOT null
	//retourne l'index du slot(dans table[]) qui contient le cle K, sinon retorun -(a+1)
	protected int findSlot(int h, K k) {
		int avail = -1;			//pas de slot disponible jusqu'a present
		int j = h;
		do {
			if(isAvailable(j)) {	//j est soit vide soit DEFUNCT
				if(avail == -1) avail = j;
				if(table[j] == null) break;	//si vide, on annule la recherche
			} else if (table[j].getKey().equals(k))
				return j;			//la recherche a echoue
			j = (j+1) % this.capacity;
		} while (j != h);			//On stop si on revient a la case depart
		return -(avail + 1);		//La recherche a echoue
	}

	//retourne la valeur V de la cle k et int h passe en parametre, si aucune cle K existe dans la table: retourne null;
	protected V bucketGet(int h, K k) {
		int j = findSlot(h, k);
		if(j < 0) return null;		//Pas de match trouve
		return table[j].getValue();
	}

	//Modifie la valeur d'une clee si elle existe deja, sinon rajoute une nouvelle entree MapEntry(cle,valeur)
	protected V bucketPut(int h, K k, V v) {
		int j = findSlot(h, k);
		if(j >= 0)					//Si cette cle a deja une entree
			return table[j].setValue(v);
		table[-(j+1)] = new MapEntry<>(k,v);	//Convertir en index
		this.n++;
		return null;
	}

	//Supprime l'entree de cle K si elle existe, sinon ne fait rien
	protected V bucketRemove(int h, K k) {
		int j = findSlot(h, k);
		if(j < 0) return null;					//Rien a supprimer, puisque la cle K n'existe pas
		V retV = table[j].getValue();
		table[j] = DEFUNCT;
		this.n--;
		return retV;
	}

}


















