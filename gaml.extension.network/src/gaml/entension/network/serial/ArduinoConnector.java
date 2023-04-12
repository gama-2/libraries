/*******************************************************************************************************
 *
 * ArduinoConnector.java, in ummisco.gama.network, is part of the source code of the
 * GAMA modeling and simulation platform (v.1.9.0).
 *
 * (c) 2007-2022 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gaml.extension.network.serial;
 
import gama.core.metamodel.agent.IAgent;
import gama.core.runtime.IScope;
import gaml.extension.network.common.Connector;
import gaml.extension.network.common.GamaNetworkException;
import gaml.extension.network.common.socket.SocketService;

/**
 * The Class ArduinoConnector.
 */
public class ArduinoConnector extends Connector {

	/** The arduino. */
	MyArduino arduino;
	
	/** The port. */
	String PORT = "";
	
	/** The baud. */
	int BAUD = 9600;	
	
	/** The ss thread. */
	MultiThreadedArduinoReceiver ssThread;	
	
	/**
	 * Instantiates a new arduino connector.
	 *
	 * @param scope the scope
	 */
	public ArduinoConnector(final IScope scope) {}	
	
	@Override
	protected void connectToServer(IAgent agent) throws GamaNetworkException {
		MyPortDropdownMenu portList = new MyPortDropdownMenu();
		portList.refreshMenu();
		
		// cu.usbmodem1441012		
		for(int i = 0; i < portList.getItemCount(); i++) {
			System.out.println(portList.getItemAt(i));
			if(portList.getItemAt(i).contains("cu.usbmodem")) {
				System.out.println(portList.getItemAt(i));
				PORT = portList.getItemAt(i);
			}
		}		
		if("".equals(PORT)) {
			PORT=this.getConfigurationParameter(SERVER_URL);
		}
		arduino = new MyArduino(PORT,BAUD);
		
		if(arduino.openConnection()){
			System.out.println("CONNECTION OPENED");
		}

		ssThread = new MultiThreadedArduinoReceiver(agent, 100, arduino);
		ssThread.start();
	}

	@Override
	protected boolean isAlive(IAgent agent) throws GamaNetworkException {
		return true;
		
		// return false;
	}

	@Override
	protected void subscribeToGroup(IAgent agt, String boxName) throws GamaNetworkException {}

	@Override
	protected void unsubscribeGroup(IAgent agt, String boxName) throws GamaNetworkException {}

	@Override
	protected void releaseConnection(IScope scope) throws GamaNetworkException {
		if (ssThread != null) {
			ssThread.interrupt();
		}
		
		arduino.closeConnection();
		System.out.println("CONNECTION CLOSED");		
	}

	@Override
	protected void sendMessage(IAgent sender, String receiver, String content) throws GamaNetworkException {
		
		
	}

	@Override
	public SocketService getSocketService() {
		
		return null;
	}

	
}
