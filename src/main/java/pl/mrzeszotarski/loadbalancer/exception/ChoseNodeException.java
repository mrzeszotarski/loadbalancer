package pl.mrzeszotarski.loadbalancer.exception;

public class ChoseNodeException extends RuntimeException {

    public ChoseNodeException(Exception e){
        super("Choose of node went wrong!", e);
    }
}
