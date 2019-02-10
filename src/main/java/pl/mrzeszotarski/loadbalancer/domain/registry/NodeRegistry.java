package pl.mrzeszotarski.loadbalancer.domain.registry;

import pl.mrzeszotarski.loadbalancer.domain.nodes.LoadBalancerNode;

public interface NodeRegistry<T> {

    void downNodeWithIdentifier(T identifier);
    LoadBalancerNode<T> choseNode();

}
