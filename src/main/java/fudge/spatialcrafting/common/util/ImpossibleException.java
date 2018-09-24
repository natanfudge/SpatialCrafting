package fudge.spatialcrafting.common.util;

public class ImpossibleException extends RuntimeException {
    public ImpossibleException() {
        super("This should never happen");
    }
}
