function test1() {
   const jsonContent = '{"name": "User"}';
   const templateContent = '<div>Hello&nbsp;<span th:text="${name}">World</span>!</div>';
   const cssContent = '';
   const expected = 'Hello World';
   multipartUpload('/api/json-to-html', jsonContent, templateContent, cssContent, showHtml, showError, expected);
}

function test2() {
   const jsonContent = '{"name": "Red"}';
   const templateContent = '<div>Hello&nbsp;<span class="strong" th:text="${name}">World</span>!</div>';
   const cssContent = '.strong { color: red; font-weight: bold; }';
   const expected = 'Hello Red';
   multipartUpload('/api/json-to-html', jsonContent, templateContent, cssContent, showHtml, showError, expected);
}

function multipartUpload(url, jsonContent, templateContent, cssContent, successCallback, errorCallback, expected) {

   const formData = new FormData();
   formData.append('data', new Blob([jsonContent], { type: 'application/json;charset=UTF-8' }), 'data.json');
   formData.append('template', new Blob([templateContent], { type: 'text/html;charset=UTF-8'}), 'template.html');
   formData.append('css', new Blob([cssContent], { type: 'text/css;charset=UTF-8'}), 'template.css');

   let req = new XMLHttpRequest();
   req.open('POST', url);
   req.setRequestHeader('Accept', 'text/html');

   req.onload = function() {
       if (req.status == 200) {
           showHtml(req.responseText, expected);
       } else {
           showError(req.status, req.statusText, req.responseText);
       }
   }

   req.send(formData);
}

function showHtml(htmlContent, expected) {
    document.getElementById('status').textContent = 'Status: OK. Expected: ' + expected;
    document.getElementById('htmlPreview').src = 'data:text/html;charset=iso8859-1,' + escape(htmlContent);
}

function showError(status, statusText, errorText, htmlContent) {
    document.getElementById('status').textContent = 'ERROR: ' + statusText + '(' + status + ')';
    document.getElementById('htmlPreview').src = 'data:text/html;charset=iso8859-1,' + escape(htmlContent);
}