package pl.mrzeszotarski.loadbalancer.domain;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import pl.mrzeszotarski.loadbalancer.domain.registry.NodeRegistry;

import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.function.Predicate;

@Slf4j
@ToString
public class LoadBalancer<U> {

    private static final Predicate<Throwable> NOT_SKIP_PREDICATE = throwable -> false;

    private NodeRegistry<U> nodesRegistry;
    private Predicate<Throwable> throwableSkipPredicate;

    public LoadBalancer(NodeRegistry<U> nodesRegistry) {
        this.throwableSkipPredicate = NOT_SKIP_PREDICATE;
        this.nodesRegistry = nodesRegistry;
    }

    public LoadBalancer(NodeRegistry<U> nodesRegistry, Predicate<Throwable> throwablePredicate) {
        this.throwableSkipPredicate = throwablePredicate;
        this.nodesRegistry = nodesRegistry;
    }

    public static <T, U> T decorateCallable(LoadBalancer<U> loadBalancer, Callable<T> callable, Function<U, ?> settingNodeFunction) throws Exception {
        U identifier = loadBalancer.nodesRegistry.choseNode().identifier();
        try {
            settingNodeFunction.apply(identifier);
            return callable.call();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            if (loadBalancer.throwableSkipPredicate.test(e)) {
                throw e;
            }
            loadBalancer.nodesRegistry.downNodeWithIdentifier(identifier);
            return decorateCallable(loadBalancer, callable, settingNodeFunction);
        }
    }

}
