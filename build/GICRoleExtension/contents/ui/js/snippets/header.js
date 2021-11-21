/**
 * Created by Siddharth Chaudhary.
 */
var dataFromRestCall = "nothing yet";
var userRoles = {};
var url = SailPoint.CONTEXT_PATH + '/plugins/pluginPage.jsf?pn=TodoPlugin';
console.log("Plugin called ");
//the function to call the java class
var data = 'data for testing';
var lbl = undefined;

jQuery(document).ready(function() {

	console.log("going to call disableAndAddDates");
	disableAndAddDates();


});

function disableAndAddDates()
{
	var elements = document.getElementsByClassName("row ng-scope");
	
	if(elements.length>0)
	{
		console.log("elements length is greater than zero");
		for (var i = 0, len = elements.length; i < len; i++) {
			if (i == 0 || i == 1) {

				if (i == 0) {
					//this will give the header as manager approval
					userRoles["Approval Type"]= elements[0].getElementsByClassName("panel-title")[0].innerText;

					//this will give the workItem ID
					userRoles["WorkItem"]=elements[0].getElementsByClassName("text-muted small m-r-sm inline ng-binding")[1].innerText;
				}
				else {
					continue;
				}
			}
			else {
				userRoles[i]=elements[i].getElementsByTagName('span')[1].innerText;
			}

		}
		
		if (userRoles["Approval Type"].includes("Manager Approval") || userRoles["Approval Type"].includes("Owner Approval")) {
			console.log("Approval type is either manager or owner approval");
			//disable all buttons instantly
			disableAllDateButtons();

			callJavaForBundle();
			console.log("After rest calls Data returned by REST call : " + dataFromRestCall);

			//now iterating the respone and again fetching the list to map
			elements = document.getElementsByClassName("row ng-scope");
			for (var key in dataFromRestCall) {
				console.log("Key = " + key);
				var lbl = document.createElement("LABEL");
				lbl.setAttribute("id", key);

				for (var i = 0, len = elements.length; i < len; i++) {
					if (i == 0 || i == 1) {
						continue;
					}
					else {
						console.log("value of I is : " + i);
						//match the response element with the div span which in return is the role name 

						console.log("elements  I innerText  : " + elements[i].getElementsByTagName('span')[1].innerText);
						console.log("key to match is  : " + key);
						if (key == elements[i].getElementsByTagName('span')[1].innerText) {
							var date = dataFromRestCall[key];
							console.log("Both key and elements innerText are same as key = " + key + " and innerHtml = " + elements[i].getElementsByTagName('span')[1].innerText);
							lbl.innerHTML = date;
							elements[i].querySelectorAll("[id^='btnSunriseSunsetApproval']")[0].appendChild(lbl);
							console.log("Chile Appended to elements[i] : " + elements[i].querySelectorAll("[id^='btnSunriseSunsetApproval']")[0] + " and breaking the loop");
							break;
						}
						//below will be the date out of response

					}

				}

			}
		}
		else
		{
			console.log("Approval type is not manager or owner approval");
		}
		
	}
	else
	{
		console.log("elements length is NOT  greater than zero so wait for 1 second");
		setTimeout(disableAndAddDates,1000);
	}
		
		
		

		
		
}

function disableAllDateButtons() {
	elements = document.getElementsByClassName("row ng-scope");
	for (var i = 0, len = elements.length; i < len; i++) {
		if (i == 0 || i == 1) {
			continue;
		}
		else {
			elements[i].querySelectorAll("[id^='btnSunriseSunsetApproval']")[0].disabled = true;

		}


	}
}

function callJavaForBundle() {
	var url = PluginHelper.getPluginRestUrl("checkforbundle/bundleDetails");
	console.log("entered callJavaForBundle going to call method with data : " + userRoles);
	
	jQuery.ajax({
		url: url,
		type: "POST",
		async: false,
		contentType: "application/json; charset=utf-8",
		beforeSend: function(request) {
			request.setRequestHeader("X-XSRF-TOKEN", PluginHelper.getCsrfToken());
		},
		data: JSON.stringify(userRoles),
		success: function(data, textStatus, jQxhr) {
			dataFromRestCall = data;
			console.log("Data returned by REST call : " + dataFromRestCall);
		},
		error: function(jqXhr, textStatus, errorThrown) {
			console.log(errorThrown);
		}
	});
}