import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ServerHandler extends Thread {
	private Server serverThread;
	private Socket socket;
	private PrintWriter printWriter;

	public ServerHandler(Socket socket, Server serverThread) {
		this.serverThread = serverThread;
		this.socket = socket;
	}

	@Override
	public void run() {
		try {
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
			this.printWriter = new PrintWriter(socket.getOutputStream(), true);

			while (true) {
				serverThread.sendMessage(bufferedReader.readLine());
			}
		} catch (Exception e) {
			System.out.println("ServerHandler: " + e);
			e.printStackTrace();
			interrupt();
		}
	}

	public PrintWriter getPrintWriter() {
		return printWriter;
	}
}
