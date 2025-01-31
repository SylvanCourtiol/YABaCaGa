package callbacks;

import java.io.IOException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ingescape.Agent;
import com.ingescape.ServiceListener;

import server.Server;
import util.Blobizer;
import yabacaga.model.Player;

public class ServerServiceCallback implements ServiceListener {

	private static Logger _logger = LoggerFactory.getLogger(ServerServiceCallback.class);

	private Server functionHandler;

	public ServerServiceCallback(Server functionHandler) {
		super();
		this.functionHandler = functionHandler;
	}

	@Override
	public void handleCallToService(Agent agent, String senderAgentName, String senderAgentUUID, String serviceName,
			List<Object> arguments, String token) {
		_logger.debug("**received service call from {} ({}): {} (with token {})", senderAgentName, senderAgentUUID,
				serviceName, arguments, token);

		switch (serviceName) {
		case "enterPlayer": {
			System.out.println("Test Service Reçu");
			if (arguments.size() == 1 && arguments.get(0) instanceof String) {
				System.out.println("Test Nb Arguments and String Ok");
				try {
					Object o = Blobizer.fromString((String)arguments.get(0));
					if (o instanceof Player) {
						functionHandler.enterPlayer(agent, senderAgentName, (Player) o);
					}
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			break;
		}
		case "receiveBet": {
			if (arguments.size() == 4) {
				if (arguments.get(0) instanceof Integer && arguments.get(1) instanceof Integer
						&& arguments.get(2) instanceof Integer && arguments.get(3) instanceof Boolean) {
					functionHandler.receiveBet(agent, senderAgentName, (int) arguments.get(0), (int) arguments.get(1),
							(int) arguments.get(2), (boolean) arguments.get(3));
				}
			}
			break;
		}
		default:
			throw new IllegalArgumentException("Unexpected value: " + serviceName);
		}
	}
}
