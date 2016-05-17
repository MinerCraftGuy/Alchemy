package index.alchemy.potion;

import com.google.common.base.Predicate;

import index.alchemy.api.IEventHandle;
import index.alchemy.core.AlchemyEventSystem;
import index.alchemy.core.AlchemyEventSystem.EventType;
import index.alchemy.entity.ai.EntityAIFindEntityNearestHelper;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class PotionIgnore extends AlchemyPotion implements IEventHandle {
	
	public static final Predicate<EntityLivingBase> NOT_ACTIVE = new Predicate<EntityLivingBase>() {
        public boolean apply(EntityLivingBase player) {
            return !player.isPotionActive(AlchemyPotionLoader.ignore);
        }
    };
	
	public PotionIgnore() {
		super("ignore", false, 0xFFFFFF);
	}
	
	@Override
	public EventType[] getEventType() {
		return AlchemyEventSystem.EVENT_BUS;
	}
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onLivingSetAttackTarget(LivingSetAttackTargetEvent event) {
		if (event.getEntityLiving() instanceof EntityLiving && event.getEntityLiving().isNonBoss() &&
			event.getTarget() != null && event.getTarget().isPotionActive(this) &&
			event.getEntityLiving().getCombatTracker().getBestAttacker() != event.getTarget()) {
			EntityLiving living = (EntityLiving) event.getEntityLiving();
			Class<EntityLivingBase> type = (Class<EntityLivingBase>) 
					(event.getEntityLiving() instanceof EntityPlayer ? EntityPlayer.class : event.getEntityLiving().getClass());
			living.attackTarget = EntityAIFindEntityNearestHelper.<EntityLivingBase>findNearest(
					(EntityLiving) event.getEntityLiving(), type, NOT_ACTIVE);
		}
	}

}