package red.jackf.chesttracker.mixins;

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(HandledScreen.class)
public interface AccessorHandledScreen {
    @Accessor(value = "backgroundWidth")
    int getBackgroundWidth();

    @Accessor(value = "backgroundHeight")
    int getBackgroundHeight();

    @Accessor(value = "focusedSlot")
    Slot getFocusedSlot();
}
