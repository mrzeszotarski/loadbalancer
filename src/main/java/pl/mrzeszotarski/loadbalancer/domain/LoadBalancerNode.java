package pl.mrzeszotarski.loadbalancer.domain;

public interface LoadBalancerNode<T> {

    LoadBalancerNodeState currentState();
    void down();
    void up();
    T identifier();

}
