package it.unibo.oop.workers02;

import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Multi threaded matrix sum.
 */
public class MultiThreadedSumMatrix implements SumMatrix {

    private final int nThreads;

    /**
     * Create a new matrix sum object with multi threading.
     * 
     * @param threads
     *                  number of threads used.
     */
    public MultiThreadedSumMatrix(final int threads) {
        nThreads = threads;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double sum(final double[][] matrix) {
        final int rowsPerThread = matrix.length / nThreads + matrix.length % nThreads;
        return IntStream
                .iterate(0, start -> start + rowsPerThread)
                .limit(nThreads)
                .mapToObj(start -> new Worker(matrix, start, rowsPerThread))
                .peek(Thread::start)
                .peek(MultiThreadedSumMatrix::joinUninterruptibly)
                .mapToDouble(Worker::getResult)
                .sum();
    }

    @SuppressWarnings("PMD.AvoidPrintStackTrace")
    private static void joinUninterruptibly(final Thread target) {
        var joined = false;
        while (!joined) {
            try {
                target.join();
                joined = true;
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static class Worker extends Thread {
        private final double[][] matrix;
        private final int startpos;
        private final int nelem;
        private double res;

        Worker(final double[][] list, final int startpos, final int nelem) {
            super();
            this.matrix = list; // NOPMD: This is a private sub-class
            this.startpos = startpos;
            this.nelem = nelem;
        }

        @Override
        @SuppressWarnings("PMD.SystemPrintln")
        public synchronized void run() {
            System.out.println("Working from position " + startpos + " to position " + (startpos + nelem - 1));
            this.res = Stream.of(this.matrix)
                .skip(startpos)
                .limit(nelem)
                .flatMapToDouble(DoubleStream::of)
                .sum();
        }

        public synchronized double getResult() {
            return this.res;
        }
    }
}
