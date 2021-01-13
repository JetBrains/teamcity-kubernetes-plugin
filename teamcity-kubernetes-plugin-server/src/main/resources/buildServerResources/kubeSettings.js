/*
 * Copyright 2000-2021 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

if (!BS) BS = {};
if (!BS.Kube) BS.Kube = {
    serializeParameters: function() {
        var parameters = BS.Clouds.Admin.CreateProfileForm.serializeParameters();
        var split = parameters.split('&');
        var result = '';
        for (var i = 0; i < split.length; i++) {
            var pair = split[i].split('=');
            if (pair[0] === 'prop%3Asource_images_json'){
                continue;
            }
            if (pair[0].startsWith('prop:encrypted:')){
                // check whether value is empty or not
                var s = pair[0].substr('prop:encrypted:'.length);
                if (!$j('#' + s.replace(':', '\\:')).val())
                    continue;
            }
            result += '&'
            result += split[i];
        }
        return result.substr(1);
    }
};

if(!BS.Kube.ProfileSettingsForm) BS.Kube.ProfileSettingsForm = OO.extend(BS.PluginPropertiesForm, {

    testConnectionUrl: '',

    templates: {
        imagesTableRow: $j('<tr class="imagesTableRow">\
<td class="imageDescription highlight"></td>\
<td class="imageInstanceLimit highlight"></td>\
<td class="edit highlight"><a href="#" class="editVmImageLink">Edit</a></td>\
<td class="remove"><a href="#" class="removeVmImageLink">Delete...</a></td>\
        </tr>')},

    _dataKeys: [ 'imageDescription', 'dockerImage', 'agent_pool_id', 'imageInstanceLimit', 'customPodTemplate', 'podTemplateMode', 'sourceDeployment', 'agentNamePrefix' ],

    selectors: {
        rmImageLink: '.removeVmImageLink',
        editImageLink: '.editVmImageLink',
        imagesTableRow: '.imagesTableRow'
    },

    _errors: {
        badParam: 'Bad parameter',
        required: 'The field must not be empty',
        notSelectedPodTemplateMode: 'Select pod specification',
        notSelectedAgentPool: 'Select agent pool',
        nonNegative: 'Must be non-negative number',
        invalidAgentNamePrefix: 'Invalid agent name prefix'
    },

    _displayedErrors: {},

    defaults: {
        imageInstanceLimit: '<Unlimited>'
    },

    initialize: function(){
        this.$imagesTable = $j('#kubeImagesTable');
        this.$imagesTableWrapper = $j('.imagesTableWrapper');

        this.$authStrategySelector = $j('#authStrategy');
        this.$eksUseInstanceProfile = $j('#eksUseInstanceProfile');
        this.$eksAssumeIAMRole = $j('#eksAssumeIAMRole');

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
        this.$customPodTemplate = $j('#customPodTemplate');
        this.$agentNamePrefix = $j('#agentNamePrefix');
        this.$imageInstanceLimit = $j('#imageInstanceLimit');
        this.$agentPoolSelector = $j('#agent_pool_id');

        this.$imagesDataElem = $j('#' + 'source_images_json');

        var self = this;
        var rawImagesData = this.$imagesDataElem.val() || '[]';
        this._imagesDataLength = 0;
        try {
            var imagesData = JSON.parse(rawImagesData);
            this.imagesData = imagesData.reduce(function (accumulator, imageDataStr) {
                accumulator[self._imagesDataLength++] = imageDataStr;
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
        this._toggleEKSCredentials();
        this._toggleEKSIAM();

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
        this.$eksUseInstanceProfile.on('change', this._toggleEKSCredentials.bind(this));
        this.$eksAssumeIAMRole.on('change', this._toggleEKSIAM.bind(this));

        this.$podSpecModeSelector.on('change', function(e, value) {
            if (arguments.length === 1) {
                this._image['podTemplateMode'] = this.$podSpecModeSelector.val();
                this._updateImageDescription(this._image);
            } else {
                this.$podSpecModeSelector.val(value);
            }
            this._togglePodSpecMode();
        }.bind(this));

        ///// Change handlers
        this.$dockerImage.on('change', function (e, value) {
            if (arguments.length === 1) {
                this._image['dockerImage'] = this.$dockerImage.val();
                this._updateImageDescription(this._image);
            } else {
                this.$dockerImage.val(value);
            }
        }.bind(this));

        this.$imagePullPolicy.on('change', function(e, value) {
            if (arguments.length === 1) {
                this._image['imagePullPolicy'] = this.$imagePullPolicy.val();
                this._updateImageDescription(this._image);
            } else {
                this.$imagePullPolicy.val(value);
            }
        }.bind(this));

        this.$dockerCommand.on('change', function (e, value) {
            if (arguments.length === 1) {
                this._image['dockerCmd'] = this.$dockerCommand.val();
                this._updateImageDescription(this._image);
            } else {
                this.$dockerCommand.val(value);
            }
        }.bind(this));

        this.$dockerArgs.on('change', function (e, value) {
            if (arguments.length === 1) {
                this._image['dockerArgs'] = this.$dockerArgs.val();
                this._updateImageDescription(this._image);
            } else {
                this.$dockerArgs.val(value);
            }
        }.bind(this));

        this.$deploymentName.on('change', function (e, value) {
            if(value !== undefined) this.$deploymentName.val(value);
            this._image['sourceDeployment'] = this.$deploymentName.val();
            this._updateImageDescription(this._image);
        }.bind(this));

        this.$agentNamePrefix.on('change', function (e, value) {
            if (arguments.length === 1) {
                this._image['agentNamePrefix'] = this.$agentNamePrefix.val();
                this._updateImageDescription(this._image);
            } else {
                this.$agentNamePrefix.val(value);
            }
        }.bind(this));

        this.$imageInstanceLimit.on('change', function (e, value) {
            if (arguments.length === 1) {
                this._image['imageInstanceLimit'] = this.$imageInstanceLimit.val();
            } else {
                this.$imageInstanceLimit.val(value);
            }
        }.bind(this));

        this.$agentPoolSelector.on('change', function(e, value) {
            if (arguments.length === 1) {
                this._image['agent_pool_id'] = this.$agentPoolSelector.val();
            } else {
                this.$agentPoolSelector.val(value);
            }
        }.bind(this));

        this.$customPodTemplate
        .on('change', function(e, data){
            var val = e.target.value;
            if (arguments.length === 1) {
                self._image[this.getAttribute('id')] = val;
            } else {
                this.value = data;
                $j(this).trigger('cm-set-value', data);
            }
        })
        .on('cm-change', function(e, value){
            self._image[this.getAttribute('id')] = value;
        });
    },

    _updateImageDescription: function (image) {
        var imageDescription = '';
        var podSpecMode = image['podTemplateMode'];
        if(podSpecMode){
            switch (podSpecMode){
                case 'custom-pod-template':
                    imageDescription = 'Custom pod template: ' + image['agentNamePrefix'];
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
            $j('.auth-ui.' + selectedStrategyId).removeClass('hidden');
        }
        //workaround for TW-51797
        BS.MultilineProperties.updateVisible();
        this._toggleEKSIAM();
    },

    _toggleEKSCredentials: function () {
        var selectedStrategyId = this.$authStrategySelector.val();
        if (selectedStrategyId == "eks") {
            var checked = this.$eksUseInstanceProfile.is(":checked");
            $j('.eks.aws-credential').toggleClass('hidden', checked);

            //workaround for TW-51797
            BS.MultilineProperties.updateVisible();
        }
    },

    _toggleEKSIAM: function () {
        var selectedStrategyId = this.$authStrategySelector.val();
        if (selectedStrategyId == "eks") {
            var checked = this.$eksAssumeIAMRole.is(":checked");
            $j('.eks.aws-iam').toggleClass('hidden', !checked);

            //workaround for TW-51797
            BS.MultilineProperties.updateVisible();
        }
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
                    this.addOptionError('notSelectedPodTemplateMode', 'podTemplateMode');
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

            agentNamePrefix: function(){
                if (this._image['podTemplateMode'] === 'custom-pod-template' && !this._image['agentNamePrefix']) {
                    this.addOptionError('required', 'agentNamePrefix');
                    isValid = false;
                } else if (this._image['agentNamePrefix']){
                    if (!(/([A-Za-z0-9][-A-Za-z0-9_.]*)?[A-Za-z0-9]$/.test(this._image['agentNamePrefix']))){
                        this.addOptionError('invalidAgentNamePrefix', 'agentNamePrefix');
                        isValid = false;
                    }
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
                if (!agentPoolId || agentPoolId === '' || agentPoolId === undefined) {
                    this.addOptionError('notSelectedAgentPool', 'agent_pool_id');
                    isValid = false;
                }
            }.bind(this),

            customPodTemplate : function () {
                var valueToValidate = this._image['customPodTemplate'];
                if (this._image['podTemplateMode'] === 'custom-pod-template' && (!valueToValidate || valueToValidate === '')) {
                    this.addOptionError('required', 'customPodTemplate');
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
            parameters: BS.Kube.serializeParameters(),
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
        this.$customPodTemplate.trigger('change', image['customPodTemplate'] || '');

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
        var newImageId = this.generateNewImageId(),
            newImage = this._image;
        this.setupSourceId(newImage);
        this._renderImageRow(newImage, newImageId);
        this.imagesData[newImageId] = newImage;
        this._imagesDataLength += 1;
        this.saveImagesData();
        this._toggleImagesTable();
    },

    setupSourceId: function(image){
        var namePrefix = $j.trim(image.agentNamePrefix);
        if (namePrefix !== ''){
            image['source-id'] = namePrefix;
        } else {
            var mode = $j('#podTemplateMode').val();
            if (mode === 'deployment-base'){
                image['source-id'] = $j('#sourceDeployment').val();
            } else if (mode === 'simple'){
                image['source-id'] = $j('#dockerImage').val();
            }
        }
    },

    generateNewImageId: function () {
        if($j.isEmptyObject(this.imagesData)) return 1;
        return Math.max.apply(Math, $j.map(this.imagesData, function callback(currentValue) {
            return currentValue['source-id'];
        })) + 1;
    },

    editImage: function (id) {
        this.setupSourceId(this._image);
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
        this.$customPodTemplate.trigger('change', '');
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
            parameters: BS.Kube.serializeParameters(),
            url: dataLoadUrl,
            method: "post",
            shift:{x:15,y:15}
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
            parameters: BS.Kube.serializeParameters(),
            url: dataLoadUrl,
            method: "post",
            shift:{x:15,y:15}
        });
    };

    BS.Kube.DeploymentChooser.selectDeployment = function (deployment) {
        BS.Kube.ProfileSettingsForm.selectDeployment(deployment);
        this.hidePopup();
    };
}