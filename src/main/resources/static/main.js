function testJsonToHtmlSimple() {
  const jsonContent = '{"name": "User"}';
  const templateContent = '<div>Hello&nbsp;<span th:text="${name}">World</span>!</div>';
  const cssContent = '';
  const expected = 'Hello User!';
  multipartUploadForHtml('/api/json-to-html', jsonContent, templateContent, cssContent, expected);
}

function testJsonToHtmlWithCss() {
  const jsonContent = '{"name": "Red"}';
  const templateContent = '<html><head><style></style></head><body><div>Hello&nbsp;<span class="strong" th:text="${name}">World</span>!</div></body></html>';
  const cssContent = '.strong { color: red; font-weight: bold; }';
  const expected = 'Hello Red!';
  multipartUploadForHtml('/api/json-to-html', jsonContent, templateContent, cssContent, expected);
}

function testJsonToPdfWithCss() {
  const jsonContent = '{"name": "Red"}';
  const templateContent = '<html><head><style></style></head><body><div>Hello&nbsp;<span class="strong" th:text="${name}">World</span>!</div></body></html>';
  const cssContent = '.strong { color: red; font-weight: bold; }';
  multipartUploadForPdf('/api/json-to-pdf', jsonContent, templateContent, cssContent);
}

function testJsonToPdfWithFonts() {
  const jsonContent = '{"name": "Red"}';
  const templateContent = '<html><head><style></style></head><body><div>Hello&nbsp;<span class="strong" th:text="${name}">World</span>!</div></body></html>';
  const cssContent = '* { font-family: "Bad Script", cursive; } .strong { color: red; font-weight: bold; }';
  multipartUploadForPdf('/api/json-to-pdf', jsonContent, templateContent, cssContent);
}

function multipartUploadForHtml(url, jsonContent, templateContent, cssContent, expected) {

  const formData = new FormData();
  formData.append('data', new Blob([jsonContent], { type: 'application/json;charset=UTF-8' }), 'data.json');
  formData.append('template', new Blob([templateContent], { type: 'text/html;charset=UTF-8'}), 'template.html');
  formData.append('css', new Blob([cssContent], { type: 'text/css;charset=UTF-8'}), 'template.css');

  const req = new XMLHttpRequest();
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

function multipartUploadForPdf(url, jsonContent, templateContent, cssContent, errorCallback) {

  const formData = new FormData();
  formData.append('data', new Blob([jsonContent], { type: 'application/json;charset=UTF-8' }), 'data.json');
  formData.append('template', new Blob([templateContent], { type: 'text/html;charset=UTF-8'}), 'template.html');
  formData.append('css', new Blob([cssContent], { type: 'text/css;charset=UTF-8'}), 'template.css');

  const contentType = 'application/pdf';
  const req = new XMLHttpRequest();
  req.open('POST', url);
  req.setRequestHeader('Accept', contentType);
  req.responseType = 'arraybuffer';
  req.onload = function() {
    if (req.status == 200) {
      const arrayBuffer = req.response; // Note: not req.responseText!
      if (arrayBuffer) {
        const blob = new Blob([req.response], {type: contentType});
        const nextUrl = URL.createObjectURL(blob);
        window.open(nextUrl, '_blank');
      } else {
         alert("No content returned from " + url);
      }
    } else {
      showError(req.status, req.statusText, '');
    }
  };

  req.send(formData);
}

function showHtml(htmlContent, expected) {
  document.getElementById('status').textContent = 'Status: OK. Expected: ' + expected;
  document.getElementById('htmlPreview').src = 'data:text/html;charset=iso8859-1,' + escape(htmlContent);
}

function showError(status, statusText, errorText) {
  document.getElementById('status').textContent = 'ERROR: ' + statusText + '(' + status + ')';
  document.getElementById('htmlPreview').src = '';
}