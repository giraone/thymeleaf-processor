# Thymeleaf Processor

Renders a *Thymeleaf* **HTML template** together with a **CSS file** and the dynamic data, given as a **JSON file**
into HTML (and optionally PDF).

The project can be used *standalone* or as a backend service for the editor [thymeleaf-preview-editor](https://github.com/giraone/thymeleaf-preview-editor).

## Prerequisites

- This project is build for Java 17 (it is using records and multi line strings)
- If you want to generate PDF from Thymeleaf the [PD4ML v4](https://pd4ml.tech/) library is needed. You have to:
  - perform the Maven setup described [here](https://pd4ml.tech/support-topics/maven/) or install the lib manually
  - obtain a license file `pd4ml.lic` and place it into in to `src/main/resources/pd4ml`

### Using fonts for PDF generation

- See [Preparing TTF Fonts](https://pd4ml.com/support-topics/usage-examples/#ttf-fonts)
- If an environment variable `PD4ML_FONTS_FILE_SOURCE` is defined, the value is passed to the *useTTF()* method of PD4ML.
- If an environment variable `PD4ML_FONTS_HTTP_SOURCE` is defined, TTF fonts from this URL are copied to the TEMP file system
  and this font directory is passed to the *useTTF()* method of PD4ML.
- If neither `PD4ML_FONTS_FILE_SOURCE` nor `PD4ML_FONTS_HTTP_SOURCE` is defined, the directory [src/main/resources/defaultfonts](src/main/resources/defaultfonts)
  is used. It contains three sample fonts downloaded from https://google-webfonts-helper.herokuapp.com/:
  - [Roboto Regular and Roboto Italic](https://google-webfonts-helper.herokuapp.com/fonts/roboto?subsets=latin)
  - [Bad Script](https://google-webfonts-helper.herokuapp.com/fonts/bad-script?subsets=latin)
- There is an example [using-fonts](src/test/resources/testdata/input/using-fonts) to show the usage in HTML and PDF generation.
  The TTF fonts for the example are already contained in [src/main/resources/defaultfonts](src/main/resources/defaultfonts).
  WOFF/WOFF2 fonts have to be added manually to new directory [src/main/resources/static/fonts](src/main/resources/static/fonts),
  when  the Thymeleaf HTML output should be displayed properly in a browser.

### PDF/A 1b support

PD4ML can create PDF/A 1b. To use it, set `USE_PDF_A` in
[RenderHtmlController.java](src/main/java/com/giraone/thymeleaf/controller/RenderHtmlController.java) to true
and ensure, that all prerequisites for PDF/A 1b are fulfilled (font embedding, Metadata setup, image formats, color profile).

## CSS

The CSS passed in the multipart request, must be integrated into the HTML code. To perform this, the content is
placed into an empty `<style>` tag, which must be present in the HTML header. Example:

```html
<html>
<head>
    <style></style>
</head>
<body>
<div>Hello&nbsp;<span class="strong" th:text="${name}">World</span>!</div>
</body>
</html>
```

## Troubleshooting

### Limits

- The limit for a complete multipart request is currently 2MB and 1 MB for each file. See [application.yml](src/main/resources/application.yml).

### PDF Display

- Ensure, that your browser (especially *Firefox*) is configured correctly to display PDFs directly (and not externally with Acrobat or download it).
- For perfect colors in documents, you may have to place color profiles in `src/main/resources/color-profiles`.

## Release Notes

- 0.3.3, 23.10.2022
  - Example for using fonts (WOFF for HTML, TTF for PDF) improved
  - Font copying refactored. Fonts within src/main/resource/defaultfonts can be placed in subdirectories
- 0.3.2, 03.10.2022
  - Maven plugin `versions-maven-plugin` added
  - Dependency upgrade
- 0.3.1, 28.09.2022
  - Dependency upgrade (e.g. Spring Boot 2.7.4, PD4ML 4.0.15)
- 0.3.0, 08.07.2022
  - Dependency upgrade (e.g. Spring Boot 2.7.1, PD4ML 4.0.14)
  - Extensions for date formatting based on ISO 8601 date strings added
- 0.2.0, 17.01.202
  - Support for PDF generation via PD4Ml added
  - Switch to Java 17
- 0.0.1, 07.02.2021
  - Initial version
