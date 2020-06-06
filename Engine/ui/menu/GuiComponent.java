package ui.menu;

public interface GuiComponent {
	public static final int VERTICAL = 0, HORIZONTAL = 1;
	
	int getDepth();

	boolean isTemporary();

	void markAsTemporary();

	GuiComponent setDepth(int depth);

}
