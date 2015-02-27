//Add companysite aspect for for existing sites - link company home folder
var sites = search.luceneSearch('+TYPE:"st:site"');

for each (var s in sites){
	if(!s.hasAspect("koya:companySite")){
		var companyPropertiesFile = search.luceneSearch('PATH:"/app:company_home/st:sites/cm:'+s.name+'/cm:koya-config/cm:company.properties"');
		//add aspect
		var props = new Array(1);
		props["koya:companyHome"] = companyPropertiesFile[0].nodeRef;		
		s.addAspect("koya:companySite", props);
		s.save();
	}	
}
