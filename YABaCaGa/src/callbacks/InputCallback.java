package callbacks;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ingescape.*;

public class InputCallback implements IopListener {
private static Logger _logger = LoggerFactory.getLogger(InputCallback.class);

	public InputCallback() {}

	@Override
	public void handleIOP(Agent agent, Iop io, String name, IopType type, Object value) {
		_logger.debug("**received input {} with type {} and value {}", name, type, value);

		// Add code here if needed //

	}
}
