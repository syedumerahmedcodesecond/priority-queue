package main;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class PriorityQueue<T extends Comparable<T>> {

	private static final int INDEX_OF_ROOT = 0;

	private static final int SIZE_OF_EMPTY_HEAP = 0;

	// The number of elements currently inside the heap
	private int heapSize = 0;

	// The internal capacity of the heap
	private int heapCapacity = 0;

	// A dynamic list to track the elements inside the heap
	private List<T> heap = null;

	/*
	 * This map keeps track of the possible indices a particular node value is found
	 * in the heap. Having this mapping lets us have O(log(n)) removals and O(1)
	 * element containment check at the cost of some additional space and minor
	 * overhead
	 */

	// value of node|Indices tracked inside a TreeSet via Integer
	private Map<T, TreeSet<Integer>> nodeValueMapWithPossibleIndices = new HashMap<T, TreeSet<Integer>>();

	// Construct an initially empty priority queue
	public PriorityQueue() {
		this(1);
	}

	// Construct a priority queue with an initial capacity
	public PriorityQueue(int initialCapacity) {
		heap = new ArrayList<T>(initialCapacity);
	}

	// Construct a priority queue using heapify in O(n) time.
	// Please see: //
	// http://www.cs.umd.edu/~meesh/351/mount/lectures/lect14-heapsort-analysis-part.pdf
	public PriorityQueue(T[] elements) {
		heapSize = heapCapacity = elements.length;
		heap = new ArrayList<T>(heapSize);

		// Place all elements in heap
		for (int index = SIZE_OF_EMPTY_HEAP; index < heapSize; index++) {
			mapAdd(elements[index], index);
			heap.add(elements[index]);
		}

		// Heapify process
		for (int index = Math.max(SIZE_OF_EMPTY_HEAP, (heapSize / 2) - 1); index >= SIZE_OF_EMPTY_HEAP; index--) {
			sink(index);
		}
	}

	// Priority queue construction, O(nlog(n))
	public PriorityQueue(Collection<T> elements) {
		this(elements.size());
		for (T element : elements) {
			add(element);
		}
	}

	// Returns true/false depending on if the priority queue is empty
	public boolean isEmpty() {
		return size() == SIZE_OF_EMPTY_HEAP;
	}

	// Clears everything inside the heap, O(n)
	public void clear() {
		heap.clear();
		nodeValueMapWithPossibleIndices.clear();
	}

	public T peek() {
		if (isEmpty()) {
			return null;
		} else {
			return heap.get(INDEX_OF_ROOT);
		}
	}

	// Removes the root of the heap, O(log(n))
	public T poll() {
		return removeAt(INDEX_OF_ROOT);
	}

	// Test if an element is in heap, O(1)
	public boolean contains(T element) {
		if (isElementNull(element)) {
			return false;
		}
		// Map lookup to check containment, O(1)
		return nodeValueMapWithPossibleIndices.containsKey(element);

		// Linear scan to check containment, O(n)
		// for(int i = 0; i < heapSize; i++)
		// if (heap.get(i).equals(elem))
		// return true;
		// return false;
	}

	// Removes a particular element in the heap, O(log(n))
	public boolean remove(T element) {
		if (isElementNull(element)) {
			return false;
		}
		// Linear removal via search, O(n)
		// for (int i = 0; i < heapSize; i++) {
		// if (element.equals(heap.get(i))) {
		// removeAt(i);
		// return true;
		// }
		// }
		Integer index = mapGet(element);
		if (isIndexValid(index)) {
			removeAt(index);
		}
		return isIndexValid(index);
	}

	// Recursively checks if this heap is a min heap
	// This method is just for testing purposes to make
	// sure the heap invariant is still being maintained
	// Called this method with index=0 to start at the root
	public boolean isMinHeap(int indexToCheck) {
		// If we are outside the bounds of the heap return true
		int heapsize = size();
		if (isIndexGreaterThanHeapSize(indexToCheck, heapsize)) {
			return true;
		}
		int left = calculateLeftNode(indexToCheck);
		int right = calculateRightNode(indexToCheck);

		// Make sure that the current node k is less than
		// both of its children left, and right if they exist
		// return false otherwise to indicate an invalid heap
		if (isIndexLessThanHeapSize(left, heapsize) && !lessValueOf(indexToCheck, left)) {
			return false;
		}
		if (isIndexLessThanHeapSize(right, heapsize) && !lessValueOf(indexToCheck, right)) {
			return false;
		}

		// Recurse on both children to make sure they're also valid heaps
		return isMinHeap(left) && isMinHeap(right);
	}

	

	// Adds an element to the priority queue, the
	// element must not be null, O(log(n))
	public void add(T element) {
		if (isElementNull(element)) {
			throw new IllegalArgumentException();
		}

		heap.add(element);
		int indexOfLastElement = calculateIndexOfLastElement();
		mapAdd(element, indexOfLastElement);

		swim(indexOfLastElement);

	}

	// Return the size of the heap
	public int size() {
		return heap.size();
	}

	private int calculateIndexOfLastElement() {
		return size() - 1;
	}

	private boolean isElementNull(T element) {
		return element == null;
	}
	
	private boolean isIndexLessThanHeapSize(int left, int heapsize) {
		return left < heapsize;
	}

	private boolean isIndexGreaterThanHeapSize(int indexToCheck, int heapsize) {
		return indexToCheck >= heapsize;
	}

	// Add a node value and its index to the map
	private void mapAdd(T value, int index) {
		TreeSet<Integer> setOfIndices = getListOfAlreadyExistingIndicesForValue(value);

		if (isListOfIndicesEmpty(setOfIndices)) {
			addValueToMap(value, index);
		} else {
			addIndexToListOfIndicesForAValue(index, setOfIndices);
		}
	}

	private void mapRemove(T value, int index) {
		TreeSet<Integer> setOfIndices = getListOfAlreadyExistingIndicesForValue(value);
		setOfIndices.remove(index);
		final int zeroReferenceIndices = 0;
		if (setOfIndices.size() == zeroReferenceIndices) {
			nodeValueMapWithPossibleIndices.remove(value);
		}
	}

	// Extract an index position for the given value
	// NOTE: If a value exists multiple times in the heap the highest
	// index is returned (this has arbitrarily been chosen)
	private Integer mapGet(T value) {
		TreeSet<Integer> set = nodeValueMapWithPossibleIndices.get(value);
		if (set != null) {
			return set.last();
		}
		return null;
	}

	// Removes a node at particular index, O(log(n))
	private T removeAt(int index) {
		if (isEmpty()) {
			return null;
		}
		int indexOfLastElement = calculateIndexOfLastElement();
		T dataToRemove = fetchElementForIndex(index);
		swap(index, indexOfLastElement);

		destroyLastNodeElement(indexOfLastElement, dataToRemove);

		if (index == indexOfLastElement) {
			return dataToRemove;
		}

		T element = fetchElementForIndex(index);

		// Try sinking element
		sink(index);

		// If sinking did not work try swimming
		if (fetchElementForIndex(index).equals(element)) {
			swim(index);
		}

		return dataToRemove;

	}

	private T fetchElementForIndex(int index) {
		return heap.get(index);
	}

	private void destroyLastNodeElement(int indexOfLastElement, T dataToRemove) {
		heap.remove(indexOfLastElement);
		mapRemove(dataToRemove, indexOfLastElement);
	}

	private void addValueToMap(T value, int index) {
		TreeSet<Integer> setOfIndices;
		setOfIndices = new TreeSet<Integer>();
		addIndexToListOfIndicesForAValue(index, setOfIndices);
		nodeValueMapWithPossibleIndices.put(value, setOfIndices);
	}

	private void addIndexToListOfIndicesForAValue(int index, TreeSet<Integer> setOfIndices) {
		setOfIndices.add(index);
	}

	private boolean isListOfIndicesEmpty(TreeSet<Integer> setOfIndices) {
		return setOfIndices == null;
	}

	private boolean isIndexValid(Integer index) {
		return index != null;
	}

	private TreeSet<Integer> getListOfAlreadyExistingIndicesForValue(T value) {
		TreeSet<Integer> setOfIndices = nodeValueMapWithPossibleIndices.get(value);
		return setOfIndices;
	}

	private void mapSwap(T firstValue, T secondValue, int indexOfFirstValue, int indexOfSecondValue) {
		Set<Integer> firstSet = nodeValueMapWithPossibleIndices.get(firstValue);
		Set<Integer> secondSet = nodeValueMapWithPossibleIndices.get(secondValue);

		firstSet.remove(indexOfFirstValue);
		secondSet.remove(indexOfSecondValue);

		firstSet.add(indexOfSecondValue);
		secondSet.add(indexOfFirstValue);
	}

	// Swap two nodes. O(1)
	private void swap(int firstIndex, int secondIndex) {
		T firstElement = fetchElementForIndex(firstIndex);
		T secondElement = fetchElementForIndex(secondIndex);

		heap.set(firstIndex, secondElement);
		heap.set(secondIndex, firstElement);

		mapSwap(firstElement, secondElement, firstIndex, secondIndex);
	}

	private boolean lessValueOf(int firstIndex, int secondIndex) {
		T firstElement = fetchElementForIndex(firstIndex);
		T secondElement = fetchElementForIndex(secondIndex);

		return firstElement.compareTo(secondElement) <= 0;
	}

	// Perform bottom up node swim, O(log(n))
	private void swim(int indexOfNode) {

		// Grab the index of next parent node with respect to k
		int parent = getNextParentWithRespectToNodeIndex(indexOfNode);

		// Keep swimming till we have not reached the root
		// and we are less than our parent
		while (indexOfNode > INDEX_OF_ROOT && lessValueOf(indexOfNode, parent)) {
			swap(parent, indexOfNode);
			indexOfNode = parent;
			parent = getNextParentWithRespectToNodeIndex(indexOfNode);
		}
	}

	// Top down node sink, O(log(n))
	private void sink(int indexOfNode) {
		int heapsize = size();

		while (true) {
			int left = calculateLeftNode(indexOfNode); // Left node
			int right = calculateRightNode(indexOfNode); // Right node
			int smallest = left; // Assume left is the smallest node of the two children

			// Find which is smaller left or right
			// if right is smaller set smallest to the right
			if (isIndexLessThanHeapSize(right, heapsize) && lessValueOf(right, left)) {
				smallest = right;
			}

			// stop if we are outside the bounds of the tree
			// or stop early if we cannot sink indexOfNode anymore
			if (isIndexGreaterThanHeapSize(left, heapsize) && lessValueOf(indexOfNode, smallest)) {
				break;
			}
			// Move down the tree following the smallest node
			swap(smallest, indexOfNode);
			indexOfNode = smallest;
		}
	}

	private int getNextParentWithRespectToNodeIndex(int nodeIndex) {
		return (nodeIndex - 1) / 2;
	}

	private int calculateRightNode(int indexOfNode) {
		return 2 * indexOfNode + 2;
	}

	private int calculateLeftNode(int indexOfNode) {
		return 2 * indexOfNode + 1;
	}

	@Override
	public String toString() {
		return heap.toString();
	}

}
