package index.alchemy.api;

import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;

public interface IPhaseRunnable {
	
	public boolean run(Phase phase);

}