/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.trino.plugin.iceberg.util;

import com.google.common.collect.ImmutableMap;
import io.trino.spi.metrics.DataSkippingMetrics;
import io.trino.spi.metrics.Metrics;
import org.apache.iceberg.FilterMetrics;

import static io.trino.spi.metrics.DataSkippingMetrics.Builder;
import static io.trino.spi.metrics.DataSkippingMetrics.MetricType;
import static io.trino.spi.metrics.DataSkippingMetrics.MetricType.SKIPPED_BY_DF_IN_COORDINATOR;
import static io.trino.spi.metrics.DataSkippingMetrics.MetricType.SKIPPED_BY_INDEX_IN_COORDINATOR;
import static io.trino.spi.metrics.DataSkippingMetrics.MetricType.SKIPPED_BY_MINMAX_STATS;
import static io.trino.spi.metrics.DataSkippingMetrics.MetricType.SKIPPED_BY_PART_FILTER;
import static io.trino.spi.metrics.DataSkippingMetrics.MetricType.TOTAL;

public class MetricsUtils
{
    public static final String DATA_SKIPPING_METRICS_NAME = "iceberg_data_skipping_metrics";
    public static final Metrics EMPTY_DATA_SKIPPING_METRICS =
            new Metrics(ImmutableMap.of(DATA_SKIPPING_METRICS_NAME, DataSkippingMetrics.EMPTY));

    private MetricsUtils() {}

    public static Metrics makeMetrics(
            FilterMetrics filterMetrics,
            int skippedSplitsByDfInCoordinator,
            long skippedDataSizeByDfInCoordinator,
            int skippedSplitsByPartitionFilter,
            long skippedDataSizeByPartitionFilter)
    {
        Builder builder = DataSkippingMetrics.builder();
        addNonZeroMetric(builder, SKIPPED_BY_DF_IN_COORDINATOR, skippedSplitsByDfInCoordinator, skippedDataSizeByDfInCoordinator);
        addNonZeroMetric(builder, SKIPPED_BY_PART_FILTER, skippedSplitsByPartitionFilter, skippedDataSizeByPartitionFilter);

        if (filterMetrics != null) {
            filterMetrics.getMetricEntry(FilterMetrics.MetricType.TOTAL).ifPresent(entry ->
                    addNonZeroMetric(builder, TOTAL, entry.getRawSplitCount(), entry.getTotalFileSize()));
            filterMetrics.getMetricEntry(FilterMetrics.MetricType.SKIPPED_BY_MINMAX).ifPresent(entry ->
                    addNonZeroMetric(builder, SKIPPED_BY_MINMAX_STATS, entry.getRawSplitCount(), entry.getTotalFileSize()));
            filterMetrics.getMetricEntry(FilterMetrics.MetricType.SKIPPED_BY_IN_PLACE).ifPresent(entry ->
                    addNonZeroMetric(builder, SKIPPED_BY_INDEX_IN_COORDINATOR, entry.getRawSplitCount(), entry.getTotalFileSize()));
        }

        return new Metrics(ImmutableMap.of(DATA_SKIPPING_METRICS_NAME, builder.build()));
    }

    private static void addNonZeroMetric(Builder builder, MetricType metricType, int splitCount, long dataSize)
    {
        if (splitCount != 0 && dataSize != 0) {
            builder.withMetric(metricType, splitCount, dataSize);
        }
    }

    public static Metrics makeMetrics(MetricType metricType, int splitCount, long dataSize)
    {
        DataSkippingMetrics dataSkippingMetrics = DataSkippingMetrics.builder()
                .withMetric(metricType, splitCount, dataSize)
                .build();
        return new Metrics(ImmutableMap.of(DATA_SKIPPING_METRICS_NAME, dataSkippingMetrics));
    }
}