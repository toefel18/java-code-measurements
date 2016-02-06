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

package nl.toefel.java.code.measurements;

import nl.toefel.java.code.measurements.api.Snapshot;
import nl.toefel.java.code.measurements.api.Statistic;
import nl.toefel.java.code.measurements.api.Statistics;
import nl.toefel.java.code.measurements.api.Stopwatch;
import org.junit.Before;
import org.junit.Test;

import static nl.toefel.java.code.measurements.singlethreadedimpl.AssertionHelper.*;
import static nl.toefel.java.code.measurements.singlethreadedimpl.TimingHelper.expensiveMethodTakingMillis;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/**
 * Base class for tests that verify correctness of the API.
 */
public abstract class StatisticsApiTestBase {

	private Statistics stats;

	protected abstract Statistics createStatistics();

	@Before
	public void setUp() {
		stats = createStatistics();
	}

	@Test
	public void testFindStatisticsNotNull() {
		Statistic empty = stats.findStatistic("test.empty");
		assertThat(empty).isNotNull();
		assertThat(empty.isEmpty()).isTrue();
	}

	@Test
	public void testFindOccurrenceNotNull() {
		long emptyCounter = stats.findOccurrence("test.empty");
		assertThat(emptyCounter).isZero();
	}

	@Test
	public void testStartStopwatch() {
		Stopwatch stopwatch = stats.startStopwatch();
		assertThat(stopwatch).isNotNull();
		assertThat(stopwatch.elapsedMillis()).isLessThan(100);
	}

	@Test
	public void testRecordElapsedTime() {
		Stopwatch stopwatch = stats.startStopwatch();

		expensiveMethodTakingMillis(100);
		stats.recordElapsedTime("test.duration", stopwatch);

		Statistic record = stats.findDuration("test.duration");

		assertRecordHasParametersWithin(record, 1, 100, 100, 100, 20);
		assertThat(record.getSampleVariance()).as("variance").isCloseTo(0.0d, within(0.0d));
		assertThat(record.getSampleStdDeviation()).as("standardDeviation").isEqualTo(Double.NaN);
	}

	@Test
	public void testRecordElapsedTimeMultiple() {
		Stopwatch stopwatch = stats.startStopwatch();
		expensiveMethodTakingMillis(80);
		stats.recordElapsedTime("test.duration", stopwatch);

		Stopwatch anotherStopwatch = stats.startStopwatch();
		expensiveMethodTakingMillis(150);
		stats.recordElapsedTime("test.duration", anotherStopwatch);

		Statistic record = stats.findDuration("test.duration");
		assertRecordHasParametersWithin(record, 2, 80, 150, 115.0d, 20);
		assertThat(record.getSampleVariance()).as("variance").isCloseTo(2450.0d, within(200.0d));
		assertThat(record.getSampleStdDeviation()).as("standardDeviation").isCloseTo(50, within(5.0d));
	}

	@Test
	public void testAddOccurrence_sinleInvocation() {
		stats.addOccurrence("test.occurrence");
		assertThat(stats.findOccurrence("test.occurrence")).isEqualTo(1);
	}

	@Test
	public void testAddOccurrence_findStatistic() {
		stats.addOccurrence("test.occurrence");
		Statistic counterStat = stats.findStatistic("test.occurrence");
		assertThat(counterStat.isEmpty()).as("occurences are queried through findOccurence").isTrue();
	}

	@Test
	public void testAddOccurrence_multipleInvocations() {
		stats.addOccurrence("test.occurrence");
		stats.addOccurrence("test.occurrence");
		stats.addOccurrence("test.occurrence");

		assertThat(stats.findOccurrence("test.occurrence")).isEqualTo(3);
	}

	@Test
	public void testAddOccurrence_newInstancesEachInvocation() {
		stats.addOccurrence("test.occurrence");
		assertThat(stats.findOccurrence("test.occurrence")).isEqualTo(1);

		stats.addOccurrence("test.occurrence");
		assertThat(stats.findOccurrence("test.occurrence")).isEqualTo(2);
	}

	@Test
	public void testAddOccurrences_singleInvocation() {
		stats.addOccurrences("test.occurrences", 3);
		assertThat(stats.findOccurrence("test.occurrences")).isEqualTo(3);
	}

	@Test
	public void testAddOccurrences_multipleInvocations() {
		stats.addOccurrences("test.occurrences", 1);
		stats.addOccurrences("test.occurrences", 4);
		assertThat(stats.findOccurrence("test.occurrences")).isEqualTo(5);
	}

	@Test
	public void testAddSample() {
		stats.addSample("test.sample", 5);
		Statistic stat = stats.findStatistic("test.sample");
		assertRecordHasExactParameters(stat, 1, 5, 5, 5, 0, Double.NaN);
	}

	@Test
	public void testAddSamples_calculation() {
		stats.addSample("test.sample", 5);
		stats.addSample("test.sample", 10);
		stats.addSample("test.sample", 15);
		Statistic stat = stats.findStatistic("test.sample");

		assertThat(stat.getSampleCount()).as("sampleCount").isEqualTo(3);
		assertThat(stat.getMinimum()).as("minimum").isEqualTo(5);
		assertThat(stat.getMaximum()).as("maximum").isEqualTo(15);
		assertThat(stat.getSampleAverage()).as("average").isCloseTo(10, within(0.01d));
		assertThat(stat.getSampleVariance()).as("variance").isCloseTo(50.0, within(0.01d));
		assertThat(stat.getSampleStdDeviation()).as("standardDeviation").isCloseTo(5, within(0.01d));
	}

	@Test
	public void testAddSampleAndCounterSameName() {
		stats.addSample("test.samename", 5);
		stats.addOccurrence("test.samename");

		Statistic stat = stats.findStatistic("test.samename");
		assertRecordHasExactParameters(stat, 1, 5, 5, 5, 0, Double.NaN);

		assertThat(stats.findOccurrence("test.samename")).isEqualTo(1);
	}

	@Test
	public void testGetSortedSnapshotWithSameOccurrenceAsStat() {
		stats.addOccurrence("test.test");
		stats.addSample("test.test", 5);
		stats.recordElapsedTime("test.test", stats.startStopwatch());

		Snapshot snapshot = stats.getSnapshot();

		assertThat(snapshot.getCounters()).containsKeys("test.test");
		assertThat(snapshot.getSamples()).containsKeys("test.test");
		assertThat(snapshot.getDurations()).containsKeys("test.test");
		assertThat(snapshot.getCounters().get("test.test")).isEqualTo(1L);
		assertRecordHasExactParameters(snapshot.getSamples().get("test.test"), 1, 5, 5, 5, 0, Double.NaN);
		assertRecordHasParametersWithin(snapshot.getDurations().get("test.test"), 1, 0, 0, 0, 10);
	}

	@Test
	public void testGetSortedSnapshotEmpty() {
		Snapshot snapshot = assertEmpty(stats.getSnapshot());
		assertThat(snapshot.getTimestampTaken()).isCloseTo(System.currentTimeMillis(), within(100L));
	}


	@Test
	public void testGetSortedSnapshotAndReset() {
		addCounterDurationAndSample("test.test");
		Snapshot snapshot = assertSize(stats.getSnapshotAndReset(), 1, 1, 1);
		assertEmpty(stats.getSnapshot());
	}

	@Test
	public void testReset() {
		addCounterDurationAndSample("test.test");
		Snapshot snapshot = assertSize(stats.getSnapshotAndReset(), 1, 1, 1);
		stats.reset();
		assertEmpty(stats.getSnapshot());
	}

	private void addCounterDurationAndSample(String name) {
		stats.addOccurrences(name, 1);
		stats.addSample(name, 5);
		stats.recordElapsedTime(name, stats.startStopwatch());
	}
}