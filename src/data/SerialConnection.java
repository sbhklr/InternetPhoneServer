package data;
import processing.core.PApplet;
import processing.serial.Serial;

public class SerialConnection {
	
	private static final int DEFAULT_BAUDRATE = 19200;
	private Serial serialPort;
    private StringBuffer serialDataBuffer = new StringBuffer();
	
    public SerialConnection(PApplet applet) {
    	this(applet, DEFAULT_BAUDRATE);
	}
    
	public SerialConnection(PApplet applet, int baudRate) {
		serialPort = new Serial(applet, getSerialPort(), baudRate);
		serialPort.clear();
	}
	
	public String readData(){
		while (serialPort != null && serialPort.available() > 0) {
            char currentChar = serialPort.readChar();
            
            if (currentChar == '\n') {
                String data = serialDataBuffer.toString();				
                serialDataBuffer = new StringBuffer();                
                return data;
            } else {
            	serialDataBuffer.append(currentChar);
            }
        }
		return null;
	}
	
	public void writeData(String data){
		serialPort.write(data);
	}
	
	private String getSerialPort(){
    	for (String port : Serial.list()) {
			if(port.contains("usbmodem")) return port;
		}
    	return null;
    }
}
