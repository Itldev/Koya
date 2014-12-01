/*
*  Company deleting script 
*   Removes all invitation, users permission and users involved in company
*/


function deleteCompany(companyToDel,apply){
	
	var site = siteService.getSite(companyToDel);
	if(site === null){
		logger.log("company : "+companyToDel +" doesn't exists" );
		return;
	}
	
	logger.log("company to delete : " );
	logger.log("=======================");
	
	
	var wfdef = workflow.getDefinitionByName("activiti$activitiInvitationNominated");
	/*
Get all invitationNominatded instances
*/
	for each(var w in wfdef.getActiveInstances()) {
		var props = w.getPaths()[0].getTasks()[0].getProperties();
		if (site.shortName === props["{http://www.alfresco.org/model/workflow/invite/nominated/1.0}resourceName"]) {
			logger.log("Delete invitation for user " + props["{http://www.alfresco.org/model/workflow/invite/nominated/1.0}inviteeEmail"]);
			if (!apply) {
				w.cancel();
				var inviteeUserName = props["{http://www.alfresco.org/model/workflow/invite/nominated/1.0}inviteeUserName"];
				var inviteUserSites =  siteService.listUserSites(inviteeUserName);
				if(inviteUserSites.length === 0){
					people.deletePerson(inviteeUserName);
				}
			}
		}
	}
	
	
	
	/*
Iterate On Site Members to remove membership
*/
	for (var m in site.listMembers(null, null)) {
		var user = people.getPerson(m);
		var username = user.properties['cm:userName'];
		var usermail = user.properties['cm:email'];
		
		/*
		Remove membership on the site to delete
		*/
		var userSiteMembershipLimit = 1;
		if (!apply) {
			try{
				site.removeMembership(username);
			}catch(e){
				logger.log("INFO : cannot remove user '"+ username +"' site membership");
			}
			userSiteMembershipLimit = 0;
		}
		
		var userSites = siteService.listUserSites(username, 100000);
		
		if (userSites.length > userSiteMembershipLimit) {
			logger.log("DO NOT DELETE (multisites) : " + usermail);
		} else {
			//on vérifie également s'il n'appartiens pas a des groupes publics          
			if (people.getContainerGroups(user).length > 0) {
				logger.log("DO NOT DELETE (belongs public groups) : " + usermail);
			} else {
				logger.log("DELETE USER : " + usermail);
				
				if (!apply) {
					//effective user deletion
					people.deletePerson(username);
				}
			}
		}
	}
	
	if (!apply) {
		//effective site deletion
		site.deleteSite();
		logger.log(site.shortName + " Deleted");
	}
}



//usage deleteCompany(comapnyName,apply)

deleteCompany("CompanyName",false);

