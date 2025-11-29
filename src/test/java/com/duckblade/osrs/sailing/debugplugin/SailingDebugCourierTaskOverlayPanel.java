package com.duckblade.osrs.sailing.debugplugin;

import com.duckblade.osrs.sailing.model.CourierTask;
import com.duckblade.osrs.sailing.features.util.CourierTaskUtil;
import com.google.inject.Inject;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

@Slf4j
public class SailingDebugCourierTaskOverlayPanel extends OverlayPanel
{

	private final Client client;

	@Inject
	public SailingDebugCourierTaskOverlayPanel(Client client)
	{
		this.client = client;
		setPreferredPosition(OverlayPosition.ABOVE_CHATBOX_RIGHT);
		setLayer(OverlayLayer.ALWAYS_ON_TOP);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		getPanelComponent().getChildren()
			.add(TitleComponent.builder()
				.text("Courier Tasks")
				.build());
		var tasks = CourierTaskUtil.getCurrentTasks(client);

		for (CourierTask task : tasks)
		{
			getPanelComponent().getChildren()
				.add(LineComponent.builder()
					.left(task.getFromPort().getShortCode())
					.right(task.getToPort().getShortCode())
					.build());

			boolean isRetrieved = task.getNumCargoRetrieved() == task.getCargoAmount();
			boolean isDelivered = task.getNumCargoDelivered() == task.getCargoAmount();
			getPanelComponent().getChildren()
				.add(LineComponent.builder()
					.left("RETRIEVED")
					.leftColor(isRetrieved ? Color.GREEN : Color.RED)
					.right("DELIVERED")
					.rightColor(isDelivered ? Color.GREEN : Color.RED)
					.build());

		}

		super.render(graphics);
		return null;
	}
}