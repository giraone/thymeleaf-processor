# Thymeleaf Processor

Renders a *Thymeleaf* **HTML template** together with a **CSS file** and the dynamic data, given as a **JSON file**
into HTML (and optionally PDF).

The project can be used *standalone* or as a backend service for the editor [thymeleaf-preview-editor](https://github.com/giraone/thymeleaf-preview-editor).

## Prerequisites

- This project is build for Java 17 (it is using records and multi line strings)
- If you want to generate PDF from Thymeleaf, you need to place the license file `pd4ml.lic` in to `src/main/resources/pd4ml`.

## Release Notes

- 0.2.0, 17.01.202
  - Support for PDF generation via PD4Ml added
  - Switch to Java 17
- 0.0.1, 07.02.2021
  - Initial version
