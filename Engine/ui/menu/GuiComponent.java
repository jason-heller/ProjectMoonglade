package ui.menu;

public interface GuiComponent {
	int getDepth();

	boolean isTemporary();

	void markAsTemporary();

	GuiComponent setDepth(int depth);

}
