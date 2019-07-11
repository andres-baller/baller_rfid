import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.awt.event.ActionEvent;
import java.util.Collections;

public class AssetCheckin {

    private JPanel mainPanel;
    private JList assetsList;
    private DefaultListModel<String> assetsListModel;
    private JButton toggleButton;
    private JLabel assetsCountLabel;
    private JLabel statusLabel;

    public AssetCheckin() {
        toggleButton.addActionListener(this::toggleScanning);

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
    }

    private void toggleScanning(ActionEvent e) {
        if (e.getActionCommand().equals("Start Scanning")) {
            assetsListModel.clear();
            assetsCountLabel.setText("Assets Scanned: 0");

            Scanner.startScanning();
            toggleButton.setText("Finish Scanning");
            statusLabel.setText("STATUS: SCANNING");
        } else {
            statusLabel.setText("STATUS: CHECKING IN..."); // while snipe-it works
            Scanner.stopScanning();

            String kitName = SnipeIt.findKitName(Collections.list(assetsListModel.elements()));
            System.out.println("Kit ID: " + kitName);
            SnipeIt.checkinAssets(Collections.list(assetsListModel.elements()), null);

            toggleButton.setText("Start Scanning");
            statusLabel.setText("STATUS: READY");
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Asset Checkin Manager");
        frame.setContentPane(new AssetCheckin().mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

    }

}
