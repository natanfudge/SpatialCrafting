package fudge.spatialcrafting.common.tile.util;

import fudge.spatialcrafting.SpatialCrafting;
import org.jetbrains.annotations.Nullable;

public class ArrayUtils {

    public static <T1, T2, T3> void tripleForEach(Arr3D<T1> arr1, Arr3D<T2> arr2, Arr3D<T3> arr3, TripleArrayAction<T1, T2, T3> action) {
        if (arr1.dimsEqual(arr2) && arr2.dimsEqual(arr3)) {
            for (int i = 0; i < arr1.getHeight(); i++) {
                for (int j = 0; j < arr1.getLength(); j++) {
                    for (int k = 0; k < arr1.getWidth(); k++) {
                        action.apply(arr1.get(i, j, k), arr2.get(i, j, k), arr3.get(i, j, k));
                    }
                }
            }
        } else {
            SpatialCrafting.LOGGER.error(new ArrayIndexOutOfBoundsException("Attempt to tripleForEach with mismatching arrays!"));
        }
    }

    public interface TripleArrayAction<T1, T2, T3> {
        void apply(@Nullable T1 obj1, @Nullable T2 obj2, @Nullable T3 obj3);
    }

}
