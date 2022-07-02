package main;


public class Entrypoint {

	public static void main(String[] args) {
		PriorityQueue<Integer> priorityQueue=new PriorityQueue<Integer>(5);
		priorityQueue.add(1);
		
		priorityQueue.add(2);
		priorityQueue.add(2);
		
		priorityQueue.add(4);
		priorityQueue.add(5);
		
		System.out.println("Is the Priority Queue min sorted? -->"+priorityQueue.isMinHeap(0));
		
		

	}

}
