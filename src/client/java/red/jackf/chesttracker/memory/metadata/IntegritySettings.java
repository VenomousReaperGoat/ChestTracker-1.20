package red.jackf.chesttracker.memory.metadata;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import red.jackf.chesttracker.util.I18nUtil;
import red.jackf.chesttracker.util.ModCodecs;

import java.util.Optional;

public class IntegritySettings {
    protected static final Codec<IntegritySettings> CODEC = RecordCodecBuilder.create(instance -> {
        final var def = new IntegritySettings();
        return instance.group(
                        Codec.BOOL.optionalFieldOf("removeOnPlayerBlockBreak")
                                .forGetter(settings -> Optional.of(settings.removeOnPlayerBlockBreak)),
                        Codec.BOOL.optionalFieldOf("checkPeriodicallyForMissingBlocks")
                                .forGetter(settings -> Optional.of(settings.checkPeriodicallyForMissingBlocks)),
                        ModCodecs.ofEnum(MemoryLifetime.class).optionalFieldOf("memoryLifetime")
                                .forGetter(settings -> Optional.of(settings.memoryLifetime)),
                        Codec.BOOL.optionalFieldOf("preserveNamed")
                                .forGetter(settings -> Optional.of(settings.preserveNamed)),
                        ModCodecs.ofEnum(LifetimeCountMode.class).optionalFieldOf("lifetimeCountMode")
                                .forGetter(settings -> Optional.of(settings.lifetimeCountMode))
                )
                .apply(instance, (removeOnPlayerBlockBreak, checkPeriodicallyForMissingBlocks, memoryLifetime, preserveNamed, lifetimeCountMode) ->
                        new IntegritySettings(
                                removeOnPlayerBlockBreak.orElse(def.removeOnPlayerBlockBreak),
                                checkPeriodicallyForMissingBlocks.orElse(def.checkPeriodicallyForMissingBlocks),
                                memoryLifetime.orElse(def.memoryLifetime),
                                preserveNamed.orElse(def.preserveNamed),
                                lifetimeCountMode.orElse(def.lifetimeCountMode)
                        ));
    });

    public boolean removeOnPlayerBlockBreak = true;
    public boolean checkPeriodicallyForMissingBlocks = true;
    public MemoryLifetime memoryLifetime = MemoryLifetime.TWELVE_HOURS;
    public boolean preserveNamed = true;
    public LifetimeCountMode lifetimeCountMode = LifetimeCountMode.LOADED_TIME;

    IntegritySettings() {
    }

    public IntegritySettings(boolean removeOnPlayerBlockBreak,
                             boolean checkPeriodicallyForMissingBlocks,
                             MemoryLifetime memoryLifetime,
                             boolean preserveNamed,
                             LifetimeCountMode lifetimeCountMode) {
        this();
        this.removeOnPlayerBlockBreak = removeOnPlayerBlockBreak;
        this.checkPeriodicallyForMissingBlocks = checkPeriodicallyForMissingBlocks;
        this.memoryLifetime = memoryLifetime;
        this.preserveNamed = preserveNamed;
        this.lifetimeCountMode = lifetimeCountMode;
    }

    public IntegritySettings copy() {
        return new IntegritySettings(removeOnPlayerBlockBreak, checkPeriodicallyForMissingBlocks, memoryLifetime, preserveNamed, lifetimeCountMode);
    }

    public enum LifetimeCountMode {
        REAL_TIME(Component.translatable("chesttracker.gui.editMemoryBank.integrity.lifetimeCountMode.real_time")),
        WORLD_TIME(Component.translatable("chesttracker.gui.editMemoryBank.integrity.lifetimeCountMode.world_time")),
        LOADED_TIME(Component.translatable("chesttracker.gui.editMemoryBank.integrity.lifetimeCountMode.loaded_time"));

        public final Component label;

        LifetimeCountMode(Component label) {
            this.label = label;
        }
    }

    private static Component lifetimePrefix() {
        return Component.translatable("chesttracker.gui.editMemoryBank.integrity.memoryLifetime");
    }

    public enum MemoryLifetime {
        TEN_SECONDS(10L, I18nUtil.colon(lifetimePrefix(), I18nUtil.seconds(10))),
        FIVE_MINUTES(60L * 5L, I18nUtil.colon(lifetimePrefix(), I18nUtil.minutes(5))),
        TWENTY_MINUTES(60L * 15L, I18nUtil.colon(lifetimePrefix(), I18nUtil.minutes(15))),
        FORTY_MINUTES(60L * 30L, I18nUtil.colon(lifetimePrefix(), I18nUtil.minutes(30))),
        ONE_HOUR(60L * 60L, I18nUtil.colon(lifetimePrefix(), I18nUtil.hours(1))),
        TWO_HOURS(60L * 60L * 2L, I18nUtil.colon(lifetimePrefix(), I18nUtil.hours(2))),
        FOUR_HOURS(60L * 60L * 4L, I18nUtil.colon(lifetimePrefix(), I18nUtil.hours(4))),
        SIX_HOURS(60L * 60L * 6L, I18nUtil.colon(lifetimePrefix(), I18nUtil.hours(6))),
        TWELVE_HOURS(60L * 60L * 12L, I18nUtil.colon(lifetimePrefix(), I18nUtil.hours(12))),
        ONE_DAY(60L * 60L * 24L, I18nUtil.colon(lifetimePrefix(), I18nUtil.days(1))),
        TWO_DAYS(60L * 60L * 24L * 2L, I18nUtil.colon(lifetimePrefix(), I18nUtil.days(2))),
        FIVE_DAYS(60L * 60L * 24L * 5L, I18nUtil.colon(lifetimePrefix(), I18nUtil.days(5))),
        SEVEN_DAYS(60L * 60L * 24L * 7L, I18nUtil.colon(lifetimePrefix(), I18nUtil.days(7))),
        NEVER(null, I18nUtil.colon(lifetimePrefix(), Component.translatable("chesttracker.gui.editMemoryBank.integrity.memoryLifetime.never")));

        public final Long seconds;
        public final Component label;

        MemoryLifetime(@Nullable Long seconds, Component label) {
            this.seconds = seconds;
            this.label = label;
        }
    }
}
