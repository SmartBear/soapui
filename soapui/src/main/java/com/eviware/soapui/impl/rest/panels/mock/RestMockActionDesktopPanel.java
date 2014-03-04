package com.eviware.soapui.impl.rest.panels.mock;

import com.eviware.soapui.impl.rest.HttpMethod;
import com.eviware.soapui.impl.rest.mock.RestMockAction;
import com.eviware.soapui.impl.rest.panels.request.TextPanelWithTopLabel;
import com.eviware.soapui.impl.wsdl.actions.mockoperation.NewMockResponseAction;
import com.eviware.soapui.impl.wsdl.actions.mockoperation.OpenRequestForMockOperationAction;
import com.eviware.soapui.impl.wsdl.mock.dispatch.MockOperationDispatcher;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.mock.MockOperation;
import com.eviware.soapui.model.mock.MockResponse;
import com.eviware.soapui.model.mock.MockServiceListener;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.swing.ActionList;
import com.eviware.soapui.support.action.swing.ActionSupport;
import com.eviware.soapui.support.action.swing.DefaultActionList;
import com.eviware.soapui.support.action.swing.SwingActionDelegate;
import com.eviware.soapui.support.components.JInspectorPanel;
import com.eviware.soapui.support.components.JInspectorPanelFactory;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.support.swing.ExtendedComboBoxModel;
import com.eviware.soapui.support.swing.ModelItemListKeyListener;
import com.eviware.soapui.support.swing.ModelItemListMouseListener;
import com.eviware.soapui.ui.support.ModelItemDesktopPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

public class RestMockActionDesktopPanel extends ModelItemDesktopPanel<RestMockAction>
{
	private JList responseList;
	private ResponseListModel responseListModel;
	private MockOperationDispatcher dispatcher;
	private JInspectorPanel inspectorPanel;

	public RestMockActionDesktopPanel( RestMockAction mockOperation )
	{
		super( mockOperation );

		buildUI();
		setPreferredSize( new Dimension( 600, 440 ) );
	}

	private void buildUI()
	{
		add( buildToolbar(), BorderLayout.NORTH );


		inspectorPanel = JInspectorPanelFactory.build( buildResponseList() );
		inspectorPanel.setDefaultDividerLocation( 0.5F );
		add( inspectorPanel.getComponent(), BorderLayout.CENTER );
	}

	private JComponent buildResponseList()
	{
		responseListModel = new ResponseListModel();
		responseList = new JList( responseListModel );
		responseList.addKeyListener( new ModelItemListKeyListener()
		{
			@Override
			public ModelItem getModelItemAt( int ix )
			{
				return getModelItem().getMockResponseAt( ix );
			}
		} );

		responseList.addMouseListener( new ModelItemListMouseListener()
		{

			private DefaultActionList defaultActions;

			@Override
			protected ActionList getActionsForRow( JList list, int row )
			{
				ActionList actions = super.getActionsForRow( list, row );

				actions.insertAction( SwingActionDelegate.createDelegate( NewMockResponseAction.SOAPUI_ACTION_ID,
						getModelItem(), null, "/addToRestMockService.gif" ), 0 );

				actions.insertAction( SwingActionDelegate.createDelegate(
						OpenRequestForMockOperationAction.SOAPUI_ACTION_ID, getModelItem(), null, "/open_request.gif" ), 1 );

				if( actions.getActionCount() > 2 )
					actions.insertAction( ActionSupport.SEPARATOR_ACTION, 2 );

				return actions;
			}

			@Override
			protected ActionList getDefaultActions()
			{
				if( defaultActions == null )
				{
					defaultActions = new DefaultActionList();
					defaultActions.addAction( SwingActionDelegate.createDelegate( NewMockResponseAction.SOAPUI_ACTION_ID,
							getModelItem(), null, "/addToRestMockService.gif" ) );
				}

				return defaultActions;
			}

		} );
		responseList.setCellRenderer( new ResponseListCellRenderer() );

		JScrollPane scrollPane = new JScrollPane( responseList );
		JTabbedPane tabs = new JTabbedPane();
		tabs.addTab( "MockResponses", UISupport.buildPanelWithToolbar( buildMockResponseListToolbar(), scrollPane ) );

		return UISupport.createTabPanel( tabs, true );
	}

	private JComponent buildMockResponseListToolbar()
	{
		JXToolBar toolbar = UISupport.createToolbar();
		toolbar.add( UISupport.createToolbarButton( SwingActionDelegate.createDelegate(
				NewMockResponseAction.SOAPUI_ACTION_ID, getModelItem(), null, "/addToRestMockService.gif" ) ) );

		return toolbar;
	}

	private JComponent buildDispatchEditor()
	{
		// TODO: implement
		return null;
	}

	private Component buildToolbar()
	{
		JXToolBar toolbar = UISupport.createToolbar();
		toolbar.setLayout( new BorderLayout() );

		Box methodBox = Box.createHorizontalBox();
		methodBox.add( createMethodComboBox() );
		methodBox.add ( Box.createHorizontalStrut( 10 ));
		toolbar.add( methodBox, BorderLayout.WEST );

		toolbar.add( createResourcePathTextField(), BorderLayout.CENTER );

		return toolbar;
	}

	private JComponent createResourcePathTextField()
	{
		final JTextField resourcePathEditor = new JTextField(  );
		resourcePathEditor.addKeyListener( new KeyAdapter()
		{
			@Override
			public void keyReleased( KeyEvent e )
			{
				getModelItem().setResourcePath( resourcePathEditor.getText() );
			}
		} );
		return new TextPanelWithTopLabel( "Resource", getModelItem().getResourcePath(), resourcePathEditor );
	}

	private JComponent createMethodComboBox()
	{
		JPanel comboPanel = new JPanel( new BorderLayout(  ) );

		comboPanel.add( new JLabel( "Method" ), BorderLayout.NORTH );

		final JComboBox methodCombo = new JComboBox( HttpMethod.getMethods() );

		methodCombo.setSelectedItem( getModelItem().getMethod() );
		methodCombo.setToolTipText( "Set desired HTTP method" );
		methodCombo.addItemListener( new ItemListener()
		{
			public void itemStateChanged( ItemEvent e )
			{
				getModelItem().setMethod( ( HttpMethod )methodCombo.getSelectedItem() );
			}
		} );

		comboPanel.add( methodCombo, BorderLayout.SOUTH );

		return comboPanel;
	}

	public boolean onClose( boolean canCancel )
	{

		responseListModel.release();

		if( dispatcher != null )
			dispatcher.releaseEditorComponent();

		return release();
	}

	public boolean dependsOn( ModelItem modelItem )
	{
		return modelItem == getModelItem() || modelItem == getModelItem().getMockService()
				|| modelItem == getModelItem().getMockService().getProject();
	}

	public class ResponseListModel extends AbstractListModel implements ListModel, MockServiceListener,
			PropertyChangeListener
	{
		private java.util.List<MockResponse> responses = new ArrayList<MockResponse>();

		public ResponseListModel()
		{
			for( int c = 0; c < getModelItem().getMockResponseCount(); c++ )
			{
				MockResponse mockResponse = getModelItem().getMockResponseAt( c );
				mockResponse.addPropertyChangeListener( this );

				responses.add( mockResponse );
			}

			getModelItem().getMockService().addMockServiceListener( this );
		}

		public Object getElementAt( int arg0 )
		{
			return responses.get( arg0 );
		}

		public int getSize()
		{
			return responses.size();
		}

		public void mockOperationAdded( MockOperation operation )
		{

		}

		public void mockOperationRemoved( MockOperation operation )
		{

		}

		public void mockResponseAdded( MockResponse response )
		{
			if( response.getMockOperation() != getModelItem() )
				return;

			responses.add( response );
			response.addPropertyChangeListener( this );
			fireIntervalAdded( this, responses.size() - 1, responses.size() - 1 );

		}

		public void mockResponseRemoved( MockResponse response )
		{
			if( response.getMockOperation() != getModelItem() )
				return;

			int ix = responses.indexOf( response );
			responses.remove( ix );
			response.removePropertyChangeListener( this );
			fireIntervalRemoved( this, ix, ix );

		}

		public void propertyChange( PropertyChangeEvent arg0 )
		{
			if( arg0.getPropertyName().equals( RestMockAction.NAME_PROPERTY ) )
			{
				int ix = responses.indexOf( arg0.getSource() );
				fireContentsChanged( this, ix, ix );

			}
		}

		public void release()
		{
			for( MockResponse operation : responses )
			{
				operation.removePropertyChangeListener( this );
			}

			getModelItem().getMockService().removeMockServiceListener( this );
		}
	}

	private final static class ResponseListCellRenderer extends JLabel implements ListCellRenderer
	{
		public Component getListCellRendererComponent( JList list, Object value, int index, boolean isSelected,
																	  boolean cellHasFocus )
		{
			MockResponse testStep = ( MockResponse )value;
			setText( testStep.getName() );
			setIcon( testStep.getIcon() );

			if( isSelected )
			{
				setBackground( list.getSelectionBackground() );
				setForeground( list.getSelectionForeground() );
			}
			else
			{
				setBackground( list.getBackground() );
				setForeground( list.getForeground() );
			}

			setEnabled( list.isEnabled() );
			setFont( list.getFont() );
			setOpaque( true );
			setBorder( BorderFactory.createEmptyBorder( 3, 3, 3, 3 ) );

			return this;
		}
	}


}

