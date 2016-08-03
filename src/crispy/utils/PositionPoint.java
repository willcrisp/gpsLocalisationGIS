package crispy.utils;

import org.openstreetmap.gui.jmapviewer.Coordinate;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Will Crisp
 */
public class PositionPoint {

    private Coordinate position;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd-mm-yyyy hh:mm:ss");
    private SimpleDateFormat aliveFormat = new SimpleDateFormat("mm:ss");
    private long createdTime;
    private PointClasses pointClass;
    private double distance;

    public PositionPoint(Coordinate position, PointClasses pointClass) {
        this.position = position;
        this.pointClass = pointClass;
        createdTime = System.currentTimeMillis();
    }

    public Coordinate getPosition() {
        return position;
    }

    public PointClasses getPointClass() {
        return pointClass;
    }

    public String getFormatedDate(){
        return dateFormat.format(createdTime);
    }

    public String getTimeAlive() {
        return aliveFormat.format(System.currentTimeMillis() - createdTime);
    }

    public Date getCreatedTime(){
        return new Date(createdTime);
    }
}
