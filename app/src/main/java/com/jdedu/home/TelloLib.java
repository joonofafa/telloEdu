
package com.jdedu.home;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;

public class TelloLib {
     
    public enum TelloCommand {
		CMD_SDK_MODE				("command"),
		CMD_TAKE_OFF				("takeoff"),
		CMD_LAND					("land"),
		CMD_STREAM_ON				("streamon"),
		CMD_STREAM_OFF				("streamoff"),
		CMD_EMERGENCY				("emergency"),
		CMD_UP						("up", 		"20~500"),	//@Parameter: x
		CMD_DOWN					("down", 	    "20~500"),	//@Parameter: x
		CMD_LEFT					("left",	    "20~500"),	//@Parameter: x
		CMD_RIGHT					("right",	    "20~500"),	//@Parameter: x
		CMD_FORWARD					("forward",	"20~500"),	//@Parameter: x
		CMD_BACK					("back",	    "20~500"),	//@Parameter: x
		CMD_CLOCKWISE				("cw",		    "20~500"),	//@Parameter: x
		CMD_COUNTER_CLOCKWISE		("ccw",		"1~360"),	//@Parameter: x
		CMD_FLIP					("flip", 	    "l,r,f,b"),	//@Parameter: x
		CMD_GO						("go", 		"-500~500", "-500~500", "-500~500", "10~100", "m1,m2,m3,m4,m5,m6,m7,m8"),	 //@Parameter: x, y, z, speed, mid
		CMD_CURVE					("curve", 	    "-500~500", "-500~500", "-500~500", "10~60", "m1,m2,m3,m4,m5,m6,m7,m8"),	 //@Parameter: x, y, z, speed, mid
		CMD_JUMP					("jump", 	    "-500~500", "-500~500", "-500~500", "10~60", "1~360", "m1,m2,m3,m4,m5,m6,m7,m8"),//@Parameter: x, y, z, speed, yaw, mid
		CMD_SPEED					("speed",	    "10~100"),
		
		STATUS_SPEED				("speed?"),
		STATUS_BETTERY				("bettery?"),
		STATUS_FLYING_TIME			("time?"),
		STATUS_WIFI_SNR				("wifi?"),
		STATUS_SDK					("sdk?"),
		STATUS_SERIAL_NUMBER		("sn?"),
		;
		
		private final String m_strCommandPrefix;
		private final String[] m_strParams;
			
		TelloCommand(String strCommand) {
		    m_strCommandPrefix = strCommand;
		    m_strParams = null;
		}
		
		TelloCommand(String strCommand, String... strParams) {
		    m_strCommandPrefix = strCommand;
		    m_strParams = strParams;
		}
		
		private boolean verifyingDataWithFormat(int iIndex, String strParamData) {

		    if (m_strParams == null) {
		    	return true;
		    }
		    
		    if (m_strParams.length <= iIndex) {
		    	return true;
		    }
		    
		    try {
				if (m_strParams[iIndex].contains("~")) {		//Integer bounds
				    String[] saBounds = m_strParams[iIndex].split("~");
				    Integer iParamValue = Integer.parseInt(strParamData);
				    
				    System.out.print(String.format("[0] %s", saBounds[0]));
				    System.out.print(String.format("[1] %s,", saBounds[1]));
				    if (Integer.parseInt(saBounds[0]) <= iParamValue || 
					    iParamValue <= Integer.parseInt(saBounds[1])) {
				    	return true;
				    } else {
				    	return false;
				    }
				} else if (m_strParams[iIndex].contains(",")) {		//Definition bounds
				    String[] saBounds = m_strParams[iIndex].split(",");
				    
				    for (String strItem : saBounds) {
						System.out.print(String.format("%s,", strItem));
						if (!strParamData.toLowerCase().equals(strItem.toLowerCase())) {
						    return false;
						}
				    }
				    return true;
				} else {
				    return false;
				}
		    }
		    catch (Exception e) {
			
		    }
		    return false;
		}
		
		public byte[] getCommand(String... strParams) { 
		    StringBuilder sbTemp = new StringBuilder();
		    
		    sbTemp.append(m_strCommandPrefix);
		    if (strParams != null) {
				for (int i = 0; i < strParams.length; i++) {
					    sbTemp.append(" ");
					    if (verifyingDataWithFormat(i, strParams[i])) {
						sbTemp.append(strParams[i]);
				    }
				}
		    }
		    
		    System.out.println(String.format("COMMAND [%s]", sbTemp.toString()));
		    return sbTemp.toString().getBytes(); 
		}
    }
    
    public enum TelloResponse {
		RES_OK					("ok"),
		RES_ERROR				("error"),
		RES_NOT_CONNECTED_YET,
		RES_INVALID_CONNECTION,
		RES_FAILED_TO_SEND_A_COMMAND,
		;
		
		private final String m_strPrefix;
		
		TelloResponse() { 
		    m_strPrefix = null;
		}
		
		TelloResponse(String strPrefix) { 
		    m_strPrefix = strPrefix;
		}
		
		public String getPrefix()	{ return m_strPrefix; }
    }

    public enum TelloHandlerResult {
    	RESULT_NOT_CONNECTED_YET,
		RESULT_INVALID_CONNECTION,
		RESULT_FAILED_TO_SEND_A_COMMAND
	}

    public class TelloStatus {

	}

	public interface TelloHandler {
    	void onConnection();
    	void onDisconnection();
		TelloStatus onStatus();
	}

    private final int DEFAULT_TIMEOUT_WITH_DRONE = 100;
    
    private DatagramSocket m_dsSender;
    private DatagramSocket m_dsReceiver;
    private DatagramSocket m_dsVideoStreamer;
    
    public TelloLib() {
		m_dsSender = null;
		m_dsReceiver = null;
    }
    
    public boolean connectToDrone() {
		try {
		    m_dsSender = new DatagramSocket();
		    m_dsSender.connect(InetAddress.getByName("192.168.10.1"), 8889);
		    
		    m_dsReceiver = new DatagramSocket(8890);
			m_dsVideoStreamer = new DatagramSocket(11111);
		    
		    if (m_dsSender.isConnected()) {
		    	if (sendCommand(TelloCommand.CMD_SDK_MODE) == TelloResponse.RES_OK) {
		    		return true;
		    	}
		    } else {
		    	TelloUtil.DLog("* Failed to connect to drone!");
		    }
		} catch (Exception e) {
		    e.printStackTrace();
		}

		return false;
    }

    public void disconnectWithDrone() {

	try {
	    m_dsSender.disconnect();
		} catch (Exception e) {
		    e.printStackTrace();
		}
    }

    public TelloResponse sendCommand(TelloCommand emCmd, String...strArgs) {
    	return sendCommand(emCmd, true, strArgs);
    }

    public TelloResponse sendCommand(TelloCommand emCmd, boolean isCheckingResponse, String...strArgs) {
        TelloResponse trResult = TelloResponse.RES_ERROR;
        byte[] baCommand;

        if (m_dsSender != null) {
            if (m_dsSender.isConnected()) {
                if (emCmd != null) {
                    try {
                        baCommand = emCmd.getCommand(strArgs);

                        TelloUtil.DBuffer("* [SENT]", baCommand,  baCommand.length);
                        m_dsSender.send(new DatagramPacket(baCommand, baCommand.length));
                        if (isCheckingResponse) {
                            byte[] baResponse = new byte[128];
							m_dsSender.setSoTimeout(DEFAULT_TIMEOUT_WITH_DRONE);
							m_dsSender.receive(new DatagramPacket(baResponse, baResponse.length));

                            TelloUtil.DBuffer("* [RECEIVED]", baResponse,  baResponse.length);
                        } else {
                            trResult = TelloResponse.RES_OK;
                        }
                    } catch (IOException e) {
						e.printStackTrace();
						trResult = TelloResponse.RES_FAILED_TO_SEND_A_COMMAND;
                    }
                }
            } else {
                trResult = TelloResponse.RES_INVALID_CONNECTION;
            }
        } else {
            trResult = TelloResponse.RES_NOT_CONNECTED_YET;
        }
        return trResult;
    }
}
