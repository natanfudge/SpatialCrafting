package fudge.spatialcrafting.common.util;

import java.util.Objects;

public class Rectangle<T,S> {

    private T width;
    private S height;

    public Rectangle(T width, S height){
        this.width = width;
        this.height = height;
    }


    public T getWidth() {
        return width;
    }

    public void setWidth(T width) {
        this.width = width;
    }

    public S getHeight() {
        return height;
    }

    public void setHeight(S height) {
        this.height = height;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        Rectangle<?, ?> rectangle = (Rectangle<?, ?>) other;
        return Objects.equals(this.width, rectangle.width) && Objects.equals(this.height, rectangle.height);
    }

    @Override
    public int hashCode() {
        return Objects.hash(width, height);
    }
}
