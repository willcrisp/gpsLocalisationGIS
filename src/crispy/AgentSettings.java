package crispy;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Will Crisp
 */
public class AgentSettings implements ActionListener {

    private static final Dimension BUTTON_SIZE = new Dimension(140,30);
    private JFrame settingsFrame = new JFrame();
    private JPanel settingsPanel = new JPanel();
    private AgentEntity agentSetting;
    private GuiMain mainReference;
    private JButton lastKnown = new JButton("Center last known");
    private JButton pointsToggle = new JButton("Hide Geodata");
    private JButton deletePoints = new JButton("Delete all Geodata");
    private JLabel numberOfGeopoints = new JLabel("0");
    private JCheckBox stickModeCheckbox = new JCheckBox();
    private boolean pointButtonCurrentlyHide = true;

    public AgentSettings(AgentEntity agentEntity, GuiMain guiMain) {

        mainReference = guiMain;
        agentSetting = agentEntity;

        settingsFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        settingsFrame.setSize(new Dimension(400, 400));
        settingsPanel = new JPanel(new MigLayout("", "[]", "[][]"));
        settingsFrame.setLocationRelativeTo(null);
        settingsFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        settingsFrame.setTitle(agentEntity.getCallSign() + " settings");
        initPanel();
        initListeners();
        settingsFrame.add(settingsPanel);


    }

    private void initPanel(){

        lastKnown.setPreferredSize(BUTTON_SIZE);
        pointsToggle.setPreferredSize(BUTTON_SIZE);
        deletePoints.setPreferredSize(BUTTON_SIZE);
        numberOfGeopoints.setText("" + agentSetting.getAllPositions().size());

        //settingsPanel.add(new JLabel("Point decay time (0 = infinity): "));
       //settingsPanel.add(new JSlider(), "wrap");
        settingsPanel.add(new JLabel("Sticky mode on: "),"w 300");
        settingsPanel.add(stickModeCheckbox, "center, wrap");
        settingsPanel.add(new JLabel("Number of geopoint pairs: "), "w 300");
        settingsPanel.add(numberOfGeopoints, "center, wrap");
        settingsPanel.add(lastKnown, "center, skip 1, wrap");
        settingsPanel.add(pointsToggle, "center, skip 1, wrap");
        settingsPanel.add(deletePoints, "center, skip 1, wrap");

    }

    private void initListeners(){
        pointsToggle.addActionListener(actionEvent -> {
            if(pointButtonCurrentlyHide){
                pointsToggle.setText("Show Geodata");
                mainReference.removeAllAgentPoints(agentSetting);
            } else{
                pointsToggle.setText("Hide Geodata");
                mainReference.showAllAgentPoints(agentSetting);
            }
            settingsFrame.repaint();
            pointButtonCurrentlyHide = !pointButtonCurrentlyHide;
        });

        stickModeCheckbox.addActionListener(actionEvent -> {
            agentSetting.setStickyMode(stickModeCheckbox.isSelected());
            mainReference.stickModeRepaint(agentSetting);
        });
        deletePoints.addActionListener(actionEvent -> {
            mainReference.removeAllAgentPoints(agentSetting);
            agentSetting.purgeAgentPoints();
            numberOfGeopoints.setText("" + agentSetting.getAllPositions().size());
        });

        lastKnown.addActionListener(actionEvent -> mainReference.centerOnPoint(agentSetting.getCurrentPoint()));

    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        settingsFrame.setVisible(true);
    }
}
