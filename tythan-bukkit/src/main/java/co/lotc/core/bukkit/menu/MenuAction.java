package co.lotc.core.bukkit.menu;

import java.util.ArrayList;
import java.util.List;

import co.lotc.core.bukkit.util.InventoryUtil.MovedItem;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access=AccessLevel.PACKAGE)
@Getter
public class MenuAction {
	private final MenuAgent menuAgent;
	private final List<MovedItem> movedItems;

	MenuAction(MenuAgent a){
		menuAgent = a;
		movedItems = new ArrayList<>();
	}
}
