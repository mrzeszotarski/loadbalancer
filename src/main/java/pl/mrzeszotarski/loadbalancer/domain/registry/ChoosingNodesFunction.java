package pl.mrzeszotarski.loadbalancer.domain.registry;

import pl.mrzeszotarski.loadbalancer.domain.nodes.LoadBalancerNode;
import pl.mrzeszotarski.loadbalancer.exception.AllHostsDownException;

import static pl.mrzeszotarski.loadbalancer.domain.nodes.LoadBalancerNodeState.UP;

public class ChoosingNodesFunction {

    public static <T> LoadBalancerNode<T> defaultChooseNodeFunction(NodeRegistry<T> nodesRegistry) {
        if (!nodesRegistry.currentNode().isPresent()) {
            return defaultFindUpNode(nodesRegistry);
        }
        if (nodesRegistry.currentNode().get().currentState() == UP)
            return nodesRegistry.currentNode().get();
        else {
            return defaultFindUpNode(nodesRegistry);
        }
    }

    private static <T> LoadBalancerNode<T> defaultFindUpNode(NodeRegistry<T> nodesRegistry) {
        return nodesRegistry.nodes().stream().filter(node -> node.currentState() == UP).findFirst().orElseThrow(AllHostsDownException::new);
    }
}
