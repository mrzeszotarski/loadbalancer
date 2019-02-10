package pl.mrzeszotarski.loadbalancer.domain.nodes;

public interface LoadBalancerNode<T> {

    LoadBalancerNodeState currentState();

    void down();

    void up();

    T identifier();

}
