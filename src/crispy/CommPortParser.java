package crispy;

import crispy.utils.PointClasses;
import crispy.utils.PositionPoint;
import jssc.SerialPort;
import jssc.SerialPortException;
import org.openstreetmap.gui.jmapviewer.Coordinate;
import java.io.UnsupportedEncodingException;

public class CommPortParser {

    public CommPortParser(GuiMain mapInterface) {

        mapInterface.agentDataArrived(fakeData("Crisp"));
        mapInterface.agentDataArrived(fakeBarryData("Barry"));

        SerialPort serialPort = new SerialPort("COM9");
        try {
            System.out.println("Port opened: " + serialPort.openPort());
            System.out.println("Params setted: " + serialPort.setParams(9600, 8, 1, 0));
            System.out.println("Lets read");
            new Thread(() -> {
                while(true) {
                    try {
                        byte test[] = serialPort.readBytes();
                        if (test != null) {

                            String serialMsg = new String(test, "UTF8");
                            System.out.println(serialMsg);
                            String parsed[] = serialMsg.split(",");
                            AgentEntity arrivedEntity = new AgentEntity(parsed[0]);

                            arrivedEntity.addPosition(new PositionPoint(new Coordinate(Double.parseDouble(parsed[2]),Double.parseDouble(parsed[3])), PointClasses.HOSTILE));
                            arrivedEntity.setCurrentPoint(new Coordinate(Double.parseDouble(parsed[2]),Double.parseDouble(parsed[3])));

                            arrivedEntity.addTarget(new PositionPoint(new Coordinate(Double.parseDouble(parsed[5]),Double.parseDouble(parsed[6])),PointClasses.HOSTILE));
                            arrivedEntity.setCurrentTarget(new Coordinate(Double.parseDouble(parsed[5]),Double.parseDouble(parsed[6])));

                            mapInterface.agentDataArrived(arrivedEntity);
                        } else{
                            Thread.sleep(100);
                        }
                    } catch (UnsupportedEncodingException | SerialPortException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
        catch (SerialPortException ex){
            System.out.println(ex);
        }
    }

    private AgentEntity fakeData(String name){
        AgentEntity entity = new AgentEntity(name);
        entity.addPosition(new PositionPoint(new Coordinate(-32.005736,115.759885), PointClasses.HOSTILE));
        entity.addTarget(new PositionPoint(new Coordinate(-32.004517,115.75931), PointClasses.HOSTILE));

        entity.addPosition(new PositionPoint(new Coordinate(-32.005736,115.759785), PointClasses.HOSTILE));
        entity.addTarget(new PositionPoint(new Coordinate(-32.004517,115.75831), PointClasses.HOSTILE));

        entity.addPosition(new PositionPoint(new Coordinate( -32.005736,115.759685), PointClasses.HOSTILE));
        entity.addTarget(new PositionPoint(new Coordinate(-32.004517,115.75731), PointClasses.HOSTILE));

        entity.addPosition(new PositionPoint(new Coordinate(-32.005736,115.759585), PointClasses.HOSTILE));
        entity.addTarget(new PositionPoint(new Coordinate(-32.004517,115.75631), PointClasses.HOSTILE));

        entity.setCurrentPoint(entity.getAllPositions().get(0).getPosition());
        entity.setCurrentTarget(entity.getAllTargets().get(0).getPosition());

        return entity;
    }

    private AgentEntity fakeBarryData(String name){
        AgentEntity entity = new AgentEntity(name);
        entity.addPosition(new PositionPoint(new Coordinate(-32.005536,115.760020), PointClasses.HOSTILE));
        entity.addTarget(new PositionPoint(new Coordinate(-32.004517,115.760300), PointClasses.HOSTILE));

        entity.addPosition(new PositionPoint(new Coordinate(-32.005436,115.760200), PointClasses.HOSTILE));
        entity.addTarget(new PositionPoint(new Coordinate(-32.004417,115.760400), PointClasses.HOSTILE));

        entity.addPosition(new PositionPoint(new Coordinate( -32.005336,115.760500), PointClasses.HOSTILE));
        entity.addTarget(new PositionPoint(new Coordinate(-32.004317,115.760600), PointClasses.HOSTILE));

        entity.addPosition(new PositionPoint(new Coordinate(-32.005236,115.760700), PointClasses.HOSTILE));
        entity.addTarget(new PositionPoint(new Coordinate(-32.002517,115.760890), PointClasses.HOSTILE));

        entity.setCurrentPoint(entity.getAllPositions().get(0).getPosition());
        entity.setCurrentTarget(entity.getAllTargets().get(0).getPosition());

        return entity;
    }

}