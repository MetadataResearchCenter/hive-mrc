package org.unc.hive.client;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import com.claudiushauptmann.gwt.multipage.client.MultipageEntryPoint;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DecoratedPopupPanel;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
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
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TabBar;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.widgetideas.client.GlassPanel;


@MultipageEntryPoint(urlPattern = "/ConceptBrowser.html")
//@MultipageEntryPoint(urlPattern = "/(.*)/ConceptBrowser.html")
/*
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class ConceptBrowser implements EntryPoint, ValueChangeHandler<String> {

	private final ConceptBrowserServiceAsync conceptBrowserService = GWT
			.create(ConceptBrowserService.class);

	private HorizontalPanel configure;
	private Grid alphabetGrid;
	private HorizontalPanel search;
	private TextBox queryBox;
	private PushButton openNewVocabulary;
	private TabBar browsingTab;
	private ScrollPanel conceptList;
	private ClosablePanel searchResult;
	private ScrollPanel resultList;
	private VerticalPanel conceptDetail;
	private SimplePanel conceptInfo;
	private FlowPanel addVocabularyPanel;
	private VerticalPanel filteringPanel;
	private CaptionPanel captionForFiltering;
	private Image loadingBar;
	private Image loadingBar2;

	// Below is the data received from server

	private String subAlpha;
	private List<String> openedVocabularies; // store the name of current loaded
												// vocabularies in client side
	private List<String> allVocabulary; // store the name of all vocabularies
										// that hive have
	private String[] alphabetical = { "A", "B", "C", "D", "E", "F", "G", "H",
			"I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U",
			"V", "W", "X", "Y", "Z", "[0-9]" };
	private List<ConceptProxy> resultStorage;
	private List<String> filteringVocabularies;
	private String queryfromhome = "";
	private String currentViewing; // store the name of the vocabulary the user currently is browsing
	
	// private ConceptProxy randomConcept;

	public ConceptBrowser() {
		// Could be changed through user selection
		this.subAlpha = "A";
		History.addValueChangeHandler(this);
		this.queryfromhome = History.getToken();
	}

	public void onModuleLoad() {
        final GlassPanel glass = new GlassPanel(false);
        RootPanel.get().add(glass, 0, 0);
        final PopupPanel loadingPopup = new PopupPanel();
        loadingPopup.add(new Label("Loading..."));
        loadingPopup.addStyleName("z-index");
        loadingPopup.show();
        loadingPopup.center();
		setup(); 
		conceptBrowserService.getAllVocabulariesName(new AsyncCallback<List<String>>()
		{

			@Override
			public void onFailure(Throwable caught) {
				// TODO Auto-generated method stub
				
				Window.alert("Get all vocabularies failed!");
				
			}

			@Override
			public void onSuccess(List<String> result) 
			{
				// TODO Auto-generated method stub
				loadingPopup.hide();
				glass.removeFromParent();
				allVocabulary = result;
				/*Different path of initialization*/
				if (queryfromhome.startsWith("query="))
				{
					/*Return search result and view default vocabulary*/
					String query = queryfromhome.substring(queryfromhome.indexOf("=", 0) + 1);
					queryBox.setText(query);
					searchResult.setHeaderText(query);
					searchResult.setContent(resultList);
					searchResult.setFiltering(captionForFiltering);
					resultList.clear();
					resultList.add(loadingBar2);
					if (searchResult.getIsOpened() == false) {
						conceptDetail.insert(searchResult, 0);
						searchResult.setIsOpened(true);
					}
					conceptBrowserService.searchForConcept(query, allVocabulary,
							new AsyncCallback<List<ConceptProxy>>() {

								@Override
								public void onFailure(Throwable caught) {
									// TODO Auto-generated method stub
									HTML reminder = new HTML(
											"<span style = 'color:red'>Server is unresponse, please try again later.</span>");
									resultList.clear();
									filteringPanel.clear();
									resultList.add(reminder);
								}

								@Override
								public void onSuccess(List<ConceptProxy> result) {
									// TODO Auto-generated method stub

									if (result.size() == 0) {
										HTML reminder = new HTML(
												"<span style = 'color:red'> No result returns regarding your search.</span>");
										resultList.clear();
										filteringPanel.clear();
										resultList.add(reminder);

									} else {
										resultStorage = result;
										resultList.clear();
										if (filteringVocabularies == null)
											filteringVocabularies = new ArrayList<String>();
										else
											filteringVocabularies.clear();
										FlexTable resulttable = new FlexTable();
										resultList.add(resulttable);
										int i = 0;
										for (ConceptProxy cp : result) {
											String origin = cp.getOrigin();
											if (filteringVocabularies.contains(origin) == false) {
												filteringVocabularies.add(origin);
											}
											String preLabel = cp.getPreLabel();
											String uri = cp.getURI();
											String[] tokens = uri.split(" ");
											String namespaceURI = tokens[0];
											String lp = tokens[1];
											ConceptLink cpl = new ConceptLink(origin,
													namespaceURI, lp, preLabel, origin + ":" + lp);
											cpl.setWidth("300px");
											cpl.addClickHandler(new ConceptHandler(namespaceURI, lp));
											cpl.setTitle(preLabel);
											Label originlb = new Label(origin);
											resulttable.setWidget(i, 0, originlb);
											resulttable.setWidget(i, 1, cpl);
											i++;
										}
										
										openedVocabularies = filteringVocabularies; 
										currentViewing = filteringVocabularies.get(0);
										
										for (int j = 0; j < resulttable.getRowCount(); j++) {
											resulttable.getCellFormatter().addStyleName(j,
													0, "origin-style");
											resulttable.getCellFormatter().addStyleName(j,
													1, "concept-style");
										}

										filteringPanel.clear();
										for (String ori : filteringVocabularies) {
											final CheckBox check = new CheckBox(ori);
											check.setValue(true);
											check.addClickHandler(new ClickHandler() {

												@Override
												public void onClick(ClickEvent event) {
													// TODO Auto-generated method stub
													Boolean isChecked = check.getValue();
													String voc = check.getText();
													resultList.clear();
													FlexTable flex = new FlexTable();
													if (!isChecked) {
														filteringVocabularies.remove(voc);

													} else {
														filteringVocabularies.add(voc);
													}

													int i = 0;
													for (ConceptProxy c : resultStorage) {
														String origin = c.getOrigin();
														if (filteringVocabularies.contains(origin)) {
															flex.setText(i, 0, c
																	.getOrigin());
															String preLabel = c
																	.getPreLabel();
															String uri = c.getURI();
															String[] tokens = uri
																	.split(" ");
															String namespaceURI = tokens[0];
															String lp = tokens[1];
															ConceptLink cpl = new ConceptLink(
																	origin, namespaceURI,
																	lp, preLabel, origin
																			+ ":" + lp);
															cpl.addClickHandler(new ConceptHandler(
																			namespaceURI,
																			lp));
															flex.setWidget(i, 1, cpl);
															i++;
														}
													}

													for (int j = 0; j < flex.getRowCount(); j++) {
														flex.getCellFormatter()
																.addStyleName(j, 0,
																		"origin-style");
														flex.getCellFormatter()
																.addStyleName(j, 1,	"concept-style");
													}
													resultList.add(flex);
												}
											});
											filteringPanel.add(check);
										}
									}
									displayOpenedVocabularies();
									initVocabulariesMenu();	
									initBrowsingTab();
								}	
							});

				}
				else if(queryfromhome.startsWith("voc="))
				{
					currentViewing = queryfromhome.substring(queryfromhome.indexOf("=", 0) + 1);
					openedVocabularies = new ArrayList<String>();
					openedVocabularies.add(currentViewing);
					conceptBrowserService.getFirstConcept(currentViewing, new AsyncCallback<ConceptProxy>()
							{

								@Override
								public void onFailure(Throwable caught) {
									// TODO Auto-generated method stub
									
								}
								
								@Override
								public void onSuccess(ConceptProxy result) {
									// TODO Auto-generated method stub
									
								   displayConceptInfo(result);
									
								}
						
							});
					displayOpenedVocabularies();
					initVocabulariesMenu();	
					initBrowsingTab();
					
				}
				else
				{
				   final GlassPanel glass = new GlassPanel(false);
				   RootPanel.get().add(glass, 0, 0);
				   openedVocabularies = new ArrayList<String>();
				   final PopupPanel choosePanel = new PopupPanel();
				   choosePanel.addStyleName("choose-panel");
				   DockPanel dock = new DockPanel();
				   Label lb = new Label("Please choose which vocabularies to open");
				   lb.addStyleName("heading");
				   dock.add(lb, DockPanel.NORTH);
				   CaptionPanel caption = new CaptionPanel("List of Vocabularies at HIVE");
				   FlowPanel flow = new FlowPanel();
				   for(String c : allVocabulary)
				   {
					   final CheckBox check = new CheckBox(c);
					   flow.add(check);
					   check.addClickHandler(new ClickHandler()
					   {

						@Override
						public void onClick(ClickEvent event) {
							// TODO Auto-generated method stub
							Boolean isChecked = check.getValue();
							if(isChecked)
							{
								openedVocabularies.add(check.getText());
							}
							else
							{
								openedVocabularies.remove(check.getText());
							}
						}   
					   });
				   }
				   caption.add(flow);
				   caption.setWidth("380px");
				   caption.setHeight("200px");
				   dock.add(caption, DockPanel.CENTER);
				   HorizontalPanel hp = new HorizontalPanel();
				   Button okButton = new Button("OK");
				   okButton.addClickHandler(new ClickHandler()
				   {

					@Override
					public void onClick(ClickEvent event) {
						// TODO Auto-generated method stub
						choosePanel.removeFromParent();
						glass.removeFromParent();
						if(openedVocabularies.isEmpty())
						{
							openedVocabularies.add(allVocabulary.get(0)); 
						}
						currentViewing = openedVocabularies.get(0);
						conceptBrowserService.getFirstConcept(currentViewing, new AsyncCallback<ConceptProxy>()
								{

									@Override
									public void onFailure(Throwable caught) {
										// TODO Auto-generated method stub
										
									}
									
									@Override
									public void onSuccess(ConceptProxy result) {
										// TODO Auto-generated method stub
										
									   displayConceptInfo(result);
										
									}
							
								});
						displayOpenedVocabularies();
						initVocabulariesMenu();	
						initBrowsingTab();
						
					}
				   });
				   Button cancelButton = new Button("CANCEL");
				   cancelButton.addClickHandler(new ClickHandler()
				   {

					@Override
					public void onClick(ClickEvent event) {
						// TODO Auto-generated method stub
						choosePanel.removeFromParent();
						glass.removeFromParent();
						openedVocabularies.clear();
						openedVocabularies.add(allVocabulary.get(0));	
						currentViewing = openedVocabularies.get(0);
						conceptBrowserService.getFirstConcept(currentViewing, new AsyncCallback<ConceptProxy>()
								{

									@Override
									public void onFailure(Throwable caught) {
										// TODO Auto-generated method stub
										
									}
									
									@Override
									public void onSuccess(ConceptProxy result) {
										// TODO Auto-generated method stub
										
									   displayConceptInfo(result);
										
									}
							
								});
						displayOpenedVocabularies();
						initVocabulariesMenu();	
						initBrowsingTab();						
					}
					   
				   });
				   Label tip = new Label("By click on CANCEL or not choose any vocabularies, HIVE will open a default vocabulary for you.");
				   tip.setWidth("250px");
				   tip.addStyleName("tip");
				   hp.add(okButton);
				   hp.add(cancelButton);
				   hp.add(tip);
				   hp.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
				   hp.setSpacing(5);
				   dock.add(hp, DockPanel.SOUTH);
				   choosePanel.add(dock);
				   choosePanel.center();
				   choosePanel.show();
				}			
			}
			
		});
	}
	
	private void setup()
	{	
		loadingBar = new Image("./img/loadingbar.gif");
		loadingBar.addStyleName("loading-image");

		loadingBar2 = new Image("./img/loadingbar.gif");
		loadingBar2.addStyleName("loading-image");

		configure = new HorizontalPanel();
		configure.setVerticalAlignment(HasVerticalAlignment.ALIGN_TOP);
	//	configure.setStyleName("configure");
		configure.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
		SimplePanel configureWrapper = new SimplePanel();
		configureWrapper.add(configure);
		configureWrapper.setStyleName("configure");
		final Label lb1 = new Label("Opened vocabularies:");
		lb1.setWidth("150px");
		lb1.addStyleName("label");
		configure.add(lb1);
		addVocabularyPanel = new FlowPanel();
		addVocabularyPanel.setSize("200px", "150px");
		
		queryBox = new TextBox();
		queryBox.setWidth("250px");
		initializeSearchbox();

		final VerticalPanel verticalPanel = new VerticalPanel();
		verticalPanel.setWidth("320px");
		verticalPanel.addStyleName("browsingLeft");
		verticalPanel.add(search);
		this.initAlphabeticalGrid();
		final VerticalPanel vpBrowser = new VerticalPanel();
		vpBrowser.add(alphabetGrid);
		vpBrowser.addStyleName("tabpanel");
		conceptList = new ScrollPanel();
		conceptList.setWidth("330px");
		conceptList.setHeight("600px");
		vpBrowser.add(conceptList);
		browsingTab = new TabBar();
		verticalPanel.add(browsingTab);
		verticalPanel.add(vpBrowser);

		resultList = new ScrollPanel();
		searchResult = new ClosablePanel();
		searchResult.setHeaderStyle("resultlist-header-style");
		resultList.setWidth("420px");
		resultList.setHeight("300px");
		resultList.addStyleName("background-white");
		resultList.addStyleName("border-default");
		resultList.addStyleName("add-margin");
		resultList.addStyleName("add-padding");
		searchResult.addStyleName("border-default");
		searchResult.addStyleName("background-default");
		filteringPanel = new VerticalPanel();
		captionForFiltering = new CaptionPanel("Filter the result");
		captionForFiltering.add(filteringPanel);
		captionForFiltering.addStyleName("add-margin");
		captionForFiltering.setWidth("120px");
		
		conceptInfo = new SimplePanel();
		conceptDetail = new VerticalPanel();
		conceptInfo.setWidth("625px");
		conceptDetail.add(conceptInfo);
		conceptDetail.setWidth("625px");

		final HorizontalPanel hsp = new HorizontalPanel();
		hsp.add(verticalPanel);
		hsp.add(conceptDetail);
		hsp.addStyleName("border-top");
		hsp.setWidth("980px");
		RootPanel.get("content").add(configureWrapper);
		RootPanel.get("content").add(hsp);	
	}

	private void initializeSearchbox() {

		queryBox.addFocusHandler(new FocusHandler() {

			@Override
			public void onFocus(FocusEvent event) {
				// TODO Auto-generated method stub
				queryBox.addStyleName("focus");
				if (queryBox.getValue() != "") {
					queryBox.setValue("");
				}
			}
		});

		queryBox.addBlurHandler(new BlurHandler() {

			@Override
			public void onBlur(BlurEvent event) {
				// TODO Auto-generated method stub
				queryBox.removeStyleName("focus");
			}

		});
		queryBox.addKeyPressHandler(new KeyPressHandler() {

			@Override
			public void onKeyPress(KeyPressEvent event) {
				// TODO Auto-generated method stub

				if (event.getCharCode() == KeyCodes.KEY_ENTER) {
					String query = queryBox.getValue();
					if (query.equals("")) {
						Window.alert("Please enter a query!");
					} else {
						loadingResultList(query);
					}
				}
			}
		});
		final Button searchButton = new Button("Search");
		searchButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent e) {

				final String query = queryBox.getValue();
				if (query.equals("")) {
					Window.alert("please enter query!");
				} else {
					loadingResultList(query);
				}
			}

		});
		this.search = new HorizontalPanel();
		search.setWidth("100%");
		search.addStyleName("searchConcept");
		search.setSpacing(0);
		search.add(queryBox);
		search.add(searchButton);
	}

	private void displayOpenedVocabularies() {

		for (final String c : openedVocabularies) {

			final ToggleButton closeVocabulary = new ToggleButton(new Image("./img/close.jpg"), new Image("./img/disabled.jpg"));
			Label vname = new Label(c);
			vname.addStyleName("vname");
			final HorizontalPanel hp = new HorizontalPanel();
			hp.setVerticalAlignment(HasVerticalAlignment.ALIGN_TOP);
			hp.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
			hp.addStyleName("vocabularyMenu");
			hp.add(closeVocabulary);
			hp.add(vname);
			configure.add(hp);
			configure.setCellHorizontalAlignment(hp, HasHorizontalAlignment.ALIGN_LEFT);

			closeVocabulary.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent e) {
					if (closeVocabulary.isDown()) {
						closeVocabulary.setDown(false);
						ConfirmDialog dlg = new ConfirmDialog(hp,closeVocabulary, c, false, true);
						dlg.show();
						dlg.center();
					} else {
						/* Bring back the deleted vocabulary */
						openedVocabularies.add(c);
						final Hyperlink hp = new Hyperlink(c, c);
						browsingTab.addTab(hp);
					}
				}
			});
		}
	}

	private void initVocabulariesMenu() {
		this.openNewVocabulary = new PushButton(new Image("./img/add.jpg"));
		openNewVocabulary.setSize("12px", "11px");

		openNewVocabulary.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				final PopupPanel pop = new PopupPanel(true, false);
				pop.addStyleName("add-pop");
				addVocabularyPanel.clear();
				addVocabularyPanel.removeFromParent();
				for (final String c : allVocabulary) {
					if (!openedVocabularies.contains(c)) {
						final Hyperlink hp = new Hyperlink(c, c);
						hp.addClickHandler(new ClickHandler() {

							public void onClick(ClickEvent e) {
								openedVocabularies.add(c);
								currentViewing = c;
								final ToggleButton closeVocabulary = new ToggleButton(
										new Image("./img/close.jpg"),
										new Image("./img/disabled.jpg"));
								Label vname = new Label(c);
								vname.addStyleName("vname");
								final HorizontalPanel vpanel = new HorizontalPanel();
								vpanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_TOP);
								vpanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
								vpanel.addStyleName("vocabularyMenu");
								vpanel.add(closeVocabulary);
								vpanel.add(vname);
								configure.insert(vpanel, configure.getWidgetCount() - 2);
								pop.hide();
								final Hyperlink newtabhead = new Hyperlink(c, c);
								browsingTab.addTab(newtabhead);
								browsingTab.selectTab(browsingTab.getTabCount()-1);
								getAndDisplayConcepts(currentViewing.toString(), subAlpha.toString());
								newtabhead.addClickHandler(new ClickHandler() {
									@Override
									public void onClick(ClickEvent event) {
										// TODO Auto-generated method stub
										currentViewing = c;
										getAndDisplayConcepts(currentViewing.toString(), subAlpha.toString());
									}

								});

								closeVocabulary.addClickHandler(new ClickHandler() {
											public void onClick(ClickEvent e) {
												if (closeVocabulary.isDown()) {
													closeVocabulary.setDown(false);
													ConfirmDialog dlg = new ConfirmDialog(vpanel,closeVocabulary, c,false, true);
													dlg.show();
													dlg.center();
												} else {
													openedVocabularies.add(c);
													final Hyperlink hp = new Hyperlink(c, c);
													hp.addClickHandler(new ClickHandler() {

																@Override
																public void onClick(
																		ClickEvent event) {
																	currentViewing = c;
																	getAndDisplayConcepts(
																			currentViewing
																					.toString(),
																			subAlpha
																					.toString());
																}
															});
													browsingTab.addTab(hp);
												}
											}
										});
							}
						});
						addVocabularyPanel.add(hp);
					}
				}
				if (addVocabularyPanel.getWidgetCount() == 0) {
					Label msg = new Label("You have opened all the vocabularies that HIVE have.");
					addVocabularyPanel.add(msg);
				}
				pop.add(addVocabularyPanel);
				pop.setPopupPosition(openNewVocabulary.getAbsoluteLeft() + 12,
						openNewVocabulary.getAbsoluteTop() + 11);
				pop.show();
			}
		});
		configure.add(openNewVocabulary);
		configure.setCellHorizontalAlignment(openNewVocabulary, HasHorizontalAlignment.ALIGN_LEFT);
		Label lb = new Label("Add");
		lb.addStyleName("addlabel");
		configure.add(lb);
	}

	private void initAlphabeticalGrid() {
		this.alphabetGrid = new Grid(3, 13);
		alphabetGrid.setCellSpacing(0);
		alphabetGrid.addStyleName("alphabetGrid");

		int i = 0;
		int j = 0;

		for (final String c : alphabetical) {

			final Hyperlink hp = new Hyperlink(c, c);
			hp.setStyleName("hyperlink");
			if (c == "A") {
				hp.setStyleName("selected");
			}
			hp.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent e) {
					Iterator<Widget> it = alphabetGrid.iterator();
					while (it.hasNext()) {
						Hyperlink hp = (Hyperlink) it.next();
						if ((hp.getStyleName()).equals("selected")) {
							hp.setStyleName("hyperlink");
							break;
						}
					}
					hp.setStyleName("selected");
					subAlpha = hp.getText();
					getAndDisplayConcepts(currentViewing.toString(), subAlpha
							.toString());
				}
			});
			alphabetGrid.setWidget(i, j, hp);
			j++;
			if (j == 13) {
				i++;
				j = 0;
			}
		}
	}

	private void initBrowsingTab() {
		for (final String c : openedVocabularies) {
			Hyperlink hp = new Hyperlink(c, c);
			hp.addClickHandler(new ClickHandler() {

				@Override
				public void onClick(ClickEvent event) {
					// TODO Auto-generated method stub
					currentViewing = c;
					getAndDisplayConcepts(currentViewing.toString(), subAlpha.toString());
				}
			});
			browsingTab.addTab(hp);
		}
		int index = openedVocabularies.indexOf(currentViewing);
		browsingTab.selectTab(index);
		getAndDisplayConcepts(currentViewing.toString(), subAlpha.toString());
	}

	public void onValueChange(ValueChangeEvent<String> event) {
		// TODO Auto-generated method stub

		// Window.alert(event.getValue());

	}

	private void getAndDisplayConcepts(String vocabulary, String letter) {
		final Tree topSub = new Tree();
		conceptList.clear();
		conceptList.add(loadingBar);
		conceptBrowserService.getSubTopConcept(vocabulary.toLowerCase(), letter.toLowerCase(), new AsyncCallback<List<ConceptProxy>>() {

			@Override
			public void onFailure(Throwable caught) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onSuccess(List<ConceptProxy> result) {
				// TODO Auto-generated method stub
				conceptList.clear();
				conceptList.add(topSub);
				Iterator<ConceptProxy> it = result.iterator();

				while (it.hasNext()) {
					ConceptProxy thisfather = it.next();
					String forigin = thisfather.getOrigin();
					String fpreLabel = thisfather.getPreLabel();
					String fURI = thisfather.getURI();
					String[] furis = fURI.split(" ");
					String fnamespaceURI = furis[0];
					String flocalPart = furis[1];
					final ConceptLink fcpl = new ConceptLink(forigin,
							fnamespaceURI, flocalPart, fpreLabel, forigin + ":"
									+ flocalPart);
					fcpl.addClickHandler(new ConceptHandler(fcpl.getNamespaceURI(), fcpl.getlocalPart()));
					final TreeItem fitem = new TreeItem(fcpl);
					boolean isleaf = thisfather.getIsLeaf();
					if (isleaf == false) {
						TreeItem fakeItem = new TreeItem();
						fitem.addItem(fakeItem);
					}
					topSub.addItem(fitem);
				}
			}

		});
		topSub.addOpenHandler(new OpenHandler<TreeItem>() {
			@Override
			public void onOpen(OpenEvent<TreeItem> event) {
				// TODO Auto-generated method stub
				final TreeItem thisItem = event.getTarget();
				int count = thisItem.getChildCount();
				if (count == 1) {
					thisItem.removeItems();
					ConceptLink hp = (ConceptLink) thisItem.getWidget();
					String namespaceURI = hp.getNamespaceURI();
					String localPart = hp.getlocalPart();
					conceptBrowserService.getChildConcept(namespaceURI,
							localPart, new AsyncCallback<List<ConceptProxy>>() {
								@Override
								public void onFailure(Throwable caught) {
									// TODO Auto-generated method stub
									TreeItem error = new TreeItem("Cannot load the information!");
									thisItem.addItem(error);
									thisItem.setState(true, false);
								}

								@Override
								public void onSuccess(List<ConceptProxy> result) {
									// TODO Auto-generated method stub
									if (result != null) {
										for (ConceptProxy cp : result) {
											String origin = cp.getOrigin();
											String preLabel = cp.getPreLabel();
											String URI = cp.getURI();
											String[] uris = URI.split(" ");
											String namespace = uris[0];
											String lp = uris[1];
											final ConceptLink cpl = new ConceptLink(origin, namespace, lp, preLabel, origin + ":" + lp);
											cpl.addClickHandler(new ConceptHandler(namespace, lp));
											final TreeItem citem = new TreeItem(cpl);
											boolean isleaf = cp.getIsLeaf();
											if (isleaf == false) {
												TreeItem fakeItem = new TreeItem();
												citem.addItem(fakeItem);
											}
											thisItem.addItem(citem);
										}
									}
								}
							});
				}
			}
		});
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
	
	private void loadingResultList(final String query) {
		searchResult.setHeaderText(query);
		searchResult.setContent(resultList);
		searchResult.setFiltering(captionForFiltering);
		resultList.clear();
		resultList.add(this.loadingBar2);
		if (searchResult.getIsOpened() == false) {

			conceptDetail.insert(searchResult, 0);
			searchResult.setIsOpened(true);
		}
		conceptBrowserService.searchForConcept(query, openedVocabularies,
				new AsyncCallback<List<ConceptProxy>>() {

					@Override
					public void onFailure(Throwable caught) {
						// TODO Auto-generated method stub
						HTML reminder = new HTML(
								"<span style = 'color:red'>Server is unresponse, please try again later.</span>");
						resultList.clear();
						filteringPanel.clear();
						resultList.add(reminder);
					}

					@Override
					public void onSuccess(List<ConceptProxy> result) {
						// TODO Auto-generated method stub

						if (result.size() == 0) {
							HTML reminder = new HTML(
									"<span style = 'color:red'> No result returns regarding your search.</span>");
							resultList.clear();
							filteringPanel.clear();
							resultList.add(reminder);

						} else {
							resultStorage = result;
							resultList.clear();
							if (filteringVocabularies == null)
								filteringVocabularies = new ArrayList<String>();
							else
								filteringVocabularies.clear();
							FlexTable resulttable = new FlexTable();
							resultList.add(resulttable);
							int i = 0;
							for (ConceptProxy cp : result) {
								String origin = cp.getOrigin();
								if (filteringVocabularies.contains(origin) == false) {
									filteringVocabularies.add(origin);
								}
								String preLabel = cp.getPreLabel();
								String uri = cp.getURI();
								String[] tokens = uri.split(" ");
								String namespaceURI = tokens[0];
								String lp = tokens[1];
								ConceptLink cpl = new ConceptLink(origin,
										namespaceURI, lp, preLabel, origin + ":" + lp);
								cpl.setWidth("300px");
								cpl.addClickHandler(new ConceptHandler(namespaceURI, lp));
								cpl.setTitle(preLabel);
								Label originlb = new Label(origin);
								resulttable.setWidget(i, 0, originlb);
								resulttable.setWidget(i, 1, cpl);
								i++;
							}

							for (int j = 0; j < resulttable.getRowCount(); j++) {
								resulttable.getCellFormatter().addStyleName(j,
										0, "origin-style");
								resulttable.getCellFormatter().addStyleName(j,
										1, "concept-style");
							}

							filteringPanel.clear();
							for (String ori : filteringVocabularies) {
								final CheckBox check = new CheckBox(ori);
								check.setValue(true);
								check.addClickHandler(new ClickHandler() {

									@Override
									public void onClick(ClickEvent event) {
										// TODO Auto-generated method stub
										Boolean isChecked = check.getValue();
										String voc = check.getText();
										resultList.clear();
										FlexTable flex = new FlexTable();
										if (!isChecked) {
											filteringVocabularies.remove(voc);

										} else {
											filteringVocabularies.add(voc);
										}

										int i = 0;
										for (ConceptProxy c : resultStorage) {
											String origin = c.getOrigin();
											if (filteringVocabularies.contains(origin)) {
												flex.setText(i, 0, c
														.getOrigin());
												String preLabel = c
														.getPreLabel();
												String uri = c.getURI();
												String[] tokens = uri
														.split(" ");
												String namespaceURI = tokens[0];
												String lp = tokens[1];
												ConceptLink cpl = new ConceptLink(
														origin, namespaceURI,
														lp, preLabel, origin
																+ ":" + lp);
												cpl.addClickHandler(new ConceptHandler(
																namespaceURI,
																lp));
												flex.setWidget(i, 1, cpl);
												i++;
											}
										}

										for (int j = 0; j < flex.getRowCount(); j++) {
											flex.getCellFormatter()
													.addStyleName(j, 0,
															"origin-style");
											flex.getCellFormatter()
													.addStyleName(j, 1,	"concept-style");
										}
										resultList.add(flex);
									}
								});
								filteringPanel.add(check);
							}
						}
					}

				});
	}

	private class ConfirmDialog extends DecoratedPopupPanel {
		String associateVoc;
		GlassPanel glassPanel;
		int vocIndex;

		public ConfirmDialog(final HorizontalPanel toBeDeleted,
				final ToggleButton trigger, String vocabulary,
				boolean autohide, boolean modal) {
			super(autohide, modal);
			associateVoc = vocabulary;
			vocIndex = openedVocabularies.indexOf(associateVoc);
			glassPanel = new GlassPanel(false);
			com.google.gwt.user.client.ui.Button yesBtn = new com.google.gwt.user.client.ui.Button(
					"Yes");
			com.google.gwt.user.client.ui.Button cancelBtn = new com.google.gwt.user.client.ui.Button(
					"Cancel");
			VerticalPanel vp = new VerticalPanel();
			vp.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
			vp.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
			vp.setSpacing(10);
			HTML msg = new HTML("Are you sure to close <span style = 'color: #3399FF'>"
							+ associateVoc + "</span>?", true);
			vp.add(msg);
			yesBtn.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent e) {
					trigger.setDown(true);
					toBeDeleted.removeFromParent();
					ConfirmDialog.this.hide();
					browsingTab.removeTab(vocIndex);
					conceptList.clear();
					/* Delete the vocabulary from UI */
					openedVocabularies.remove(vocIndex);
					if(browsingTab.getTabCount() != 0)
					{
						browsingTab.selectTab(0);
						getAndDisplayConcepts(openedVocabularies.get(0), subAlpha);
					}
					else
					{
						conceptList.add(new Label("Currently none vocabularies are opened."));
					}
				}
			});
			cancelBtn.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent e) {
					trigger.setDown(false);
					ConfirmDialog.this.hide();
				}
			});
			HorizontalPanel opr = new HorizontalPanel();
			opr.setSpacing(10);
			opr.add(yesBtn);
			opr.add(cancelBtn);
			vp.add(opr);
			this.add(vp);
		}

		public void show() {
			super.show();
			RootPanel.get().add(glassPanel, 0, 0);
		}

		public void hide() {
			super.hide();
			glassPanel.removeFromParent();
		}

	}

	private void displayConceptInfo(ConceptProxy result) {
		conceptInfo.clear();
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
				ConceptLink hp = new ConceptLink(namespaceURI, localPart, key,
						currentViewing + ":" + localPart);
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
				ConceptLink hp = new ConceptLink(namespaceURI, localPart, key,
						currentViewing + ":" + localPart);
				hp.addClickHandler(new ConceptHandler(namespaceURI, localPart));
				hp.addStyleName("Hyperlink-trick");
				narrowerPanel.add(hp);
			}

		conceptTable.setWidget(4, 1, narrowerPanel);
		} 
		else 
		{
			conceptTable.setText(4, 1,
					"This concept does not have narrower terms.");
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
				ConceptLink hp = new ConceptLink(namespaceURI, localPart, key,
						currentViewing + ":" + localPart);
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