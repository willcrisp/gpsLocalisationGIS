package crispy;

import crispy.utils.PositionPoint;
import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.MapMarkerDot;
import org.openstreetmap.gui.jmapviewer.MapPolygonImpl;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Will Crisp
 */
public class AgentEntity {

    private Coordinate currentPoint;
    private Coordinate currentTarget;
    private String callSign;
    private List<PositionPoint> allPositions = new ArrayList<>();
    private List<PositionPoint> allTargets = new ArrayList<>();
    private List<MapMarkerDot> allPositionMapMarkers = new ArrayList<>();
    private List<MapMarkerDot> allTargetMapMarkers = new ArrayList<>();
    private List<MapPolygonImpl> allTraceMapMarkers = new ArrayList<>();

    private boolean stickyMode = false;

    public boolean getStickyMode(){
        return stickyMode;
    }

    public void setStickyMode(boolean stickyOn){

        stickyMode = stickyOn;
    }

    public List<MapMarkerDot> getAllPositionMapMarkers() {
        return allPositionMapMarkers;
    }

    public void addPositionMapmarkers(MapMarkerDot positionMapmarker) {
        allPositionMapMarkers.add(positionMapmarker);
    }

    public List<MapMarkerDot> getAllTargetMapMarkers() {
        return allTargetMapMarkers;
    }

    public void addTargetMapmarkers(MapMarkerDot targetMapmarker) {
        allTargetMapMarkers.add(targetMapmarker);
    }

    public List<MapPolygonImpl> getAllTraceMapMarkers() {
        return allTraceMapMarkers;
    }

    public void addTraceMapmarkers(MapPolygonImpl traceMapmarker) {
        allTraceMapMarkers.add(traceMapmarker);
    }

    public AgentEntity(String callSign) {
        this.callSign = callSign;
    }

    public void setCurrentPoint(Coordinate currentPoint) {
        this.currentPoint = currentPoint;
    }

    public void setCurrentTarget(Coordinate currentTarget) {
        this.currentTarget = currentTarget;
    }

    public void setCallSign(String callSign) {
        this.callSign = callSign;
    }

    public void addPosition(PositionPoint newPosition) {
        allPositions.add(newPosition);
    }

    public void addTarget(PositionPoint newTarget) {
      allTargets.add(newTarget);
    }

    public void removePosition(Coordinate newPosition) {
        allPositions.remove(newPosition);
    }

    public void removeTarget(Coordinate newTarget) {
        allTargets.remove(newTarget);
    }

    public Coordinate getCurrentPoint() {
        return currentPoint;
    }

    public Coordinate getCurrentTarget() {
        return currentTarget;
    }

    public String getCallSign() {
        return callSign;
    }

    public List<PositionPoint> getAllPositions() {
        return allPositions;
    }

    public List<PositionPoint> getAllTargets() {
        return allTargets;
    }

    public void purgeAgentPoints(){
        allPositions.clear();
        allTargets.clear();
        allPositionMapMarkers.clear();
        allTargetMapMarkers.clear();
        allTraceMapMarkers.clear();
        setCurrentPoint(null);
        setCurrentTarget(null);
    }
}
