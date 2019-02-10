package pl.mrzeszotarski.loadbalancer.domain;

public interface NodeRegistry<T> {

    void downNodeWithIdentifier(T identifier);
    LoadBalancerNode<T> choseNode();

}
