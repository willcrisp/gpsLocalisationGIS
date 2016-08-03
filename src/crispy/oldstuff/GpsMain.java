package crispy.oldstuff;

/**
 * Created by willc on 23-Feb-16.
 */
public class GpsMain {

    //***********************     New Altitude stuff
    private static double originAlt = 5000;

    private static double pointAlt;
    private static double pointaltDelta;

    private static double inclineAngle = 39;



    //***********************

    //***********************    Gps Stuff
    private static double EarthRadius = 6378.1;

    /**
     *  Degrees
     */
    private static double pointBearing = Math.toRadians(90);

    /**
     * KM
     */
    private static double pointDistance = 30;
    private static double straightDistance=0;

    private static double pointLat;
    private static double pointLong;
    private static double originLat = Math.toRadians(-31.989677);
    private static double originLong = Math.toRadians(115.752601);
    //***********************

    public static void main(String[] args){

        //calcNewGPSPoint();
        //straightlineDistance();
        calcNewGpsAlt();
    }


    /**
     * Calculates a new GPS point given current location a bearing and distance.
     */
    private static void calcNewGPSPoint(){

        pointLat = Math.asin(Math.sin(originLat)*Math.cos(pointDistance/EarthRadius) +
                Math.cos(originLat)*Math.sin(pointDistance/EarthRadius)*Math.cos(pointBearing));

        pointLong = originLong + Math.atan2(Math.sin(pointBearing)*Math.sin(pointDistance/EarthRadius)*Math.cos(pointLat),
                Math.cos(pointDistance/EarthRadius)-Math.sin(originLat)*Math.sin(pointLat));

        pointLat = Math.toDegrees(pointLat);
        pointLong = Math.toDegrees(pointLong);

        System.out.println("New GPS Lat:" + pointLat + " Long:" + pointLong);

    }

    private static void calcNewGpsAlt(){

        straightlineDistance(6.40312);
        pointAlt = Math.sqrt(Math.pow(6.40312,2) - Math.pow(straightDistance,2));
        //Check value signage here
        pointAlt += originAlt;
        System.out.println("Point Altitude: " + pointAlt);
        System.out.println("Origin/Point Delta: " + pointAlt);

    }

    /**
     * Calculates the straight line distance to new gps point as if flat
     * NOTE: given small distance i am not compensating for curve of earth
     */
    private static void straightlineDistance(double distance){
        straightDistance = Math.sin(Math.toRadians(inclineAngle)) * distance;
        System.out.println("Straight line distance: " + straightDistance);
    }
}
