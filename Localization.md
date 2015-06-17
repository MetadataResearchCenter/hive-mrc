# Localization #

The HIVE-Web user interface can be translated into any language. Four basic steps are required:
  * Translate the HTML files
  * Translate the HIVEMessages.properties
  * Run the HIVEMessages-i18n script
  * Re-buile HIVE-Web (`ant deploywar`)

## Translating the HTML files ##

The Home, Concept Browser, and Indexer HTML pages can be translated by simply copying the English HTML files (e.g., home.html) to `filename_locale.html` (e.g., home\_es.html). Each file should also contain a `<meta>` tag specifying the default page locale for GWT.

`<meta name="gwt:property" content="locale=es">`


## GWT ##

The HIVE-Web application also supports GWT localization using [static string internationalization](https://developers.google.com/web-toolkit/doc/1.6/tutorial/i18n).

The file `src/org/unc/hive/client/HIVEMessages.properties` contains the HIVE-Web GWT strings in English. To create a new translation, copy this file to `HIVEMessages_locale.properties`.

Each time a new `HIVEMessages.properties` is added or edited, you need to run the `HIVEMessage-i18n` script. This generates the `src/org/unc/hive/client/HIVEMessages.java`.

To use the `HIVEMessages` class:

```
private HIVEMessages messages = (HIVEMessages)GWT.create(HIVEMessages.class);
System.out.println(messages.your_message());
```

