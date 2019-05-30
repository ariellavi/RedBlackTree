package Trees;
import java.util.*;



/**
 * @author ariellavi
 *
 * My own attempt at a Red-Black Tree, similar to the one used to implement TreeMap.
 * Implemented as a Binary Search Tree with doubly linked nodes (children and parent)
 * 
 * The tree has four main rules:
 * 	1) Every node is assigned a color RED or BLACK
 * 	2) The root of the (main) tree is always BLACK
 * 	3) There are no two adjacent RED nodes (i.e. a RED node cannot have a 
 * 	   parent or child that is RED)
 * 	4) For every node, each path from that node to a descendant (null) node 
 * 	   must have the same number of BLACK nodes	(null counts as BLACK)
 * 
 * See RBTree-info.txt for more info	
 *
 * @param <K>: map key used for comparison, must be comparable
 * @param <V>: value stored
 */
public class RBTree<K extends Comparable<K>, V>
	extends AbstractMap<K, V>{
	
	private static final boolean BLACK = false;
	private static final boolean RED = true;
	
	private class Node<K extends Comparable<K>, V> 
		implements Map.Entry<K, V> {
		
		//Node Data Fields
		K key;
		V value;
		int size = 1;	// size of subtree rooted at this node
		
		Node parent, left, right;
		
		boolean COLOR = RED;
		
		Node (K key, V value){
			this.key = key;
			this.value = value;
		}
		
		Node (K key, V value, boolean color) {
			this(key, value);
			this.COLOR = color;
		}
		
		// Public accessor methods
		public K getKey() { return key; }
		public V getValue() { return value; }
		public V setValue(V newValue) {
			V oldValue = value;
			value = newValue;
			return oldValue;
		}
		
	}
	
	Node theRoot, pointer;		// pointer is a "helper" reference used in rebalancing
	Node nil = new Node(null, null, BLACK);		// nil is a sentinel node used in place of 'null' in delete
	
	public int size () { 
	    if (theRoot == null)
	      return 0;
	    return theRoot.size;
	  }

	public boolean getColor (Node<K,V> node) {
		if (node == null)
			return BLACK;
		return node.COLOR;
	}
	
	public int compare(Node<K,V> one, Node<K,V> two) {
		return one.key.compareTo(two.key);
	}
	
	public boolean equals(Node<K,V> one, Node<K,V> two) {
		if (one == null | two == null)
			return false;
		if (one == nil | two == nil)
			return two == nil;
		return (compare(one, two) == 0);
	}
	
	static final boolean leftCheck = false;			// if leftCheck is true, checks blackHeight of
	public int blackHeight (Node<K,V> root) {		// the left subtree. If false, right subtree 
		
		if (root == null)
			return 0;
		if (root.size == 1)
			return 1;
		int height;
		if (leftCheck) {
			height = blackHeight(root.left);
			if (getColor(root.left) == BLACK)
				height++;
		}
		else {
			height = blackHeight(root.right);
			if (getColor(root.right) == BLACK)
				height++;
		}
			
		return height;
	}
	
	public boolean heightTest(Node<K,V> root) {
		
		if (root == null || root.size == 1)
			return true;
		
		if ( !heightTest(root.left) || !heightTest(root.right) )
			return false;
		
		// Get the blackHeight along the left path
		int left = blackHeight(root.left);
		if (getColor(root.left) == BLACK)
			left++;
		
		// Get the blackHeight along the right path
		int right = blackHeight(root.right);
		if (getColor(root.right) == BLACK)
			right++;
		
		
		return (left == right);
	}
	
	/**
	 * Find the node with the given key.
	 * @param key The key to be found.
	 * @return The node with that key.
	 */
	private Node<K, V> find (K key, Node<K,V> root) {
		  
		if (root == null)
			return null;
		  
		int cmp = key.compareTo(root.key);

		if (cmp == 0)
			return root;
		else if (cmp > 0)
			return find(key, root.right);
		else
			return find(key, root.left);
	}    

	/**
	 * Returns true if there is a Node in tree with key key,
	 * false otherwise
	 */
	public boolean containsKey (Object key) {
		return find((K) key, theRoot) != null;
	}
	
	public V get (Object key) {
	    Node<K, V> node = find((K) key, theRoot);
	    if (node != null)
	      return node.value;
	    return null;
	  }
	
	/**
	 * 	Puts a node with <key, value> into tree, if a node with the same key is not
	 * 	already in tree. If there already is a node with this key, change its value
	 * 	to value. Return the former value, or null if there was none
	 */
	public V put(K key, V value) {
		
		Node target = find(key, theRoot);
		if (target != null)
			return (V) target.setValue(value);
		
		theRoot = add(key, value, theRoot);
			
		addRebalance(pointer);

		return null;
	}
	
	
	private Node<K,V> add(K key, V value, Node<K,V> root) {
		
		if (root == null) {
			pointer = new Node(key, value, (theRoot != null) ); // New node is BLACK if theRoot is null, RED otherwise
			return pointer;
		}
		int cmp = key.compareTo(root.key);
		
		if (cmp < 0) {
			root.left = add(key, value, root.left);
			root.left.parent = root;
		}
		else {
			root.right = add(key, value, root.right);
			root.right.parent = root;
		}
		
		root.size++;
		
		return root;
	}
	
	
	public V remove(Object objKey) {
		
		K key = (K) objKey;
		Node target = find(key, theRoot);
		if (target == null) 
			return null;
			
		theRoot = remove(key, theRoot);
		if (deleteRebalance)
			deleteRebalance(pointer);
			
		deleteRebalance = false;
		return (V) target.getValue();
	}
	
	/**
	   * Remove the node with key from the tree with root.  Return the
	   * root of the resulting tree.
	   */
	private Node<K, V> remove(K key, Node<K,V> root) {
		
		if (root == null)
			return null;
		
		int cmp = key.compareTo(root.key);
		
		if (cmp == 0)
			return removeRoot(root);
		else if (cmp < 0)
			root.left = remove(key, root.left);
		else
			root.right = remove(key, root.right);
		
		root.size--;
		
		return root;
	}
	
	private boolean deleteRebalance = false;
	
	/**	 Remove root of this subtree, and return the new replacement root of the subtree. 
	 * pre: root is not null
	 * post: pointer is set to the new node at target's former position (pointer is set to child of target)
	 * @param root: root of subtree 
	 * @return root of new tree
	 */
	private Node<K, V> removeRoot(Node<K,V> root) {
		
		Node target = root;				// target is the Node being removed or reallocated
		
		if (root.left == null)				// if one or both of root's children are null,
			pointer = root.right;			// we do not need successor, so set pointer to child
		else if (root.right == null)		// of target, which will be the new root
			pointer = root.left;
		else										// If both children are non-null,
			target = getMinimum(root.right);		// getMinimum gets successor, and sets pointer
													// to its child
		
		deleteRebalance = !getColor(target);	// Set deleteRebalance to true if target == BLACK
		
		if (pointer == null)
			pointer = nil;
		pointer.parent = target.parent;
		
		if (pointer.parent == null)
			theRoot = pointer;
		else if (target != root) {		// If target is not root, it is the successor, copy fields
			target.right = removeMinimum(root.right);	
			if (target.right != null)
				target.right.parent = target;
			target.left = root.left;
			target.left.parent = target;
			target.COLOR = root.COLOR;
			return target;
		}
		else {
			if (compare(pointer.parent.left, target) == 0)
				pointer.parent.left = pointer;
			else
				pointer.parent.right = pointer;
		}
			
		
		return pointer;
	}
	/** @return the Node with minimum key in tree rooted at root
	 */
	private Node<K, V> getMinimum(Node<K,V> root) {
		
		if (root == null)
			return null;
		
		if (root.left == null) {
			pointer = root.right;
			return root;
		}
		else
			return getMinimum(root.left);
	}
	
	/** Remove the Node with minimum value from tree rooted at root
	 * pre: root is not null
	 * @return the root of new tree
	 */
	private Node<K, V> removeMinimum(Node<K,V> root) {
		
		if (root.left == null) {
			return root.right;
		}
		
		root.left = removeMinimum(root.left);
		root.size--;
			
		return root;
	}

	/** Rebalances the tree starting at node ptr, which was just added. 
	 * 	pre: 1) ptr has color RED, and that this node was just added
	 * 		 2) Any imbalance in the tree is due to only this node
	 * @param ptr: a reference to the Node that was just added to tree
	 */
	private void addRebalance(Node<K,V> ptr) {
		
		while (compare(ptr,theRoot) != 0 && ptr.parent.COLOR == RED) {
			
			// Get grandpa, who must exist, since parent is RED (can't be root). 
		    // Also, grandpa must be therefore be BLACK
			Node gramps = ptr.parent.parent;
			
			// 2 main cases: 1) parent is gramp's left, 2) parent is gramp's right
			// Each case has 3 subcases
			if (ptr.parent == gramps.left) {				// C A S E  1
				Node uncle = gramps.right;
			
				// SUBCASE 1: If uncle is RED, we can recolor 
				if (getColor(uncle) == RED) {	
					
					ptr.parent.COLOR = BLACK;
					uncle.COLOR = BLACK;
					gramps.COLOR = RED;
					theRoot.COLOR = BLACK;
					
					// Reiterate on gramps
					ptr = gramps;
					continue;
				}
				// SUBCASE 2: uncle is BLACK or null. If ptr is the inner (right) child of parent,
				if (ptr == ptr.parent.right) 
					ptr = leftRotate(ptr.parent);
				
				// SUBCASE 3: uncle is BLACK or null, ptr is outer (left) child of parent
				gramps = rightRotate(gramps);
				gramps.COLOR = RED;
				gramps.parent.COLOR = BLACK;
			}
			
			else {											// C A S E  2
				Node uncle = gramps.left;
				
				// SUBCASE 1: uncle is RED, recolor
				if (getColor(uncle) == RED) {
					gramps.COLOR = RED;
					ptr.parent.COLOR = uncle.COLOR = BLACK;
					theRoot.COLOR = BLACK;
					ptr = gramps;
					continue;
				}
				// SUBCASE 2: uncle is BLACK or null, ptr is inner (left) child of parent
				if (ptr == ptr.parent.left)
					ptr = rightRotate(ptr.parent);
				
				// SUBCASE 3: uncle is BLACK or null, ptr is outer (right) child of parent
				gramps = leftRotate(gramps);
				gramps.COLOR = RED;
				gramps.parent.COLOR = BLACK;
			}
			theRoot.COLOR = BLACK;
		}
	}
	
	private void deleteRebalance(Node<K,V> ptr) {
		
		if (ptr == null)
			System.out.println("Error: cannot perform deleteRebalance on a null pointer, wtf");
		
		// Two main cases: 1) ptr is the left child	  2) ptr is the right child
		// Each case has 4 subcases
		while (ptr != theRoot && getColor(ptr) == BLACK) {
			
			Node parent = ptr.parent;
			
			if (ptr.parent.left == ptr) {							// C A S E  1
				
				Node sibling = parent.right;
				if (ptr == nil)
					parent.left = null;
				
				// SUBCASE 1: sibling is RED, so parent must be BLACK 
				// rotate parent left and change sibling (new parent) color to BLACK
				// change parent color to RED to keep RB-balance in the rest of tree
				// Subcase 1 turns into Subcase 2, 3, or 4
				if (getColor(sibling) == RED) {
					sibling.COLOR = BLACK;
					parent.COLOR = RED;
					parent = leftRotate(parent);
					sibling = parent.right;
				}
				
				// SUBCASE 2: sibling is BLACK, both its children are BLACK
				// Sibling can be changed to black, reiterate on parent as new ptr
				if (getColor(sibling.left) == BLACK && getColor(sibling.right) == BLACK) {
					sibling.COLOR = RED;
					ptr = parent;
					continue;
				}
				
				// SUBCASE 3: sibling is BLACK, sibling.right is BLACK, sibling.left is RED
				// Subcase 3 turns into subcase 4
				if (getColor(sibling.right) == BLACK) {
					sibling.COLOR = RED;
					sibling.left.COLOR = BLACK;
					rightRotate(sibling);
					sibling = parent.right;
				}
				
				// SUBCASE 4: sibling is BLACK, sibling.right is RED
				// 
				sibling.right.COLOR = BLACK;
				sibling.COLOR = parent.COLOR;
				parent.COLOR = BLACK;
				
				leftRotate(parent);
				break;
				
			}
			else {													// C A S E  2
				
				Node sibling = parent.left;
				if (ptr == nil)
					parent.right = null;
				
				// SUBCASE 1
				if (getColor(sibling) == RED) {
					sibling.COLOR = BLACK;
					parent.COLOR = RED;
					parent = rightRotate(parent);
					sibling = parent.left;
				}
				
				// SUBCASE 2
				if (getColor(sibling.left) == BLACK && getColor(sibling.right) == BLACK) {
					sibling.COLOR = RED;
					ptr = parent;
					continue;
				}
				
				// SUBCASE 3
				if (getColor(sibling.left) == BLACK) {
					sibling.COLOR = RED;
					sibling.right.COLOR = BLACK;
					leftRotate(sibling);
					sibling = parent.left;
				}
				
				// SUBCASE 4
				sibling.left.COLOR = BLACK;
				sibling.COLOR = parent.COLOR;
				parent.COLOR = BLACK;
				
				rightRotate(parent);
				break;
			}
		}
		
		if (ptr != null)
			ptr.COLOR = BLACK;
		if (ptr == nil)
			ptr.parent = null;
		
		deleteRebalance = false;
	}
	
	
	/**  Rotates the root left, putting root.right in its place as newRoot. 
	 * root becomes newRoot's left child, and root's new right child is newRoot's 
	 * former left child
	 * 
	 * @pre: root != null, root.right != null
	 * @param root: node that will be rotated left and become left subroot
	 * @return the previous root of this configuration (root)
	 */
	private Node<K,V> leftRotate(Node<K,V> root) {
		
		if (root == null)
			System.out.println("ERROR: Cannot left-rotate on a null node");
		if (root.right == null)
			System.out.println("ERROR: Cannot replace root with a null root.right ");
		
		// Get the new root, which is root's right child
		Node newRoot = root.right;
		
		// root takes newRoot's left child, which becomes root's right child
		root.right = newRoot.left;
		if (root.right != null)
			root.right.parent = root;
		
		// root becomes the left child of newRoot
		newRoot.left = root;
		
		// Set parents: newRoot gets root's parent, and becomes root's new parent
		newRoot.parent = root.parent;
		root.parent = newRoot;
		
		//  If parent is null, root was theRoot, so set theRoot to newRoot.
		// 	Otherwise, set parent's appropriate child reference to newRoot
		//  instead of root
		if (newRoot.parent == null)					// root was theRoot
			theRoot = newRoot;
		else if (newRoot.parent.left == root)		// root was parent's left child
			newRoot.parent.left = newRoot;
		else										// root was parent's right child
			newRoot.parent.right = newRoot;
		
		// Adjust root.size, and newRoot.size
		root.size = 1;
		if (root.left != null)
			root.size += root.left.size;
		if (root.right != null)
			root.size += root.right.size;
		
		newRoot.size = 1 + root.size;
		if (newRoot.right != null)
			newRoot.size += newRoot.right.size;
		
		return root;
	}
	
	/**  Rotates the root right, putting root.left in its place as newRoot. 
	 * root becomes newRoot's right child, and root's new left child is newRoot's 
	 * former right child
	 * 
	 * @pre: root != null, root.left != null
	 * @param root: node that will be rotated right and become right subRoot
	 * @return the previous root of this configuration
	 */
	private Node<K,V> rightRotate(Node<K,V> root) {
		
		if (root == null)
			System.out.println("ERROR: Cannot right-rotate on a null node");
		if (root.left == null)
			System.out.println("ERROR: Cannot replace root with a null root.left ");
		
		// Get the new root, which is root's left child
		Node newRoot = root.left;
		
		// root takes newRoot's right child, which becomes root's left child
		root.left = newRoot.right;
		if (root.left != null)
			root.left.parent = root;
		
		// root becomes the right child of newRoot
		newRoot.right = root;
		
		// Set parents: newRoot gets root's parent, and becomes root's new parent
		newRoot.parent = root.parent;
		root.parent = newRoot;
		
		//  If parent is null, root was theRoot, so set theRoot to newRoot.
		// 	Otherwise, set parent's appropriate child reference to newRoot
		//  instead of root
		if (newRoot.parent == null)					// root was theRoot
			theRoot = newRoot;
		else if (newRoot.parent.left == root)		// root was parent's left child
			newRoot.parent.left = newRoot;
		else										// root was parent's right child
			newRoot.parent.right = newRoot;
		
		// Adjust root.size, and newRoot.size
		root.size = 1;
		if (root.left != null)
			root.size += root.left.size;
		if (root.right != null)
			root.size += root.right.size;
				
		newRoot.size = 1 + root.size;
		if (newRoot.left != null)
			newRoot.size += newRoot.left.size;
				
		return root;
	}
	
	
	@Override
	public Set<Entry<K, V>> entrySet() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	public String toString( ) {
		return toString(theRoot, 0);
	}
	
	private String toString (Node root, int indent) {
	    if (root == null)
	      return "";
	    String ret = toString(root.right, indent + 2);
	    for (int i = 0; i < indent; i++)
	      ret = ret + "  ";
	    ret = ret + root.key + ":" + blackHeight(root)  +  ":" + root.value  ;
	    if (root.COLOR == BLACK)
	    	ret = ret + ":" + "BLK" + "\n";
	    else
	    	ret = ret + ":" + "RED" + "\n";
	    ret = ret + toString(root.left, indent + 2);
	    return ret;
	}
	
	public static void main(String[] args) {
		long start = System.nanoTime();
		  
	    RBTree<Character, Integer> tree = new RBTree<Character, Integer>();
	    String s = "abcdefghijklmnopqrstuvwxyz";
	    String p = "balanced";
	    s = s + s + s;
	    for (int i = 0; i < p.length(); i++) {
	      tree.put(p.charAt(i), i);
	      System.out.print(tree);
	      System.out.println("---------------");
	      System.out.println("blackHeight: " + tree.blackHeight(tree.theRoot));
	      System.out.println("Passed BLHeight Test: " + tree.heightTest(tree.theRoot));
	      System.out.println("-------------------------" + "\n" + "-------------------------");
	    }

	    System.out.println(tree.heightTest(tree.theRoot));
	    
	    
	    for (int i = 0; i < p.length(); i++) {
	      tree.remove(p.charAt(i));
	      System.out.print(tree);
	      System.out.println("blackHeight: " + tree.blackHeight(tree.theRoot));
	      System.out.println("Passed BLHeight Test: " + tree.heightTest(tree.theRoot));
	      System.out.println("-------------");
	    } 
	    
	    long end = System.nanoTime();
	    
	    System.out.println("Time in seconds: " + (end-start)/Math.pow(10, 9));
	}

}
