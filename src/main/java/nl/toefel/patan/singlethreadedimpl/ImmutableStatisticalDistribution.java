/*
 *    Copyright 2016 Christophe Hesters
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package nl.toefel.patan.singlethreadedimpl;

import nl.toefel.patan.api.StatisticalDistribution;

/**
 * Immutable structure describing a statistical distribution, making it threadsafe.
 * <p>
 * Equals and hashcode are not overridden because default behaviour makes more sense in this case.
 */
public final class ImmutableStatisticalDistribution implements StatisticalDistribution {

	private static final StatisticalDistribution EMPTY_STATISTICAL_DISTRIBUTION = new ImmutableStatisticalDistribution();

	private final long sampleCount;
	private final double minimum;
	private final double maximum;
	private final double average; ///qqqq ren to mean
	private final double totalVariance; //qqqq drop
	private final double stdDeviation; //qqqq drop, calculated field
	private final double shift;
	private final double sum; //qqqq ren to shiftedSum?
	private final double sumSqr; //qqqq ren to shiftedSumSqr?

	/**
	 * @return an empty statistic;
	 */
	public static StatisticalDistribution createEmpty() {
		return new ImmutableStatisticalDistribution();

	}

	/**
	 * @param sampleValue the value to initialize the distribution with
	 * @return a statistic with one sample
	 */
	public static StatisticalDistribution createWithSingleSample(long sampleValue) {
		return new ImmutableStatisticalDistribution().newWithExtraSample(sampleValue);
	}

	/**
	 * Creates a new statistical distribution object based on this distribution and the extra
	 * value that is being added.
	 * <p>
	 * Average, sampleVariance and sampleStdDeviation calculations are taken from
	 * <a href="http://en.wikipedia.org/wiki/Standard_deviation">http://en.wikipedia.org/wiki/Standard_deviation</a>
	 * qqqq https://en.wikipedia.org/wiki/Algorithms_for_calculating_variance#Computing_shifted_data
	 *
	 * @param sampleValue the value to merge with this record
	 * @return new, immutable, distribution
	 */
	@Override
	public StatisticalDistribution newWithExtraSample(double sampleValue) { //qqqq use https://en.wikipedia.org/wiki/Algorithms_for_calculating_variance Computing shifted data[edit]
		ImmutableStatisticalDistribution previous = this; // for readability
		double shift = previous.isEmpty() ? sampleValue : previous.shift;
		long updatedCount = previous.sampleCount + 1;
		double updatedMinimum = sampleValue < previous.minimum ? sampleValue : previous.minimum;
		double updatedMaximum = sampleValue > previous.maximum ? sampleValue : previous.maximum;
		double updatedAverage = previous.average + ((sampleValue - previous.average) / updatedCount);
		double updatedTotalVariance = previous.totalVariance + ((sampleValue - previous.average) * (sampleValue - updatedAverage));
		double updatedStdDeviation = Math.sqrt(updatedTotalVariance / (updatedCount - 1));
		double updatedSum = sum + sampleValue - shift;
		double updatedSumSqr = sumSqr + (sampleValue - shift) * (sampleValue - shift);
		//qqqq System.out.println(String.format("sampleValue=%s, updatedCount=%s, shift=%s, updatedSum=%s, updatedSumSqr=%s", sampleValue, updatedCount, shift, updatedSum, updatedSumSqr));
		ImmutableStatisticalDistribution newDist = new ImmutableStatisticalDistribution(updatedCount, updatedMinimum, updatedMaximum, updatedAverage, updatedTotalVariance, updatedStdDeviation, shift, updatedSum, updatedSumSqr);
		return newDist;
	}

	/**
	 * Private constructor to enforce immutability, use factory methods
	 */
	private ImmutableStatisticalDistribution() {
		this(0, Double.MAX_VALUE, Double.MIN_VALUE, 0, 0, 0, 0, 0, 0);
	}

	/**
	 * Private constructor to enforce immutability, use factory methods
	 */
	private ImmutableStatisticalDistribution(final long sampleCount, final double minimum, final double maximum, final double sampleAverage, final double sampleVariance, final double sampleStdDeviation,
			final double shift, final double sum, final double sumSqr) {
		this.sampleCount = sampleCount;
		this.minimum = minimum;
		this.maximum = maximum;
		this.average = sampleAverage;
		this.totalVariance = sampleVariance;
		this.stdDeviation = sampleStdDeviation;
		this.shift = shift;
		this.sum = sum;
		this.sumSqr = sumSqr;
	}


	@Override
	public boolean isEmpty() {
		return sampleCount <= 0;
	}


	@Override
	public long getSampleCount() {
		return sampleCount;
	}


	@Override
	public double getMinimum() {
		return minimum;
	}


	@Override
	public double getMaximum() {
		return maximum;
	}


	@Override
	public double getAverage() {
		return average;
	}

	@Override //qqqq moet weg
	public double getTotalVariance() {
		return totalVariance;
	}

	@Override
	public double getStdDeviation() {
		return stdDeviation;
	}

	public double getStdDeviationQqqq() {
		return Math.sqrt((sumSqr - sum * sum / sampleCount)/(sampleCount - 1));
	}

	@Override
	public String toString() {
		return "StatisticalDistribution [" +
				"sampleCount=" + sampleCount +
				", min=" + minimum +
				", max=" + maximum +
				", avg=" + average +
				", stddev=" + stdDeviation +
				']';
	}
}
