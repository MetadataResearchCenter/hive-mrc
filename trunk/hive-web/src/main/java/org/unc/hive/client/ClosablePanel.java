package org.unc.hive.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Panel;

public class ClosablePanel extends SimplePanel {
	
	private VerticalPanel container;
	private HorizontalPanel header;
	private HorizontalPanel contentWrapper;
	private CaptionPanel filtering;
	private ScrollPanel content;
	private PushButton closeButton;
	private String headerStyle;
	private String headerText = "";
	private boolean isOpened;
	private HTML headerHTML;
	
	public ClosablePanel()
	{
		super();
		isOpened = false;
		header = new HorizontalPanel();
	//	header.setWidth("100%");
		header.setVerticalAlignment(HasVerticalAlignment.ALIGN_TOP);
		header.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
		closeButton = new PushButton(new Image("./img/closebutton.png"));
//		closeButton.setStyleName("closebutton-style");
		closeButton.setWidth("17px");
		closeButton.setHeight("18px");
		headerHTML = new HTML();
		closeButton.addClickHandler(new ClickHandler()
		{

			@Override
			public void onClick(ClickEvent event) {
				// TODO Auto-generated method stub
				ClosablePanel.this.setIsOpened(false);
				ClosablePanel.this.removeFromParent();
			}
			
		});
		header.add(headerHTML);
		header.add(closeButton);
		container = new VerticalPanel();
		container.add(header);
		contentWrapper = new HorizontalPanel();
		container.add(contentWrapper);
		this.add(container);
	}
	
	public void setHeaderStyle(String style)
	{
		this.headerStyle = style;
		header.setStyleName(this.headerStyle);
	}
	
	public void setHeaderText(String text)
	{
		this.headerText  = text;
		headerHTML.setHTML("<h4>Your search for<span> "+ this.headerText + "</span> returns following concepts:</h4>");
	}
	public void setIsOpened(boolean isOpened)
	{
		this.isOpened = isOpened;
	}
	
	public boolean getIsOpened()
	{
		return this.isOpened;
	}
	
	public void setContent(ScrollPanel sp)
	{
		if (this.content == null)
		{
			this.content = sp;
			contentWrapper.add(content);
		}
		else
		{
			this.content.removeFromParent();
			this.content = sp;
			this.contentWrapper.add(sp);
		}
	}
	
	public void setFiltering(CaptionPanel filtering)
	{
		if(this.filtering == null)
		{
			this.filtering = filtering;
			contentWrapper.add(filtering);
		}
		else
		{
			this.filtering.removeFromParent();
			this.filtering = filtering;
			this.contentWrapper.add(filtering);
		}
	}
	
	
	public void reopen(Panel parent, ScrollPanel content)
	{
		this.isOpened = true;
		this.setContent(content);
		parent.add(this);
	}

}
