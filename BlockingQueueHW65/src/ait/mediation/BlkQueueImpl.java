package ait.mediation;

import java.util.LinkedList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BlkQueueImpl<T> implements BlkQueue<T> {
    private LinkedList<T> linkedList ;
    private int maxSize;

    Lock mutex = new ReentrantLock();
    Condition consumerWaitingCondition = mutex.newCondition();
    Condition producerWaitingCondition = mutex.newCondition();

    public BlkQueueImpl( int maxSize) {
        this.linkedList = new LinkedList<>();
        this.maxSize = maxSize;
    }

    @Override
    public void push(T message) {
        mutex.lock(); // Захват мьютекса
        try {
            while (linkedList.size() >= maxSize) {
                try {
                    consumerWaitingCondition.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            linkedList.add(message);
            producerWaitingCondition.signal();
        } finally {
            mutex.unlock(); // Освобождение мьютекса
        }
    }

    @Override
    public T pop() {
        mutex.lock(); // Захват мьютекса
        try {
            while (linkedList.isEmpty()) {
                try {
                    producerWaitingCondition.await(); // Ожидание, пока сообщение не будет отправлено
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            T res = linkedList.poll();
            consumerWaitingCondition.signal(); // Уведомление отправителя о получении сообщения
            return res; // Возврат полученного сообщения
        } finally {
            mutex.unlock(); // Освобождение мьютекса
        }
    }

}
