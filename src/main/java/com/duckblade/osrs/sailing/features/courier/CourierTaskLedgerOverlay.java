package com.duckblade.osrs.sailing.features.courier;

import com.duckblade.osrs.sailing.SailingConfig;
import com.duckblade.osrs.sailing.features.util.CourierTaskUtil;
import com.duckblade.osrs.sailing.model.CourierTaskInfo;
import com.duckblade.osrs.sailing.model.Port;
import com.duckblade.osrs.sailing.module.PluginLifecycleComponent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayUtil;

@Slf4j
@Singleton
public class CourierTaskLedgerOverlay
	extends Overlay
	implements PluginLifecycleComponent
{
	private static final Set<Integer> LEDGER_TABLE_IDS = Arrays.stream(Port.values()).map(Port::getLedgerTableID).collect(Collectors.toSet());
	private final Client client;

	private GameObject activeLedger;
	private Port activePort;

	@Inject
	public CourierTaskLedgerOverlay(Client client)
	{
		super();
		this.client = client;
	}

	@Override
	public boolean isEnabled(SailingConfig config)
	{
		return config.courierItemIdentification();
	}

	@Subscribe
	public void onGameObjectSpawned(GameObjectSpawned e)
	{
		int id = e.getGameObject().getId();
		if (LEDGER_TABLE_IDS.contains(id))
		{
			activeLedger = e.getGameObject();
			activePort = Port.findByLedgerTableID(id);
		}
	}

	@Subscribe
	public void onGameObjectDespawned(GameObjectDespawned e)
	{
		int id = e.getGameObject().getId();
		if (LEDGER_TABLE_IDS.contains(id))
		{
			activeLedger = null;
			activePort = null;
		}
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (activePort == null)
		{
			return null;
		}

		var tasks = CourierTaskUtil.getCurrentTasks(client);
		var dropOffTasks = CourierTaskUtil.getDropOffTasksForPort(tasks, activePort);

		Shape hull = activeLedger.getConvexHull();
		if (hull == null)
		{
			return null;
		}

		var pickupTasks = CourierTaskUtil.getPickupTasksForPort(tasks, activePort);
		boolean allCargoRetrieved = pickupTasks.stream().allMatch(CourierTaskInfo::hasRetrievedAllCargo);

		if (!allCargoRetrieved)
		{
			OverlayUtil.renderPolygon(graphics, hull, Color.GREEN);
		}

		boolean allCargoDelivered = dropOffTasks.stream().allMatch(CourierTaskInfo::hasDeliveredAllCargo);
		if (!allCargoDelivered)
		{
			OverlayUtil.renderPolygon(graphics, hull, Color.RED);
		}

		return null;
	}
}
