package pl.mrzeszotarski.loadbalancer.domain.nodes;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import pl.mrzeszotarski.loadbalancer.recovery.AutoRecoveryRegistry;

import java.util.concurrent.Callable;

import static pl.mrzeszotarski.loadbalancer.domain.nodes.LoadBalancerNodeState.DOWN;
import static pl.mrzeszotarski.loadbalancer.domain.nodes.LoadBalancerNodeState.UP;

@ToString
@Slf4j
public class SimultanousAutoRecoveryNode<T> implements LoadBalancerNode<T> {
    private volatile LoadBalancerNodeState state = UP;

    private Callable<?> recoveryCallable;

    private T identifier;

    public SimultanousAutoRecoveryNode(T identifier, Callable<?> recoveryCallable, AutoRecoveryRegistry registry){
        this.identifier = identifier;
        this.recoveryCallable = recoveryCallable;
        registry.addRunnable(this::health);
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
