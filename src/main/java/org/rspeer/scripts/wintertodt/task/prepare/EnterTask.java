package org.rspeer.scripts.wintertodt.task.prepare;

import com.google.inject.Inject;
import org.rspeer.game.adapter.scene.SceneObject;
import org.rspeer.game.script.Task;
import org.rspeer.game.script.TaskDescriptor;
import org.rspeer.scripts.wintertodt.api.Province;
import org.rspeer.scripts.wintertodt.domain.Domain;

@TaskDescriptor(name = "Starting a game!")
public class EnterTask extends Task {

  private final Domain domain;

  @Inject
  public EnterTask(Domain domain) {
    this.domain = domain;
  }

  @Override
  public boolean execute() {
    if (Province.isInGame()) {
      return false;
    }

    SceneObject door = Province.findDoor();
    return door != null && door.interact("Enter");
  }
}
