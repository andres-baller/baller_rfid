public class Asset {
    String id;
    String tag;
    String name;

    @Override
    public String toString() {
        return name + " (" + tag + ")";
    }
}
