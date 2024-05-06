package com.cwelth.craft_on_surface;

import com.cwelth.craft_on_surface.setup.MainSetup;
import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

@Mod(CraftOnSurface.MODID)
public class CraftOnSurface
{
    public static final String MODID = "craft_on_surface";
    public static final Logger LOGGER = LogUtils.getLogger();
	
    public CraftOnSurface()
    {
        MainSetup.setup();
    }
}
