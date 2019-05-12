package backend;

public interface ConnectionListener {
	public void onDisconnect();

	public void connectionEstablished();
}
