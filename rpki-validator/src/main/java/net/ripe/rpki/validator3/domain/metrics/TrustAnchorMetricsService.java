package net.ripe.rpki.validator3.domain.metrics;


import io.micrometer.core.instrument.*;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.ripe.rpki.validator3.storage.Storage;
import net.ripe.rpki.validator3.storage.data.Key;
import net.ripe.rpki.validator3.storage.data.TrustAnchor;
import net.ripe.rpki.validator3.storage.data.validation.ValidationCheck;
import net.ripe.rpki.validator3.storage.data.validation.ValidationRun;
import net.ripe.rpki.validator3.storage.stores.ValidationRuns;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
@Setter
public class TrustAnchorMetricsService {
    @Autowired
    private MeterRegistry registry;

    @Autowired
    private Storage storage;

    @Autowired
    private ValidationRuns validationRuns;

    /**
     * Creating multiple counters for the same trust anchor has no negative side-effects.
     */
    private ConcurrentHashMap<String, ValidationEntry> counters = new ConcurrentHashMap<>();

    public void update(TrustAnchor ta, ValidationRun vr, long durationMs) {
        final String uri = ta.getLocations().size() > 0 ? ta.getLocations().get(0) : null;
        assert uri != null;

        if (counters.containsKey(uri)) {
            counters.get(uri).update(vr, durationMs);
        } else {
            counters.put(uri, new ValidationEntry(ta, vr, durationMs));
        }
    }

    private class ValidationEntry {
        private final String rsyncPrefetchUri;

        private final AtomicInteger warningCount;
        private final AtomicInteger errorCount;
        private final AtomicInteger objectCount;

        private final AtomicLong lastValidationRunTime;
        private Key lastValidationRunKey = Key.of(-1);

        private final Counter validationRunFailedCount;
        private final Counter validationRunSuccessCount;

        private final Timer validationRunDuration;

        public ValidationEntry(TrustAnchor trustAnchor, ValidationRun vr, long durationMs) {
            this.objectCount = new AtomicInteger(0);
            this.errorCount = new AtomicInteger(0);
            this.warningCount = new AtomicInteger(0);

            this.lastValidationRunTime = new AtomicLong(0);

            this.rsyncPrefetchUri = trustAnchor.getLocations().get(0);

            this.validationRunDuration = Timer.builder("validation_run_duration")
                    .tag("trust_anchor", rsyncPrefetchUri)
                    .register(registry);

            this.validationRunSuccessCount = Counter.builder("validation_run_count")
                    .tag("trust_anchor", rsyncPrefetchUri)
                    .tag("succeeded", "true")
                    .register(registry);
            this.validationRunFailedCount = Counter.builder("validation_run_count")
                    .tag("trust_anchor", rsyncPrefetchUri)
                    .tag("succeeded", "false")
                    .register(registry);

            Gauge.builder("validation_results", objectCount::get)
                    .tag("status", "total")
                    .tag("trust_anchor", rsyncPrefetchUri)
                    .register(registry);

            Gauge.builder("validation_results", errorCount::get)
                    .tag("status", "error")
                    .tag("trust_anchor", rsyncPrefetchUri)
                    .register(registry);

            Gauge.builder("validation_results", warningCount::get)
                    .tag("status", "warning")
                    .tag("trust_anchor", rsyncPrefetchUri)
                    .register(registry);

            Gauge.builder("last_validation_run", lastValidationRunTime::get)
                    .tag("trust_anchor", rsyncPrefetchUri)
                    .register(registry);

            this.update(vr, durationMs);
        }

        public void update(ValidationRun vr, long durationMs) {
            final Key currentRunKey = vr.key();
            if (!lastValidationRunKey.equals(currentRunKey)) {
                lastValidationRunKey = currentRunKey;

                if (vr.isSucceeded()) {
                    validationRunSuccessCount.increment();
                } else {
                    validationRunFailedCount.increment();
                }

                validationRunDuration.record(durationMs, TimeUnit.MILLISECONDS);

                lastValidationRunTime.set(vr.getCompletedAt().toEpochMilli()/1000);
                errorCount.set(vr.countChecks(ValidationCheck.Status.ERROR));

                storage.readTx0(tx -> {
                    objectCount.set(validationRuns.getObjectCount(tx, vr));
                });
                warningCount.set(vr.countChecks(ValidationCheck.Status.WARNING));
            }
        }
    }
}
