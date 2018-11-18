package fudge.spatialcrafting.common.tile.util;


import java.util.function.Predicate;

public class ListUtil {
    public static<T> boolean any(Iterable<T> list, Predicate<T> condition){
        for (T element : list){
            if(condition.test(element)) return true;
        }
        return false;
    }
}
