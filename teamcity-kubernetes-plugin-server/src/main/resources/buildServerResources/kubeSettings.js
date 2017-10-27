if (!BS) BS = {};
if (!BS.Kube) BS.Kube = {};

if(!BS.Kube.ProfileSettingsForm) BS.Kube.ProfileSettingsForm = OO.extend(BS.PluginPropertiesForm, {

    testConnectionUrl: '',

    templates: {
        imagesTableRow: $j('<tr class="imagesTableRow">\
<td class="imageDescription highlight"></td>\
<td class="imageInstanceLimit highlight"></td>\
<td class="edit highlight"><a href="#" class="editVmImageLink">Edit</a></td>\
<td class="remove"><a href="#" class="removeVmImageLink">Delete...</a></td>\
        </tr>')},

    _dataKeys: [ 'imageDescription', 'dockerImage', 'agent_pool_id', 'imageInstanceLimit', 'podTemplateMode', 'sourceDeployment', 'agentNamePrefix' ],

    selectors: {
        rmImageLink: '.removeVmImageLink',
        editImageLink: '.editVmImageLink',
        imagesTableRow: '.imagesTableRow'
    },

    _errors: {
        badParam: 'Bad parameter',
        required: 'This field cannot be blank',
        notSelected: 'Something should be seleted',
        nonNegative: 'Must be non-negative number'
    },

    _displayedErrors: {},

    defaults: {
        imageInstanceLimit: '<Unlimited>'
    },

    initialize: function(){
        this.$imagesTable = $j('#kubeImagesTable');
        this.$imagesTableWrapper = $j('.imagesTableWrapper');

        this.$authStrategySelector = $j('#authStrategy');

        this.$showAddImageDialogButton = $j('#showAddImageDialogButton');
        this.$addImageButton = $j('#kubeAddImageButton');
        this.$cancelAddImageButton = $j('#kubeCancelAddImageButton');

        this.$deleteImageButton = $j('#kubeDeleteImageButton');
        this.$cancelDeleteImageButton = $j('#kubeCancelDeleteImageButton');

        this.$podSpecModeSelector = $j('#podTemplateMode');
        this.$dockerImage = $j('#dockerImage');
        this.$imagePullPolicy = $j('#imagePullPolicy');
        this.$dockerCommand = $j('#dockerCmd');
        this.$dockerArgs = $j('#dockerArgs');
        this.$deploymentName = $j('#sourceDeployment');
        this.$agentNamePrefix = $j('#agentNamePrefix');
        this.$imageInstanceLimit = $j('#imageInstanceLimit');
        this.$agentPoolSelector = $j('#agent_pool_id');

        this.$imagesDataElem = $j('#' + 'source_images_json');

        var self = this;
        var rawImagesData = this.$imagesDataElem.val() || '[]';
        this._lastImageId = this._imagesDataLength = 0;
        try {
            var imagesData = JSON.parse(rawImagesData);
            this.imagesData = imagesData.reduce(function (accumulator, imageDataStr) {
                accumulator[self._lastImageId++] = imageDataStr;
                self._imagesDataLength++;
                return accumulator;
            }, {});
        } catch (e) {
            this.imagesData = {};
            BS.Log.error('bad images data: ' + rawImagesData);
        }

        this._bindHandlers();
        this._renderImagesTable();
        this.$addImageButton.removeAttr('disabled');
        this._toggleAuth();

        this._resetDataAndDialog();

        BS.Clouds.Admin.CreateProfileForm.checkIfModified();
    },

    _bindHandlers: function () {
        var self = this;

        //// Click Handlers
        this.$showAddImageDialogButton.on('click', this._showAllImageDialogClickHandler.bind(this));
        this.$addImageButton.on('click', this._submitImageDialogClickHandler.bind(this));
        this.$cancelAddImageButton.on('click', this._cancelImageDialogClickHandler.bind(this));

        this.$deleteImageButton.on('click', this._submitDeleteImageDialogClickHandler.bind(this));
        this.$cancelDeleteImageButton.on('click', this._cancelDeleteImageDialogClickHandler.bind(this));

        this.$imagesTable.on('click', this.selectors.rmImageLink, function () {
            self.showDeleteImageDialog($j(this));
            return false;
        });
        var editDelegates = this.selectors.imagesTableRow + ' .highlight, ' + this.selectors.editImageLink;
        var that = this;
        this.$imagesTable.on('click', editDelegates, function () {
            if (!that.$addImageButton.prop('disabled')) {
                self.showEditImageDialog($j(this));
            }
            return false;
        });

        this.$authStrategySelector.on('change', this._toggleAuth.bind(this));

        this.$podSpecModeSelector.on('change', function(e, value) {
            if (arguments.length === 1) {
                this._image['podTemplateMode'] = this.$podSpecModeSelector.val();
                this._updateImageDescription(this._image);
            } else {
                this.$podSpecModeSelector.val(value);
            }
            this._togglePodSpecMode();
            this.validateOptions(e.target.getAttribute('data-id'));
        }.bind(this));

        ///// Change handlers
        this.$dockerImage.on('change', function (e, value) {
            if (arguments.length === 1) {
                this._image['dockerImage'] = this.$dockerImage.val();
                this._updateImageDescription(this._image);
            } else {
                this.$dockerImage.val(value);
            }
            this.validateOptions(e.target.getAttribute('data-id'));
        }.bind(this));

        this.$imagePullPolicy.on('change', function(e, value) {
            if (arguments.length === 1) {
                this._image['imagePullPolicy'] = this.$imagePullPolicy.val();
                this._updateImageDescription(this._image);
            } else {
                this.$imagePullPolicy.val(value);
            }
            this.validateOptions(e.target.getAttribute('data-id'));
        }.bind(this));

        this.$dockerCommand.on('change', function (e, value) {
            if (arguments.length === 1) {
                this._image['dockerCmd'] = this.$dockerCommand.val();
                this._updateImageDescription(this._image);
            } else {
                this.$dockerCommand.val(value);
            }
            this.validateOptions(e.target.getAttribute('data-id'));
        }.bind(this));

        this.$dockerArgs.on('change', function (e, value) {
            if (arguments.length === 1) {
                this._image['dockerArgs'] = this.$dockerArgs.val();
                this._updateImageDescription(this._image);
            } else {
                this.$dockerArgs.val(value);
            }
            this.validateOptions(e.target.getAttribute('data-id'));
        }.bind(this));

        this.$deploymentName.on('change', function (e, value) {
            if(value !== undefined) this.$deploymentName.val(value);
            this._image['sourceDeployment'] = this.$deploymentName.val();
            this._updateImageDescription(this._image);
            this.validateOptions(e.target.getAttribute('data-id'));
        }.bind(this));

        this.$agentNamePrefix.on('change', function (e, value) {
            if (arguments.length === 1) {
                this._image['agentNamePrefix'] = this.$agentNamePrefix.val();
                this._updateImageDescription(this._image);
            } else {
                this.$agentNamePrefix.val(value);
            }
            this.validateOptions(e.target.getAttribute('data-id'));
        }.bind(this));

        this.$imageInstanceLimit.on('change', function (e, value) {
            if (arguments.length === 1) {
                this._image['imageInstanceLimit'] = this.$imageInstanceLimit.val();
            } else {
                this.$imageInstanceLimit.val(value);
            }
            this.validateOptions(e.target.getAttribute('data-id'));
        }.bind(this));

        this.$agentPoolSelector.on('change', function(e, value) {
            if (arguments.length === 1) {
                this._image['agent_pool_id'] = this.$agentPoolSelector.val();
            } else {
                this.$agentPoolSelector.val(value);
            }
            this.validateOptions(e.target.getAttribute('data-id'));
        }.bind(this));
    },

    _updateImageDescription: function (image) {
        var imageDescription = '';
        var podSpecMode = image['podTemplateMode'];
        if(podSpecMode){
            switch (podSpecMode){
                case 'custom-pod-template':
                    imageDescription = 'Custom pod template';
                    break;
                case 'deployment-base':
                    imageDescription = 'Use deployment: ' + image['sourceDeployment'];
                    break;
                case 'simple':
                    imageDescription = 'Run container: ' + image['dockerImage'];
                    break;
                default : imageDescription = 'UNKNOWN';
            }
        }
        image['imageDescription'] = imageDescription;
    },

    _showAllImageDialogClickHandler: function () {
        if (! this.$showAddImageDialogButton.attr('disabled')) {
            this.showAddImageDialog();
        }
        return false;
    },

    _cancelImageDialogClickHandler: function () {
        BS.Kube.ImageDialog.close();
        return false;
    },

    _submitImageDialogClickHandler: function() {
        if (this.validateOptions()) {
            if (this.$addImageButton.val().toLowerCase() === 'save') {
                this.editImage(this.$addImageButton.data('image-id'));
            } else {
                this.addImage();
            }
            BS.Kube.ImageDialog.close();
        }
        return false;
    },

    _cancelDeleteImageDialogClickHandler: function () {
        BS.Kube.DeleteImageDialog.close();
        return false;
    },

    _submitDeleteImageDialogClickHandler: function() {
        var imageId = BS.Kube.DeleteImageDialog.currentImageId;
        BS.ajaxRequest(BS.Kube.DeleteImageDialog.url + window.location.search, {
            method: 'post',
            parameters : {
                imageId : imageId
            },
            onComplete: function() {
                BS.Kube.ProfileSettingsForm.removeImage(imageId);
                BS.Kube.DeleteImageDialog.close();
            }
        });
    },

    _renderImagesTable: function () {
        this._clearImagesTable();

        if (this._imagesDataLength) {
            Object.keys(this.imagesData).forEach(function (imageId) {
                var image = this.imagesData[imageId];
                var src = image['source-id'];
                $j('#initial_images_list').val($j('#initial_images_list').val() + src + ",");
                this._updateImageDescription(image);
                this._renderImageRow(image, imageId);
            }.bind(this));
        }

        this._toggleImagesTable();
        BS.Clouds.Admin.CreateProfileForm.checkIfModified();
    },

    _renderImageRow: function (props, id) {
        var $row = this.templates.imagesTableRow.clone().attr('data-image-id', id);
        var defaults = this.defaults;

        this._dataKeys.forEach(function (className) {
            $row.find('.' + className).text(props[className] || defaults[className]);
        });

        $row.find(this.selectors.rmImageLink).data('image-id', id);
        $row.find(this.selectors.editImageLink).data('image-id', id);
        this.$imagesTable.append($row);
    },

    _clearImagesTable: function () {
        this.$imagesTable.find('.imagesTableRow').remove();
    },

    _toggleImagesTable: function () {
        var toggle = !!this._imagesDataLength;
        this.$imagesTableWrapper.removeClass('hidden');
        this.$imagesTable.toggleClass('hidden', !toggle);
    },

    _toggleAuth: function () {
        var selectedStrategyId = this.$authStrategySelector.val();
        $j('.auth-ui').toggleClass('hidden', true);
        if(selectedStrategyId) {
            $j('.' + selectedStrategyId).removeClass('hidden');
        }
        //workaround for TW-51797
        BS.MultilineProperties.updateVisible();
    },

    _togglePodSpecMode: function () {
        var selectedMode = this.$podSpecModeSelector.val();
        $j('.pod-spec-ui').toggleClass('hidden', true);
        if(selectedMode) {
            $j('.' + selectedMode).removeClass('hidden');
        }
    },

    validateOptions: function (options){
        var isValid = true;

        var validators = {

            podTemplateMode : function () {
                var podTemplateMode = this._image['podTemplateMode'];
                if (!podTemplateMode || podTemplateMode === 'notSelected' || podTemplateMode === 'undefined') {
                    this.addOptionError('notSelected', 'podTemplateMode');
                    isValid = false;
                }
            }.bind(this),

            dockerImage : function () {
                if (this._image['podTemplateMode'] === 'simple' && !this._image['dockerImage']) {
                    this.addOptionError('required', 'dockerImage');
                    isValid = false;
                }
            }.bind(this),

            sourceDeployment : function () {
                if (this._image['podTemplateMode'] === 'deployment-base' && !this._image['sourceDeployment']) {
                    this.addOptionError('required', 'sourceDeployment');
                    isValid = false;
                }
            }.bind(this),

            imageInstanceLimit: function () {
                var imageInstanceLimit = this._image['imageInstanceLimit'];
                if (imageInstanceLimit && (!$j.isNumeric(imageInstanceLimit) || imageInstanceLimit < 0 )) {
                    this.addOptionError('nonNegative', 'imageInstanceLimit');
                    isValid = false;
                }
            }.bind(this),

            agent_pool_id : function () {
                var agentPoolId = this._image['agent_pool_id'];
                if (!agentPoolId || agentPoolId === '' || agentPoolId === 'undefined') {
                    this.addOptionError('notSelected', 'agent_pool_id');
                    isValid = false;
                }
            }.bind(this)
        };

        if (options && ! $j.isArray(options)) {
            options = [options];
        }

        this.clearOptionsErrors(options);

        (options || this._dataKeys).forEach(function(option) {
            if(validators[option]) validators[option]();
        });

        return isValid;
    },

    addOptionError: function (errorKey, optionName) {
        var html;

        if (errorKey && optionName) {
            this._displayedErrors[optionName] = this._displayedErrors[optionName] || [];

            if (typeof errorKey !== 'string') {
                html = this._errors[errorKey.key];
                Object.keys(errorKey.props).forEach(function(key) {
                    html = html.replace('%%'+key+'%%', errorKey.props[key]);
                });
                errorKey = errorKey.key;
            } else {
                html = this._errors[errorKey];
            }

            if (this._displayedErrors[optionName].indexOf(errorKey) === -1) {
                this._displayedErrors[optionName].push(errorKey);
                this.addError(html, $j('.option-error_' + optionName));
            }
        }
    },

    addError: function (errorHTML, target) {
        target.append($j('<div>').html(errorHTML));
    },

    clearOptionsErrors: function (options) {
        (options || this._dataKeys).forEach(function (optionName) {
            this.clearErrors(optionName);
        }.bind(this));
    },

    clearErrors: function (errorId) {
        var target = $j('.option-error_' + errorId);
        if (errorId) {
            delete this._displayedErrors[errorId];
        }
        target.empty();
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
    },

    showAddImageDialog: function () {
        $j('#KubeImageDialogTitle').text('Add Kubernetes Cloud Image');

        BS.Hider.addHideFunction('KubeImageDialog', this._resetDataAndDialog.bind(this));
        this.$addImageButton.val('Add').data('image-id', 'undefined');

        this._image = {};

        BS.Kube.ImageDialog.showCentered();
    },

    showEditImageDialog: function ($elem) {
        var imageId = $elem.parents(this.selectors.imagesTableRow).data('image-id');

        $j('#KubeImageDialogTitle').text('Edit Kubernetes Cloud Image');

        BS.Hider.addHideFunction('KubeImageDialog', this._resetDataAndDialog.bind(this));

        typeof imageId !== 'undefined' && (this._image = $j.extend({}, this.imagesData[imageId]));
        this.$addImageButton.val('Save').data('image-id', imageId);
        if (imageId === 'undefined'){
            this.$addImageButton.removeData('image-id');
        }

        var image = this._image;

        this.$podSpecModeSelector.trigger('change', image['podTemplateMode'] || 'notSelected');
        this.$dockerImage.trigger('change', image['dockerImage'] || '');
        this.$imagePullPolicy.trigger('change', image['imagePullPolicy'] || 'IfNotPresent');
        this.$dockerCommand.trigger('change', image['dockerCmd'] || '');
        this.$dockerArgs.trigger('change', image['dockerArgs'] || '');
        this.selectDeployment(image['sourceDeployment']);
        this.$agentNamePrefix.trigger('change', image['agentNamePrefix'] || '');
        this.$imageInstanceLimit.trigger('change', image['imageInstanceLimit'] || '');
        this.$agentPoolSelector.trigger('change', image['agent_pool_id'] || '');

        BS.Kube.ImageDialog.showCentered();
    },

    showDeleteImageDialog: function ($elem) {
        var imageId = $elem.parents(this.selectors.imagesTableRow).data('image-id');

        BS.ajaxUpdater($("kubeDeleteImageDialogBody"), BS.Kube.DeleteImageDialog.url + window.location.search, {
            method: 'get',
            parameters : {
                imageId : imageId
            },
            onComplete: function() {
                BS.Kube.DeleteImageDialog.show(imageId);
            }
        });
    },

    selectDeployment: function (deployment) {
        this.$deploymentName.trigger('change', deployment || '');
    },

    addImage: function () {
        var newImageId = this._lastImageId++,
            newImage = this._image;
        newImage['source-id'] = newImageId;
        this._renderImageRow(newImage, newImageId);
        this.imagesData[newImageId] = newImage;
        this._imagesDataLength += 1;
        this.saveImagesData();
        this._toggleImagesTable();
    },

    editImage: function (id) {
        this._image['source-id'] = id;
        this.imagesData[id] = this._image;
        this.saveImagesData();
        this.$imagesTable.find(this.selectors.imagesTableRow).remove();
        this._renderImagesTable();
    },

    removeImage: function (imageId) {
        delete this.imagesData[imageId];
        this._imagesDataLength -= 1;
        this.$imagesTable.find('tr[data-image-id=\'' + imageId + '\']').remove();
        this.saveImagesData();
        this._toggleImagesTable();
    },

    saveImagesData: function () {
        var imageData = Object.keys(this.imagesData).reduce(function (accumulator, id) {
            var _val = $j.extend({}, this.imagesData[id]);

            delete _val.$image;
            accumulator.push(_val);

            return accumulator;
        }.bind(this), []);
        this.$imagesDataElem.val(JSON.stringify(imageData));
    },

    _resetDataAndDialog: function () {
        this._image = {};

        this.$podSpecModeSelector.trigger('change', 'notSelected');
        this.$dockerImage.trigger('change', '');
        this.$imagePullPolicy.trigger('change', 'IfNotPresent');
        this.$dockerCommand.trigger('change', '');
        this.$dockerArgs.trigger('change', '');
        this.selectDeployment('');
        this.$agentNamePrefix.trigger('change', '');
        this.$imageInstanceLimit.trigger('change', '');
        this.$agentPoolSelector.trigger('change', '');
    }
});

if(!BS.Kube.ImageDialog) BS.Kube.ImageDialog = OO.extend(BS.AbstractModalDialog, {
    getContainer: function() {
        return $('KubeImageDialog');
    }
});

if(!BS.Kube.DeleteImageDialog) BS.Kube.DeleteImageDialog = OO.extend(BS.AbstractModalDialog, {
    url: '',
    currentImageId: '',

    getContainer: function() {
        return $('KubeDeleteImageDialog');
    },

    show: function (imageId) {
        BS.Kube.DeleteImageDialog.currentImageId = imageId;
        BS.Kube.DeleteImageDialog.showCentered();
    }
});

if(!BS.Kube.NamespaceChooser){
    BS.Kube.NamespaceChooser = new BS.Popup('namespaceChooser', {
        hideDelay: 0,
        hideOnMouseOut: false,
        hideOnMouseClickOutside: true,
        loadingText: "Loading namespaces..."
    });

    BS.Kube.NamespaceChooser.showPopup = function(nearestElement, dataLoadUrl){
        this.showPopupNearElement(nearestElement, {
            parameters: BS.Clouds.Admin.CreateProfileForm.serializeParameters(),
            url: dataLoadUrl
        });
    };

    BS.Kube.NamespaceChooser.selectNamespace = function (namespace) {
        $j("#kubernetes-namespace").val(namespace || '');
        this.hidePopup();
    };
}

if(!BS.Kube.DeploymentChooser){
    BS.Kube.DeploymentChooser = new BS.Popup('deploymentChooser', {
        hideDelay: 0,
        hideOnMouseOut: false,
        hideOnMouseClickOutside: true,
        loadingText: "Loading deployments..."
    });

    BS.Kube.DeploymentChooser.showPopup = function(nearestElement, dataLoadUrl){
        this.showPopupNearElement(nearestElement, {
            parameters: BS.Clouds.Admin.CreateProfileForm.serializeParameters(),
            url: dataLoadUrl
        });
    };

    BS.Kube.DeploymentChooser.selectDeployment = function (deployment) {
        BS.Kube.ProfileSettingsForm.selectDeployment(deployment);
        this.hidePopup();
    };
}