package com.eviware.soapui.impl.rest.panels.mock;

import com.eviware.soapui.impl.rest.mock.RestMockAction;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.actions.mockoperation.NewMockResponseAction;
import com.eviware.soapui.impl.wsdl.actions.mockoperation.OpenRequestForMockOperationAction;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockOperation;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockResponse;
import com.eviware.soapui.impl.wsdl.mock.dispatch.MockOperationDispatcher;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.model.mock.MockOperation;
import com.eviware.soapui.model.mock.MockResponse;
import com.eviware.soapui.model.mock.MockServiceListener;
import com.eviware.soapui.model.support.ModelSupport;
import com.eviware.soapui.model.support.ProjectListenerAdapter;
import com.eviware.soapui.model.util.ModelItemNames;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.swing.ActionList;
import com.eviware.soapui.support.action.swing.ActionSupport;
import com.eviware.soapui.support.action.swing.DefaultActionList;
import com.eviware.soapui.support.action.swing.SwingActionDelegate;
import com.eviware.soapui.support.components.JComponentInspector;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.support.swing.ExtendedComboBoxModel;
import com.eviware.soapui.support.swing.ModelItemListKeyListener;
import com.eviware.soapui.support.swing.ModelItemListMouseListener;
import com.eviware.soapui.ui.support.ModelItemDesktopPanel;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

public class RestMockActionDesktopPanel extends ModelItemDesktopPanel<RestMockAction>
{
	private JList responseList;
	private JComboBox defaultResponseCombo;
	private ResponseListModel responseListModel;
	private JComponentInspector<JComponent> dispatchInspector;
	private MockOperationDispatcher dispatcher;
	private InternalProjectListener projectListener = new InternalProjectListener();

	public RestMockActionDesktopPanel( RestMockAction mockOperation )
	{
		super( mockOperation );

		buildUI();
		setPreferredSize( new Dimension( 600, 440 ) );
	}

	private void buildUI()
	{
		add( buildToolbar(), BorderLayout.NORTH );
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
						getModelItem(), null, "/addToMockService.gif" ), 0 );

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
							getModelItem(), null, "/addToMockService.gif" ) );
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
				NewMockResponseAction.SOAPUI_ACTION_ID, getModelItem(), null, "/mockResponse.gif" ) ) );

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
		toolbar.addSpace( 3 );

		toolbar.addFixed( UISupport.createToolbarButton( SwingActionDelegate.createDelegate(
				NewMockResponseAction.SOAPUI_ACTION_ID, getModelItem(), null, "/addToMockService.gif" ) ) );
		toolbar.addFixed( UISupport.createToolbarButton( SwingActionDelegate.createDelegate(
				OpenRequestForMockOperationAction.SOAPUI_ACTION_ID, getModelItem(), null, "/open_request.gif" ) ) );
		toolbar.addUnrelatedGap();

		ModelItemNames<WsdlInterface> names = new ModelItemNames<WsdlInterface>( ModelSupport.getChildren( getModelItem()
				.getMockService().getProject(), WsdlInterface.class ) );


		return toolbar;
	}

	public boolean onClose( boolean canCancel )
	{

		getModelItem().getMockService().getProject().removeProjectListener( projectListener );
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

			responses.add( ( WsdlMockResponse )response );
			response.addPropertyChangeListener( this );
			fireIntervalAdded( this, responses.size() - 1, responses.size() - 1 );

			defaultResponseCombo.addItem( response.getName() );
		}

		public void mockResponseRemoved( MockResponse response )
		{
			if( response.getMockOperation() != getModelItem() )
				return;

			int ix = responses.indexOf( response );
			responses.remove( ix );
			response.removePropertyChangeListener( this );
			fireIntervalRemoved( this, ix, ix );

			defaultResponseCombo.removeItem( response.getName() );
		}

		public void propertyChange( PropertyChangeEvent arg0 )
		{
			if( arg0.getPropertyName().equals( WsdlMockOperation.NAME_PROPERTY ) )
			{
				int ix = responses.indexOf( arg0.getSource() );
				fireContentsChanged( this, ix, ix );

				ExtendedComboBoxModel model = ( ExtendedComboBoxModel )defaultResponseCombo.getModel();
				model.setElementAt( arg0.getNewValue(), ix );

				if( model.getSelectedItem().equals( arg0.getOldValue() ) )
					model.setSelectedItem( arg0.getNewValue() );
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

	private final class InternalProjectListener extends ProjectListenerAdapter
	{
		@Override
		public void interfaceAdded( Interface iface )
		{
			//TODO: implement
		}

		@Override
		public void interfaceRemoved( Interface iface )
		{
		 // TODO: implement
		}
	}

}

