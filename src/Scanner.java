import com.thingmagic.*;
import org.llrp.ltk.generated.parameters.Custom;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Scanner {
    private static final String PORT = "tmr:///dev/cu.usbserial-A105I6M5";

    private static SerialReader reader = null;
    private static CustomListener listener = null;

    /**
     * Returns a Serial Reader for more hands on approaches.
     *
     * @param port
     * @return
     */
    public static SerialReader getReader(String port) {

        System.out.println("Attempting to connect to Serial Reader at: " + port);

        try {
            SerialReader reader = (SerialReader) Reader.create(port);
            reader.paramSet("/reader/region/id", Reader.Region.NA);
            reader.paramSet("/reader/read/asyncOffTime", 0); // Enable continuous reading
            reader.connect();

            System.out.println("Successfully connected to reader.");

            return reader;
        } catch (Exception e) {
            System.out.println("EXCEPTION (initReader): " + e.getLocalizedMessage());
            return null;
        }
    }

    private static void initReader() {
        if (reader != null)
            return;

        System.out.println("Attempting to connect to Serial Reader at: " + PORT);

        try {
            SerialReader reader = (SerialReader) Reader.create(PORT);
            reader.paramSet("/reader/region/id", Reader.Region.OPEN);
            reader.paramSet("/reader/read/asyncOffTime", 0); // Enable continuous reading
            reader.connect();

            System.out.println("Successfully connected to reader.");

            Scanner.reader = reader;
        } catch (Exception e) {
            System.out.println("EXCEPTION (initReader): " + e.getLocalizedMessage());
        }
    }

    public static void bindReader(DefaultListModel<String> listModel) {
        if (reader == null)
            initReader();

        listener = new CustomListener(listModel);
    }



    public static void startScanning() {
        if (reader == null)
            initReader();

        listener.reset();
        reader.addReadListener(listener);
        reader.startReading();
    }

    public static void stopScanning() {
        if (reader == null)
            return;

        reader.stopReading();
        reader.removeReadListener(listener);
    }
}

class CustomListener implements ReadListener {

    private DefaultListModel listModel;
    private List<String> tags = new ArrayList<>();

    CustomListener(DefaultListModel listModel) {
        this.listModel = listModel;
    }

    void reset() {
        tags.clear();
    }

    @Override
    public void tagRead(Reader reader, TagReadData tagReadData) {
        String tag = tagReadData.epcString();

        if (!tags.contains(tag)) {
            listModel.addElement(tag);
            tags.add(tag);
        }
    }
}