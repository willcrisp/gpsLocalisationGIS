package crispy;


import crispy.utils.PointClasses;
import crispy.utils.PositionPoint;
import net.miginfocom.swing.MigLayout;
import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.MapMarkerDot;
import org.openstreetmap.gui.jmapviewer.MapPolygonImpl;
import org.openstreetmap.gui.jmapviewer.interfaces.MapMarker;


import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.*;
import java.util.List;

public class GuiMain {


    private JMapViewer frostGUI = new JMapViewer();
    private JPanel buttonPanel;
    private JFrame mainFrame;
    private JPanel mainPane;
    private HashMap<String, AgentEntity> agentEntities = new HashMap<>();


    public static void main(String[] args) {
        new GuiMain();
    }

    public GuiMain() {
        mainFrame = new JFrame("Frost");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(new Dimension(1600, 1200));
        mainPane = new JPanel(new MigLayout("fill", "[]", "[]"));

        mainPane.add(frostGUI, "w 100%, h 100%");
        buttonPanel = new JPanel(new MigLayout("top", "[]", "[]"));
        mainPane.add(buttonPanel, "grow");
        mainFrame.add(mainPane);
        mainFrame.setVisible(true);

        setUpFrostMap();
        new CommPortParser(this);


    }

    private void setUpFrostMap(){

        Coordinate home = new Coordinate(-32.005593,115.759885);
        frostGUI.setDisplayPosition(home,16);
        frostGUI.setZoomContolsVisible(false);
        frostGUI.addMouseListener(new NodeInfoListener());

    }

    public void agentDataArrived(AgentEntity arrivedAgentData){

        if(agentEntities.containsKey(arrivedAgentData.getCallSign())){
            AgentEntity existingAgentData = agentEntities.get(arrivedAgentData.getCallSign());

            existingAgentData.addPosition(new PositionPoint(arrivedAgentData.getCurrentPoint(), PointClasses.FRIENDLY));
            existingAgentData.addTarget(new PositionPoint(arrivedAgentData.getCurrentTarget(), PointClasses.HOSTILE)); ///don't hard code this

            existingAgentData.setCurrentPoint(arrivedAgentData.getCurrentPoint());
            existingAgentData.setCurrentTarget(arrivedAgentData.getCurrentTarget());

        } else{
            agentEntities.put(arrivedAgentData.getCallSign(), arrivedAgentData);
            addAgentButton(arrivedAgentData);
        }


    }

    public void showAllAgentPoints(AgentEntity agentToShow){

        boolean stickyMode = agentToShow.getStickyMode();
        MapMarkerDot latest = null;
        if(stickyMode) {
            if(agentToShow.getCurrentPoint() != null) {
                latest = new MapMarkerDot(agentToShow.getCurrentPoint().getLat(), agentToShow.getCurrentPoint().getLon());
                agentToShow.addPositionMapmarkers(setMapMarker(latest.getCoordinate(), true));
            }
        }
        for (int i = 0; i < agentToShow.getAllPositions().size(); i++) {
            agentToShow.addTargetMapmarkers(setMapMarker(agentToShow.getAllTargets().get(i).getPosition(), true));
            if(stickyMode) {
                agentToShow.addPositionMapmarkers(setMapMarker(agentToShow.getAllPositions().get(i).getPosition(), false));
                agentToShow.addTraceMapmarkers(setTrace(latest.getCoordinate(),agentToShow.getAllTargets().get(i).getPosition(), true));
            } else{
                agentToShow.addPositionMapmarkers(setMapMarker(agentToShow.getAllPositions().get(i).getPosition(), true));
                agentToShow.addTraceMapmarkers(setTrace(agentToShow.getAllPositions().get(i).getPosition(),agentToShow.getAllTargets().get(i).getPosition(), true));
            }
        }
    }

    public void stickModeRepaint(AgentEntity agentToShow){
        removeAllAgentPoints(agentToShow);
        showAllAgentPoints(agentToShow);
    }

    public void removeAllAgentPoints(AgentEntity agentToShow){

        for(MapMarkerDot mapMarker : agentToShow.getAllTargetMapMarkers()){
            frostGUI.removeMapMarker(mapMarker);
        }
        for(MapMarkerDot mapMarker : agentToShow.getAllPositionMapMarkers()){
            frostGUI.removeMapMarker(mapMarker);
        }
        for(MapPolygonImpl mapTrace: agentToShow.getAllTraceMapMarkers()){
            frostGUI.removeMapPolygon(mapTrace);
        }

    }


    public void centerOnPoint(Coordinate pointToCenterOn){
        frostGUI.setDisplayPosition(pointToCenterOn,frostGUI.getZoom());
    }

    private void addAgentButton(AgentEntity agentEntity){
        SwingUtilities.invokeLater(() -> {
            JButton agentButton = new JButton(agentEntity.getCallSign());
            buttonPanel.add(agentButton, "top, w 400px, h 40px, wrap");
            agentButton.addActionListener(new AgentSettings(agentEntity, this));
            mainPane.revalidate();
            mainPane.repaint();
        });
    }


    //Add type distinction
    private MapMarkerDot setMapMarker(Coordinate position, boolean draw) {
        MapMarkerDot mapMaker = new MapMarkerDot(Color.BLUE, position.getLat(), position.getLon());
        if(draw) {
            frostGUI.addMapMarker(mapMaker);
        }
        return mapMaker;
        //pointTimedRemoval(mapMaker);
    }


    private MapPolygonImpl setTrace(Coordinate position, Coordinate target, boolean draw) {
        java.util.List<Coordinate> route = new ArrayList<>(Arrays.asList(position, target, target));
        MapPolygonImpl pointTrace = new MapPolygonImpl(route);
        if(draw) {
            frostGUI.addMapPolygon(pointTrace);
        }
        return pointTrace;
        //lineTimedRemoval(pointTrace);
    }

    public class NodeInfoListener implements MouseListener {

        @Override
        public void mouseClicked(MouseEvent mouseEvent) {
            if(mouseEvent.getClickCount() == 1 && mouseEvent.getButton() == MouseEvent.BUTTON1){
                boolean displayedInfo = false;
                Point p = mouseEvent.getPoint();
                int X = p.x+3;
                int Y = p.y+3;
                List<MapMarker> ar = frostGUI.getMapMarkerList();
                for (MapMarker mapMarker : ar) {

                    Point MarkerPosition = frostGUI.getMapPosition(mapMarker.getLat(), mapMarker.getLon());
                    if (MarkerPosition != null) {

                        int centerX = MarkerPosition.x;
                        int centerY = MarkerPosition.y;

                        // calculate the radius from the touch to the center of the dot
                        double radCircle = Math.sqrt((((centerX - X) * (centerX - X)) + (centerY - Y) * (centerY - Y)));

                        // if the radius is smaller then 23 (radius of a ball is 5), then it must be on the dot
                        if (radCircle < 8) {
                            for(AgentEntity entity : agentEntities.values()){
                                if(!displayedInfo) {
                                    for (PositionPoint selectedPoint : entity.getAllTargets()) {
                                        if (selectedPoint.getPosition().getLat() == mapMarker.getLat() && selectedPoint.getPosition().getLon() == mapMarker.getLon()) {
                                            JOptionPane.showMessageDialog(mainPane,
                                                    "Date Created: " + selectedPoint.getFormatedDate() + "\n" +
                                                    "Time since creation: " + selectedPoint.getTimeAlive() + " \n" +
                                                    "Lat: " + selectedPoint.getPosition().getLat() + " \n" +
                                                    "Long: " + selectedPoint.getPosition().getLon() + " \n" +
                                                    "Distance to Point (m): " + Math.round(distance(selectedPoint.getPosition().getLat(),selectedPoint.getPosition().getLon(),
                                                                                entity.getCurrentPoint().getLat(),entity.getCurrentPoint().getLon(), 0.0,0.0)),
                                                    entity.getCallSign() + " Point",
                                                    JOptionPane.INFORMATION_MESSAGE);
                                            displayedInfo = true;
                                            break;
                                        }
                                    }
                                    for (PositionPoint selectedPoint : entity.getAllPositions()) {
                                        if (selectedPoint.getPosition().getLat() == mapMarker.getLat() && selectedPoint.getPosition().getLon() == mapMarker.getLon()) {
                                            JOptionPane.showMessageDialog(mainPane,
                                                    "Date Created: " + selectedPoint.getFormatedDate() + "\n" +
                                                    "Time since creation: " + selectedPoint.getTimeAlive() + " \n" +
                                                    "Lat: " + selectedPoint.getPosition().getLat() + " \n" +
                                                    "Long: " + selectedPoint.getPosition().getLon(),
                                                    entity.getCallSign() + " Point",
                                                    JOptionPane.INFORMATION_MESSAGE);
                                            displayedInfo = true;
                                            break;
                                        }
                                    }
                                }
                            }
                        }

                    }
                }
            }
        }

        @Override
        public void mousePressed(MouseEvent mouseEvent) {
            //Do nothing
        }

        @Override
        public void mouseReleased(MouseEvent mouseEvent) {
//Do nothing
        }

        @Override
        public void mouseEntered(MouseEvent mouseEvent) {
//Do nothing
        }

        @Override
        public void mouseExited(MouseEvent mouseEvent) {
//Do nothing
        }

        /*
         * Calculate distance between two points in latitude and longitude taking
         * into account height difference. If you are not interested in height
         * difference pass 0.0. Uses Haversine method as its base.
         *
         * lat1, lon1 Start point lat2, lon2 End point el1 Start altitude in meters
         * el2 End altitude in meters
         * @returns Distance in Meters
         */
        public double distance(double lat1,  double lon1, double lat2,
                                      double lon2, double el1, double el2) {

            final int R = 6371; // Radius of the earth

            Double latDistance = Math.toRadians(lat2 - lat1);
            Double lonDistance = Math.toRadians(lon2 - lon1);
            Double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                    + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                    * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
            Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
            double distance = R * c * 1000; // convert to meters

            double height = el1 - el2;

            distance = Math.pow(distance, 2) + Math.pow(height, 2);

            return Math.sqrt(distance);
        }
    }


    private void pointTimedRemoval(MapMarkerDot pointToRemove){
        new Thread(() -> {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            frostGUI.removeMapMarker(pointToRemove);
        }).start();
    }
    private void lineTimedRemoval(MapPolygonImpl lineToRemove){
        new Thread(() -> {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            frostGUI.removeMapPolygon(lineToRemove);
        }).start();
    }
}
