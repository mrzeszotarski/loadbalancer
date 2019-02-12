package pl.mrzeszotarski.loadbalancer.domain.registry;

import pl.mrzeszotarski.loadbalancer.domain.nodes.LoadBalancerNode;

import java.util.Collection;
import java.util.Optional;

public interface NodeRegistry<T> {

    void downNodeWithIdentifier(T identifier);
    Optional<LoadBalancerNode<T>> currentNode();
    Collection<LoadBalancerNode<T>> nodes();
    void choseNode(T identifier);

}
