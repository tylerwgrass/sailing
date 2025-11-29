package com.duckblade.osrs.sailing.features.util;

import com.duckblade.osrs.sailing.model.CourierTaskInfo;
import com.duckblade.osrs.sailing.model.Port;
import com.google.common.collect.ImmutableList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.gameval.DBTableID;
import net.runelite.api.gameval.VarbitID;

@Slf4j
public class CourierTaskUtil
{
	private enum TaskType
	{
		DELIVERY,
		BOUNTY
	}

	private static final List<Integer> TASK_SLOT_IDS = ImmutableList.of(
		VarbitID.PORT_TASK_SLOT_0_ID,
		VarbitID.PORT_TASK_SLOT_1_ID,
		VarbitID.PORT_TASK_SLOT_2_ID,
		VarbitID.PORT_TASK_SLOT_3_ID,
		VarbitID.PORT_TASK_SLOT_4_ID
	);

	private static final List<Integer> CARGO_RETRIEVED_VARBITS = ImmutableList.of(
		VarbitID.PORT_TASK_SLOT_0_CARGO_TAKEN,
		VarbitID.PORT_TASK_SLOT_1_CARGO_TAKEN,
		VarbitID.PORT_TASK_SLOT_2_CARGO_TAKEN,
		VarbitID.PORT_TASK_SLOT_3_CARGO_TAKEN,
		VarbitID.PORT_TASK_SLOT_4_CARGO_TAKEN
	);

	private static final List<Integer> CARGO_DELIVERED_VARBITS = ImmutableList.of(
		VarbitID.PORT_TASK_SLOT_0_CARGO_DELIVERED,
		VarbitID.PORT_TASK_SLOT_1_CARGO_DELIVERED,
		VarbitID.PORT_TASK_SLOT_2_CARGO_DELIVERED,
		VarbitID.PORT_TASK_SLOT_3_CARGO_DELIVERED,
		VarbitID.PORT_TASK_SLOT_4_CARGO_DELIVERED
	);

	public static Set<CourierTaskInfo> getCurrentTasks(Client client)
	{
		Set<CourierTaskInfo> tasks = new HashSet<>();
		for (int i = 0; i < TASK_SLOT_IDS.size(); i++)
		{
			CourierTaskInfo task = getTaskInfo(client, i);
			if (task != null)
			{
				tasks.add(task);
			}
		}

		return tasks;
	}

	public static CourierTaskInfo getTaskInfo(Client client, int taskIndex)
	{
		int taskID = client.getVarbitValue(TASK_SLOT_IDS.get(taskIndex));
		List<Integer> taskRow = client.getDBRowsByValue(DBTableID.PortTask.ID, DBTableID.PortTask.COL_TASK_ID, 0, taskID);

		if (taskRow.isEmpty())
		{
			return null;
		}

		Integer rowID = taskRow.get(0);
		int taskType = (int) client.getDBTableField(rowID, DBTableID.PortTask.COL_TASK_TYPE, 0)[0];
		if (taskType != TaskType.DELIVERY.ordinal())
		{
			return null;
		}

		int fromPortID = (int) client.getDBTableField(rowID, DBTableID.PortTask.COL_CARGO_PORT, 0)[0];
		int toPortID = (int) client.getDBTableField(rowID, DBTableID.PortTask.COL_ENDING_PORT, 0)[0];
		int cargoAmount = (int) client.getDBTableField(rowID, DBTableID.PortTask.COL_CARGO, 1)[0];

		Port fromPort = Port.findByID(fromPortID);
		Port toPort = Port.findByID(toPortID);

		int numRetrieved = client.getVarbitValue(CARGO_RETRIEVED_VARBITS.get(taskIndex));
		int numDelivered = client.getVarbitValue(CARGO_DELIVERED_VARBITS.get(taskIndex));

		return new CourierTaskInfo(taskIndex, fromPort, toPort, cargoAmount, numRetrieved, numDelivered);
	}

	public static List<CourierTaskInfo> getDropOffTasksForPort(Set<CourierTaskInfo> tasks, Port port)
	{
		return tasks.stream().filter(task -> task.getToPort() == port).collect(Collectors.toList());
	}

	public static List<CourierTaskInfo> getPickupTasksForPort(Set<CourierTaskInfo> tasks, Port port)
	{
		return tasks.stream().filter(task -> task.getFromPort() == port).collect(Collectors.toList());
	}
}
