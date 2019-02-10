package pl.mrzeszotarski.loadbalancer.domain;

import lombok.Builder;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import pl.mrzeszotarski.loadbalancer.recovery.AutoRecovery;
import pl.mrzeszotarski.loadbalancer.recovery.ScheduledAutoRecovery;

import java.util.concurrent.Callable;

import static pl.mrzeszotarski.loadbalancer.domain.LoadBalancerNodeState.DOWN;
import static pl.mrzeszotarski.loadbalancer.domain.LoadBalancerNodeState.UP;

@ToString
@Slf4j
public class AutoRecoveryNode<T> implements LoadBalancerNode<T> {

    private volatile LoadBalancerNodeState state = UP;

    private Callable<?> recoveryCallable;
    private AutoRecovery autoRecovery;

    private T identifier;

    public AutoRecoveryNode(T identifier, Callable<?> recoveryCallable, long periodInSeconds) {
        this.identifier = identifier;
        this.recoveryCallable = recoveryCallable;
        this.autoRecovery = new ScheduledAutoRecovery(periodInSeconds, this::up);
    }

    public LoadBalancerNodeState currentState() {
        return state;
    }

    public synchronized void down() {
        this.state = DOWN;
        autoRecovery.recover();
    }

    public synchronized void up() {
        log.info("Upping");
        if (recoveryCallable != null) {
            try {
                recoveryCallable.call();
                this.state = UP;
                log.info("Node upped");
                autoRecovery.stopRecover();
            } catch (Exception e) {
                log.error("Upping went wrong :(", e);
            }
        } else {
            this.state = UP;
            log.info("Node upped");
            autoRecovery.stopRecover();
        }
    }

    public T identifier() {
        return this.identifier;
    }
}
