package pl.mrzeszotarski.loadbalancer.exception;

import pl.mrzeszotarski.loadbalancer.domain.nodes.LoadBalancerNodeState;

public class TransitionNotAllowedException extends RuntimeException {

    public TransitionNotAllowedException(LoadBalancerNodeState from, LoadBalancerNodeState to){
        super(String.format("Transition not allowed from %s to %s", from, to));
    }
}
