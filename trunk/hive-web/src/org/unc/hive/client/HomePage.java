package org.unc.hive.client;

import java.util.List;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
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
import com.claudiushauptmann.gwt.multipage.client.MultipageEntryPoint;

@MultipageEntryPoint(urlPattern = "/home.html")
//@MultipageEntryPoint(urlPattern = "/(.*)/home.html")
public class HomePage implements EntryPoint {

	private CaptionPanel vocabularyStatistics;
	private CaptionPanel searchConcept;
	private CaptionPanel indexing;
	private TextBox queryBox;
	private Button searchBtn;

	private final ConceptBrowserServiceAsync conceptBrowserService = GWT
			.create(ConceptBrowserService.class);

	public void onModuleLoad() {
		// TODO Auto-generated method stub
		vocabularyStatistics = new CaptionPanel("Vocabulary Statistics");
		vocabularyStatistics.setWidth("100%");
		vocabularyStatistics.setStyleName("caption");
		searchConcept = new CaptionPanel("Search a Concept");
		searchConcept.setStyleName("caption");
		indexing = new CaptionPanel("Index a Document");
		indexing.setStyleName("caption");
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
					 Window.alert("Please enter a query!");
				   }
				   else
				   {
					   String url = "../ConceptBrowser.html#query=" + query;
					   redirect(url);	
				   }
				}
			}
		});
		
		searchBtn = new Button("Search");
		searchBtn.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				// TODO Auto-generated method stub
				String query = queryBox.getValue();
				if (query.equals("")!= true) {
					String url = "ConceptBrowser.html#query=" + query;
					redirect(url);	
				}
			}
		});
		final HTML introCB = new HTML(
				"HIVE <a href = 'ConceptBrowser.html'>Concept Browser</a> allows users to browse and search concepts in interdisciplinary vocabularies.",
				true);
		final HorizontalPanel search = new HorizontalPanel();
		search.setSpacing(5);
		search.add(queryBox);
		search.add(searchBtn);
		final HTML gotoCB = new HTML(
				"Go to<a href = 'ConceptBrowser.html'> Concept Browser </a>");
		final VerticalPanel searchVP = new VerticalPanel();
		searchVP.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
		searchVP.add(introCB);
		searchVP.add(search);
		searchVP.add(gotoCB);
		searchConcept.add(searchVP);
		final HTML introIndex = new HTML(
				"HIVE <a href = 'indexing.html'>Indexing</a> automatically extracts concepts from a given document to aid the cataloging and indexing practice.",
				true);
		final HTML gotoIndex = new HTML(
				"Go to <a href = 'indexing.html'>Indexing</a>");
		gotoIndex.addStyleName("footerByline");
		final Button submitBtn = new Button("Upload");
		submitBtn.addStyleName("uploadBtn");

		final HorizontalPanel uploaderPanel = new HorizontalPanel();
		uploaderPanel.setSpacing(0);
		uploaderPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_TOP);
		uploaderPanel.add(submitBtn);

		final VerticalPanel indexVP = new VerticalPanel();
		indexVP.add(introIndex);
		indexVP.add(uploaderPanel);
		indexVP.add(gotoIndex);
		indexing.add(indexVP);

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
		vp.add(logoPanel);

		conceptBrowserService.getAllVocabularies(new AsyncCallback<List<List<String>>>() {
					@Override
					public void onFailure(Throwable caught) {
						// TODO Auto-generated method stub

						vocabularyStatistics.add(new Label(
								"Cannot get the information!"));
						Window.alert("Lina is sleeping!");
					}

					@Override
					public void onSuccess(List<List<String>> result) {
						// TODO Auto-generated method stub
						Grid vocabulariesGrid = new Grid(result.size() + 1, 4);
						vocabulariesGrid.setWidth("540px");
						if (result.size() < 10) {
							vocabulariesGrid.setHeight("350px");
						} else {
							vocabulariesGrid.setHeight("100%");
						}
						for (int i = 0; i < result.size() + 1; i++) {
							if (i == 0) {
								vocabulariesGrid.setWidget(i, 0, new Label(
										"Vocabulary"));
								vocabulariesGrid.setWidget(i, 1, new Label(
										"Concepts"));
								vocabulariesGrid.setWidget(i, 2, new Label(
										"Relationships"));
								vocabulariesGrid.setWidget(i, 3, new Label(
										"Date Added"));
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
												String url = "../ConceptBrowser.html#voc=" + hp.getTargetHistoryToken();
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
						final Label lb = new Label(
								"Last Updated On: October 23, 2009");
						lb.addStyleName("lastupdate");
						final VerticalPanel vp = new VerticalPanel();
						vp.add(vocabulariesGrid);
						vp.add(lb);
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