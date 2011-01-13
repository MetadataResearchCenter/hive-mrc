package org.unc.hive.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import com.claudiushauptmann.gwt.multipage.client.MultipageEntryPoint;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.DecoratedPopupPanel;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitEvent;
import com.google.gwt.widgetideas.client.GlassPanel;
import com.google.gwt.user.client.ui.FlexTable;

@MultipageEntryPoint(urlPattern = "/indexing.html")
//@MultipageEntryPoint(urlPattern = "/(.*)/indexing.html")
public class Indexer implements EntryPoint {

	private CaptionPanel indexingCaption;
	private String fileName;
	private List<String> openedVocabularies;  // store the name of current loaded vocabularies in client side
	private List<String> allVocabulary; // store the name of all vocabularies that hive have
	private FlowPanel addVocabularyPanel;
	private HorizontalPanel configure;	
	private Button openNewVocabulary; 
	private FlexTable indexingTable;
	private GlassPanel glass;
	private PopupPanel uploadPopup;
	private HorizontalPanel deleteFile; 
	private String fileToProcess;
	private SimplePanel conceptInfo;
	private DockPanel resultDock;
	private final IndexerServiceAsync indexerService = GWT
			.create(IndexerService.class);
	private final ConceptBrowserServiceAsync conceptBrowserService = GWT.create(ConceptBrowserService.class);
	
	private boolean isFileUploaded;
	private boolean isURL;

	@Override
	public void onModuleLoad() {
		
		conceptBrowserService.getAllVocabulariesName(new AsyncCallback<List<String>>()
				{

					@Override
					public void onFailure(Throwable caught) {
						// TODO Auto-generated method stub
						Window.alert(caught.getMessage());
					}

					@Override
					public void onSuccess(List<String> result) {
						// TODO Auto-generated method stub
					   allVocabulary = result;
					}
				});
		
		this.initialize();

	}

	private void displayOpenedVocabularies() 
	{	
		
		for (final String c : openedVocabularies) {
			
			final ToggleButton closeVocabulary = new ToggleButton(new Image("./img/close-white.jpg"), new Image("./img/disabled.jpg"));
			Label vname = new Label(c);
			vname.addStyleName("vname");			
			final HorizontalPanel hp = new HorizontalPanel();
			hp.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
			hp.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
			hp.addStyleName("vocabularyMenu");
			hp.add(closeVocabulary);
			hp.add(vname);
			configure.add(hp);
			closeVocabulary.addClickHandler(new ClickHandler()
			{
				public void onClick(ClickEvent e)
				{
					if(closeVocabulary.isDown())
					{
						closeVocabulary.setDown(false);
					    ConfirmDialog dlg = new ConfirmDialog(hp, closeVocabulary,c,false,true);	
					    dlg.center();	
					    dlg.show();
					}
					else
					{
						openedVocabularies.add(c);
					}
				}				
			});
		}
	}
	
	
	private void initVocabulariesMenu()
	{
		
		this.openNewVocabulary = new Button("Select");
		
		openNewVocabulary.addClickHandler(new ClickHandler()
		{
			public void onClick(ClickEvent event) {
				final PopupPanel pop = new PopupPanel(true,false);
				addVocabularyPanel.clear();
				addVocabularyPanel.removeFromParent();
				for(final String c : allVocabulary)
				{
					if (!openedVocabularies.contains(c.toLowerCase()))
					{
						final Hyperlink hp = new Hyperlink(c,c);						
						hp.addClickHandler(new ClickHandler()
						{

							public void onClick(ClickEvent e)
							{
								openedVocabularies.add(c.toLowerCase());
								final ToggleButton closeVocabulary = new ToggleButton(new Image("./img/close-white.jpg"), new Image("./img/disabled.jpg"));
								Label vname = new Label(c);
								vname.addStyleName("vname");
								final HorizontalPanel vpanel = new HorizontalPanel();
								vpanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
								vpanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
								vpanel.addStyleName("vocabularyMenu");
								vpanel.add(closeVocabulary);
								vpanel.add(vname);
								configure.insert(vpanel, configure.getWidgetCount()-1);
								pop.hide();
								
								closeVocabulary.addClickHandler(new ClickHandler()
								{
									public void onClick(ClickEvent e)
									{
										if(closeVocabulary.isDown())
										{
											closeVocabulary.setDown(false);
					            			ConfirmDialog dlg = new ConfirmDialog(vpanel, closeVocabulary,c,false,true);	
											dlg.show();
											dlg.center();	
										}
										else
										{
											openedVocabularies.add(c);
											final Hyperlink hp = new Hyperlink(c, c);
											hp.addClickHandler(new ClickHandler()
											{
												@Override
												public void onClick(ClickEvent event) {
													// TODO Auto-generated method stub
													
												}	
											});
										}
									}				
								});						
							}
						}
						);		
						addVocabularyPanel.add(hp);	
					}
				}
				if(addVocabularyPanel.getWidgetCount() == 0)
				{
					Label msg = new Label("You have selected all the vocabularies that HIVE have.");
					addVocabularyPanel.add(msg);
				}
				pop.add(addVocabularyPanel);
				pop.addStyleName("add-pop");
				pop.setPopupPosition(openNewVocabulary.getAbsoluteLeft()+12, openNewVocabulary.getAbsoluteTop()+11);
				pop.show();
			}		
		});
		configure.add(openNewVocabulary);
	}
	
	private void initialize()
	{
		this.deleteFile = new HorizontalPanel();
		this.isFileUploaded = false;
		this.isURL = false;
		glass = new GlassPanel(false);
		uploadPopup = new PopupPanel(false);
		uploadPopup.addStyleName("upload-popup");
		Label uploading = new Label("Uploading...");
		
		uploading.setHeight("100%");
		uploadPopup.add(uploading);
		openedVocabularies = new ArrayList<String>();
		indexingCaption = new CaptionPanel("HIVE Automatic Concepts Extractor");
		indexingCaption.addStyleName("indexing-Caption");
		indexingTable = new FlexTable();
		
		this.configure = new HorizontalPanel();
		configure.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		SimplePanel configureWrapper = new SimplePanel();
		configureWrapper.setStyleName("configure");
		final Label lb1 = new Label("Select vocabulary source ");
		final HTML step1 = new HTML("<img src = './img/step1.png'/>");
		HorizontalPanel hp1 = new HorizontalPanel();
		hp1.add(step1);
		hp1.add(lb1);
		hp1.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		hp1.setCellVerticalAlignment(lb1, HasVerticalAlignment.ALIGN_MIDDLE);
		hp1.setCellVerticalAlignment(step1, HasVerticalAlignment.ALIGN_MIDDLE);
		
		lb1.addStyleName("label");
		indexingTable.setWidget(0, 0, hp1);
		indexingTable.setWidget(0, 1, configure);
		indexingTable.getCellFormatter().setVerticalAlignment(0, 0, HasVerticalAlignment.ALIGN_MIDDLE);
		indexingTable.getCellFormatter().setVerticalAlignment(0, 1, HasVerticalAlignment.ALIGN_MIDDLE);
		addVocabularyPanel = new FlowPanel();
		addVocabularyPanel.setSize("200px", "150px");
		this.displayOpenedVocabularies();
		this.initVocabulariesMenu();
		
		final FormPanel form = new FormPanel();
		form.setEncoding(FormPanel.ENCODING_MULTIPART);
		form.setMethod(FormPanel.METHOD_POST);
		form.setAction("/FileUpload");
		HorizontalPanel uploadholder = new HorizontalPanel();
		final FileUpload upload = new FileUpload();
		upload.setName("uploadFormElement");
		form.add(upload);
		Button uploadButton = new Button("Upload");
		uploadButton.addStyleName("upload-button");
		uploadholder.setVerticalAlignment(HasVerticalAlignment.ALIGN_TOP);
		uploadholder.setCellVerticalAlignment(uploadButton, HasVerticalAlignment.ALIGN_TOP);
		uploadholder.addStyleName("uploadholder");
		uploadholder.add(form);
		uploadholder.add(uploadButton);
		uploadButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent e)
			{
				form.submit();
			}		
		});			
		form.addSubmitHandler(new FormPanel.SubmitHandler() {
			public void onSubmit(SubmitEvent event) {
				// This event is fired just before the form is submitted. We can
				// take this opportunity to perform validation.
			//	String path = upload.getFilename();	
				
			//	Window.alert(path);
				if (upload.getFilename().length() == 0) {
					Window.alert("Please choose the file you want to upload.");
					event.cancel();
				}
				else
				{
				RootPanel.get().add(glass, 0, 0);
				uploadPopup.center();
				uploadPopup.show();
				}
			}
		});

		form.addSubmitCompleteHandler(new FormPanel.SubmitCompleteHandler() {
			public void onSubmitComplete(SubmitCompleteEvent event) {
				String result = event.getResults();
				if(result.contains("success"))
				{
					uploadPopup.hide();
					glass.removeFromParent();
					if(isFileUploaded = true)
					{
						deleteFile.clear();
						deleteFile.removeFromParent();
					}
					fileName = result.substring(result.indexOf('|')+1, result.indexOf('?'));
					isFileUploaded = true;
					Label filename = new Label(fileName);
			//		Window.alert(fileName);
					final PushButton delete = new PushButton(new Image("./img/cancel-upld.gif"));
					deleteFile.add(delete);
					deleteFile.add(filename);
					indexingTable.insertRow(2);
					indexingTable.setWidget(2, 1, deleteFile);
					delete.addClickHandler(new ClickHandler()
					{
						@Override
						public void onClick(ClickEvent event) {
							// TODO Auto-generated method stub
							isFileUploaded = false;
							deleteFile.removeFromParent();
						}	
					});
				}
				
			}
		});	
	    
		Label lb3 = new Label("Upload a document");
		lb3.addStyleName("label");
		HorizontalPanel hp2 = new HorizontalPanel();
		final HTML step2 = new HTML("<img src = './img/step2.png'/>");
		hp2.add(step2);
		hp2.add(lb3);
		hp2.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		hp2.setCellVerticalAlignment(lb3, HasVerticalAlignment.ALIGN_MIDDLE);
		hp2.setCellVerticalAlignment(step2, HasVerticalAlignment.ALIGN_MIDDLE);
		indexingTable.setWidget(1, 0, hp2);
		indexingTable.setWidget(1, 1, uploadholder);
		indexingTable.getCellFormatter().setVerticalAlignment(1, 0, HasVerticalAlignment.ALIGN_MIDDLE);
		indexingTable.getCellFormatter().setVerticalAlignment(1, 1, HasVerticalAlignment.ALIGN_MIDDLE);
		
		final FlowPanel logoPanel = new FlowPanel();
		Label powered = new Label("Powered by");
	    HTML kea = new HTML("<a class = 'kea' href='http://www.nzdl.org/Kea/index.html' target = '_blank'><img src = './img/kea_logo.gif'/></a>", true);
		logoPanel.add(powered);
		logoPanel.add(kea);
		
		HTML lb4 = new HTML("<span>OR</span> Enter the URL", false);
		lb4.addStyleName("or-label");
		lb4.addStyleName("label");
		indexingTable.setWidget(2, 0, lb4);
		final TextBox docURL = new TextBox();
		docURL.setWidth("300px");
		docURL.addStyleName("docURL");
		indexingTable.setCellSpacing(0);
		indexingTable.setWidget(2, 1, docURL);
		Button startProcessing = new Button("Start Processing");
		startProcessing.setStyleName("start-processing");
		final HTML step3 = new HTML("<img src = './img/step3.png'/>");
		indexingTable.setWidget(0, 2, step3);
		indexingTable.setWidget(1, 2, startProcessing);
		indexingTable.getFlexCellFormatter().setHorizontalAlignment(1, 2, HasHorizontalAlignment.ALIGN_RIGHT);
		indexingTable.setWidget(2, 2, logoPanel);
		indexingTable.getFlexCellFormatter().addStyleName(0, 2, "border-left");
		indexingTable.getFlexCellFormatter().addStyleName(1, 2, "border-left-increase");
		indexingTable.getFlexCellFormatter().addStyleName(2, 2, "border-left-increase2");
		startProcessing.addClickHandler(new ClickHandler()
		{
			@Override
			public void onClick(ClickEvent event) 
			{
				// TODO Auto-generated method stub
				boolean isValid = false;
				String url = docURL.getValue();
				if(openedVocabularies.isEmpty())
				{
					Window.alert("Please select at least one vocabulary.");
				}
				else if(isFileUploaded == true && url.equals(""))
				{
					fileToProcess = fileName;
					isValid = true;
				}
				else if(isFileUploaded == false && !url.equals(""))
				{
					fileToProcess = docURL.getValue();
					isValid = true;
				}
				else if(isFileUploaded == true && !url.equals(""))
				{
					Window.alert("You can only upload a document or enter a URL, but not both.");
				}
				else if(isFileUploaded == false && url.equals(""))
				{
					Window.alert("Please upload a document or enter a URL (use http://).");
				}
				
				if(isValid == true)
				{
					RootPanel.get().add(glass, 0, 0);
					final PopupPanel processingPopup = new PopupPanel();
					final Label processing = new Label("Processing...");
					processingPopup.addStyleName("z-index");
					processingPopup.add(processing);
					processingPopup.center();
					processingPopup.show();
					indexerService.getTags(fileToProcess, openedVocabularies,
							new AsyncCallback<List<ConceptProxy>>() {
								@Override
								public void onFailure(Throwable caught) {
									Window.alert("Terms can not be extracted. Check!");
									caught.printStackTrace();
									processingPopup.hide();
									glass.removeFromParent();
								}
								@Override
								public void onSuccess(List<ConceptProxy> result) {
									// TODO Auto-generated method stub
									processingPopup.hide();
									glass.removeFromParent();
									displayResult(result);
								}
							});
				}
			}
			
		});
		
		for(int i=0; i<indexingTable.getRowCount(); i++)
		{
			indexingTable.getCellFormatter().addStyleName(i, 0, "indexing-table-prompt");
			indexingTable.getCellFormatter().addStyleName(i, 1, "indexing-table-control");
			if(i <= 2)
			{
				indexingTable.getRowFormatter().addStyleName(i, "indexing-table-operation");
			}
		}
		indexingTable.addStyleName("indexing-table");
		indexingCaption.add(indexingTable);
		RootPanel.get("indexer").add(indexingCaption);
	}
	
	private void displayResult(List<ConceptProxy> result)
	{
		indexingCaption.clear();
		indexingCaption.setCaptionText("Extracted Concepts Cloud");
		indexingCaption.removeFromParent();
		indexingTable.clear();
		resultDock = new DockPanel();
		resultDock.addStyleName("result-Dock");
		Button startover = new Button("Start Over");
		startover.addClickHandler(new ClickHandler()
		{
			@Override
			public void onClick(ClickEvent event) {
				// TODO Auto-generated method stub
				resultDock.clear();
				resultDock.removeFromParent();
				initialize();
			}
		});
		startover.setStyleName("start-over");
		resultDock.add(startover, DockPanel.NORTH);
		resultDock.setCellHorizontalAlignment(startover, HasHorizontalAlignment.ALIGN_RIGHT);
		resultDock.add(indexingCaption, DockPanel.CENTER);
		conceptInfo = new SimplePanel();
		resultDock.add(conceptInfo,DockPanel.SOUTH);
		RootPanel.get("indexer").add(resultDock);
	    DockPanel tagDock = new DockPanel();
	    FlowPanel tagcloud = new FlowPanel();
	    tagcloud.setWidth("600px");
	    tagcloud.addStyleName("tag-cloud");
	    VerticalPanel oriVoc = new VerticalPanel();
	    oriVoc.setSpacing(5);
	    List<String> oriList = new ArrayList<String>();
	    indexingCaption.add(tagDock);
	    tagDock.add(tagcloud,DockPanel.CENTER);
	    tagDock.add(oriVoc, DockPanel.WEST);
	    for(ConceptProxy cp : result)
	    {
	    	String ori = cp.getOrigin();
	    	if(!openedVocabularies.contains(ori.toLowerCase()))
	    	{
	    		continue;
	    	}
	    	String uri = cp.getURI();
	    	String uris[] = uri.split(" ");
	    	String namespace = uris[0];
	    	String lp = uris[1];
	    	if(!oriList.contains(ori)) oriList.add(ori);
	    	String colorCss = ori.toLowerCase() + "-color";
	    	String term = cp.getPreLabel();
	    	double score = cp.getScore();
	    	/*Decide the font-size based on the score*/
	    	double rate = (score * 10000);
	    	String fontCss = "";
	    	if(rate >= 0 && rate < 30)
	    		fontCss = "font-one";
	    	else if(rate >= 30 && rate < 60)
	    		fontCss = "font-two";
	    	else if(rate >= 60 && rate < 90)
	    		fontCss = "font-three";
	    	else if(rate >= 90 && rate < 120)
	    		fontCss = "font-four";
	    	else if(rate >=120 && rate < 150)
	    		fontCss = "font-five";
	    	else
	    		fontCss = "font-six";
          	final Anchor a = new Anchor(term);
	    	a.setStyleName("base-css");
	    	a.addStyleName(colorCss);
	    	a.addStyleName(fontCss);
	    	a.addClickHandler(new ConceptHandler(namespace, lp));
	    	tagcloud.add(a); 	
	    }
	    for(String ori : oriList)
	    {
	    	Label square = new Label("");
	    	square.setSize("15px", "15px");
	    	square.addStyleName(ori.toLowerCase() + "-bgcolor");
	    	Label name = new Label(ori);
	    	HorizontalPanel hp = new HorizontalPanel();
	    	hp.add(square);
	    	hp.add(name);
	    	oriVoc.add(hp);
	    }
	}
	
	private class ConfirmDialog extends DialogBox
	{
		String associateVoc;
		GlassPanel glassPanel;
		int vocIndex;
		public ConfirmDialog(final HorizontalPanel toBeDeleted, final ToggleButton trigger, String vocabulary, boolean autohide, boolean modal)
		{			
			super(autohide, modal);
			associateVoc = vocabulary;
			vocIndex = openedVocabularies.indexOf(associateVoc.toLowerCase());
			glassPanel = new GlassPanel(false);
			this.setText("Confirm");
			this.setAnimationEnabled(true);
			com.google.gwt.user.client.ui.Button yesBtn = new com.google.gwt.user.client.ui.Button("Yes");
			com.google.gwt.user.client.ui.Button cancelBtn = new com.google.gwt.user.client.ui.Button("Cancel");
			VerticalPanel vp = new VerticalPanel();
		    vp.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		    vp.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		    vp.setSpacing(10);
		    vp.setSize("100%", "100%");
			HTML msg = new HTML("Are you sure to close <span style = 'color: #3399FF'>" + associateVoc + "</span>?", true);
			vp.add(msg);
			yesBtn.addClickHandler(new ClickHandler()
			{
				public void onClick(ClickEvent e)
				{ 
					trigger.setDown(true);
					toBeDeleted.removeFromParent();
					ConfirmDialog.this.hide();
					/*Delete the vocabulary from UI*/			
					openedVocabularies.remove(vocIndex);
				}
			});		
			cancelBtn.addClickHandler(new ClickHandler()
			{
				public void onClick(ClickEvent e)
				{
					trigger.setDown(false);
					ConfirmDialog.this.hide();
				}
			}
			);
			HorizontalPanel opr = new HorizontalPanel();
			opr.setSpacing(10);
			opr.add(yesBtn);
			opr.add(cancelBtn);
			vp.add(opr);
			this.add(vp);	
		}	
		
		public void show()
		{		
			super.show();
			RootPanel.get().add(glassPanel, 0, 0);
		}

		public void hide()
		{
			super.hide();
			glassPanel.removeFromParent();
		}
	}
	
	private class ConceptHandler implements ClickHandler {
		private String namespaceURI;
		private String localPart;

		private ConceptHandler(String uri, String lp) {
			this.namespaceURI = uri;
			this.localPart = lp;
		}

		@Override
		public void onClick(ClickEvent event) {

			conceptBrowserService.getConceptByURI(namespaceURI, localPart,
					new AsyncCallback<ConceptProxy>() {

						@Override
						public void onFailure(Throwable caught) {
							// TODO Auto-generated method stub
							Window.alert(caught.getMessage());
						}

						@Override
						public void onSuccess(ConceptProxy result) {
							// TODO Auto-generated method stub
							displayConceptInfo(result);
						}
					});
		}
	}
	
	private class SKOSHandler implements ClickHandler
	{
		private String SKOSCode;
		
		public SKOSHandler(String SKOSCode)
		{
			super();
			this.SKOSCode = SKOSCode;
		}
		
		public void onClick(ClickEvent event) {
			// TODO Auto-generated method stub
			final GlassPanel glass = new GlassPanel(false);
			RootPanel.get().add(glass,0,0);
			final DecoratedPopupPanel skosDlg = new DecoratedPopupPanel(false);
			skosDlg.setAnimationEnabled(false);
			TextArea skos = new TextArea();
			skos.setSize("650px", "400px");
			skos.setValue(SKOSCode);
			PushButton closeButton = new PushButton(new Image("./img/closebutton.png"));		
			closeButton.addStyleName("close-button");
			closeButton.addClickHandler(new ClickHandler()
			{
				@Override
				public void onClick(ClickEvent event) {
					// TODO Auto-generated method stub
					skosDlg.removeFromParent();
					glass.removeFromParent();
				}
				
			});
			DockPanel dock = new DockPanel();
			dock.add(closeButton,DockPanel.NORTH);
			dock.add(skos, DockPanel.CENTER);
			dock.setCellHorizontalAlignment(closeButton, HasHorizontalAlignment.ALIGN_RIGHT);
			dock.setCellWidth(closeButton, "20px");
			skosDlg.add(dock);
			skosDlg.show();
			skosDlg.center();
		}	
	}
	
	private void displayConceptInfo(ConceptProxy result) {
		conceptInfo.clear();
		conceptInfo.addStyleName("concept-info");
		VerticalPanel vp = new VerticalPanel();
		VerticalPanel header = new VerticalPanel();
		Label htext = new Label(result.getOrigin() + "->" + result.getPreLabel());
		htext.addStyleName("concept-name-style");
		Button showSKOSBtn = new Button("View in SKOS");
		showSKOSBtn.setStyleName("skos-btn");
		showSKOSBtn.addClickHandler(new SKOSHandler(result.getSkosCode()));
		header.addStyleName("concept-name-header");
		header.add(htext);
		header.add(showSKOSBtn);
		header.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		header.setCellHorizontalAlignment(showSKOSBtn, HasHorizontalAlignment.ALIGN_RIGHT);
		header.addStyleName("concept-header");
		header.setWidth("100%");
		vp.add(header);
		Label preLabel = new Label(result.getPreLabel());
		FlexTable conceptTable = new FlexTable();
		conceptTable.setText(0, 0, "Preferred Label");
		conceptTable.setWidget(0, 1, preLabel);
		conceptTable.setText(1, 0, "URI");
		conceptTable.setText(1, 1, result.getURI());
		List<String> altLabel = result.getAltLabel();
		conceptTable.setText(2, 0, "Alternative Label");
		String altlabels = "";
		if (altLabel != null) {
			for (String c : altLabel) {
				altlabels = altlabels + c + "; ";
			}
		} else {
			altlabels = "This concept does not have alternative labels.";
		}
		conceptTable.setText(2, 1, altlabels);
		HashMap<String, String> broader = result.getBroader();
		conceptTable.setText(3, 0, "Broader Concepts");
		if (broader != null) {
			FlowPanel broaderPanel = new FlowPanel();
			Set<String> keys = broader.keySet();
			int i = 0;
			for (final String key : keys) {
				i++;
				String uri = broader.get(key);
				String[] tokens = uri.split(" ");
				String namespaceURI = tokens[0];
				String localPart = tokens[1];
				ConceptLink hp = new ConceptLink(namespaceURI, localPart, key, localPart);
				hp.addClickHandler(new ConceptHandler(namespaceURI, localPart));
				hp.addStyleName("Hyperlink-trick");
				broaderPanel.add(hp);
			}

			conceptTable.setWidget(3, 1, broaderPanel);
		} else {
			conceptTable.setText(3, 1,
					"This concept does not have broader terms.");
		}

		conceptTable.setText(4, 0, "Narrower Concepts");
		HashMap<String, String> narrower = result.getNarrower();
		if (narrower != null) {
			FlowPanel narrowerPanel = new FlowPanel();
			Set<String> keys = narrower.keySet();
			for (String key : keys) {
				String uri = narrower.get(key);
				String[] tokens = uri.split(" ");
				String namespaceURI = tokens[0];
				String localPart = tokens[1];
				ConceptLink hp = new ConceptLink(namespaceURI, localPart, key, localPart);
				hp.addClickHandler(new ConceptHandler(namespaceURI, localPart));
				hp.addStyleName("Hyperlink-trick");
				narrowerPanel.add(hp);
			}

		conceptTable.setWidget(4, 1, narrowerPanel);
		} 
		else 
		{
			conceptTable.setText(4, 1, "This concept does not have narrower terms.");
		}

		HashMap<String, String> related = result.getRelated();
		conceptTable.setText(5, 0, "Related Concepts");
		if (related != null) {
			FlowPanel relatedPanel = new FlowPanel();
			Set<String> keys = related.keySet();
			for (String key : keys) {
				String uri = related.get(key);
				String[] tokens = uri.split(" ");
				String namespaceURI = tokens[0];
				String localPart = tokens[1];
				ConceptLink hp = new ConceptLink(namespaceURI, localPart, key, localPart);
				hp.addClickHandler(new ConceptHandler(namespaceURI, localPart));
				hp.addStyleName("Hyperlink-trick");
				relatedPanel.add(hp);
				conceptTable.setWidget(5, 1, relatedPanel);
			}
		} else {
			conceptTable.setText(5, 1,
					"This concept does not have related concepts.");
		}

		List<String> scopeNotes = result.getScopeNotes();
		conceptTable.setText(6, 0, "Scope Notes");
		String sn = "";
		if (scopeNotes != null) {
			for (String s : scopeNotes) {
				sn = sn + s + "; ";
			}

		} else {
			sn = "This concept does not have scope notes.";
		}

		conceptTable.setText(6, 1, sn);
		conceptTable.setWidth("580px");
		conceptTable.setCellSpacing(0);
		conceptTable.addStyleName("concept-table");

		for (int j = 0; j < conceptTable.getRowCount(); j++) {
			conceptTable.getCellFormatter().addStyleName(j, 0, "table-heading");
			conceptTable.getCellFormatter().addStyleName(j, 1, "common-row");
			if ((j % 2) == 0) {
				conceptTable.getRowFormatter().addStyleName(j, "even-row");
			} else {
				conceptTable.getRowFormatter().addStyleName(j, "uneven-row");
			}
		}
		vp.add(conceptTable);
		conceptInfo.add(vp);
	}

}