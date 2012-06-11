package org.unc.hive.client;

import java.util.List;
import java.util.Locale;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import org.gwtmultipage.client.UrlPatternEntryPoint;


//@UrlPatternEntryPoint(value = "home.html")
@UrlPatternEntryPoint(value = "home([^.]*).html(\\\\?.*)?")

public class HomePage implements EntryPoint {

	private CaptionPanel vocabularyStatistics;
	private CaptionPanel searchConcept;
	private CaptionPanel indexing;
	private CaptionPanel demoServerStmt;
	private TextBox queryBox;
	private Button searchBtn;

	private final ConceptBrowserServiceAsync conceptBrowserService = GWT
			.create(ConceptBrowserService.class);
	
	
	private HIVEMessages messages = (HIVEMessages)GWT.create(HIVEMessages.class);

	public void onModuleLoad() {
		// TODO Auto-generated method stub
		vocabularyStatistics = new CaptionPanel(messages.homepage_stats() );
		vocabularyStatistics.setWidth("100%");
		vocabularyStatistics.setStyleName("caption");
		searchConcept = new CaptionPanel("<a href = '" + messages.homepage_conceptBrowserURL() + "'> " + 
				messages.homepage_searchLabel() + " </a>", true);
		searchConcept.setStyleName("caption");
		indexing = new CaptionPanel("<a href = '" + messages.homepage_indexingURL() + "'> "+ 
				messages.homepage_indexLabel() +" </a>", true);
		indexing.setStyleName("caption");
		demoServerStmt = new CaptionPanel("");
		demoServerStmt.setStyleName("caption"); 
		queryBox = new TextBox();
		queryBox.setWidth("240px");
		queryBox.addKeyPressHandler(new KeyPressHandler()
		{
			
			@Override
			public void onKeyPress(KeyPressEvent event) {
				// TODO Auto-generated method stub
				
				if(event.getCharCode() == KeyCodes.KEY_ENTER)
				{
				   String query = queryBox.getValue();
				   if(query.equals(""))
				   {
					 Window.alert(messages.homepage_enterQuery());
				   }
				   else
				   {
					   String url = "../" + messages.homepage_conceptBrowserURL() + "#query=" + query;
					   redirect(url);	
				   }
				}
			}
		});
		
		
		
		searchBtn = new Button(messages.homepage_searchButton());
		searchBtn.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				// TODO Auto-generated method stub
				String query = queryBox.getValue();
				if (query.equals("")!= true) {
					String url =  messages.homepage_conceptBrowserURL() + "#query=" + query;
					redirect(url);	
				}
			}
		});
		final HTML introCB = new HTML(messages.homepage_browseDesc(), true);
		final HorizontalPanel search = new HorizontalPanel();
		search.setSpacing(5);
		// 2011/1/20 craig.willis: Disabled non-functioning search box and button.
		//search.add(queryBox);
		//search.add(searchBtn);
		final VerticalPanel searchVP = new VerticalPanel();
		searchVP.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
		searchVP.add(introCB);
		searchVP.add(search);
		searchConcept.add(searchVP);
		final HTML introIndex = new HTML(
				messages.homepage_indexDesc(),
				true);
	
		final Button submitBtn = new Button(messages.homepage_uploadButton());
		submitBtn.addStyleName("uploadBtn");

		final HorizontalPanel uploaderPanel = new HorizontalPanel();
		uploaderPanel.setSpacing(0);
		uploaderPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_TOP);
		// 2011/1/20 - craig.willis: Disabled non-functioning upload button.
		//uploaderPanel.add(submitBtn);

		final VerticalPanel indexVP = new VerticalPanel();
		indexVP.add(introIndex);
		indexVP.add(uploaderPanel);
		indexing.add(indexVP);
		
		final VerticalPanel demoVP = new VerticalPanel();
		final HTML demoStmt = new HTML(
				"<i>"+messages.homepage_info()+"</i>" + messages.homepage_contact(), true);
		demoVP.add(demoStmt);
		demoVP.add(uploaderPanel);
		demoServerStmt.add(demoVP);
				
		final FlowPanel logoPanel = new FlowPanel();
		Image mrc = new Image("./img/MRC_logo.png");
		Image NEScent = new Image("./img/NESCentLogo.png");
		logoPanel.addStyleName("logos");
		logoPanel.add(mrc);
		logoPanel.add(NEScent);

		final VerticalPanel vp = new VerticalPanel();
		vp.setWidth("300px");
		vp.add(searchConcept);
		vp.add(indexing);
		vp.add(demoServerStmt);
		vp.add(logoPanel);

		conceptBrowserService.getAllVocabularies(new AsyncCallback<List<List<String>>>() {
					@Override
					public void onFailure(Throwable caught) {
						// TODO Auto-generated method stub

						vocabularyStatistics.add(new Label(
								messages.homepage_loadStatsError()));
						
						Window.alert(messages.homepage_loadVocabError());

					}

					@Override
					public void onSuccess(List<List<String>> result) {
						// TODO Auto-generated method stub
						Grid vocabulariesGrid = new Grid(result.size() + 1, 4);
						vocabulariesGrid.setWidth("540px");
						for (int i = 0; i < result.size() + 1; i++) {
							if (i == 0) {
								vocabulariesGrid.setWidget(i, 0, new Label(
										messages.homepage_vocabLabel() ));
								vocabulariesGrid.setWidget(i, 1, new Label(
										messages.homepage_conceptsLabel()));
								vocabulariesGrid.setWidget(i, 2, new Label(
										messages.homepage_relationshipslabel()));
								vocabulariesGrid.setWidget(i, 3, new Label(
										messages.homepage_lastUpdatedLabel()));
							} else {
								List<String> vocabularyInfo = result.get(i - 1);
								for (int j = 0; j < vocabularyInfo.size(); j++) {
									String c = vocabularyInfo.get(j);
									if (j == 0) {
										final Hyperlink hp = new Hyperlink(c,c);
										hp.addClickHandler(new ClickHandler()
										{
											@Override
											public void onClick(ClickEvent event) {
												// TODO Auto-generated method stub
												String url = "../" + messages.homepage_conceptBrowserURL() + 
													"#voc=" + hp.getTargetHistoryToken();
												redirect(url);	
											}
										});
										vocabulariesGrid.setWidget(i, j, hp);
									} else {
										Label lb = new Label(c);
										vocabulariesGrid.setWidget(i, j, lb);
									}
								}
							}
						}
						vocabulariesGrid.addStyleName("statTable");
						vocabulariesGrid.setCellSpacing(0);
						vocabulariesGrid.getRowFormatter().setStyleName(0,"tableHead");
						vocabularyStatistics.add(vocabulariesGrid);
						/*
						final Label lb = new Label(
								"Last Updated On: April 26, 2011");
						lb.addStyleName("lastupdate");
						final VerticalPanel vp = new VerticalPanel();
						vp.add(vocabulariesGrid);
						vp.add(lb);
						*/
						vocabularyStatistics.add(vp);
					}
				});
		final HorizontalPanel hp = new HorizontalPanel();
		hp.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
		hp.setVerticalAlignment(HasVerticalAlignment.ALIGN_TOP);
		hp.add(vp);
		hp.add(vocabularyStatistics);
		hp.setSpacing(10);
		RootPanel.get("majorPart").add(hp);
	}
	
	public native void redirect(String URL)
	/*-{
		$wnd.location = URL; 
	}-*/;

}