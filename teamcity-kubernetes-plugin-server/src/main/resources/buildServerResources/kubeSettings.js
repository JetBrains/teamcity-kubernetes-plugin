if (!BS) BS = {};
if (!BS.Kube) BS.Kube = {};

if(!BS.Kube.ProfileSettingsForm) BS.Kube.ProfileSettingsForm = OO.extend(BS.PluginPropertiesForm, {

    testConnectionUrl: '',

    templates: {
        imagesTableRow: $j('<tr class="imagesTableRow">\
<td class="dockerImage highlight"></td>\
<td class="maxInstances highlight"></td>\
<td class="edit highlight"><a href="#" class="editVmImageLink">edit</a></td>\
<td class="remove"><a href="#" class="removeVmImageLink">delete</a></td>\
        </tr>')},

    _dataKeys: [ 'dockerImage', 'pool', 'maxInstances' ],

    selectors: {
        rmImageLink: '.removeVmImageLink',
        editImageLink: '.editVmImageLink',
        imagesTableRow: '.imagesTableRow'
    },

    _errors: {
        badParam: 'Bad parameter',
        required: 'This field cannot be blank',
        nonNegative: 'Must be non-negative number'
    },

    _displayedErrors: {},

    initialize: function(){
        this.$imagesTable = $j('#kubeImagesTable');
        this.$imagesTableWrapper = $j('.imagesTableWrapper');
        this.$emptyImagesListMessage = $j('.emptyImagesListMessage');

        this.$showAddImageDialogButton = $j('#showAddImageDialogButton');
        this.$addImageButton = $j('#kubeAddImageButton');
        this.$cancelAddImageButton = $j('#kubeCancelAddImageButton');
        this.$authStrategySelector = $j('#authStrategy');

        this.$podSpecModeSelector = $j('#podTemplateMode');
        this.$dockerImage = $j('#dockerImage');
        this.$imagePullPolicy = $j('#imagePullPolicy');
        this.$dockerCommand = $j('#dockerCmd');
        this.$dockerArgs = $j('#dockerArgs');
        this.$deploymentName = $j('#source-deployment');
        this.$customPodTemplate = $j('#custom-pod-template');
        this.$maxInstances = $j('#maxInstances');

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

        this._resetDataAndDialog();

        BS.Clouds.Admin.CreateProfileForm.checkIfModified();
    },

    _bindHandlers: function () {
        var self = this;

        //// Click Handlers
        this.$showAddImageDialogButton.on('click', this._showDialogClickHandler.bind(this));
        this.$addImageButton.on('click', this._submitDialogClickHandler.bind(this));
        this.$cancelAddImageButton.on('click', this._cancelDialogClickHandler.bind(this));
        this.$imagesTable.on('click', this.selectors.rmImageLink, function () {
            var $this = $j(this),
                id = $this.data('image-id'),
                name = self.imagesData[id].dockerImage;

            if (confirm('Are you sure you want to remove the image "' + name + '"?')) {
                self.removeImage($this);
            }
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
        this.$podSpecModeSelector.on('change', this._togglePodSpecMode.bind(this));

        ///// Change handlers
        this.$dockerImage.on('change', function (e, value) {
            if (arguments.length === 1) {
                this._image['dockerImage'] = this.$dockerImage.val();
            } else {
                this.$dockerImage.val(value);
            }
            this.validateOptions(e.target.getAttribute('data-id'));
        }.bind(this));

        this.$imagePullPolicy.on('change', function(e, value) {
            if (arguments.length === 1) {
                this._image['imagePullPolicy'] = this.$imagePullPolicy.val();
            } else {
                this.$imagePullPolicy.val(value);
            }
            this.validateOptions(e.target.getAttribute('data-id'));
        }.bind(this));

        this.$dockerCommand.on('change', function (e, value) {
            if (arguments.length === 1) {
                this._image['dockerCommand'] = this.$dockerCommand.val();
            } else {
                this.$dockerCommand.val(value);
            }
            this.validateOptions(e.target.getAttribute('data-id'));
        }.bind(this));

        this.$dockerArgs.on('change', function (e, value) {
            if (arguments.length === 1) {
                this._image['dockerArgs'] = this.$dockerArgs.val();
            } else {
                this.$dockerArgs.val(value);
            }
            this.validateOptions(e.target.getAttribute('data-id'));
        }.bind(this));

        this.$maxInstances.on('change', function (e, value) {
            if (arguments.length === 1) {
                this._image['maxInstances'] = this.$maxInstances.val();
            } else {
                this.$maxInstances.val(value);
            }
            this.validateOptions(e.target.getAttribute('data-id'));
        }.bind(this));
    },

    _resetImageData: function () {
        this.image = [];
    },

    _showDialogClickHandler: function () {
        if (! this.$showAddImageDialogButton.attr('disabled')) {
            this.showAddImageDialog();
        }
        return false;
    },

    _cancelDialogClickHandler: function () {
        BS.Kube.ImageDialog.close();
        return false;
    },

    _submitDialogClickHandler: function() {
        if (this.validateOptions()) {
            if (this.$addImageButton.val().toLowerCase() === 'edit') {
                this.editImage(this.$addImageButton.data('image-id'));
            } else {
                this.addImage();
            }
            BS.Kube.ImageDialog.close();
        }
        return false;
    },

    _renderImagesTable: function () {
        this._clearImagesTable();

        if (this._imagesDataLength) {
            Object.keys(this.imagesData).forEach(function (imageId) {
                var src = this.imagesData[imageId]['source-id'];
                $j('#initial_images_list').val($j('#initial_images_list').val() + src + ",");
                this._renderImageRow(this.imagesData[imageId], imageId);
            }.bind(this));
        }

        this._toggleImagesTable();
        BS.Clouds.Admin.CreateProfileForm.checkIfModified();
    },

    _renderImageRow: function (props, id) {
        var $row = this.templates.imagesTableRow.clone().attr('data-image-id', id);

        this._dataKeys.forEach(function (className) {
            $row.find('.' + className).text(props[className]);
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
        this.$emptyImagesListMessage.toggleClass('hidden', toggle);
        this.$imagesTable.toggleClass('hidden', !toggle);
    },

    _toggleAuth: function () {
        var selectedStrategyId = this.$authStrategySelector.val();
        $j('.auth-ui').toggleClass('hidden', true);
        if(selectedStrategyId) {
            $j('.' + selectedStrategyId).removeClass('hidden');
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
        var maxInstances = this._image['maxInstances'];

        var validators = {
            dockerImage : function () {
                if (!this._image['dockerImage']) {
                    this.addOptionError('required', 'dockerImage');
                    isValid = false;
                }
            }.bind(this),

            maxInstances: function () {
                if (!$j.isNumeric(maxInstances) || maxInstances < 0 ) {
                    this.addOptionError('nonNegative', 'maxInstances');
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
        (target || this.$fetchOptionsError)
            .append($j('<div>').html(errorHTML));
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

        this._image = {
            maxInstances: 1
        };

        BS.Kube.ImageDialog.showCentered();
    },

    showEditImageDialog: function ($elem) {
        var imageId = $elem.parents(this.selectors.imagesTableRow).data('image-id');

        $j('#KubeImageDialogTitle').text('Edit Kubernetes Cloud Image');

        BS.Hider.addHideFunction('KubeImageDialog', this._resetDataAndDialog.bind(this));

        typeof imageId !== 'undefined' && (this._image = $j.extend({}, this.imagesData[imageId]));
        this.$addImageButton.val('Edit').data('image-id', imageId);
        if (imageId === 'undefined'){
            this.$addImageButton.removeData('image-id');
        }

        var image = this._image;

        this.$podSpecModeSelector.trigger('change', image['podTemplateMode'] || '');
        this.$dockerImage.trigger('change', image['dockerImage'] || '');
        this.$imagePullPolicy.trigger('change', image['imagePullPolicy'] || '');
        this.$dockerCommand.trigger('change', image['dockerCmd'] || '');
        this.$dockerArgs.trigger('change', image['dockerArgs'] || '');
        this.$deploymentName.trigger('change', image['source-deployment'] || '');
        this.$customPodTemplate.trigger('change', image['custom-pod-template'] || '');
        this.$maxInstances.trigger('change', image['maxInstances'] || '');

        BS.Kube.ImageDialog.showCentered();
    },

    addImage: function () {
        console.info('addimage called');
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

    removeImage: function ($elem) {
        delete this.imagesData[$elem.data('image-id')];
        this._imagesDataLength -= 1;
        $elem.parents(this.selectors.imagesTableRow).remove();
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
        console.info('_resetDataAndDialog called');

        this._image = {};

        this.$podSpecModeSelector.trigger('change', '0');
        this.$dockerImage.trigger('change', '');
        this.$imagePullPolicy.trigger('change', '');
        this.$dockerCommand.trigger('change', '');
        this.$dockerArgs.trigger('change', '');
        this.$deploymentName.trigger('change', '');
        this.$customPodTemplate.trigger('change', '');
        this.$maxInstances.trigger('change', '1');
    }
});

if(!BS.Kube.ImageDialog) BS.Kube.ImageDialog = OO.extend(BS.AbstractModalDialog, {
    getContainer: function() {
        return $('KubeImageDialog');
    }
});