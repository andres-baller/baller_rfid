import com.thingmagic.SerialReader;
import com.thingmagic.TagReadData;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class Storage {
    public static void main(String[] args) {
        String port = "tmr:///dev/cu.usbserial-A105I6M5"; // default for mac
        String locationId = "7"; // default to ID of 'STORAGE Test' location
        int duration = 5000; // default to 5 seconds

        for(int i = 0; i < args.length; i++) {
            String arg = args[i];
            switch (arg) {
                case "--port":
                case "-p":
                    port = args[++i];
                    break;
                case "--duration":
                case "-d":
                    duration = Integer.parseInt(args[++i]);
                    break;
                case "--location":
                case "-l":
                    locationId = args[++i];
                    break;
                case "--help":
                case "-h":
                    System.out.println("Options:");
                    System.out.println("\t--port | -p     \t: The RFID scanner port, as a file path. e.g. tmr:///dev/USB0");
                    System.out.println("\t--location | -l \t: The Snipe-It ID of the storage location.");
                    System.out.println("\t--duration | -d \t: The desired scan duration, in milliseconds.");
                    return;
            }
        }

        // Print timestamp
        System.out.println("\n\n" + LocalDate.now() + " " + LocalTime.now() + "\n");

        SerialReader reader = Scanner.getReader(port);

        try {
            TagReadData[] data = reader.read(duration);
            List<String> tags = new ArrayList<>();

            for(TagReadData d: data) {
                tags.add(d.epcString());
            }

            SnipeIt.updateStoredAssets(tags, locationId);

        } catch (Exception e) {
            System.out.println(e.getLocalizedMessage());
        }
    }
}