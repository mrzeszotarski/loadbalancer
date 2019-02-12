package pl.mrzeszotarski.loadbalancer.domain.nodes;

public interface StateTransition {

    void down(LoadBalancerNode node);
    void up(LoadBalancerNode node);

}
