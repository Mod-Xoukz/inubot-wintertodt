package org.rspeer.scripts.wintertodt.task.prepare;

import com.google.inject.Inject;
import org.rspeer.game.Definitions;
import org.rspeer.game.House;
import org.rspeer.game.adapter.component.inventory.Bank;
import org.rspeer.game.adapter.component.inventory.Inventory;
import org.rspeer.game.adapter.definition.ItemDefinition;
import org.rspeer.game.component.Inventories;
import org.rspeer.game.component.Item;
import org.rspeer.game.config.item.entry.builder.ItemEntryBuilder;
import org.rspeer.game.config.item.loadout.BackpackLoadout;
import org.rspeer.game.script.Task;
import org.rspeer.game.script.TaskDescriptor;
import org.rspeer.scripts.wintertodt.api.Province;
import org.rspeer.scripts.wintertodt.data.Axe;
import org.rspeer.scripts.wintertodt.data.WintertodtItem;
import org.rspeer.scripts.wintertodt.domain.config.Config;

import java.util.Comparator;
import java.util.function.Predicate;

@TaskDescriptor(
        name = "Banking!",
        blocking = true,
        blockIfSleeping = true,
        priority = 99
)
public class BankTask extends Task {

    private final Config config;

    @Inject
    public BankTask(Config config) {
        this.config = config;
    }

    private Item getAxe(Inventory inv) {
        return inv.query().ids(getUsable(x -> true).getId()).results().first();
    }

    private Axe getUsable(Predicate<Axe> predicate) {
        return Axe.getUsable()
                .stream()
                .sorted(Comparator.comparingInt(Axe::getLevel).reversed())
                .filter(predicate)
                .findFirst()
                .orElse(null);
    }

    @Override
    public boolean execute() {
        if (Province.isInGame() || config.isReady()) {
            return false;
        }

        if (House.isInside()) {
            return false;
        }

        if (!Bank.isOpen()) {
            if (config.isOpenCrates()) {
                Inventories.backpack().getItems("Supply crate").limit(9).forEach(x -> x.interact("Open"));
            }

            Bank.open();
            return true;
        }

        BackpackLoadout loadout = new BackpackLoadout("todt");
        for (WintertodtItem item : WintertodtItem.values()) {
            if (item.isRequired(config)) {
                loadout.add(new ItemEntryBuilder()
                        .key(item.getName())
                        .quantity(1)
                        .build());
            }
        }

        Axe axe = getUsable(x -> true);
        if (getAxe(Inventories.equipment()) == null) {
            if (Inventories.bank().contains(x -> x.ids(axe.getId()).results()) && !Inventories.backpack().contains(x -> x.ids(axe.getId()).results())) {
                loadout.add(new ItemEntryBuilder()
                        .key(axe.getName())
                        .quantity(1)
                        .build());
            }
        }


        ItemDefinition definition = Definitions.getItem(config.getFoodId());
        if (definition != null) {
            loadout.add(new ItemEntryBuilder()
                    .key(definition.getName())
                    .quantity(config.getFoodAmount())
                    .build());
        }

        if (!loadout.isBagged()) {
            loadout.withdraw(Inventories.bank());
        }

        return true;
    }
}
