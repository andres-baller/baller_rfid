import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.basic.BasicListUI;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Application extends JFrame {

    private JButton button1;
    private JTextField textField1;

    private Application() {
        initUI();
    }

    private final int WIDTH = 800;
    private final int HEIGHT = 600;

    private static List<AssetLocation> locations = new ArrayList<>();

    private void initUI() {
        // Create main window
        this.setTitle("Asset Checkout Manager");
        this.setSize(WIDTH, HEIGHT);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Set absolute positioning
        this.setLayout(null);

        //
        // Locations Panel
        //

        JLabel locationsLabel = new JLabel("Select Checkout Location");
        JLabel currentLocationLabel = new JLabel("Current checkout location: ");
        JPanel locationsPanel = new JPanel();
        DefaultListModel<AssetLocation> locationsListModel = new DefaultListModel<>();

        // Label
        locationsLabel.setBounds(32, 48, 300, 16);
        this.add(locationsLabel);

        // Panel
        locationsPanel.setBounds(32, 64, 300, 450);
        this.add(locationsPanel);

        // Create list model for AssetLocations
        for(AssetLocation loc : locations) {
            locationsListModel.addElement(loc);
        }

        // Refresh button
        JButton refreshLocationsbutton = new JButton(new ImageIcon("../images/refresh.png"));
        refreshLocationsbutton.setBounds(300, 48,24, 24);
        refreshLocationsbutton.setBackground(Color.black);
        refreshLocationsbutton.addActionListener((ActionEvent e) -> {
            System.out.println("asd");
            locationsListModel.clear();

            locations = SnipeIt.fetchLocations();
            for(AssetLocation loc : locations)
                locationsListModel.addElement(loc);

        });
        this.add(refreshLocationsbutton);

        JList<AssetLocation> locationsList = new JList<>(locationsListModel);
        locationsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        locationsList.setVisibleRowCount(-1);
        locationsList.addListSelectionListener((ListSelectionEvent e) ->
                currentLocationLabel.setText("Current checkout location: " + locationsList.getSelectedValue().name)
        );


        JScrollPane locationsScrollPane = new JScrollPane(locationsList);
        locationsScrollPane.setPreferredSize(new Dimension(300, 442));

        locationsPanel.add(locationsScrollPane);

        // Current location label
        currentLocationLabel.setBounds(32, 525, 300, 32);
        this.add(currentLocationLabel);

        //
        // Scanned Assets Panel
        //

        JLabel assetsLabel = new JLabel("Scanned Assets");
        assetsLabel.setBounds(400, 48, 300, 16);
        this.add(assetsLabel);

        JLabel assetsCountLabel = new JLabel("Assets Scanned: ");
        assetsCountLabel.setBounds(400, 500, 300, 32);
        this.add(assetsCountLabel);

        JPanel assetsPanel = new JPanel();
        assetsPanel.setBounds(400, 64, 300, 400);
        this.add(assetsPanel);

        // Create empty list model for AssetLocations
        // TODO: Change to use Assets instead of strings
        DefaultListModel<String> assetsListModel = new DefaultListModel<>();
        assetsListModel.addListDataListener(new ListDataListener() {
            @Override
            public void intervalAdded(ListDataEvent e) {
                assetsCountLabel.setText("Assets Scanned: " + assetsListModel.size());
            }

            @Override public void intervalRemoved(ListDataEvent e) { }
            @Override public void contentsChanged(ListDataEvent e) { }
        });

        JList<String> assetsList = new JList<>(assetsListModel);
        assetsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        assetsList.setVisibleRowCount(-1);

        // Connect assets list model to scanner to receive scan updates.
        Scanner.bindReader(assetsListModel);

        JScrollPane assetsScrollPane = new JScrollPane(assetsList);
        assetsScrollPane.setPreferredSize(new Dimension(300, 400 - 8));

        assetsPanel.add(assetsScrollPane);










        // Create toggle scan button
        JButton toggleScanButton = new JButton("Start Scanning");
        toggleScanButton.setBounds(WIDTH / 2 - 256 / 2, HEIGHT - 80, 208, 48);

        toggleScanButton.addActionListener((ActionEvent e) -> {
            String locationId = (locationsList.isSelectionEmpty()) ? null : locationsList.getSelectedValue().id;

            if (e.getActionCommand().equals("Start Scanning")) {
                if (locationId == null) {
                    JOptionPane.showMessageDialog(this,"Please select a checkout location first.");
                    return;
                }

                System.out.println("start scanning");
                Scanner.startScanning();
                toggleScanButton.setText("Finish Scanning");
            } else {
                // TODO: check in/out all scanned assets

                //SnipeIt.checkinAssetsWithTags(Collections.list(assetsListModel.elements()));
                //SnipeIt.checkoutAssets(Collections.list(assetsListModel.elements()), locationId);

                toggleScanButton.setText("Start Scanning");
            }
        });

        this.add(toggleScanButton);

        this.setVisible(true);
    }

    public static void main(String[] args) {
        locations = SnipeIt.fetchLocations();

        EventQueue.invokeLater(() -> {
            Application app = new Application();
        });

    }
}