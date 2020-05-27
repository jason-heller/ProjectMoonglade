package ui;

public interface Component {
	int getDepth();

	boolean isTemporary();

	void markAsTemporary();

	Component setDepth(int depth);

}
