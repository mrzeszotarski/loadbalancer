package pl.mrzeszotarski.loadbalancer.domain;

import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import pl.mrzeszotarski.loadbalancer.domain.nodes.AutoRecoveryNode;
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
                new AutoRecoveryNode<>("1", () -> {
                    counter++;
                    if (counter % 2 == 0) {
                        throw new RuntimeException("Recovery fail!");
                    }
                    return counter;
                }, 1),
                new AutoRecoveryNode<>("2", () -> {
                    counter++;
                    if (counter % 2 == 0) {
                        throw new RuntimeException("Recovery fail!");
                    }
                    return counter;
                }, 1)
        ), ChoosingNodesFunction::defaultChooseNodeFunction);
        loadBalancer = new LoadBalancer<>(nodesRegistry, throwable -> throwable instanceof NoSuchFieldException);
    }

    @Test
    public void testDecorate() throws Exception {
        String callable = LoadBalancer.decorateCallable(loadBalancer, this::loadBalancerFunction, this::nodeSetterFunction);
        Assert.assertEquals("success", callable);
    }

    @Test
    public void testDecorateWithFailWillSwitchNode() throws Exception {
        String first = LoadBalancer.decorateCallable(loadBalancer, this::loadBalancerFunction, this::nodeSetterFunction); //loadBalancerFunctionCounter = 1
        String second = LoadBalancer.decorateCallable(loadBalancer, this::loadBalancerFunction, this::nodeSetterFunction); //loadBalancerFunctionCounter = 2

        Assert.assertEquals(3, nodesUsed.size());
        Assert.assertEquals("2", nodesUsed.get(2));
        Assert.assertEquals("1", nodesUsed.get(0));
        Assert.assertEquals("success", first);
        Assert.assertEquals("success", second);
    }

    @Test(expected = RuntimeException.class)
    public void testDecorateWithRecoveryAllNodesDown() throws Exception {
        LoadBalancer.decorateCallable(loadBalancer, this::loadBalancerFunction, this::nodeSetterFunction); //loadBalancerFunctionCounter = 1
        LoadBalancer.decorateCallable(loadBalancer, this::loadBalancerFunction, this::nodeSetterFunction); //loadBalancerFunctionCounter = 2
        LoadBalancer.decorateCallable(loadBalancer, this::loadBalancerFunction, this::nodeSetterFunction); //throws All Nodes Down
    }

    @Test
    public void testDecorateWithThrowableFilter() throws Exception {
        boolean expectedException = false;
        LoadBalancer.decorateCallable(loadBalancer, this::loadBalancerFunctionWithSkipCount, this::nodeSetterFunction); //loadBalancerFunctionCounter = 1
        try {
            LoadBalancer.decorateCallable(loadBalancer, this::loadBalancerFunctionWithSkipCount, this::nodeSetterFunction); //loadBalancerFunctionCounter = 2
        } catch (NoSuchFieldException e) {
            expectedException = true;
        }
        LoadBalancer.decorateCallable(loadBalancer, this::loadBalancerFunctionWithSkipCount, this::nodeSetterFunction); //loadBalancerFunctionCounter = 3

        Assert.assertEquals(true, expectedException);
        Assert.assertEquals(3, loadBalancerFunctionCounter);
    }

    @Test
    public void testDecorateWithRecovery() throws Exception {
        boolean allNodesDown = false;
        LoadBalancer.decorateCallable(loadBalancer, this::loadBalancerFunction, this::nodeSetterFunction); //loadBalancerFunctionCounter = 1
        LoadBalancer.decorateCallable(loadBalancer, this::loadBalancerFunction, this::nodeSetterFunction); //loadBalancerFunctionCounter = 2
        try {
            LoadBalancer.decorateCallable(loadBalancer, this::loadBalancerFunction, this::nodeSetterFunction); //loadBalancerFunctionCounter = 2 throws All Nodes Down
        } catch (Exception e) {
            allNodesDown = true;
        }
        Thread.sleep(1100);

        String actual = LoadBalancer.decorateCallable(loadBalancer, this::loadBalancerFunction, this::nodeSetterFunction);//recovery

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
