function onloadFunction() {
    //- - - OBJECTS - - -

    let selectedReportName = 'simple';

    const options = {
        mode: 'code',
        onError: function (err) {
            alert(err.toString())
        },
        onModeChange: function (newMode, oldMode) {
            console.log('Mode switched from', oldMode, 'to', newMode)
        }
    };

    const initialJsonData = {
        "object1": {
            "fieldA": "Whow!",
            "fieldB": "That's cool!"
        }
    };

    const initialTemplateText = "<dl>\n" +
            "  <dt>Label 1</dt>\n" +
            "  <dd th:text=\"${object1.fieldA}\">Value 1</dd>\n" +
            "  <dt>Label 2</dt>\n" +
            "  <dd th:text=\"${object1.fieldB}\">Value 2</dd>\n" +
            "</dl>";

    // collapsable windows
    const dataToggle = document.getElementById('toggleData');
    const dataWindow = document.getElementById('dataWindow');
    const dataEditor = document.getElementById('dataEditor');
    const dataHeader = document.getElementById('dataHeader');

    const templateToggle = document.getElementById('toggleTemplate');
    const templateWindow = document.getElementById('templateWindow');
    const templateEditor = document.getElementById('templateEditor');
    const templateHeader = document.getElementById('templateHeader');

    const htmlPreviewWindow = document.getElementById('htmlPreviewWindow');
    const htmlPreviewHeader = document.getElementById('htmlPreviewHeader');

    const containerData = document.getElementById('dataEditor');
    const containerTemplate = document.getElementById('templateEditor');
    const editorData = new JSONEditor(containerData, options, initialJsonData);
    const editorTemplate = new JSONEditor(containerTemplate, options, initialTemplateText);

    //- - - LOAD JSON TEST DATA and TEMPLATE - - -

    readJsonStringFromUrlFake('/api/v1/template-names', '["simple", "lohnkonto", "geburtstagsliste", "lohnsteueranmeldung"]', function (jsonString) {
        console.log('reportNames = ' + jsonString);
        let names = JSON.parse(jsonString);
        console.log('reportNames = ' + names.length);
        let selectBox = document.getElementById('reportNames');
        while (selectBox.options.length > 0) {
            selectBox.remove(selectBox.options.length - 1);
        }
        for (let i = 0; i < names.length; i++) {
            const opt = document.createElement('option');
            opt.text = names[i] + '.html'
            opt.value = names[i];
            selectBox.add(opt, null);
        }
    });

    initializeForReportName(selectedReportName);

    //- - - CALLBACKS - - -

    document.getElementById('dataWindow').onmouseup = function () {
        editorData.resize();
    };

    document.getElementById('templateWindow').onmouseup = function () {
        editorTemplate.resize();
    };

    document.getElementById('loadData').onclick = function () {
        document.getElementById('loadDataFile').click();
    };

    document.getElementById('saveData').onclick = function () {
        let fname = window.prompt('Save JSON sample data...')
        if (fname.indexOf('.') === -1) {
            fname = fname + '.json'
        } else {
            if (fname.split('.').pop().toLowerCase() === 'json') {
                // Nothing to do
            } else {
                fname = fname.split('.')[0] + '.json'
            }
        }
        const blob = new Blob([editorData.getText()], {type: 'application/json;charset=utf-8'})
        saveAs(blob, fname)
    };

    document.getElementById('loadTemplate').onclick = function () {
        document.getElementById('loadTemplateFile').click();
    };

    document.getElementById('saveTemplate').onclick = function () {
        let fname = window.prompt('Save template ...')
        if (fname.indexOf('.') === -1) {
            fname = fname + '.json'
        } else {
            if (fname.split('.').pop().toLowerCase() === 'json') {
                // Nothing to do
            } else {
                fname = fname.split('.')[0] + '.json'
            }
        }
        const blob = new Blob([editorTemplate.getText()], {type: 'application/json;charset=utf-8'})
        saveAs(blob, fname)
    };

    document.getElementById('apply').onclick = function () {
        process(selectedReportName);
    };

    document.getElementById('reportNames').onchange = function () {
        selectedReportName = document.getElementById('reportNames').value;
        console.log('New selectedReportName = ' + selectedReportName);
        initializeForReportName(selectedReportName, function () {
            process(selectedReportName);
        });
    };

   FileReaderJS.setupInput(document.getElementById('loadDataFile'), {
        readAsDefault: 'Text',
        on: {
            load: function (event) {
                editorData.setText(event.target.result)
            }
        }
    });

    FileReaderJS.setupInput(document.getElementById('loadTemplateFile'), {
        readAsDefault: 'Text',
        on: {
            load: function (event) {
                editorTemplate.setText(event.target.result)
            }
        }
    });

    // Run the template
    process(selectedReportName);

    //- - - FUNCTIONS - - -

    function initializeForReportName(templateName, callback) {
        readJsonStringFromUrl('/document-templates/' + templateName + '.html', '{}', function (htmlString) {
            console.log('template.html = ' + htmlString);
            editorTemplate.setText(htmlString);
            readJsonStringFromUrl('/data/' + templateName + '-testdata.json', '[]', function (jsonString) {
                console.log('testdata.json = ' + jsonString);
                editorData.setText(jsonString);
                if (callback) {
                    callback();
                }
            });
        });
    }

    function loadHtml(htmlString) {
        document.getElementById('htmlPreviewControl').src = 'data:text/html;charset=iso8859-1,' + escape(htmlString);
    }

    function process(templateName) {

        console.log('process templateName=' + templateName);

        const url = '/api/v1/multipart-to-html';
        const jsonData = editorData.getText();
        let templateContent = editorTemplate.getText();
        // Wg. JsonEditor
        templateContent = templateContent.substr(1, templateContent.length - 2);
        const cssContent = 'body { background-color: red; }';

        const formData = new FormData();
        formData.append('data', new Blob([jsonData], { type: 'application/json;charset=UTF-8' }), 'data.json');
        formData.append('template', new Blob([templateContent], { type: 'text/html;charset=UTF-8'}), 'template.html');
        formData.append('css', new Blob([cssContent], { type: 'text/css;charset=UTF-8'}), 'template.css');

        let req = new XMLHttpRequest();
        req.open('POST', url);
        req.setRequestHeader('Accept', 'text/html');

        req.onload = function() {
            console.log(req.responseText);
            if (req.status == 200) {
                loadHtml(req.responseText);
            } else {
                alert('Error: ' + req.statusText);
            }
        }

        req.send(formData);
    }

    function readJsonStringFromUrlFake(url, defaultValue, onloadCallback) {
        onloadCallback(defaultValue);
    }

    function readJsonStringFromUrl(url, defaultValue, onloadCallback) {
        let req = new XMLHttpRequest();
        req.open('get', url, true);
        req.setRequestHeader('Accept', 'application/json');
        //req.overrideMimeType('application/json');
        //req.responseType = 'json';
        req.onload = function () {
            if (req.status === 200) {
                onloadCallback(req.responseText);
            } else {
                alert('Error calling ' + url + ' : ' + req.statusText);
                if (defaultValue) {
                    onloadCallback(defaultValue);
                }
            }
        };
        req.send();
    }

    // draggable windows
    dataWindow.style.zIndex = 2;
    dataHeader.style.zIndex = 2;
    templateWindow.style.zIndex = 2;
    templateHeader.style.zIndex = 2;
    htmlPreviewWindow.style.zIndex = 1;
    htmlPreviewHeader.style.zIndex = 1;

    dragElement(dataWindow);
    dragElement(templateWindow);

    function dragElement(element) {
        let pos1 = 0, pos2 = 0, pos3 = 0, pos4 = 0;
        let zIndexRaised = false;

        if (document.getElementById(element.id + 'Drag')) {
            document.getElementById(element.id + 'Drag').onmousedown = dragMouseDown;
        }

        function dragMouseDown(e) {
            // the currently dragged window must be on top
            if (!zIndexRaised) {
                zIndexRaised = true;
                if (e.srcElement.id === dataWindow.id + 'Drag') {
                    dataWindow.style.zIndex = 3;
                    dataHeader.style.zIndex = 3;
                    templateWindow.style.zIndex = 2;
                    templateHeader.style.zIndex = 2;
                    htmlPreviewWindow.style.zIndex = 1;
                    htmlPreviewHeader.style.zIndex = 1;
                } else if (e.srcElement.id === templateWindow.id + 'Drag') {
                    dataWindow.style.zIndex = 2;
                    dataHeader.style.zIndex = 2;
                    templateWindow.style.zIndex = 3;
                    templateHeader.style.zIndex = 3;
                    htmlPreviewWindow.style.zIndex = 1;
                    htmlPreviewHeader.style.zIndex = 1;
                }
            }

            e = e || window.event;
            e.preventDefault();
            // get mouse cursor position at startup
            pos3 = e.clientX;
            pos4 = e.clientY;
            document.onmouseup = closeDragElement;
            // call a function whenever the cursor moves
            document.onmousemove = elementDrag;
        }

        function elementDrag(e) {
            e = e || window.event;
            e.preventDefault();
            // calculate new cursor position
            pos1 = pos3 - e.clientX;
            pos2 = pos4 - e.clientY;
            pos3 = e.clientX;
            pos4 = e.clientY;
            // set new element position
            element.style.top = (element.offsetTop - pos2) + 'px';
            element.style.left = (element.offsetLeft - pos1) + 'px';
        }

        function closeDragElement() {
            // stop moving when mouse button is released
            document.onmouseup = null;
            document.onmousemove = null;
            zIndexRaised = false;
        }
    }

    dataToggle.addEventListener('click', function() {
        if (dataEditor.style.display === 'none') {
            dataWindow.style.zIndex = 3;
            dataHeader.style.zIndex = 3;
            templateWindow.style.zIndex = 2;
            templateHeader.style.zIndex = 2;
            htmlPreviewWindow.style.zIndex = 1;
            htmlPreviewHeader.style.zIndex = 1;

            dataEditor.style.display = 'block';
            dataWindow.style.width = '680px';
            dataWindow.style.height = '440px';
            dataToggle.innerHTML = '-';
            document.getElementById('loadData').style.display = 'inline-block';
            document.getElementById('saveData').style.display = 'inline-block';
        } else {
            dataEditor.style.display = 'none';
            dataWindow.style.height = '42px';
            dataToggle.innerHTML = '+';
            document.getElementById('loadData').style.display = 'none';
            document.getElementById('saveData').style.display = 'none';
        }
    });

    templateToggle.addEventListener('click', function() {
        if (templateEditor.style.display === 'none') {
            dataWindow.style.zIndex = 2;
            dataHeader.style.zIndex = 2;
            templateWindow.style.zIndex = 3;
            templateHeader.style.zIndex = 3;
            htmlPreviewWindow.style.zIndex = 1;
            htmlPreviewHeader.style.zIndex = 1;

            templateEditor.style.display = 'block';
            templateWindow.style.width = '680px';
            templateWindow.style.height = '440px';
            templateToggle.innerHTML = '-';
            document.getElementById('loadTemplate').style.display = 'inline-block';
            document.getElementById('saveTemplate').style.display = 'inline-block';
        } else {
            templateEditor.style.display = 'none';
            templateWindow.style.height = '42px';
            templateToggle.innerHTML = '+';
            document.getElementById('loadTemplate').style.display = 'none';
            document.getElementById('saveTemplate').style.display = 'none';
        }
    });
}