package ait.mediation;

import java.util.LinkedList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BlkQueueImpl<T> implements BlkQueue<T> {
    private LinkedList<T> linkedList;
    private int maxSize;

    Lock mutex = new ReentrantLock(); // Создание объекта блокировки для синхронизации
    Condition consumerWaitingCondition = mutex.newCondition(); // Создание условия ожидания для потребителя
    Condition producerWaitingCondition = mutex.newCondition(); // Создание условия ожидания для производителя

    public BlkQueueImpl(int maxSize) {
        this.linkedList = new LinkedList<>(); // Инициализация связанного списка
        this.maxSize = maxSize; // Установка максимального размера очереди
    }

    @Override
    public void push(T message) {
        mutex.lock(); // Захват мьютекса
        try {
            while (linkedList.size() >= maxSize) { // Проверка, заполнена ли очередь
                try {
                    consumerWaitingCondition.await(); // Если очередь заполнена, ожидание сигнала от потребителя
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            linkedList.add(message); // Добавление элемента в очередь
            producerWaitingCondition.signal(); // Сигнализация потребителю о наличии данных в очереди
        } finally {
            mutex.unlock(); // Освобождение мьютекса
        }
    }

    @Override
    public T pop() {
        mutex.lock(); // Захват мьютекса
        try {
            while (linkedList.isEmpty()) { // Проверка, пуста ли очередь
                try {
                    producerWaitingCondition.await(); // Если очередь пуста, ожидание сигнала от производителя
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            T res = linkedList.poll(); // Извлечение элемента из очереди
            consumerWaitingCondition.signal(); // Сигнализация производителю о возможности добавления новых данных
            return res; // Возвращение извлеченного элемента
        } finally {
            mutex.unlock(); // Освобождение мьютекса
        }
    }
}