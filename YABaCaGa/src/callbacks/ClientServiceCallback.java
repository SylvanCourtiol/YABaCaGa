package callbacks;

import java.io.IOException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ingescape.Agent;
import com.ingescape.ServiceListener;

import client.Client;
import util.Blobizer;
import yabacaga.model.Player;

public class ClientServiceCallback implements ServiceListener {

	private static Logger _logger = LoggerFactory.getLogger(ClientServiceCallback.class);

	private Client functionHandler;

	public ClientServiceCallback(Client functionHandler) {
		super();
		this.functionHandler = functionHandler;
	}

	@Override
	public void handleCallToService(Agent agent, String senderAgentName, String senderAgentUUID, String serviceName,
			List<Object> arguments, String token) {
		_logger.debug("**received service call from {} ({}): {} (with token {})", senderAgentName, senderAgentUUID,
				serviceName, arguments, token);
		try {
			switch (serviceName) {
			case "acceptPlayer": {
				if (arguments.size() == 1) {
					if (arguments.get(0) instanceof Integer) {
						functionHandler.acceptPlayer((int) arguments.get(0));
					}
				}
				break;
			}
			case "receiveGameInfo": {
				if (arguments.size() == 2) {
					if (arguments.get(0) instanceof Boolean firstPlayer && arguments.get(1) instanceof String s) {
						Object o = Blobizer.fromString(s);
						if (o instanceof Object[] playerList && playerList.length == 2 && playerList[0] instanceof Player) {
							Player p1 = (Player) playerList[0];
							Player p2 = (Player) playerList[1];
							Player player = null;
							Player opponent = null;
							if (p1.getId() == functionHandler.getPlayer().getId()) {
								player = p1;
								opponent = p2;
							} else {
								player = p2;
								opponent = p1;
							}
							functionHandler.receiveGameInfo(firstPlayer, player, opponent);
						}

					}
				}
				break;
			}
			case "acceptBet": {
				if (arguments.size() == 1) {
					if (arguments.get(0) instanceof Integer) {
						functionHandler.acceptBet((int) arguments.get(0));
					}
				}

				break;
			}
			case "receiveOpponentBet": {
				if (arguments.size() == 1) {
					if (arguments.get(0) instanceof Integer) {
						functionHandler.receiveOpponentBet((int) arguments.get(0));
					}
				}
				break;
			}
			case "receiveDuelResult": {

				break;
			}
			case "receiveGameResult": {

				break;
			}
			default:
				throw new IllegalArgumentException("Unexpected value: " + serviceName);
			}
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
