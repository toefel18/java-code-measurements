/*
 *
 *     Copyright 2016 Christophe Hesters
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package nl.toefel.java.code.measurements.referenceimpl;

import nl.toefel.java.code.measurements.api.Statistic;
import nl.toefel.java.code.measurements.api.Statistics;
import nl.toefel.java.code.measurements.api.Stopwatch;

import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class SynchronizedStatistics implements Statistics {
    private final Statistics statistics;

    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();

    public SynchronizedStatistics(Statistics statistics) {
        this.statistics = statistics;
    }

    @Override
    public Stopwatch startStopwatch() {
        // does not need locking because stopwatch creation does not read or write the store
        return statistics.startStopwatch();
    }

    @Override
    public void recordElapsedTime(String eventName, Stopwatch stopwatch) {
        try {
            rwLock.writeLock().lock();
            statistics.recordElapsedTime(eventName, stopwatch);
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public void addOccurrence(String eventName) {
        try {
            rwLock.writeLock().lock();
            statistics.addOccurrence(eventName);
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public void addOccurrences(String eventName, int timesOccurred) {
        try {
            rwLock.writeLock().lock();
            statistics.addOccurrences(eventName, timesOccurred);
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public Statistic findStatistic(String eventName) {
        try {
            rwLock.readLock().lock();
            return statistics.findStatistic(eventName);
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public long findOccurrence(String eventName) {
        try {
            rwLock.readLock().lock();
            return statistics.findOccurrence(eventName);
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public Map<String, Long> getAllOccurrencesSnapshot() {
        try {
            rwLock.readLock().lock();
            return statistics.getAllOccurrencesSnapshot();
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public Map<String, Long> getAllOccurrencesSnapshotAndReset() {
        try {
            rwLock.writeLock().lock();
            return statistics.getAllOccurrencesSnapshotAndReset();
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public SortedMap<String, Statistic> getSortedSnapshot() {
        try {
            rwLock.readLock().lock();
            return statistics.getSortedSnapshot();
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public SortedMap<String, Statistic> getSortedSnapshotAndReset() {
        try {
            rwLock.writeLock().lock();
            return statistics.getSortedSnapshotAndReset();
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public void reset() {
        try {
            rwLock.writeLock().lock();
            statistics.reset();
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public void addSample(String eventName, long value) {
        try {
            rwLock.writeLock().lock();
            statistics.addSample(eventName, value);
        } finally {
            rwLock.writeLock().unlock();
        }
    }
}
