package index.alchemy.dlcs.skin.core;

import com.mojang.authlib.GameProfile;

import index.alchemy.api.annotation.Patch;
import index.project.version.annotation.Omega;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

@Omega
@Patch("net.minecraft.entity.player.EntityPlayer")
public abstract class ExEntityPlayer extends EntityPlayer implements ISkinEntity {
	
	@Patch.Exception
	public ExEntityPlayer(World world, GameProfile gameProfile) { super(world, gameProfile); }
	
}
