package br.unb.cic.tdp.proof.util;

public class Stack {
    int maxSize;
    int[] content;
    int size = 0;

    public Stack(int size) {
        this.maxSize = size;
        this.content = new int[size * 3];
    }

   public void push(final int a, final int b, final int c) {
        this.content[(size * 3)] = a;
        this.content[(size * 3) + 1] = b;
        this.content[(size * 3) + 2] = c;
        size++;
   }

    public void pop() {
        size--;
    }

    public ListOfCycles toListOfCycles() {
        final ListOfCycles list = new ListOfCycles(size);
        for (int i = 0; i < size; i++) {
            list.add(new int[] {content[(i * 3)], content[(i * 3) + 1], content[(i * 3) + 2]});
        }
        return list;
    }

    public int size() {
        return size;
    }

    public Stack clone() {
        final var clone = new Stack(this.size);
        clone.maxSize = maxSize;
        clone.content = content.clone();
        clone.size = size;
        return clone;
    }
}
