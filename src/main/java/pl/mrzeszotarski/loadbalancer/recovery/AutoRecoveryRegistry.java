package pl.mrzeszotarski.loadbalancer.recovery;

public interface AutoRecoveryRegistry {
    void addRunnable(Runnable runnable);
    void startSimultanously();
}
