package trasmapi.sumo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;


import trasmapi.genAPI.Lane;
import trasmapi.genAPI.Link;
import trasmapi.genAPI.TrafficLight;
import trasmapi.genAPI.exceptions.UnimplementedMethod;
import trasmapi.genAPI.exceptions.WrongCommand;
import trasmapi.sumo.protocol.*;

public class SumoTrafficLight extends TrafficLight {

	private int p = 0;
	public int[] pTime = {31,4,6,4,31,4,6,4};
	
	String state;
	private ArrayList<String> controlledLanes;
	private ControlledLinks controlledLinks;
	
	public SumoTrafficLight(String id) {
		super(id);
	}

	/**	 
	 * @return ArrayList<String> - all Traffic Lights ids
	 */
	public static ArrayList<String> getIdList(){

		Command cmd = new Command(Constants.CMD_GET_TL_VARIABLE);

		Content cnt = new Content(Constants.ID_LIST, "dummy");

		cmd.setContent(cnt);

		//cmd.print("Command getAllVeh");

		RequestMessage reqMsg = new RequestMessage();
		reqMsg.addCommand(cmd);

		try {

			ResponseMessage rspMsg = SumoCom.query(reqMsg);
			Content content = rspMsg.validate((byte)Constants.CMD_GET_TL_VARIABLE,  (byte)Constants.RESPONSE_GET_TL_VARIABLE,
					(byte)Constants.ID_LIST,  (byte)Constants.TYPE_STRINGLIST);

			return content.getStringList();

		} catch (IOException e) {
			e.printStackTrace();
		} catch (WrongCommand e) {
			e.printStackTrace();
		}

		return null;
	}

	public String getState() {

		Command cmd = new Command(Constants.CMD_GET_TL_VARIABLE);
		Content cnt = new Content(Constants.TL_RED_YELLOW_GREEN_STATE,id);

		cmd.setContent(cnt);

		RequestMessage reqMsg = new RequestMessage();
		reqMsg.addCommand(cmd);


		try {

			ResponseMessage rspMsg = SumoCom.query(reqMsg);
			Content content = rspMsg.validate( (byte)  Constants.CMD_GET_TL_VARIABLE, (byte)  Constants.RESPONSE_GET_TL_VARIABLE,
					(byte)  Constants.TL_RED_YELLOW_GREEN_STATE, (byte)  Constants.TYPE_STRING);

			state = content.getString();

			return state;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (WrongCommand e) {
			e.printStackTrace();
		}

		return state;
	}

	public int getCurrentPhaseDuration(){

		Command cmd = new Command(Constants.CMD_GET_TL_VARIABLE);
		Content cnt = new Content(Constants.TL_PHASE_DURATION,id);

		cmd.setContent(cnt);

		RequestMessage reqMsg = new RequestMessage();
		reqMsg.addCommand(cmd);


		try {

			ResponseMessage rspMsg = SumoCom.query(reqMsg);
			Content content = rspMsg.validate( (byte)  Constants.CMD_GET_TL_VARIABLE, (byte)  Constants.RESPONSE_GET_TL_VARIABLE,
					(byte)  Constants.TL_PHASE_DURATION, (byte)  Constants.TYPE_INTEGER);

			return content.getInteger();
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (WrongCommand e) {
			e.printStackTrace();
		}

		return -1;
	}
	
	public ArrayList<String> getControlledLanes(){

		Command cmd = new Command(Constants.CMD_GET_TL_VARIABLE);
		Content cnt = new Content(Constants.TL_CONTROLLED_LANES,id);
		
		cmd.setContent(cnt);
		
		//cmd.print("Command GETEDGES");

		RequestMessage reqMsg = new RequestMessage();
		reqMsg.addCommand(cmd);
		
		try {
			
			ResponseMessage rspMsg = SumoCom.query(reqMsg);
			Content content = rspMsg.validate( (byte)  Constants.CMD_GET_TL_VARIABLE, (byte)  Constants.RESPONSE_GET_TL_VARIABLE,
					 (byte)  Constants.TL_CONTROLLED_LANES, (byte)  Constants.TYPE_STRINGLIST);
			
			controlledLanes = content.getStringList();

			return controlledLanes;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (WrongCommand e) {
			e.printStackTrace();
		}

		return null;
	}

	public void setState(String newState){
		
		Command cmd = new Command(Constants.CMD_SET_TL_VARIABLE);

		Content cnt = new Content(Constants.TL_RED_YELLOW_GREEN_STATE,id,Constants.TYPE_STRING);

		cnt.setString(newState);
		
		cmd.setContent(cnt);

		//cmd.print("setRouteById");

		RequestMessage reqMsg = new RequestMessage();
		reqMsg.addCommand(cmd);
		

		try {
			
			ResponseMessage rspMsg = SumoCom.query(reqMsg);

		} catch (IOException e) {
			System.out.println("Receiving setRouteById change Status");
			e.printStackTrace();
		}
		
	}
	
	public String[] getPhases(){

		Command cmd = new Command(Constants.CMD_GET_TL_VARIABLE);
		Content cnt = new Content(Constants.TL_COMPLETE_DEFINITION_RYG,id);

		cmd.setContent(cnt);

		RequestMessage reqMsg = new RequestMessage();
		reqMsg.addCommand(cmd);


		try {

			ResponseMessage rspMsg = SumoCom.query(reqMsg);
			Content content = rspMsg.validate( (byte)  Constants.CMD_GET_TL_VARIABLE, (byte)  Constants.RESPONSE_GET_TL_VARIABLE,
					 (byte)  Constants.TL_COMPLETE_DEFINITION_RYG, (byte)  Constants.TYPE_COMPOUND);
			
			SumoTrafficLightProgram stlp = content.getCompound();
			
			return null;
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (WrongCommand e) {
			e.printStackTrace();
		}

		return null;
	}
	
	public int getPhaseId() {
		return p;
	}

	public void setPhaseId(int p) {
		this.p = p;
	}
	
	public void changePhase(){
		p++;
		if(p > 7) p = 0;
		switch(p){
			case 0:
				setState("rrrGGGgrrrGGGg");
				break;
			case 1:
				setState("rrryyygrrryyyg");
				break;
			case 2:
				setState("rrrrrrGrrrrrrG");
				break;
			case 3:
				setState("rrrrrryrrrrrry");
				break;
			case 4:
				setState("GGgrrrrGGgrrrr");
				break;
			case 5:
				setState("yygrrrryygrrrr");
				break;
			case 6:
				setState("rrGrrrrrrGrrrr");
				break;
			case 7:
				setState("rryrrrrrryrrrr");
				break;
		}		
	}

}
