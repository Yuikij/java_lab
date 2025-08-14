package org.kubo.collections;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class Test {
    public static void main(String[] args) {


        List<Integer> arrayList = new ArrayList<>();
        List<Integer> linkedList = new ArrayList<>();

        for (int i = 0; i < 100; i++) {
            arrayList.add(i);
            linkedList.add(i);
        }
        arrayList.set(2, 1);
        arrayList.add(0, 999);
        linkedList.set(0, 999);
        System.out.println("ArrayList: " + arrayList);
        System.out.println("LinkedList: " + linkedList);
    }
}
