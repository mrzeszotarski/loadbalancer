# loadbalancer
Lightweight software loadbalancer. 

This library allows to split executions of specified methods to different places by specified setter node function. For now there are specified interfaces (`LoadBalancerNode`, `AutoRecovery`, `NodeRegistry`) to implement your own choosing algorithm and your own node types. Default implementation of `LoadBalancerNode` brings `AutoRecoveryNode` which nodes are able to perform recover action, specified in their constructor with use of implementation of `AutoRecovery` interface by `ScheduledAutoRecovery` which tries to call recovery callable after defined period of time and if no success, it tries to retry until success every defined period of time. Every node has state: `UP` or `DOWN`. Recover action can change state of node from `DOWN` to `UP`. If no recover action is specified, node is automaticaly changing state to `UP` after specified period of time (in seconds) in constructor `AutoRecoveryNode(T identifier, Callable<?> recoveryCallable, long periodInSeconds)`. 

Default implementation of `NodeRegistry` is `DefaultNodeRegistry`. It stores nodes as Java's `Collection` and provide possibility to specify node choosing function. In constructor of `DefaultNodeRegistry` you have to provide collection of nodes and node choosing function.

There is provided default choosing function `ChoosingNodesFunction::defaultChooseNodeFunction` which algorithm is very simple: if current node is in `UP` state it doesn't choose new node. In other case it choose firstly found node in `UP` state.

When creating new `LoadBalancer` you can provide `Predicate` for skipping `Throwable`s which can be not result of non working node.

Example of configuring `LoadBalancer`
```
NodeRegistry<String> nodesRegistry = new DefaultNodeRegistry<>(Lists.newArrayList(
                new AutoRecoveryNode<>("1", () -> {throw new RuntimeException("Recovery fail!");}, 1),
                new AutoRecoveryNode<>("2", () -> {throw new RuntimeException("Recovery fail!");}, 1)),       
                ChoosingNodesFunction::defaultChooseNodeFunction);
LoadBalancer loadBalancer = new LoadBalancer<>(nodesRegistry, throwable -> throwable instanceof NoSuchFieldException);
```

You can use `loadBalancer` by decorating your function (`loadBalancerFunction`) with node setting function (`nodeSetterFunction`) by 
```
LoadBalancer.decorateCallable(loadBalancer, loadBalancerFunction, nodeSetterFunction);
```
