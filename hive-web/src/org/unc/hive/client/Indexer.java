package org.unc.hive.client;

import java.util.ArrayList;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.gwtmultipage.client.*;  
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DecoratedPopupPanel;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DisclosureHandler;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.DisclosurePanelImages;
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
import com.google.gwt.user.client.ui.ListBox;
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
import com.google.gwt.user.client.ui.FlexTable;

@UrlPatternEntryPoint(value = "indexing.html")

public class Indexer implements EntryPoint {
	
	private CaptionPanel indexingCaption;
	private String fileName;
	private String tempFileName;
	private List<String> openedVocabularies;  // store the name of current loaded vocabularies in client side
	private List<String> allVocabulary; // store the name of all vocabularies that hive have
	
	private List<ConceptProxy> selectedConcepts;   
	private Button startover = new Button("Start Over");   
	private Button selectedConceptsButton;
	private FlowPanel addVocabularyPanel;
	private HorizontalPanel configure;	
	private Button openNewVocabulary; 
	private FlexTable indexingTable;
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
	
	private Anchor clickedConcept;
	private boolean selected;
	private String recText = "";
	private TextArea recs = new TextArea();
	private RecordFormatter formatter = new RecordFormatter();
		
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
					Label msg = new Label("All vocabularies have been selected.");
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
		uploadPopup = new PopupPanel(false);
		uploadPopup.addStyleName("upload-popup");
		uploadPopup.setGlassEnabled(true);   
		Label uploading = new Label("Uploading...");
		
		uploading.setHeight("100%");
		uploadPopup.add(uploading);
		openedVocabularies = new ArrayList<String>();
		
		selectedConcepts = new ArrayList<ConceptProxy>();   
		
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
					Window.alert("Please choose a file to upload.");
					event.cancel();
				}
				else
				{
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
					if(isFileUploaded = true)
					{
						deleteFile.clear();
						deleteFile.removeFromParent();
					}
					
					String response = result.substring(result.indexOf("|")+1, result.lastIndexOf("?"));
					String[] fileNames = response.split("\\|");
					fileName = fileNames[0];
					tempFileName = fileNames[1];
										
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
		
		final DisclosurePanelImages images =
			(DisclosurePanelImages) GWT.create(DisclosurePanelImages.class);
		class DisclosurePanelHeader extends HorizontalPanel
		{
		    public DisclosurePanelHeader(boolean isOpen, String html)
		    {
		        add(isOpen ? images.disclosurePanelOpen().createImage()
		              : images.disclosurePanelClosed().createImage());
		        add(new HTML(html));
		    }
		}
		
		final DisclosurePanel advancedPanel = new DisclosurePanel("Show advanced settings");

		advancedPanel.addCloseHandler(new CloseHandler<DisclosurePanel>() {
            @Override
            public void onClose(CloseEvent<DisclosurePanel> event) {
            	advancedPanel.setHeader(new DisclosurePanelHeader(false, "Show advanced settings"));
            }
        });
		
		advancedPanel.addOpenHandler(new OpenHandler<DisclosurePanel>() {
            @Override
            public void onOpen(OpenEvent<DisclosurePanel> event) {
            	advancedPanel.setHeader(new DisclosurePanelHeader(true, "Hide advanced settings"));
            }
        });
		
		// Create the max hops listbox and panel
		final ListBox maxHops = new ListBox();
		maxHops.addItem("0");
		maxHops.addItem("1");
		maxHops.addItem("2");
		maxHops.addItem("3");
		maxHops.addItem("4");
		maxHops.addItem("5");
		maxHops.setStyleName("max-hops");
		Label maxHopsLbl = new Label();
		maxHopsLbl.setText("  Number of hops");
		maxHopsLbl.addStyleName("label");
		HorizontalPanel maxHopsPanel = new HorizontalPanel();
		maxHopsPanel.setStyleName("advanced-subpanel");
		maxHopsPanel.add(maxHops);
		maxHopsPanel.add(maxHopsLbl);
		maxHopsPanel.setTitle("Maximum number of links to follow when indexing a website. " + 
			"Set to 0 to index the first page only. Increasing this value will increase indexing time.");

		// Create max terms listbox and panel
		final ListBox maxTerms = new ListBox();
		maxTerms.addItem("5");
		maxTerms.addItem("10");
		maxTerms.addItem("15");
		maxTerms.addItem("20");
		maxTerms.setItemSelected(1, true);
		maxTerms.setStyleName("max-hops");
		Label maxTermsLbl = new Label();
		maxTermsLbl.setText("  Maximum number of terms");
		maxTermsLbl.addStyleName("label");
		HorizontalPanel maxTermsPanel = new HorizontalPanel();
		maxTermsPanel.setStyleName("advanced-subpanel");
		maxTermsPanel.add(maxTerms);
		maxTermsPanel.add(maxTermsLbl);
		maxTermsPanel.setTitle("Maximum number of terms to suggest.");
		
		final CheckBox diffCb = new CheckBox();
		Label diffLbl = new Label();
		diffLbl.setText(" Index differences only");
		diffLbl.setStyleName("label");
		HorizontalPanel diffPanel = new HorizontalPanel();
		diffPanel.setStyleName("advanced-subpanel");
		diffPanel.add(diffCb);
		diffPanel.add(diffLbl);
		diffPanel.setTitle("Check this checkbox to index only the differences between multiple pages in a multipage site. " +
						"This will reduce the effect of repeated components such as headers and menus.");	
		
		
		VerticalPanel vp = new VerticalPanel();
		vp.add(maxHopsPanel);
		vp.add(maxTermsPanel);
		vp.add(diffPanel);
		advancedPanel.add(vp);
		advancedPanel.setStyleName("advanced-panel");
		advancedPanel.setWidth("300px");
		indexingTable.setWidget(3, 1, advancedPanel);

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
				int hops = Integer.parseInt(maxHops.getValue(maxHops.getSelectedIndex()));
				int terms = Integer.parseInt(maxTerms.getValue(maxTerms.getSelectedIndex()));
				boolean diff = diffCb.getValue();
				if(openedVocabularies.isEmpty())
				{
					Window.alert("Please select at least one vocabulary.");
				}
				else if(isFileUploaded == true && url.equals(""))
				{
					fileToProcess = tempFileName;
					isValid = true;
				}
				else if(isFileUploaded == false && !url.equals(""))
				{
					if (!url.startsWith("http://") && !url.startsWith("https://"))
						url = "http://" + url;
					
					fileToProcess = url;
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
					final PopupPanel processingPopup = new PopupPanel();
					final Label processing = new Label("Processing...");
					processingPopup.addStyleName("z-index");
			    	processingPopup.add(processing);
			    	processingPopup.setGlassEnabled(true);  
					processingPopup.center();
					processingPopup.show();
									
					indexerService.getTags(fileToProcess, openedVocabularies, hops, terms, diff,
							new AsyncCallback<List<ConceptProxy>>() {
								@Override
								public void onFailure(Throwable caught) {
									Window.alert("An error has occurred. Please try again.");
									caught.printStackTrace();
									processingPopup.hide();
								}
								@Override
								public void onSuccess(List<ConceptProxy> result) {
									// TODO Auto-generated method stub
									processingPopup.hide();
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
		//RootPanel.get().add(indexingCaption);
		//RootPanel.get("introduction").add(indexingCaption);
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
		selectedConceptsButton = new Button("");
		selectedConceptsButton.setHTML("<html>Select Concepts to<br>View in multiple formats</html>");
		selectedConceptsButton.setStyleName("selectedConceptsButton");
		selectedConceptsButton.addClickHandler(new FormatRecordsHandler());
 
		startover.setStyleName("start-over");
		FlowPanel fp = new FlowPanel();
		fp.setStyleName("vocabulary-tags");
	    fp.add(startover);
		fp.add(selectedConceptsButton);  
		resultDock.add(fp, DockPanel.NORTH);  
	
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
	    String current = null;
	    FlowPanel currentPanel = new FlowPanel();
	    currentPanel.setStyleName("vocabulary-tags");
	    for(ConceptProxy cp : result)
	    {	    	
	    	String ori = cp.getOrigin();
	    	if(!openedVocabularies.contains(ori.toLowerCase()))
	    	{
	    		continue;
	    	}
	    	
	    	if (!ori.equals(current)) {	    		
	    		if (current != null) {
	    			tagcloud.add(currentPanel);
	    			currentPanel = new FlowPanel();
	    			currentPanel.setStyleName("vocabulary-tags");
	    		}	    			
	    		current = ori;
	    	}
	    		
	    	String uri = cp.getURI();
	    	String uris[] = uri.split(" ");
	    	String namespace = uris[0];
	    	String lp = uris[1];
	    	if(!oriList.contains(ori)) oriList.add(ori);
	    	String colorCss = ori.toLowerCase() + "-color";
	    	String term = cp.getPreLabel();
	    	term = term.replaceAll(" ", "&nbsp;") + " ";
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
          	final Anchor a = new Anchor(term, true);
	    	a.setStyleName("base-css");
	    	a.addStyleName(colorCss);
	    	a.addStyleName(fontCss);
	    	a.addStyleName("tag-name");
	    	a.addStyleName("deselectedconcept-bgcolor");   
	    	a.addClickHandler(new ConceptHandler(namespace, lp));
	    	currentPanel.add(a);
	    }
	    
	    tagcloud.add(currentPanel);
	    
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
		int vocIndex;
		public ConfirmDialog(final HorizontalPanel toBeDeleted, final ToggleButton trigger, String vocabulary, boolean autohide, boolean modal)
		{			
			super(autohide, modal);
			associateVoc = vocabulary;
			vocIndex = openedVocabularies.indexOf(associateVoc.toLowerCase());
			this.setText("Confirm");
			this.setAnimationEnabled(true);
			com.google.gwt.user.client.ui.Button yesBtn = new com.google.gwt.user.client.ui.Button("Yes");
			com.google.gwt.user.client.ui.Button cancelBtn = new com.google.gwt.user.client.ui.Button("Cancel");
			VerticalPanel vp = new VerticalPanel();
		    vp.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		    vp.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		    vp.setSpacing(10);
		    vp.setSize("100%", "100%");
			HTML msg = new HTML("Do you really want to close <span style = 'color: #3399FF'>" + associateVoc + "</span>?", true);
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
		}

		public void hide()
		{
			super.hide();
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
            // Toggle selected concept link, de/re-highlighting with each click
			selected = false;
			try {
			    clickedConcept = (Anchor)event.getSource();
			    String styles = clickedConcept.getStyleName();
			    if (styles.contains("deselected")) {
			      selected = true;
			      clickedConcept.removeStyleName("deselectedconcept-bgcolor");	
			      clickedConcept.addStyleName("selectedconcept-bgcolor");  
			    }
			   else {
				   selected = false;
				   clickedConcept.removeStyleName("selectedconcept-bgcolor");	
			       clickedConcept.addStyleName("deselectedconcept-bgcolor");  
			   }
			}
			catch (ClassCastException exc) {
				Window.alert("Class Cast Exception: ClickEvent source to Anchor");
			}
			
			conceptBrowserService.getConceptByURI(namespaceURI, localPart,
					new AsyncCallback<ConceptProxy>() {
						@Override
						public void onFailure(Throwable caught) {
							// TODO Auto-generated method stub
							Window.alert(caught.getMessage());
						}

						@Override
						public void onSuccess(ConceptProxy result) {
							updateSelectedConcepts(result, selected);   
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
			final DecoratedPopupPanel skosDlg = new DecoratedPopupPanel(false);
			skosDlg.setAnimationEnabled(false);
			skosDlg.setGlassEnabled(true);   
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
	
	private class FormatRecordsHandler implements ClickHandler
	{
				
		public FormatRecordsHandler()
		{
			super();
		}
		
		public void onClick(ClickEvent event) {
			if(selectedConcepts.isEmpty()) {
				Window.alert("Please select at least one concept.");
				return;
			}
				
			final DecoratedPopupPanel formatRecDlg = new DecoratedPopupPanel(false);
			formatRecDlg.setAnimationEnabled(false);
			formatRecDlg.setGlassEnabled(true); 
			formatRecDlg.addStyleName("recordformat-panel");
			FlowPanel hdr = new FlowPanel();
			Label lb = new Label("Select Format:");
			lb.addStyleName("heading");
			lb.addStyleName("format-label");
		    	
			recs.setSize("800px", "400px");
			recs.setValue("");
			
			final ListBox formatList = new ListBox();
			formatList.addItem("SKOS - RDF/XML");
			formatList.addItem("SKOS - N Triples");
			formatList.addItem("Dublin Core");
			formatList.addItem("MODS/XML");
			formatList.addItem("MARC/XML");
			formatList.setVisibleItemCount(1);
			formatList.setSelectedIndex(0);
			formatList.addStyleName("format-listbox");
			
			formatter.init();
			
     		formatList.addChangeHandler(new ChangeHandler() {
			   public void onChange(ChangeEvent event)	{
				   recText = "";
				   recText = formatter.format(selectedConcepts, formatList.getItemText(formatList.getSelectedIndex()));
				   recs.setValue(recText);
			   }
			});
     		
			// default record format is SKOS RDF/XML
     		recText = formatter.format(selectedConcepts, "SKOS - RDF/XML");
			recs.setValue(recText);
			
			PushButton closeButton = new PushButton(new Image("./img/closebutton.png"));		
			closeButton.addStyleName("close-button");
			closeButton.addClickHandler(new ClickHandler()
			{
				@Override
				public void onClick(ClickEvent event) {
					formatRecDlg.removeFromParent();
				}
				
			});
			DockPanel dock = new DockPanel();
			dock.add(closeButton,DockPanel.NORTH);
			
			hdr.add(lb);
			hdr.add(formatList);
			dock.add(hdr, DockPanel.NORTH);
			dock.add(recs, DockPanel.CENTER);
			dock.setCellHorizontalAlignment(closeButton, HasHorizontalAlignment.ALIGN_RIGHT);
			dock.setCellWidth(closeButton, "20px");
			formatRecDlg.add(dock);
			formatRecDlg.show();
			formatRecDlg.center();
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
	
	private void updateSelectedConcepts(ConceptProxy result, boolean selected) {
		int numberOfSelectedConcepts = 0;
		if (selected)
		    selectedConcepts.add(result);  //make this a set??
		else {
			int indx = -1;
			for (ConceptProxy cp : selectedConcepts) {
				if (cp.getPreLabel().equalsIgnoreCase(result.getPreLabel()) &&
		             cp.getURI().equalsIgnoreCase(result.getURI()))   {
					indx = selectedConcepts.indexOf(cp);
				    selectedConcepts.remove(indx);
				    break;
				}
			}
		}
		String numConcepts = "Select Concepts to "; 
		if (!selectedConcepts.isEmpty()) {
		    numberOfSelectedConcepts = selectedConcepts.size();	
		    numConcepts = String.valueOf(numberOfSelectedConcepts);
			numConcepts = numConcepts + 
					((numberOfSelectedConcepts == 1) ? " Concept Selected" : " Concepts Selected");
		}
		numConcepts =  "<html>" + numConcepts + "<br>" + "View in multiple formats" + "</html>";
		selectedConceptsButton.setHTML(numConcepts);
	}
}