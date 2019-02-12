package pl.mrzeszotarski.loadbalancer.domain.nodes;

public enum LoadBalancerNodeState implements StateTransition{

    UP {

        @Override
        public void down(LoadBalancerNode node) {
            node.down();
        }

        @Override
        public void up(LoadBalancerNode node) {
        }
    },
    DOWN {

        @Override
        public void down(LoadBalancerNode node) {
        }

        @Override
        public void up(LoadBalancerNode node) {
            node.up();
        }
    };


}
