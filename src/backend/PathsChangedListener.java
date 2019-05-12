package backend;

import basicGui.FingerPath;

public interface PathsChangedListener {
	public void onPathAdded(FingerPath addedPath);

	public void onRepaintRequired();

	public void onRescaleRequired();
}
