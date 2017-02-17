import client.ConfigSettings;
import graph.AccessLevel;
import graph.Graph;
import graph.GraphGenDecorator;
import graph.GraphGenerator;

import org.omg.PortableInterceptor.ACTIVE;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.text.html.ImageView;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Created by lewis on 2/16/17.
 */
public class SettingsGUI {
    private final String TITLE = "UML Generator";
    private JFrame mainFrame;
    private String settingsPath;
    private AccessLevel origAccess;

    private class ImagePanel extends JPanel {
        private Image img;

        public ImagePanel(Image img) {
            this.img = img;
        }

        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(img, 0, 0, null);
        }
    }

    public SettingsGUI(String settingsPath) {
        this.settingsPath = settingsPath;
    }

    public void launch(Runnable goCommand) {
        mainFrame = new JFrame(TITLE);
        mainFrame.setLayout(new BorderLayout());
        mainFrame.setSize(1000, 800);
        JPanel controlPanel = new JPanel(new BorderLayout());
        JPanel listsPanel = new JPanel(new GridLayout());
        JPanel whiteListPanel = new JPanel(new BorderLayout());
        JPanel blackListPanel = new JPanel(new BorderLayout());
        JPanel checkboxPanel = new JPanel(new GridLayout());

        TextField inputTextField = new TextField("Enter class name here.");

        JList<String> whiteList = new JList<>(getWhiteList());
        JScrollPane wlScrollPane = new JScrollPane();
        wlScrollPane.getViewport().setView(whiteList);
        wlScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        JButton wlAddButton = new JButton("Add to whitelisted classes");
        wlAddButton.addActionListener(actionEvent -> {
            if (!ConfigSettings.getWhiteList().contains(inputTextField.getText().trim())) {
                ConfigSettings.addToWhiteList(inputTextField.getText().trim());
                whiteList.setListData(getWhiteList());
                whiteList.updateUI();
            }
        });

        JButton wlRemButton = new JButton("Remove");
        wlRemButton.addActionListener(actionEvent -> {
            ConfigSettings.removeFromWhiteList(whiteList.getSelectedValue());
            whiteList.setListData(getWhiteList());
            whiteList.updateUI();
        });

        whiteListPanel.add(wlAddButton, BorderLayout.NORTH);
        whiteListPanel.add(wlScrollPane, BorderLayout.CENTER);
        whiteListPanel.add(wlRemButton, BorderLayout.SOUTH);

        String[] stringBlackList = new String[ConfigSettings.getBlackList().size()];
        stringBlackList = ConfigSettings.getBlackList().toArray(stringBlackList);
        JList<String> blackList = new JList<>(stringBlackList);
        JScrollPane blScrollPane = new JScrollPane();
        blScrollPane.getViewport().setView(blackList);
        blScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        JButton blAddButton = new JButton("Add to blacklisted classes");
        blAddButton.addActionListener(actionEvent -> {
            if (!ConfigSettings.getBlackList().contains(inputTextField.getText().trim())) {
                ConfigSettings.addToBlackList(inputTextField.getText().trim());
                blackList.setListData(getBlackList());
                blackList.updateUI();
            }
        });

        JButton blRemButton = new JButton("Remove");
        blRemButton.addActionListener(actionEvent -> {
            ConfigSettings.removeFromBlackList(blackList.getSelectedValue());
            blackList.setListData(getBlackList());
            blackList.updateUI();
        });

        blackListPanel.add(blAddButton, BorderLayout.NORTH);
        blackListPanel.add(blScrollPane, BorderLayout.CENTER);
        blackListPanel.add(blRemButton, BorderLayout.SOUTH);

        listsPanel.add(whiteListPanel);
        listsPanel.add(blackListPanel);

        controlPanel.add(inputTextField, BorderLayout.SOUTH);
        controlPanel.add(checkboxPanel, BorderLayout.NORTH);

        AccessLevel[] visibilities = {AccessLevel.PRIVATE, AccessLevel.PROTECTED, AccessLevel.PUBLIC};
        JComboBox<AccessLevel> accessBox = new JComboBox<>(visibilities);
        origAccess = ConfigSettings.getAccessLevel();
        accessBox.setSelectedItem(origAccess);
        accessBox.setLightWeightPopupEnabled(false);
        checkboxPanel.add(accessBox);
        Checkbox recurseCB = new Checkbox("Render recursively");
        checkboxPanel.add(recurseCB);
        Checkbox syntheticCB = new Checkbox("Render synthetics");
        checkboxPanel.add(syntheticCB);

        JButton goButton = new JButton("GO!");
        goButton.addActionListener(actionEvent -> {
            AccessLevel newAccess = (AccessLevel) accessBox.getSelectedItem();
            if (newAccess != origAccess) {
                List<String> savedWL = new ArrayList<>();
                List<String> savedBL = new ArrayList<>();
                
                for (String entry : ConfigSettings.getBlackList()) {
                    savedBL.add(entry);
                }
                for (String entry : ConfigSettings.getWhiteList()) {
                    savedWL.add(entry);
                }
                
                byte[] file;
                try {
                    file = Files.readAllBytes(Paths.get(settingsPath));
                    String settingsString = new String(file);
                    if (settingsString.contains("access = ")) {
                        settingsString = settingsString.replaceAll("access = [^\\n]*\\n", "access = " + newAccess.toString().toLowerCase() + "\n");
                    } else {
                        settingsString += "access = " + newAccess.toString().toLowerCase() + "\n";
                    }
                    PrintWriter propFile = new PrintWriter(settingsPath);
                    propFile.print(settingsString);
                    propFile.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                
                try {
                    ConfigSettings.setupConfig(new String[] {("--settings=" + settingsPath)});
                } catch (Exception e) {
                    e.printStackTrace();
                }
                
                List<String> freshWL = ConfigSettings.getWhiteList();
                List<String> freshBL = ConfigSettings.getBlackList();
                
                ConfigSettings.removeFromBlackList(freshBL);
                ConfigSettings.addToBlackList(savedBL);
                ConfigSettings.removeFromWhiteList(freshWL);
                ConfigSettings.addToWhiteList(savedWL);
            }
            
            
            ConfigSettings.setIsRecursive(recurseCB.getState());
            ConfigSettings.setShowSynthetic(syntheticCB.getState());
            mainFrame.dispose();
            goCommand.run();
        });
        checkboxPanel.add(goButton);

        mainFrame.add(controlPanel, BorderLayout.NORTH);
        mainFrame.add(listsPanel, BorderLayout.CENTER);
        mainFrame.setVisible(true);
        mainFrame.setDefaultCloseOperation(mainFrame.EXIT_ON_CLOSE);
    }

    private String[] getBlackList() {
        String[] stringBlackList = new String[ConfigSettings.getBlackList().size()];
        return ConfigSettings.getBlackList().toArray(stringBlackList);
    }

    private String[] getWhiteList() {
        String[] stringWhiteList = new String[ConfigSettings.getWhiteList().size()];
        return ConfigSettings.getWhiteList().toArray(stringWhiteList);
    }

    public void showPicture(String imgPath) throws IOException {
        mainFrame = new JFrame(TITLE);
        mainFrame.add(new ImagePanel(ImageIO.read(ClassLoader.getSystemClassLoader().getResource(imgPath))));
    }
    
}
