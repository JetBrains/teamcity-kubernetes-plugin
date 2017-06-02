if (!BS) BS = {};
if (!BS.Kube) BS.Kube = {};

if(!BS.Kube.ProfileSettingsForm) BS.Kube.ProfileSettingsForm = OO.extend(BS.AbstractPasswordForm, {

    testConnectionUrl: '',

    testConnection: function() {
        var that = this;
        var info = "";
        var success = true;
        var url = this.testConnectionUrl;
        console.info("calling " + url);
        BS.PasswordFormSaver.save(that, url, OO.extend(BS.ErrorsAwareListener, {
            onBeginSave: function(form) {
                form.setSaving(true);
                form.disable();
            },

            onTestConnectionFailedError: function(elem) {
                if (success) {
                    info = "";
                } else if ("" != info) {
                    info += "\n";
                }
                info += elem.textContent || elem.text;
                success = false;
            },

            onCompleteSave: function (form, responseXML, err) {
                BS.XMLResponse.processErrors(responseXML, that, null);
                BS.TestConnectionDialog.show(success, success ? "" : info, null);
                form.setSaving(false);
                form.enable();
            }
        }));
    }
});