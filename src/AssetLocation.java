import java.util.Date;

public class AssetLocation {
    String id;
    String name;
    Date creationDate;

    @Override
    public String toString() {
        return name;
    }
}
