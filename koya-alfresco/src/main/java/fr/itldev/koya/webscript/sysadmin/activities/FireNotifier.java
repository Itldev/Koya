package fr.itldev.koya.webscript.sysadmin.activities;

import java.io.IOException;

import org.alfresco.repo.activities.feed.FeedNotifier;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

public class FireNotifier extends AbstractWebScript {

	private FeedNotifier feedNotifier;

	public void setFeedNotifier(FeedNotifier feedNotifier) {
		this.feedNotifier = feedNotifier;
	}

	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
		feedNotifier.execute(0);
		res.setContentType("application/json;charset=UTF-8");
		res.getWriter().write("");

	}

}
