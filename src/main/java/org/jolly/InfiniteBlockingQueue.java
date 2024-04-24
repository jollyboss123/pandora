package org.jolly;

import java.util.Spliterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class InfiniteBlockingQueue<T> {
    private static final Object POISON_PILL = new Object();

    private final BlockingQueue<T> queue;

    public InfiniteBlockingQueue() {
        this.queue = new LinkedBlockingQueue<>();
    }

    public InfiniteBlockingQueue(int capacity) {
        this.queue = new ArrayBlockingQueue<>(capacity);
    }

    public void put(T item) throws InterruptedException {
        queue.put(item);
    }

    public T take() throws InterruptedException {
        T item = queue.take();
        if (item == POISON_PILL) {
            return null;
        }
        return item;
    }

    public void close() throws InterruptedException {
        ((BlockingQueue<Object>) queue).put(POISON_PILL);
    }

    public Stream<T> stream() {
        return StreamSupport.stream(new QueueSpliterator<>(this), false);
    }

    private static class QueueSpliterator<T> implements Spliterator<T> {
        private final InfiniteBlockingQueue<T> queue;

        private QueueSpliterator(InfiniteBlockingQueue<T> queue) {
            this.queue = queue;
        }

        @Override
        public boolean tryAdvance(Consumer<? super T> action) {
            final T next;
            try {
                next = this.queue.take();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }

            if (next == null) {
                return false;
            }

            action.accept(next);
            return true;
        }

        @Override
        public Spliterator<T> trySplit() {
            return null;
        }

        @Override
        public long estimateSize() {
            return Long.MAX_VALUE;
        }

        @Override
        public int characteristics() {
            return Spliterator.CONCURRENT | Spliterator.NONNULL | Spliterator.ORDERED;
        }
    }
}
