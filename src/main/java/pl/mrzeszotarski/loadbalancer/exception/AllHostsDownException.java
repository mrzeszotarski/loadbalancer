package pl.mrzeszotarski.loadbalancer.exception;

public class AllHostsDownException extends RuntimeException {

    public AllHostsDownException(){
        super("All hosts are down!");
    }
}
