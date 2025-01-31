package callbacks;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ingescape.Agent;
import com.ingescape.ServiceListener;

import server.Server;
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
			if (arguments.size() == 1) {
				if (arguments.get(0) instanceof Player) {
					functionHandler.enterPlayer(agent, senderAgentName, (Player) arguments.get(0));
				}
			}
			break;
		}
		case "receiveBet": {
			if (arguments.size() != 4) {
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
