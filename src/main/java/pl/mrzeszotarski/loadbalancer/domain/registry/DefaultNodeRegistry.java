package pl.mrzeszotarski.loadbalancer.domain.registry;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.mrzeszotarski.loadbalancer.domain.nodes.LoadBalancerNode;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;


@Data
@RequiredArgsConstructor
@Slf4j
public class DefaultNodeRegistry<T> implements NodeRegistry<T> {

    private final Collection<LoadBalancerNode<T>> nodes;
    private AtomicReference<LoadBalancerNode<T>> currentNode = new AtomicReference<>();

    @Override
    public void downNodeWithIdentifier(T identifier) {
        log.info("Setting node to down " + identifier);
        nodes.stream().filter(node -> node.identifier().equals(identifier)).findFirst().ifPresent(LoadBalancerNode::down);
    }

    @Override
    public Optional<LoadBalancerNode<T>> currentNode() {
        return Optional.ofNullable(currentNode.get());
    }

    @Override
    public Collection<LoadBalancerNode<T>> nodes() {
        return nodes;
    }

    @Override
    public void choseNode(T identifier) {
        if(currentNode.get() == null){
            nodes.stream().filter(node -> node.identifier().equals(identifier)).findFirst().ifPresent(node ->this.currentNode.set(node));
        }
        else if(!currentNode.get().identifier().equals(identifier)){
            nodes.stream().filter(node -> node.identifier().equals(identifier)).findFirst().ifPresent(node ->this.currentNode.set(node));
        }
    }
}
