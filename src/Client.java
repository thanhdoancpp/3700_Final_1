import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import javax.json.Json;
import javax.json.JsonObject;

public class Client extends Thread {
	private BufferedReader bufferedReader;
	private ArrayList<String> receivedLog;
	private boolean isReceived = false;

	public Client(Socket socket, String username) throws IOException {
		bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		isReceived = false;
		receivedLog = new ArrayList<String>();
	}

	@Override
	public void run() {
		while (true) {
			try {
				JsonObject jsonObject = Json.createReader(bufferedReader).readObject();
				if (jsonObject.containsKey("running")) {
					// Log
//					System.out.format("Round %d received from %s: %s\n", jsonObject.getInt("round"), jsonObject.getString("username"), jsonObject.getString("message"));
					receivedLog.add(jsonObject.getString("message"));
					isReceived = true;
				} else if (jsonObject.containsKey("done")) {
					isReceived = true;
				}
			} catch (Exception e) {
				interrupt();
			}
		}
	}

	public void setIsReceived(boolean value) {
		this.isReceived = value;
	}

	public boolean getIsReceived() {
		return this.isReceived;
	}

	public ArrayList<String> getClientLog() {
		return receivedLog;
	}
}
