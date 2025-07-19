package xyz.nucleoid.plasmid.test;

import net.minecraft.block.Blocks;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import org.apache.commons.lang3.mutable.MutableInt;
import xyz.nucleoid.fantasy.BubbleWorldConfig;
import xyz.nucleoid.fantasy.BubbleWorldSpawner;
import xyz.nucleoid.plasmid.Plasmid;
import xyz.nucleoid.plasmid.game.GameOpenContext;
import xyz.nucleoid.plasmid.game.GameOpenProcedure;
import xyz.nucleoid.plasmid.game.event.GameTickListener;
import xyz.nucleoid.plasmid.game.event.PlayerDeathListener;
import xyz.nucleoid.plasmid.game.rule.GameRule;
import xyz.nucleoid.plasmid.game.rule.RuleResult;
import xyz.nucleoid.plasmid.game.stats.GameStatisticBundle;
import xyz.nucleoid.plasmid.game.stats.StatisticKey;
import xyz.nucleoid.plasmid.map.template.MapTemplate;
import xyz.nucleoid.plasmid.map.template.TemplateChunkGenerator;
import xyz.nucleoid.plasmid.widget.GlobalWidgets;
import xyz.nucleoid.plasmid.widget.SidebarWidget;

public final class TestGame {
    private static final StatisticKey<Double> TEST_KEY = StatisticKey.doubleKey(new Identifier(Plasmid.ID, "test"), StatisticKey.StorageType.TOTAL);

    public static GameOpenProcedure open(GameOpenContext<Unit> context) {
        MapTemplate template = TestGame.buildTemplate();

        ChunkGenerator generator = new TemplateChunkGenerator(context.getServer(), template);

        BubbleWorldConfig worldConfig = new BubbleWorldConfig()
                .setGenerator(generator)
                .setSpawner(BubbleWorldSpawner.atSurface(BlockPos.ORIGIN))
                .setDefaultGameMode(GameMode.ADVENTURE)
                .setTimeOfDay(6000)
                .setGameRule(GameRules.DO_MOB_SPAWNING, false)
                .setGameRule(GameRules.DO_WEATHER_CYCLE, false);

        return context.createOpenProcedure(worldConfig, game -> {
            game.setRule(GameRule.FALL_DAMAGE, RuleResult.DENY);
            game.setRule(GameRule.HUNGER, RuleResult.DENY);
            game.setRule(GameRule.PVP, RuleResult.ALLOW);
            game.setRule(GameRule.THROW_ITEMS, RuleResult.DENY);
            game.setRule(GameRule.MODIFY_INVENTORY, RuleResult.DENY);
            game.setRule(GameRule.MODIFY_ARMOR, RuleResult.ALLOW);

            game.listen(PlayerDeathListener.EVENT, (player, source) -> {
                player.teleport(0.0, 65.0, 0.0);
                return ActionResult.FAIL;
            });

            GlobalWidgets widgets = GlobalWidgets.addTo(game);
            SidebarWidget sidebar = widgets.addSidebar(new TranslatableText("text.plasmid.test"));

            MutableInt timer = new MutableInt();

            game.listen(GameTickListener.EVENT, () -> {
                int time = timer.incrementAndGet();
                if (time % 20 == 0) {
                    sidebar.set(b -> {
                        b.add(new LiteralText("Hello World! " + (time / 20) + "s").setStyle(Style.EMPTY.withColor(Formatting.RED)));
                        b.add(new LiteralText(""));
                        b.add(new TranslatableText("text.plasmid.game.started.player", "test"));
                    });

                    GameStatisticBundle statistics = game.getGameSpace().getStatistics("plasmid-test-game");
                    for (ServerPlayerEntity player : game.getGameSpace().getPlayers()) {
                        statistics.forPlayer(player).increment(TEST_KEY, 2.5);
                    }
                }
            });
        });
    }

    private static MapTemplate buildTemplate() {
        MapTemplate template = MapTemplate.createEmpty();

        for (BlockPos pos : BlockPos.iterate(-5, 64, -5, 5, 64, 5)) {
            template.setBlockState(pos, Blocks.BLUE_STAINED_GLASS.getDefaultState());
        }

        return template;
    }
}
