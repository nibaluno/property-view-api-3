package com.propertyview.application.service;

import com.propertyview.application.histogram.HistogramParam;
import com.propertyview.application.histogram.HistogramProvider;
import com.propertyview.application.port.in.GetHistogramUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;

/**
 * an application service that implements the {@link GetHistogramUseCase} input port
 * it employs the strategy pattern to aggregate hotel statistics
 */
public class HistogramService implements GetHistogramUseCase {
    private static final Logger log = LoggerFactory.getLogger(HistogramService.class);
    private final Map<HistogramParam, HistogramProvider> providers = new EnumMap<>(HistogramParam.class);

    public HistogramService(Collection<HistogramProvider> all) {
        for (HistogramProvider provider : all) {
            if (providers.put(provider.param(), provider) != null) {
                throw new IllegalStateException("Duplicate HistogramProvider for " + provider.param());
            }
        }
        for (HistogramParam param : HistogramParam.values()) {
            if (!providers.containsKey(param)) {
                throw new IllegalStateException("No HistogramProvider for " + param);
            }
        }
    }

    @Override
    public Map<String, Long> getHistogram(HistogramParam param) {
        Map<String, Long> histogram = providers.get(param).histogram();
        log.info("Calculated histogram for param={} (buckets={})", param, histogram.size());
        return histogram;
    }
}
