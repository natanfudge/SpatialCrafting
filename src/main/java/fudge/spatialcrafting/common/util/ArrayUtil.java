package fudge.spatialcrafting.common.util;

import lombok.experimental.UtilityClass;

import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

@UtilityClass
public class ArrayUtil {


    /**
     * Returns true if every inner element in the arrays is in the same in every position.
     * Currently throws exception if they are not the same size
     *
     * @param tester The condition in which two inner objects are considered equal
     */
    public static <T> boolean innerEquals(T[][][] arr1, T[][][] arr2, BiPredicate<T, T> tester) {
        for (int i = 0; i < arr1.length; i++) {
            for (int j = 0; j < arr1[i].length; j++) {
                for (int k = 0; k < arr1[i][j].length; k++) {

                    T object1 = arr1[i][j][k];
                    T object2 = arr2[i][j][k];

                    if (object1 != null && object2 != null) {
                        if (!tester.test(object1, object2)) {
                            return false;
                        }
                        // If one of them are null then we have to use reference comparison
                    } else if ((object1 != object2)) {
                        return false;
                    }
                }

            }
        }

        return true;
    }

    /**
     * Returns true if every inner element in the arrays is in the same in every position.
     * Currently throws exception if they are not the same size
     */
    public static boolean innerEquals(Object[][][] arr1, Object[][][] arr2) {
        return innerEquals(arr1, arr2, Object::equals);
    }

    /**
     * Returns true if every inner element in the arrays is in the same in every position.
     * Currently throws exception if they are not the same size
     *
     * @param tester The condition in which two inner objects are considered equal
     */
    public static <T1, T2> boolean innerEqualsDifferentSizes(T1[][][] arr1, T2[][][] arr2, BiPredicate<T1, T2> tester) {
        return innerEqualsDifferentSizes(arr1, arr2, tester, null);
    }

    /**
     * Returns true if every inner element in the arrays is in the same in every position.
     * Counts null the same as the array being not in bounds, allowing for different sizes of arrays to be equal.
     *
     * @param tester     The condition in which two inner objects are considered equal
     * @param nullObject An object that should count as equal to null.
     */
    public static <T1, T2, T3> boolean innerEqualsDifferentSizes(T1[][][] arr1, T2[][][] arr2, BiPredicate<T1, T2> tester, T3 nullObject) {

        boolean equals;

        // Objects that behaves the same as a null
        GeneralNull gNull = new GeneralNull(nullObject);

        for (int i = 0; i < Math.max(arr1.length, arr2.length); i++) {

            // Out of bounds handling
            if (i >= arr1.length) {
                if (areaIsNull(arr2[i], gNull)) {
                    continue;   // This is fine, this area counts as equal
                } else {
                    return false; // Out of bounds and in the other exists something that is not null, then it does not count as equal.
                }
            }
            if (i >= arr2.length) {
                if (areaIsNull(arr1[i], gNull)) {
                    continue;
                } else {
                    return false;
                }
            }

            for (int j = 0; j < Math.max(arr1[i].length, arr2[i].length); j++) {

                // Out of bounds handling
                if (j >= arr1[i].length) {
                    if (areaIsNull(arr2[i][j], gNull)) {
                        continue;   // This is fine, this area counts as equal
                    } else {
                        return false;   // Out of bounds and in the other exists something that is not null, then it does not count as equal.
                    }
                }
                if (j >= arr2[i].length) {
                    if (areaIsNull(arr1[i][j], gNull)) {
                        continue;
                    } else {
                        return false;
                    }
                }

                for (int k = 0; k < Math.max(arr1[i][j].length, arr2[i][j].length); k++) {

                    // Out of bounds handling
                    if (k >= arr1[i][j].length) {
                        if (gNull.equals(arr2[i][j][k])) {
                            continue;   // This is fine, this area counts as equal
                        } else {
                            return false;   // Out of bounds and in the other exists something that is not null, then it does not count as equal.
                        }
                    }
                    if (k >= arr2[i][j].length) {
                        if (gNull.equals(arr1[i][j][k])) {
                            continue;
                        } else {
                            return false;
                        }
                    }

                    T1 object1 = arr1[i][j][k];
                    T2 object2 = arr2[i][j][k];

                    // Null handling
                    if (object1 == null && object2 == null) continue;
                    if (object1 == null) {
                        if (object2.equals(nullObject)) {
                            continue;
                        } else {
                            return false;
                        }
                    }

                    if (object2 == null) {
                        if (object1.equals(nullObject)) {
                            continue;
                        } else {
                            return false;
                        }
                    }

                    // Finally the test

                    if (!tester.test(object1, object2)) {
                        return false;
                    }

                }

            }
        }

        return true;
    }



    private static <T> boolean areaIsNull(T[] areaOfArr, GeneralNull nullObject) {
        for (T object : areaOfArr) {
            if (!nullObject.equals(object)) {
                return false;
            }
        }

        return true;
    }

    private static <T> boolean areaIsNull(T[][] areaOfArr, GeneralNull nullObject) {
        for (T[] arr1D : areaOfArr) {
            for (T object : arr1D) {
                if (!nullObject.equals(object)) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Returns true if every inner element in a 3D array is equal to a singular object.
     */
    public static <T1, T2> boolean arrEqualsObj(T1[][][] arr1, T2 object, BiPredicate<T1, T2> tester) {
        for (T1[][] arr2D : arr1) {
            for (T1[] arr1D : arr2D) {
                for (T1 innerElement : arr1D) {
                    if (!tester.test(innerElement, object)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Perform an action on every inner object in a 3D array.
     *
     * @param function The action to perforn on every inner object.
     */
    public static <T> void innerForEach(T[][][] arr, Consumer<T> function) {
        for (T[][] arr2D : arr) {
            innerForEach2D(arr2D, function);

        }
    }

    public static <T> void innerForEach2D(T[][] arr, Consumer<T> function) {
        for (T[] arr1D : arr) {
            for (T object : arr1D) {
                function.accept(object);
            }
        }
    }

    private static class GeneralNull {
        private Object nullObject;

        private GeneralNull(Object nullObject) {
            this.nullObject = nullObject;
        }

        public boolean equals(Object other) {
            return other == null || other.equals(nullObject);
        }

        @Override
        public int hashCode() {
            return Objects.hash(nullObject);
        }
    }
}
