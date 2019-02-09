package pl.mrzeszotarski.loadbalancer.recovery;

import lombok.RequiredArgsConstructor;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class ScheduledAutoRecovery implements AutoRecovery {

    private ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> scheduledForUp;
    private final long periodInSeconds;
    private final Runnable recoveryRunnable;

    @Override
    public void recover() {
        if(recoverIsProper()){
            scheduledForUp = scheduledExecutorService.scheduleAtFixedRate(recoveryRunnable, periodInSeconds, periodInSeconds, TimeUnit.SECONDS);
        }
    }

    @Override
    public void stopRecover() {
        scheduledForUp.cancel(false);
    }

    private boolean recoverIsProper(){
        return scheduledForUp == null || scheduledForUp.isDone() || scheduledForUp.isCancelled();
    }
}
