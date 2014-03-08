package com.eviware.soapui.ui.support;

import com.eviware.soapui.impl.support.AbstractMockOperation;
import com.eviware.soapui.impl.wsdl.actions.mockoperation.NewMockResponseAction;
import com.eviware.soapui.impl.wsdl.actions.mockoperation.OpenRequestForMockOperationAction;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockOperation;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockResponse;
import com.eviware.soapui.impl.wsdl.mock.dispatch.MockOperationDispatchRegistry;
import com.eviware.soapui.impl.wsdl.mock.dispatch.MockOperationDispatcher;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.mock.MockOperation;
import com.eviware.soapui.model.mock.MockResponse;
import com.eviware.soapui.model.mock.MockServiceListener;
import com.eviware.soapui.model.util.ModelItemNames;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.swing.ActionList;
import com.eviware.soapui.support.action.swing.ActionSupport;
import com.eviware.soapui.support.action.swing.DefaultActionList;
import com.eviware.soapui.support.action.swing.SwingActionDelegate;
import com.eviware.soapui.support.components.JComponentInspector;
import com.eviware.soapui.support.components.JInspectorPanel;
import com.eviware.soapui.support.components.JInspectorPanelFactory;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.support.swing.ExtendedComboBoxModel;
import com.eviware.soapui.support.swing.ModelItemListKeyListener;
import com.eviware.soapui.support.swing.ModelItemListMouseListener;
import com.jgoodies.forms.builder.ButtonBarBuilder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

public abstract class AbstractMockOperationDesktopPanel<MockOperationType extends AbstractMockOperation>
		extends ModelItemDesktopPanel<MockOperationType>
{
	private JList responseList;
	private JComboBox dispatchCombo;
	private JPanel dispatchPanel;
	private JComboBox defaultResponseCombo;
	private ResponseListModel responseListModel;
	private JComponentInspector<JComponent> dispatchInspector;
	private JInspectorPanel inspectorPanel;
	private MockOperationDispatcher dispatcher;

	public AbstractMockOperationDesktopPanel( MockOperationType mockOperation )
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
		dispatchInspector = new JComponentInspector<JComponent>( buildDispatchEditor(), "Dispatch ("
				+ getModelItem().getDispatchStyle().toString() + ")", "Configures current dispatch style", true );
		inspectorPanel.addInspector( dispatchInspector );
		inspectorPanel.activate( dispatchInspector );

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
		dispatchPanel = new JPanel( new BorderLayout() );
		dispatchPanel.setOpaque( true );
		ButtonBarBuilder builder = new ButtonBarBuilder();
		builder.addFixed( new JLabel( "Dispatch: " ) );
		builder.addRelatedGap();
		dispatchCombo = new JComboBox( MockOperationDispatchRegistry.getDispatchTypes() );
		dispatchCombo.setSelectedItem( null );

		dispatchCombo.addItemListener( new ItemListener()
		{
			public void itemStateChanged( ItemEvent e )
			{
				if( dispatchPanel.getComponentCount() > 1 )
					dispatchPanel.remove( 1 );

				String item = ( String )dispatchCombo.getSelectedItem();
				dispatcher = getModelItem().setDispatchStyle( item );

				dispatchPanel.add( dispatcher.getEditorComponent(), BorderLayout.CENTER );
				dispatchPanel.revalidate();
				dispatchPanel.repaint();

				if( dispatchInspector != null && item != null )
				{
					dispatchInspector.setTitle( "Dispatch (" + item + ")" );
				}
			}
		} );

		builder.addFixed( dispatchCombo );

		builder.addUnrelatedGap();
		builder.addFixed( new JLabel( "Default Response: " ) );
		builder.addRelatedGap();

		ModelItemNames<MockResponse> names = new ModelItemNames<MockResponse>( getModelItem().getMockResponses() );
		defaultResponseCombo = new JComboBox( new ExtendedComboBoxModel( names.getNames() ) );
		defaultResponseCombo.setPreferredSize( new Dimension( 150, 20 ) );
		defaultResponseCombo.addItemListener( new ItemListener()
		{
			public void itemStateChanged( ItemEvent e )
			{
				Object selectedItem = defaultResponseCombo.getSelectedItem();
				getModelItem().setDefaultResponse( ( String )selectedItem );
			}
		} );

		builder.addFixed( defaultResponseCombo );
		builder.setBorder( BorderFactory.createEmptyBorder( 2, 3, 3, 3 ) );

		dispatchPanel.add( builder.getPanel(), BorderLayout.NORTH );

		// init data
		defaultResponseCombo.setSelectedItem( getModelItem().getDefaultResponse() );
		dispatchCombo.setSelectedItem( getModelItem().getDispatchStyle() );

		return dispatchPanel;
	}

	protected abstract Component buildToolbar();

	public boolean onClose( boolean canCancel )
	{
		responseListModel.release();

		inspectorPanel.release();

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
		private java.util.List<WsdlMockResponse> responses = new ArrayList<WsdlMockResponse>();

		public ResponseListModel()
		{
			for( int c = 0; c < getModelItem().getMockResponseCount(); c++ )
			{
				WsdlMockResponse mockResponse = ( WsdlMockResponse )getModelItem().getMockResponseAt( c );
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
			for( WsdlMockResponse operation : responses )
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
