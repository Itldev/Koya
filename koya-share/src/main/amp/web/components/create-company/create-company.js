// --
//  Koya is an alfresco module that provides a corporate orientated dataroom.
//
//  Copyright (C) Itl Developpement 2014
//
//  This program is free software: you can redistribute it and/or modify
//  it under the terms of the GNU Affero General Public License as
//  published by the Free Software Foundation, either version 3 of the
//  License, or (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU Affero General Public License for more details.
//
//  You should have received a copy of the GNU Affero General Public License
//  along with this program.  If not, see `<http://www.gnu.org/licenses/>`.
// --

/**
 * CreateCompany module
 *
 * A dialog for creating companies site
 *
 * @namespace Koya.module
 * @class Koya.CreateCompany
 */
// Ensure Koya namespace exists
if (typeof Koya == "undefined" || !Extras)
{
    var Koya = {};
}

(function()
{
    /**
     * YUI Library aliases
     */
    var Dom = YAHOO.util.Dom,
            Event = YAHOO.util.Event,
            Element = YAHOO.util.Element;

    /**
     * Alfresco Slingshot aliases
     */
    var $html = Alfresco.util.encodeHTML;

    /**
     * CreateCompany constructor.
     *
     * @param htmlId {string} A unique id for this component
     * @return {Alfresco.CreateCompany} The new DocumentList instance
     * @constructor
     */
    Koya.CreateCompany = function(htmlId)
    {
        this.name = "Koya.CreateCompany";
        Koya.CreateCompany.superclass.constructor.call(this, htmlId);

        /* Register this component */
        Alfresco.util.ComponentManager.register(this);

        /* Load YUI Components */
        Alfresco.util.YUILoaderHelper.require(["button", "container", "connection", "selector", "json"], this.onComponentsLoaded, this);

        /* Define panel handlers */
        var parent = this;


        // NOTE: the panel registered first is considered the "default" view and is displayed first

        /* Options Panel Handler */
        CreateCompanyPanelHandler = function CreateCompanyPanelHandler_constructor()
        {
            CreateCompanyPanelHandler.superclass.constructor.call(this, "create-company");
        };

        YAHOO.extend(CreateCompanyPanelHandler, Alfresco.ConsolePanelHandler,
                {
                    /**
                     * Called by the CreateCompanyPanelHandler when this panel shall be loaded
                     *
                     * @method onLoad
                     */
                    onLoad: function onLoad()
                    {

                        // Create the cancel button
                        parent.widgets.cancelButton = Alfresco.util.createYUIButton(parent, "create-company-cancel-button", this.onCancelButtonClick);

                        // Create the ok button, the forms runtime will handle when its clicked
                        parent.widgets.okButton = Alfresco.util.createYUIButton(parent, "create-company-ok-button", null,
                                {
                                    type: "submit"
                                });

                        // Site access form controls
                        // Configure the forms runtime
                        var createSiteForm = new Alfresco.forms.Form(parent.id + "-create-company-form");
                        parent.widgets.form = createSiteForm;

                        var elTitle = Dom.get(parent.id + "-create-company-title");

                        /**
                         * Title field
                         */
                        // Title is mandatory
                        createSiteForm.addValidation(elTitle, Alfresco.forms.validation.mandatory, null, "keyup", parent.msg("validation-hint.mandatory"));
                        // ...and has a maximum length
                        createSiteForm.addValidation(elTitle, Alfresco.forms.validation.length,
                                {
                                    max: 256,
                                    crop: true
                                }, "keyup");

                        

//                        var sitePresetEl = Dom.get(parent.id + "-create-company-sitePreset");

                        var sitePresetEl = Dom.get(parent.id + "-create-company-sitePreset");
                        Event.addListener(sitePresetEl, "change", function CreateCompany_sitePreset_change()
                        {
                            parent.onSitePresetChange(sitePresetEl.options[sitePresetEl.selectedIndex].value);
                        }, parent, true);
                        if (sitePresetEl.options.length > 0)
                        {
                            parent.onSitePresetChange(sitePresetEl.options[sitePresetEl.selectedIndex].value);
                        }

                        // The ok button is the submit button, and it should be enabled when the form is ready
                        createSiteForm.setSubmitElements([parent.widgets.okButton]);

                        // Submit as an ajax submit (not leave the page), in json format
                        createSiteForm.setAJAXSubmit(true,
                                {
                                    successCallback:
                                            {
                                                fn: parent.onCreateCompanySuccess,
                                                scope: parent
                                            },
                                    failureCallback:
                                            {
                                                fn: parent.onCreateCompanyFailure,
                                                scope: parent
                                            }
                                });
                        createSiteForm.setSubmitAsJSON(true);
//                        createSiteForm.doBeforeAjaxRequest = {
//                            fn: this.doBeforeAjaxRequest,
//                            scope: this
//                        };

                        createSiteForm.init();
                    },
//                    /**
//                     * Called when a site has been succesfully created on the server.
//                     * Redirects the user to the new site.
//                     *
//                     * @method onCreateCompanySuccess
//                     * @param response
//                     */
//                    onCreateCompanySuccess: function CreateCompany_onCreateCompanySuccess(response)
//                    {
//                        if (response.json !== undefined && response.json.success)
//                        {
//                            // The site has been successfully created - add it to the user's favourites and navigate to it
//                            var preferencesService = new Alfresco.service.Preferences(),
//                                    shortName = response.config.dataObj.shortName;
//
//                            preferencesService.favouriteSite(shortName,
//                                    {
//                                        successCallback:
//                                                {
//                                                    fn: function CreateCompany_onCreateCompanySuccess_successCallback()
//                                                    {
//                                                        document.location.href = Alfresco.constants.URL_PAGECONTEXT + "site/" + shortName + "/dashboard";
//                                                    }
//                                                }
//                                    });
//                        }
//                        else
//                        {
//                            parent._adjustGUIAfterFailure(response);
//                        }
//                    },
//                    /**
//                     * Called when a site failed to be created.
//                     *
//                     * @method onCreateCompanyFailure
//                     * @param response
//                     */
//                    onCreateCompanyFailure: function CreateCompany_onCreateCompanyFailure(response)
//                    {
//                        this._adjustGUIAfterFailure(response);
//                    },
//                    /**
//                     * Helper method that restores the gui and displays an error message.
//                     *
//                     * @method _adjustGUIAfterFailure
//                     * @param response
//                     */
//                    _adjustGUIAfterFailure: function CreateCompany__adjustGUIAfterFailure(response)
//                    {
//                        parent.widgets.cancelButton.set("disabled", false);
//                        var text = Alfresco.util.message("message.failure", parent.name);
//
//                        if (response.serverResponse.status === 403)
//                        {
//                            // User does not have permissions to create the site
//                            if (response.json.message)
//                            {
//                                text = Alfresco.util.message(response.json.message, parent.name)
//                            }
//                            else
//                            {
//                                text = Alfresco.util.message("error.noPermissions", parent.name);
//                            }
//                        }
//                        else if (response.json.message)
//                        {
//                            var tmp = Alfresco.util.message(response.json.message, parent.name);
//                            text = tmp ? tmp : text;
//                        }
//                        Alfresco.util.PopupManager.displayPrompt(
//                                {
//                                    title: Alfresco.util.message("message.failure", parent.name),
//                                    text: text
//                                });
//                    },
                    /**
                     * Called before the form is about to be submitted
                     *
                     * @method doBeforeFormSubmit
                     * @param form {HTMLFormElement} The create site form
                     * @param obj {Object} Callback object
                     */
                    doBeforeFormSubmit: function(form, obj)
                    {
                        var formEl = Dom.get(this.id + "create-company-form");

                        parent.widgets.cancelButton.set("disabled", true);

                        parent.widgets.feedbackMessage = Alfresco.util.PopupManager.displayMessage(
                                {
                                    text: Alfresco.util.message("message.creating", parent.name),
                                    spanClass: "wait",
                                    displayTime: 0
                                });
                    }
                });
        new CreateCompanyPanelHandler();

        return this;
    };


    YAHOO.extend(Koya.CreateCompany, Alfresco.ConsoleTool,
            {
                /**
                 * Called when a preset as been selected.
                 * Implement to make it possible to dispay custom site property fields
                 *
                 * @method onSitePresetChange
                 * @param sitePreset
                 */
                onSitePresetChange: function(sitePreset) {
                },
                /**
                 * Called when user clicks on the cancel button.
                 * Closes the CreateCompany panel.
                 *
                 * @method onCancelButtonClick
                 * @param type
                 * @param args
                 */
                onCancelButtonClick: function CreateCompany_onCancelButtonClick(type, args)
                {
                    // Reset the form fields
                    try
                    {
                        Dom.get(this.id + "-create-company-title").value = "";
                        Dom.get(this.id + "-create-company-sitePreset").selectedIndex = 0;
                    }
                    catch (e)
                    {
                    }

                },
                /**
                 * Called when a site has been succesfully created on the server.
                 * Redirects the user to the new site.
                 *
                 * @method onCreateCompanySuccess
                 * @param response
                 */
                onCreateCompanySuccess: function CreateCompany_onCreateCompanySuccess(response)
                {
                    if (response.json !== undefined && response.json.success)
                    {
                        // The site has been successfully created - add it to the user's favourites and navigate to it
                        var preferencesService = new Alfresco.service.Preferences(),
                                shortName = response.json.shortName;

                        preferencesService.favouriteSite(shortName,
                                {
                                    successCallback:
                                            {
                                                fn: function CreateCompany_onCreateCompanySuccess_successCallback()
                                                {
                                                    document.location.href = Alfresco.constants.URL_PAGECONTEXT + "site/" + shortName + "/dashboard";
                                                }
                                            }
                                });
                    }
                    else
                    {
                        this._adjustGUIAfterFailure(response);
                    }
                },
                /**
                 * Called when a site failed to be created.
                 *
                 * @method onCreateCompanyFailure
                 * @param response
                 */
                onCreateCompanyFailure: function CreateCompany_onCreateCompanyFailure(response)
                {
                    this._adjustGUIAfterFailure(response);
                }
                ,
                /**
                 * Helper method that restores the gui and displays an error message.
                 *
                 * @method _adjustGUIAfterFailure
                 * @param response
                 */
                _adjustGUIAfterFailure: function CreateCompany__adjustGUIAfterFailure(response)
                {
//                    this.widgets.feedbackMessage.destroy();
                    this.widgets.cancelButton.set("disabled", false);
//                    this.widgets.panel.show();
                    var text = Alfresco.util.message("message.failure", this.name);

                    if (response.serverResponse.status === 403)
                    {
                        // User does not have permissions to create the site
                        if (response.json.message)
                        {
                            text = Alfresco.util.message(response.json.message, this.name)
                        }
                        else
                        {
                            text = Alfresco.util.message("error.noPermissions", this.name);
                        }
                    }
                    else if (response.json.message)
                    {
                        var tmp = Alfresco.util.message(response.json.message, this.name);
                        text = tmp ? tmp : text;
                    }
                    Alfresco.util.PopupManager.displayPrompt(
                            {
                                title: Alfresco.util.message("message.failure", this.name),
                                text: text
                            });
                }
//                ,

            });
})();
//
//Alfresco.module.getCreateCompanyInstance = function()
//{
//    var instanceId = "koya-createCompany-instance";
//    return Alfresco.util.ComponentManager.get(instanceId) || new Koya.CreateCompany(instanceId);
//};
