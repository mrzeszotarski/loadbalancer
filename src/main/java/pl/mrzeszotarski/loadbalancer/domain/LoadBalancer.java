package pl.mrzeszotarski.loadbalancer.domain;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import pl.mrzeszotarski.loadbalancer.domain.nodes.LoadBalancerNode;
import pl.mrzeszotarski.loadbalancer.domain.registry.NodeRegistry;

import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

@Slf4j
@ToString
public class LoadBalancer<U> {

    private static final Predicate<Throwable> NOT_SKIP_PREDICATE = throwable -> false;

    private NodeRegistry<U> nodesRegistry;
    private Consumer<U> nodeSettingConsumer;
    private Predicate<Throwable> throwableSkipPredicate;
    private Function<NodeRegistry<U>, LoadBalancerNode<U>> choseNodeFunction;

    public LoadBalancer(NodeRegistry<U> nodesRegistry) {
        this.throwableSkipPredicate = NOT_SKIP_PREDICATE;
        this.nodesRegistry = nodesRegistry;
    }

    public LoadBalancer(NodeRegistry<U> nodesRegistry, Consumer<U> nodeSettingConsumer, Function<NodeRegistry<U>, LoadBalancerNode<U>> choseNodeFunction, Predicate<Throwable> throwablePredicate) {
        this.throwableSkipPredicate = throwablePredicate;
        this.nodesRegistry = nodesRegistry;
        this.nodeSettingConsumer = nodeSettingConsumer;
        this.choseNodeFunction = choseNodeFunction;
    }

    public <T> T decorateCallable(Callable<T> callable) throws Exception {
        U identifier = this.chooseNode().identifier();
        try {
            this.nodeSettingConsumer.accept(identifier);
            return callable.call();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            if (this.throwableSkipPredicate.test(e)) {
                throw e;
            }
            this.nodesRegistry.downNodeWithIdentifier(identifier);
            return decorateCallable(callable);
        }
    }

    private LoadBalancerNode<U> chooseNode(){
        LoadBalancerNode<U> apply = choseNodeFunction.apply(nodesRegistry);
        nodesRegistry.choseNode(apply.identifier());
        return apply;
    }

}
