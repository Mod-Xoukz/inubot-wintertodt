package org.rspeer.scripts.wintertodt.task.game;

import com.google.inject.Inject;
import org.rspeer.game.adapter.component.inventory.Inventory;
import org.rspeer.game.component.Item;
import org.rspeer.game.effect.Health;
import org.rspeer.game.script.TaskDescriptor;
import org.rspeer.scripts.wintertodt.data.Constant;
import org.rspeer.scripts.wintertodt.domain.Domain;

@TaskDescriptor(name = "Eating")
public class EatTask extends GameTask {

  private int tolerance = Constant.EAT_FOOD_AT.random();

  @Inject
  public EatTask(Domain domain) {
    super(domain);
  }

  @Override
  protected boolean play() {
    if (domain.getBoss().isRespawning()) {
      return false;
    }

    Item food = Inventory.backpack().query().actions("Eat", "Drink").results().first();
    if (food == null || Health.getPercent() > tolerance) {
      return false;
    }

    //TODO handle the case of no food -> idle in waiting area until game is over or if enough time just leave and reset

    food.interact(x -> true);
    tolerance = Constant.EAT_FOOD_AT.random();
    sleep(2);
    return true;
  }
}
