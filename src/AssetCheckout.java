import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.List;

public class AssetCheckout {
    private JPanel mainPanel;
    private JList<AssetLocation> locationsList;
    private JList<String> assetsList;
    private JButton refreshLocationsButton;
    private JButton toggleScanButton;
    private JLabel statusLabel;
    private JLabel locationLabel;
    private JLabel assetsCountLabel;
    private DefaultListModel<String> assetsListModel;
    private DefaultListModel<AssetLocation> locationsListModel;

    public AssetCheckout() {

        toggleScanButton.addActionListener(this::toggleScanning);

        // Assets list model
        assetsListModel = new DefaultListModel<>();
        assetsListModel.addListDataListener(new ListDataListener() {
            @Override
            public void intervalAdded(ListDataEvent e) {
                assetsCountLabel.setText("Assets Scanned: " + assetsListModel.size());
            }

            @Override public void intervalRemoved(ListDataEvent e) { }
            @Override public void contentsChanged(ListDataEvent e) { }
        });
        assetsList.setModel(assetsListModel);

        Scanner.bindReader(assetsListModel);


        // Locations list model
        locationsListModel = new DefaultListModel<>();
        locationsList.setModel(locationsListModel);
        locationsList.addListSelectionListener((ListSelectionEvent e) -> {
            AssetLocation location = locationsList.getSelectedValue();
            if (location != null) {
                locationLabel.setText("Current checkout location: " + location.name);
            } else {
                locationLabel.setText("Current checkout location: NONE");
            }
        });

        refreshLocations();
        refreshLocationsButton.addActionListener((ActionEvent e) -> refreshLocations());
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("AssetCheckout");
        frame.setContentPane(new AssetCheckout().mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    private void refreshLocations() {
        locationsListModel.clear();

        List<AssetLocation> locations = SnipeIt.fetchLocations();
        for(AssetLocation loc : locations)
            locationsListModel.addElement(loc);
    }

    private void toggleScanning(ActionEvent e) {
        if (e.getActionCommand().equals("Start Scanning")) {
            assetsListModel.clear();
            assetsCountLabel.setText("Assets Scanned: 0");

            Scanner.startScanning();
            toggleScanButton.setText("Finish Scanning");
            statusLabel.setText("STATUS: SCANNING");
        } else {
            statusLabel.setText("STATUS: CHECKING IN..."); // while snipe-it works
            Scanner.stopScanning();

            String kitName = SnipeIt.findKitName(Collections.list(assetsListModel.elements()));
            SnipeIt.checkoutAssets(Collections.list(assetsListModel.elements()), locationsList.getSelectedValue().id, kitName);

            toggleScanButton.setText("Start Scanning");
            statusLabel.setText("STATUS: READY");
        }
    }
}
