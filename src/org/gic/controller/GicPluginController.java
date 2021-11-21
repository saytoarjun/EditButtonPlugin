package org.gic.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.text.DecimalFormat;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import connector.common.Util;
import sailpoint.api.SailPointContext;
import sailpoint.object.ApprovalItem;
import sailpoint.object.ApprovalSet;
import sailpoint.object.WorkItem;
import sailpoint.rest.plugin.AllowAll;
import sailpoint.rest.plugin.BasePluginResource;
import sailpoint.tools.GeneralException;

@Path("checkforbundle")
@Consumes(MediaType.APPLICATION_JSON)
@AllowAll
public class GicPluginController extends BasePluginResource {

	@Override
	public String getPluginName() {
		// TODO Auto-generated method stub
		return null;
	}

	private static Log logger = LogFactory.getLog(GicPluginController.class);

	/**
	 * @param allAriaLabels
	 * @return the string of all the entitlements and bundles which need to be
	 *         displayed as high risk on UI
	 */
	@POST
	@Path("bundleDetails")
	@Produces(MediaType.APPLICATION_JSON)
	@AllowAll
	public Map<String, String> changeFileString(Map<String, String> map) {
		Map<String, String> returnMap = new HashMap<String, String>();
		System.out.println("Enter : changeFileString with allAriaLabels : " + map);
		WorkItem workItemObject = null;
		if (null != map) {

			try {
				System.out.println("Map value for WI : " + map.get("WorkItem"));
				workItemObject = getWorkItemObject(map.get("WorkItem"));
				System.out.println("WorkItem Object fetched is : " + workItemObject);
			} catch (GeneralException e) {

				e.printStackTrace();
			}

			for (Entry<String, String> pairOfBundle : map.entrySet()) {
				String key = pairOfBundle.getKey();// + pairOfBundle.getValue();
				System.out.println("pairOfBundle being iterated : " + pairOfBundle);
				System.out.println("valueOfRole : " + key);
				if (Util.isNotNullOrEmpty(key) && key instanceof String && (!key.equalsIgnoreCase("WorkItem"))
						&& (!key.equalsIgnoreCase("Approval Type"))) {
					String dateStatement = getStartAndEndDatesOfRole(pairOfBundle.getValue(), workItemObject);
					System.out.println("dateStatement returned after calling getStartAndEndDatesOfRole() for role : "
							+ pairOfBundle.getValue() + " dateStatement : " + dateStatement);
					returnMap.put(pairOfBundle.getValue(), dateStatement);
				} else {
					System.out.println("Key is neither Strinng nor any WorkItem or Approval Type");
				}
			}
		} else {
			System.out.println("The map object passed to the java controller is null");
		}

		System.out.println("Returning from Java class with the returnMap : " + returnMap);
		return returnMap;
	}

	public WorkItem getWorkItemObject(String key) throws GeneralException {
		System.out.println("Start getWorkItemObject() with the key : " + key);
		WorkItem itemObject = null;
		DecimalFormat decFormatter = new DecimalFormat("0000000000");
		int workItemmIdInt = 0;

		SailPointContext context = getContext();
		String[] workItemIdArray = key.split(":");
		String itemId = "";

		System.out.println("workItemIdArray : " + workItemIdArray);
		if (null != workItemIdArray && workItemIdArray.length > 1) {
			itemId = workItemIdArray[1].trim();
			System.out.println("itemId String  : " + itemId);
			if (itemId instanceof String) {
				System.out.println("ItemID as String : " + itemId);
				workItemmIdInt = Integer.parseInt(itemId);
				System.out.println("workItemmIdInt as Int : " + workItemmIdInt);
			} else {
				System.out.println("Nothing is there to parse");
			}
			itemId = decFormatter.format(workItemmIdInt);
			System.out.println("Item ID fetched and after formatted to deca zeroes is : " + itemId);
			if (null != itemId) {

				itemObject = context.getObjectByName(WorkItem.class, itemId);
				return itemObject;
			}
		} else {
			System.out.println("No Item Id is present");
		}

		return itemObject;
	}

	public String getStartAndEndDatesOfRole(String roleName, WorkItem item) {
		String itemName = null;
		Date startDate = null;
		Date endDate = null;
		StringBuilder dateStatement = new StringBuilder();
		if (Util.isNotNullOrEmpty(roleName) && null != item) {
			ApprovalSet approvalSet = item.getApprovalSet();
			if (null != approvalSet) {
				List<ApprovalItem> listItems = approvalSet.getItems();
				if (null != listItems) {
					System.out.println("ListItems is not null");
					for (ApprovalItem approvalItem : listItems) {
						System.out.println("ApprovalItem being iterated : " + approvalItem);
						if (approvalItem.getValue() instanceof String) {
							itemName = (String) approvalItem.getValue();
							System.out.println("itemName being iterated upon : " + itemName);
							if (itemName.equalsIgnoreCase(roleName)) {
								System.out.println("Item name and roleName are same : ItemName : " + itemName
										+ " and roleName is : " + roleName);
								if (null != approvalItem.getAttributes().get("startDate"))
									startDate = (Date) approvalItem.getAttributes().get("startDate");

								if (null != approvalItem.getAttributes().get("endDate"))
									endDate = (Date) approvalItem.getAttributes().get("endDate");

								System.out.println("StartDate : " + startDate + " endDate : " + endDate);

								if (null != startDate && null != endDate)
									dateStatement = dateStatement.append(startDate).append(" to ").append(endDate);
								else if (null != startDate && null == endDate)
									dateStatement = dateStatement.append(startDate).append(" to ")
											.append("No End Date");
								else if (null == startDate && null != endDate)
									dateStatement = dateStatement.append("From Today").append(" till ").append(endDate);
								else if (null == startDate && null == endDate)
									System.out.println("Both dates are null so no extensions");

								System.out.println("Date Statement is : " + dateStatement);
								return dateStatement.toString();
							} else {
								System.out.println("Item name and roleName are NOT same : ItemName : " + itemName
										+ " and roleName is : " + roleName);
							}

						} else {
							System.out
									.println("Name of the item is not String : " + approvalItem.getValue().getClass());
						}

					}
				} else {
					System.out.println("ListItems is null");
				}
			} else {
				System.out.println("ApprovalSet is null");
			}
		} else {
			System.out.println("roleName is null or item is null");
		}
		System.out.println("return dateStatement : " + dateStatement);
		return dateStatement.toString();
	}

}
