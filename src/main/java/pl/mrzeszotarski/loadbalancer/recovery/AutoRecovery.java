package pl.mrzeszotarski.loadbalancer.recovery;

public interface AutoRecovery {

    void recover();

    void stopRecover();

}
