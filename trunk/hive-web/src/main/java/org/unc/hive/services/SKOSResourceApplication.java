package org.unc.hive.services;

import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.Router;
import org.restlet.data.Request;

public class SKOSResourceApplication extends Application {

    /**
     * Creates a root Restlet that will receive all incoming calls.
     */
    @Override
    public synchronized Restlet createRoot() {
    	
        // Create a router Restlet that defines routes.
        Router router = new Router(getContext());

        // Defines a route for the resource "list of concepts"
        router.attach("/{vocabularyName}/?{SPARQLquery}", ConceptListResource.class);
        
        return router;
    }
	
}
