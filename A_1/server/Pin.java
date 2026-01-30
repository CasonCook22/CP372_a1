public class Pin {
    private final int pin_x;
    private final int pin_y;

    public Pin(int pin_x, int pin_y) {
        this.pin_x = pin_x;
        this.pin_y = pin_y;
    }

    public int getPin_x() {
        return pin_x;
    }

    public int getPin_y() {
        return pin_y;
    }

    @Override   
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Pin pin = (Pin) o;

        if (pin_x != pin.pin_x) return false;
        return pin_y == pin.pin_y;
    }

    @Override
    public int hashCode() {
        int result = pin_x;
        result = 31 * result + pin_y;
        return result;
    }
}
