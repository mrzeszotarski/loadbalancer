package pl.mrzeszotarski.loadbalancer.recovery;

import com.google.common.collect.Lists;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class ScheduledAutoRecoveryRegistry implements AutoRecoveryRegistry {

    private ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

    private List<Runnable> runnables = Lists.newArrayList();
    private long perdiodInSeconds = 30;
    private AtomicBoolean started = new AtomicBoolean(false);

    public ScheduledAutoRecoveryRegistry(long perdiodInSeconds){
        this.perdiodInSeconds = perdiodInSeconds;
    }

    @Override
    public void addRunnable(Runnable runnable) {
        if(!started.get()){
            runnables.add(runnable);
        }
    }

    @Override
    public void startSimultanously() {
        scheduledExecutorService.scheduleAtFixedRate(() -> runnables.forEach(Runnable::run), 0, perdiodInSeconds, TimeUnit.SECONDS);
        started.getAndSet(true);
    }
}
