package com.github.diegopacheco.pocs.seda.worker;

import com.github.diegopacheco.pocs.seda.ff.FeatureFlagManager;
import com.github.diegopacheco.pocs.seda.metrics.MetricsManager;
import com.github.diegopacheco.pocs.seda.queue.QueueManager;
import com.github.diegopacheco.pocs.seda.thread.SilentThread;

public class SanitizerWorker implements Worker {

    private QueueManager<String> inQueue;
    private QueueManager<String> outQueue;

    public SanitizerWorker(QueueManager<String> inQueue, QueueManager<String> outQueue) {
        this.inQueue = inQueue;
        this.outQueue = outQueue;
    }

    @Override
    public void run() {
        while(true){
            String event = inQueue.consume();
            if (null!=event){
                try{
                    String sanitized = sanitize(event);
                    outQueue.publish(sanitized);

                    MetricsManager.ok(outQueue.getName(),SanitizerWorker.class.getSimpleName());
                }catch(Exception e){
                    MetricsManager.error(outQueue.getName(),SanitizerWorker.class.getSimpleName());
                }
            }
        }
    }

    private String sanitize(String event) {
        SilentThread.sleep(FeatureFlagManager.get(FeatureFlagManager.WORKER_SANITIZER_TIME_BACKPRESSURE_MS));
        return event.trim().toLowerCase();
    }

}