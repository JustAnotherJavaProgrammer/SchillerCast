package backend;

import java.io.IOException;

public interface InputStreamReadLineResultListener {
	public void onError(IOException e);

	public void onSuccess(String result);
}
