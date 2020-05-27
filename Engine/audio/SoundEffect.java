package audio;

public class SoundEffect {
	private final int id, slot;

	public SoundEffect(int id, int slot) {
		this.id = id;
		this.slot = slot;
	}

	public int getId() {
		return id;
	}

	public int getSlot() {
		return slot;
	}
}
