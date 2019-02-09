package pl.mrzeszotarski.loadbalancer.domain;

import com.google.common.collect.Lists;
import lombok.Builder;
import lombok.Singular;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import pl.mrzeszotarski.loadbalancer.exception.AllHostsDownException;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.function.Predicate;

import static pl.mrzeszotarski.loadbalancer.domain.LoadBalancerNodeState.UP;

@Builder
@Slf4j
@ToString
public class LoadBalancer<U>{

    @Singular("addNode")
    private List<LoadBalancerNode<U>> nodes = Lists.newArrayList();
    @Builder.Default
    private Predicate<Throwable> throwableSkipPredicate = throwable -> false;

    private LoadBalancerNode<U> currentNode;

    public static <T,U> T decorateCallable(LoadBalancer<U> loadBalancer, Callable<T> callable, Function<U, ?> settingNodeFunction) throws Exception{
        U identifier = loadBalancer.currentUpNode().identifier();
        try {
            settingNodeFunction.apply(identifier);
            return callable.call();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            if(loadBalancer.throwableSkipPredicate.test(e)){
                throw e;
            }
            loadBalancer.downNodeWithIdentifier(identifier);
            return decorateCallable(loadBalancer, callable, settingNodeFunction);
        }
    }

    private LoadBalancerNode<U> currentUpNode() {
        if(currentNode == null){
            currentNode = findUpNode();
        }
        if(currentNode.currentState() == UP)
            return currentNode;
        else{
            currentNode = findUpNode();
            return currentNode;
        }
    }

    private LoadBalancerNode<U> findUpNode() {
        return nodes.stream().filter(node -> node.currentState() == UP).findFirst().orElseThrow(AllHostsDownException::new);
    }

    private void downNodeWithIdentifier(U identifier) {
        log.info("Setting node to down " + identifier);
        nodes.stream().filter(node -> node.identifier().equals(identifier)).findFirst().ifPresent(LoadBalancerNode::down);
    }

}
