package pl.mrzeszotarski.loadbalancer.domain.registry;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.mrzeszotarski.loadbalancer.domain.nodes.LoadBalancerNode;
import pl.mrzeszotarski.loadbalancer.exception.ChoseNodeException;

import java.util.Collection;
import java.util.function.Function;

@Data
@RequiredArgsConstructor
@Slf4j
public class DefaultNodeRegistry<T> implements NodeRegistry<T> {

    private final Collection<LoadBalancerNode<T>> nodes;
    private LoadBalancerNode<T> currentNode;
    private final Function<DefaultNodeRegistry<T>, LoadBalancerNode<T>> choseNodeFunction;

    @Override
    public void downNodeWithIdentifier(T identifier) {
        log.info("Setting node to down " + identifier);
        nodes.stream().filter(node -> node.identifier().equals(identifier)).findFirst().ifPresent(LoadBalancerNode::down);
    }

    @Override
    public LoadBalancerNode<T> choseNode() {
        try {
            currentNode = choseNodeFunction.apply(this);
            return currentNode;
        } catch (Exception e) {
            throw new ChoseNodeException(e);
        }
    }
}
