package pl.mrzeszotarski.loadbalancer.domain.nodes;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import pl.mrzeszotarski.loadbalancer.recovery.AutoRecovery;
import pl.mrzeszotarski.loadbalancer.recovery.ScheduledAutoRecovery;

import java.util.concurrent.Callable;

import static pl.mrzeszotarski.loadbalancer.domain.nodes.LoadBalancerNodeState.DOWN;
import static pl.mrzeszotarski.loadbalancer.domain.nodes.LoadBalancerNodeState.UP;

@ToString
@Slf4j
public class ConcurrentAutoRecoveryNode<T> implements LoadBalancerNode<T> {

    private volatile LoadBalancerNodeState state = UP;

    private Callable<?> recoveryCallable;
    private AutoRecovery autoRecovery;

    private T identifier;

    public ConcurrentAutoRecoveryNode(T identifier, Callable<?> recoveryCallable, long periodInSeconds) {
        this.identifier = identifier;
        this.recoveryCallable = recoveryCallable;
        this.autoRecovery = new ScheduledAutoRecovery(periodInSeconds, this::health);
        this.autoRecovery.recover();
    }

    public LoadBalancerNodeState currentState() {
        return state;
    }

    public synchronized void down() {
        this.state = DOWN;
    }

    private void health(){
        log.info("Upping");
        if (recoveryCallable != null) {
            try {
                recoveryCallable.call();
                this.currentState().up(this);
                log.info("Node upped");
            } catch (Exception e) {
                log.error("Upping went wrong :(", e);
                this.currentState().down(this);
            }
        } else {
            this.currentState().up(this);
            log.info("Node upped");
        }
    }

    public synchronized void up() {
        this.state = UP;
    }

    public T identifier() {
        return this.identifier;
    }

}
