# Thymeleaf Processor

Renders a *Thymeleaf* **HTML template** together with a **CSS file** and the dynamic data, given as a **JSON file**
into HTML (and optionally PDF).

The project can be used *standalone* or as a backend service for the editor [thymeleaf-preview-editor](https://github.com/giraone/thymeleaf-preview-editor).

## Prerequisites

- This project is build for Java 17 (it is using records and multi line strings)
- If you want to generate PDF from Thymeleaf the [PD4ML v4](https://pd4ml.tech/) library is needed. You have to:
  - perform the Maven setup described [here](https://pd4ml.tech/support-topics/maven/) or install the lib manually - see below
  - obtain a license file `pd4ml.lic` and place it into in to `src/main/resources/pd4ml`.

### Alternative manual maven setup for PD4ML

Place the lib in `libs/pd4ml-4.0.11.jar` and perform

```shell script
mvn install:install-file -Dfile=libs/pd4ml-4.0.14.jar \
    -DgroupId=com.pd4ml -DartifactId=pd4ml \
    -Dversion=4.0.14 -Dpackaging=jar
```

## Troubleshooting

### PDF Display

- Ensure, that your browser (especially *Firefox*) is configured correctly to display PDFs directly (and not externally with Acrobat or download it).
- For perfect colors in documents, you may have to place color profiles in `src/main/resources/color-profiles`.

## Release Notes

- 0.3.0, 08.07.2022
  - Dependency upgrade (e.g. Spring Boot 2.7.1, PD4ML 4.0.14)
- 0.2.0, 17.01.202
  - Support for PDF generation via PD4Ml added
  - Switch to Java 17
- 0.0.1, 07.02.2021
  - Initial version
