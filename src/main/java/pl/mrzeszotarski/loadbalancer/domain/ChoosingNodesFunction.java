package pl.mrzeszotarski.loadbalancer.domain;

import pl.mrzeszotarski.loadbalancer.exception.AllHostsDownException;

import static pl.mrzeszotarski.loadbalancer.domain.LoadBalancerNodeState.UP;

public class ChoosingNodesFunction {

    public static <T> LoadBalancerNode<T> defaultChooseNodeFunction(DefaultNodeRegistry<T> nodesRegistry) {
        if (nodesRegistry.getCurrentNode() == null) {
            return defaultFindUpNode(nodesRegistry);
        }
        if (nodesRegistry.getCurrentNode().currentState() == UP)
            return nodesRegistry.getCurrentNode();
        else {
            return defaultFindUpNode(nodesRegistry);
        }
    }

    private static <T> LoadBalancerNode<T> defaultFindUpNode(DefaultNodeRegistry<T> nodesRegistry) {
        return nodesRegistry.getNodes().stream().filter(node -> node.currentState() == UP).findFirst().orElseThrow(AllHostsDownException::new);
    }
}
