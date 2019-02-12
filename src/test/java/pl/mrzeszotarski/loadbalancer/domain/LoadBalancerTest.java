package pl.mrzeszotarski.loadbalancer.domain;

import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import pl.mrzeszotarski.loadbalancer.domain.nodes.ConcurrentAutoRecoveryNode;
import pl.mrzeszotarski.loadbalancer.domain.registry.ChoosingNodesFunction;
import pl.mrzeszotarski.loadbalancer.domain.registry.DefaultNodeRegistry;
import pl.mrzeszotarski.loadbalancer.domain.registry.NodeRegistry;

import java.util.List;

public class LoadBalancerTest {
    private static int counter = 0;
    private static int loadBalancerFunctionCounter = 0;

    private LoadBalancer<String> loadBalancer;
    private List<String> nodesUsed = Lists.newArrayList();

    @Before
    public void setUp() {
        counter = 0;
        loadBalancerFunctionCounter = 0;
        NodeRegistry<String> nodesRegistry = new DefaultNodeRegistry<>(Lists.newArrayList(
                new ConcurrentAutoRecoveryNode<>("1", () -> {
                    counter++;
                    if (counter % 2 == 0) {
                        throw new RuntimeException("Recovery fail!");
                    }
                    return counter;
                }, 1),
                new ConcurrentAutoRecoveryNode<>("2", () -> {
                    counter++;
                    if (counter % 2 == 0) {
                        throw new RuntimeException("Recovery fail!");
                    }
                    return counter;
                }, 1)
        ));
        loadBalancer = new LoadBalancer<>(nodesRegistry, this::nodeSetterFunction, ChoosingNodesFunction::defaultChooseNodeFunction, throwable -> throwable instanceof NoSuchFieldException);
    }

    @Test
    public void testDecorate() throws Exception {
        String callable = loadBalancer.decorateCallable(this::loadBalancerFunction);
        Assert.assertEquals("success", callable);
    }

    @Test
    public void testDecorateWithFailWillSwitchNode() throws Exception {
        String first = loadBalancer.decorateCallable(this::loadBalancerFunction); //loadBalancerFunctionCounter = 1
        String second = loadBalancer.decorateCallable(this::loadBalancerFunction); //loadBalancerFunctionCounter = 2

        Assert.assertEquals(3, nodesUsed.size());
        Assert.assertEquals("2", nodesUsed.get(2));
        Assert.assertEquals("1", nodesUsed.get(0));
        Assert.assertEquals("success", first);
        Assert.assertEquals("success", second);
    }

    @Test(expected = RuntimeException.class)
    public void testDecorateWithRecoveryAllNodesDown() throws Exception {
        loadBalancer.decorateCallable(this::loadBalancerFunction); //loadBalancerFunctionCounter = 1
        loadBalancer.decorateCallable(this::loadBalancerFunction); //loadBalancerFunctionCounter = 2
        loadBalancer.decorateCallable(this::loadBalancerFunction); //throws All Nodes Down
    }

    @Test
    public void testDecorateWithThrowableFilter() throws Exception {
        boolean expectedException = false;
        loadBalancer.decorateCallable(this::loadBalancerFunctionWithSkipCount); //loadBalancerFunctionCounter = 1
        try {
            loadBalancer.decorateCallable(this::loadBalancerFunctionWithSkipCount); //loadBalancerFunctionCounter = 2
        } catch (NoSuchFieldException e) {
            expectedException = true;
        }
        loadBalancer.decorateCallable(this::loadBalancerFunctionWithSkipCount); //loadBalancerFunctionCounter = 3

        Assert.assertEquals(true, expectedException);
        Assert.assertEquals(3, loadBalancerFunctionCounter);
    }

    @Test
    public void testDecorateWithRecovery() throws Exception {
        boolean allNodesDown = false;
        loadBalancer.decorateCallable(this::loadBalancerFunction); //loadBalancerFunctionCounter = 1
        loadBalancer.decorateCallable(this::loadBalancerFunction); //loadBalancerFunctionCounter = 2
        try {
            loadBalancer.decorateCallable(this::loadBalancerFunction); //loadBalancerFunctionCounter = 2 throws All Nodes Down
        } catch (Exception e) {
            allNodesDown = true;
        }
        Thread.sleep(1100);

        String actual = loadBalancer.decorateCallable(this::loadBalancerFunction);//recovery

        Assert.assertEquals(5, nodesUsed.size());
        Assert.assertEquals("1", nodesUsed.get(4));
        Assert.assertEquals(allNodesDown, true);
        Assert.assertEquals("success", actual);
    }

    private String loadBalancerFunction() {
        loadBalancerFunctionCounter++;
        if (loadBalancerFunctionCounter % 2 == 0) {
            throw new RuntimeException("Invoke error!");
        } else {
            return "success";
        }
    }

    private String loadBalancerFunctionWithSkipCount() throws NoSuchFieldException {
        loadBalancerFunctionCounter++;
        if (loadBalancerFunctionCounter % 2 == 0) {
            throw new NoSuchFieldException();
        } else {
            return "success";
        }
    }

    private String nodeSetterFunction(String node) {
        nodesUsed.add(node);
        return node;
    }

}
