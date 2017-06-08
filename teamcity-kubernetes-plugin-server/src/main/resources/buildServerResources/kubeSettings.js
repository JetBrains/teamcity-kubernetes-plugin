if (!BS) BS = {};
if (!BS.Kube) BS.Kube = {};

if(!BS.Kube.ProfileSettingsForm) BS.Kube.ProfileSettingsForm = OO.extend(BS.PluginPropertiesForm, {

    testConnectionUrl: '',

    initialize: function(){
        this.$imagesTable = $j('#kubeImagesTable');

        var rawImagesData = $j('#source_images_json').val() || '[]';
        try {
            this.imagesData = JSON.parse(rawImagesData);
        } catch (e) {
            this.imagesData = [];
            BS.Log.error('bad images data: ' + rawImagesData);
        }

        this._renderImagesTable();
        $j('#addKubeImageDialogButton').removeAttr('disabled');
        BS.Clouds.Admin.CreateProfileForm.checkIfModified();
    },

    _renderImagesTable: function () {
        this._clearImagesTable();

        if (this._imagesDataLength) {
            Object.keys(this.imagesData).forEach(function (imageId) {
                var src = this.data[imageId]['source-id'];
                $j('#initial_images_list').val($j('#initial_images_list').val() + src + ",");
                // this._renderImageRow(this.data[imageId], imageId);
            }.bind(this));
        }
    },

    _clearImagesTable: function () {
        this.$imagesTable.find('.imagesTableRow').remove();
    },

    testConnection: function() {
        BS.ajaxRequest(this.testConnectionUrl, {
            parameters: BS.Clouds.Admin.CreateProfileForm.serializeParameters(),
            onFailure: function (response) {
                BS.TestConnectionDialog.show(false, response, null);
            }.bind(this),
            onSuccess: function (response) {
                var wereErrors = BS.XMLResponse.processErrors(response.responseXML, {
                    onConnectionError: function(elem) {
                        BS.TestConnectionDialog.show(false, elem.firstChild.nodeValue, null);
                    }
                }, BS.PluginPropertiesForm.propertiesErrorsHandler);
                if(!wereErrors){
                    BS.TestConnectionDialog.show(true, "", null);
                }
            }.bind(this)
        });
    }
});